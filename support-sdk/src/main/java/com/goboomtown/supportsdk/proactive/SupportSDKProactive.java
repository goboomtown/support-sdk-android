package com.goboomtown.supportsdk.proactive;

import android.content.Context;
import android.util.Log;

import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.util.*;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SupportSDKProactive implements SupportSDK.SupportSDKListener {

    private static final String TAG = SupportSDKProactive.class.getSimpleName();

    private static final int DEFAULT_CHECK_QUEUE_SIZE = 5;
    private static final int DEFAULT_CHECK_QUEUE_FLUSH_INTERVAL_MS = 15000;
    private static final String EP_CONTEXT_PUT_CHECKS = SupportSDK.kSDKV1Endpoint + "/technology/putChecks";
    private static final String EP_CONTEXT_PUT_DUMP_FILE = SupportSDK.kSDKV1Endpoint + "/technology/putDump";
    public static final String DUMPCHECK_COMPRESSED_FILENAME = "appdumpcheck";

    private final WeakReference<Context> ctx;
    private final SupportSDK sdk;
    private final Queue<Check> checkQueue;
    private final Object lockObj_checkQueue;
    private final Thread checkQueueFlushHandlerThread;
    private final Object lockObj_checkQueueFlushing;

    public SupportSDKProactive(final Context ctx, final int configResourceId, final HashMap<String, String> customerInfo) {
        this.ctx = new WeakReference<>(ctx);
        this.sdk = new SupportSDK(ctx, this);
        this.sdk.loadConfiguration(configResourceId, customerInfo);
        this.checkQueue = new ConcurrentLinkedQueue<>();
        this.lockObj_checkQueue = new Object();
        this.lockObj_checkQueueFlushing = new Object();
        this.checkQueueFlushHandlerThread = (new Thread(new CheckQueueFlushHandler(DEFAULT_CHECK_QUEUE_FLUSH_INTERVAL_MS)));
        this.checkQueueFlushHandlerThread.start();
    }

    @Override
    public void supportSDKDidFailWithError(String description, String reason) {
        Log.e(TAG, "support SDK unable to load: " + description + "; " + reason);
    }

    @Override
    public void supportSDKDidGetSettings() {
        if (!sdk.isProactiveEnabled()) {
            Log.w(TAG, "Proactive module is not enabled for this API key. Enable it through the Proactive configurator in Relay.");
        }
    }

    @Override
    public void supportSDKDidFailToGetSettings() {

    }

    @Override
    public void supportSDKDidRetrieveAccount(HashMap<String, String> accountInfo) {
    }

    @Override
    public void supportSDKDidFailToRetrieveAccount(String message) {
    }


    /**
     * POST a new {@link CheckAppHealth} with given name, status, and description.
     *
     * @param name check name
     * @param checkStatus check status
     * @param checkStatusDetail check description
     */
    public void putAppHealthCheck(final String name, final Check.CheckStatus checkStatus, final String checkStatusDetail) {
        Check check = new CheckAppHealth(name, checkStatus, checkStatusDetail);
        putCheck(check);
    }

    /**
     * POST a new {@link CheckAppHardware} with given name, status, and description.
     *
     * @param name check name
     * @param checkStatus check status
     * @param checkStatusDetail check description
     */
    public void putAppHardwareCheck(final String name, final Check.CheckStatus checkStatus, final String checkStatusDetail) {
        Check check = new CheckAppHardware(name, checkStatus, checkStatusDetail);
        putCheck(check);
    }

    /**
     * POST a new {@link CheckAppCloud} with given name, status, and description.
     *
     * @param name check name
     * @param checkStatus check status
     * @param checkStatusDetail check description
     */
    public void putAppCloudCheck(final String name, final Check.CheckStatus checkStatus, final String checkStatusDetail) {
        Check check = new CheckAppCloud(name, checkStatus, checkStatusDetail);
        putCheck(check);
    }

    /**
     * POST a new {@link CheckAppDump} with given status, and description.
     *
     * @param dumpFile check dump file to compress and upload to cloud
     * @param checkStatus check status
     * @param checkStatusDetail check description
     */
    public void putAppDumpCheck(final File dumpFile, final Check.CheckStatus checkStatus, final String checkStatusDetail) {
        File[] dumpFiles = new File[1];
        dumpFiles[0] = dumpFile;
        putAppDumpCheck(dumpFiles, checkStatus, checkStatusDetail);
    }

    /**
     * POST a new {@link CheckAppDump} with given status, and description.
     *
     * @param dumpFiles check dump files to compress and upload to cloud
     * @param checkStatus check status
     * @param checkStatusDetail check description
     */
    public void putAppDumpCheck(final File[] dumpFiles, final Check.CheckStatus checkStatus, final String checkStatusDetail) {
        Check check = new CheckAppDump(checkStatus, checkStatusDetail, dumpFiles);
        putCheck(check);
    }

    /**
     * Submit a check to SupportSDK cloud.  Check can be submitted in the background for non-blocking behavior,
     * or sent immediately.
     *
     * @param check {@link Check} to submit - if {@link Check#type} == {@link CheckType#UNKNOWN} then it will be skipped
     * @param background submit check in the background if false (non-blocking); if true then submit check immediately (blocking)
     */
    public void putCheck(final Check check, final boolean background) {
        if (CheckType.UNKNOWN == check.type) {
            Log.w(TAG, "skipping unknown check of type unknown (" + check.toString() + ")");
            return;
        }
        if (!background) {
            List<Check> checks = new ArrayList<>(1);
            checks.add(check);
            postChecksToCloud(checks);
        } else {
            putCheck(check);
        }
    }

    /**
     * Submit a check to SupportSDK cloud.  Check can be submitted in the background for non-blocking behavior.
     *
     * @param check {@link Check} to submit
     */
    public void putCheck(final Check check) {
        queueCheck(check);
    }

    private void queueCheck(final Check check) {
        (new Thread(()-> {
            synchronized (lockObj_checkQueueFlushing) {
                try {
                    lockObj_checkQueueFlushing.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                checkQueue.add(check);
            }
        })).start();
    }

    /**
     * Immediately flush checks in the queue, causing them to be POSTed.
     *
     * @return how many checks flushed from queue
     */
    public int flushChecks() {
        return flushCheckQueue();
    }

    /**
     *
     * @return how many checks were flushed
     */
    private int flushCheckQueue() {
        int ret = checkQueue.size();
        synchronized (checkQueueFlushHandlerThread) {
            checkQueueFlushHandlerThread.notify();
        }
        synchronized (lockObj_checkQueueFlushing) {
            try {
                lockObj_checkQueueFlushing.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        ret = ret - checkQueue.size();
        return ret;
    }

    /**
     * POST checks to cloud using {@link SupportSDK#post(String, JSONObject, Callback)}.
     *
     * @param checks checks to POST
     */
    private void postChecksToCloud(final List<Check> checks) {
        if (checks == null || checks.size() < 1) {
            Log.w(TAG, "list of checks to POST is empty, aborting");
            return;
        }
        if (!sdk.cloudConfigComplete) {
            synchronized (lockObj_checkQueue) {
                checkQueue.addAll(checks);
            }
            Log.w(TAG, "SDK configuration pending - " + checks.size() + " checks requeued");
            return;
        }
        if (!sdk.isProactiveEnabled()) {
            Log.i(TAG, "proactive feature disabled - ignoring checks");
            return;
        }
        JSONObject params = new JSONObject();
        JSONArray checksJSON = new JSONArray();
        try {
            params.put("members_id", sdk.memberID);
            params.put("members_users_id", sdk.memberUserID);
            params.put("members_locations_id", sdk.memberLocationID);
            params.put("members_devices_id", sdk.memberDeviceID);
            params.put("checks", new JSONArray());
            for (int i = 0; i < checks.size(); i++) {
                  checksJSON.put(checks.get(i).toJson());
            }
            params.put("checks", checksJSON);
            sdk.post(EP_CONTEXT_PUT_CHECKS, params, new PostCallback(params));
            for (int i = 0; i < checks.size(); i++) {
                if (CheckAppDump.class.isAssignableFrom(checks.get(i).getClass())) {
                    CheckAppDump check = (CheckAppDump) checks.get(i);
                    compressAndUploadDumpFiles(check.files);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "unable to build JSON for POST, exception:\n" + Log.getStackTraceString(e));
        }
    }

    /**
     * @return folder path for temporary files generated by {@link this}.
     */
    protected File buildTempFolderPath() {
        return ctx.get().getCacheDir();
    }

    /**
     * Compresses given files and uploads them to cloud.
     *
     * @param files files to compress and upload
     */
    private void compressAndUploadDumpFiles(final File[] files) {
        File compressedFile = compressFiles(files);
        if (compressedFile != null && compressedFile.exists()) {
            uploadFile(compressedFile);
        }
    }

    /**
     * Compress files using tar.gz compression using default output filename {@link SupportSDKProactive#DUMPCHECK_COMPRESSED_FILENAME} and temp folder path.
     *
     * @param files set of files to compress
     * @return {@link this#compressFiles(File[], String)}
     */
    public File compressFiles(final File[] files) {
        return this.compressFiles(files, DUMPCHECK_COMPRESSED_FILENAME);
    }

    /**
     * Compress files using tar.gz compression. Clients can customize the output file's path/location by subclassing and overriding {@link this#buildTempFolderPath()}.
     *
     * @param files set of files to compress
     * @param tempFileNamePrefix filename prefix of compressed file; defaults to {@link SupportSDKProactive#DUMPCHECK_COMPRESSED_FILENAME} if null
     * @return compressed file with name {tempFileNamePrefix}.tar.gz; path given by {@link this#buildTempFolderPath()}
     */
    public File compressFiles(final File[] files, final String tempFileNamePrefix) {
        if (files == null || files.length == 0) {
            throw new IllegalStateException("support bundle folder doesn't exist, unable to upload support assets");
        }
        Log.i(TAG, "compressing app dump files");
        // tar.gz folder
        boolean compressed = false;
        OutputStream fo;
        String tfnp = tempFileNamePrefix;
        if (tfnp == null || tfnp.isEmpty()) {
            tfnp = DUMPCHECK_COMPRESSED_FILENAME;
        }
        File compressedFile = null;
        long runStartTime = System.currentTimeMillis();
        try {
            File tempFileFolderPath = buildTempFolderPath();
            compressedFile = File.createTempFile(tfnp, ".tar.gz", tempFileFolderPath);
            fo = new FileOutputStream(compressedFile); // Files.newOutputStream(compressedFile);
            OutputStream gzo = new GzipCompressorOutputStream(fo);
            ArchiveOutputStream o = new TarArchiveOutputStream(gzo);
            for (File f : files) {
                // maybe skip directories for formats like AR that don't store directories
                ArchiveEntry entry = o.createArchiveEntry(f, f.getName());
                // potentially add more flags to entry
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = new FileInputStream(f.toString())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }
            o.finish();
            o.close();
            gzo.close();
            fo.close();
            compressed = true;
        } catch (IllegalStateException | IOException e) {
            Log.e(TAG, "unexpected error compressing stats files", e);
        }
        long runDuration = System.currentTimeMillis() - runStartTime;
        if (compressed) {
            Log.i(TAG, "stats files compressed in " + runDuration + "ms, preparing upload");
        } else {
            Log.w(TAG, "unable to compress stats files, upload skipped");
        }
        return compressedFile;
    }

    /**
     * Upload file to {@link SupportSDKProactive#EP_CONTEXT_PUT_DUMP_FILE}
     * @param file file to upload
     */
    private void uploadFile(final File file) {
        if (file == null || file.getPath().isEmpty() || !file.exists()) {
            IllegalStateException e;
            if (file == null || file.getPath().isEmpty()) {
                e = new IllegalStateException("file empty/undefined, unable to upload");
            } else {
                e = new IllegalStateException("file [" + file.getName() + "] doesn't exist, unable to upload");
            }
            Log.e(TAG, "unexpected error uploading file", e);
            throw e;
        }
        try {
//            String ep = SupportSDK.kV3Endpoint.concat(EP_CONTEXT_PUT_DUMP_FILE);
            Log.i(TAG, "upload file (name=" + file.getAbsolutePath() + ", len=" + file.length() + ") to " + EP_CONTEXT_PUT_DUMP_FILE);
            // write file contents to JSON
            JSONObject fileUpload = new JSONObject();
            fileUpload.put("file_data", Utils.base64Encode(file));
            // upload
            final Thread t = Thread.currentThread();
            final long runStartTime = System.currentTimeMillis();
            sdk.post(EP_CONTEXT_PUT_DUMP_FILE, fileUpload, new PostCallback(fileUpload) {

                @Override
                public void onFailure(Call call, IOException e) {
                    super.onFailure(call, e);
                    synchronized (t) {
                        t.notify();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    long runDuration = System.currentTimeMillis() - runStartTime;
                    boolean success = false;
                    String message = "POST response from cloud in " + runDuration + "ms";
                    if (response.code() == 200 && response.body() != null) {
                        JSONObject jsonObject = SupportSDK.successJSONObject(response.body().string());
                        if (jsonObject != null) {
                            success = jsonObject.optBoolean("uploaded");
                            message += ", upload failed";
                        }
                    } else if (response.code() != 200 && response.body() != null) {
                        message += ", error response (code=" + response.code() + "), message: " + SupportSDK.failureMessageFromJSONData(response.body().string());
                    } else {
                        message += ", empty/null response (code=" + response.code() + ") received from " + call.request().url().toString();
                    }
                    if (!success) {
                        Log.w(TAG, message);
                    } else {
                        Log.v(TAG, message + ", file upload succeeded: " + payload.toString());
                    }
                    synchronized (t) {
                        t.notify();
                    }
                }
            });
            synchronized (t) {
                t.wait();  // until notified by callback
            }
        } catch (Exception e) {
            Log.e(TAG, "unexpected error uploading file", e);
        }
    }

    /**
     * Generic callback for {@link SupportSDK#post(String, JSONObject, Callback)} calls.
     */
    private static class PostCallback implements Callback {

        protected final JSONObject payload;
        protected final long runStartTime;

        public PostCallback(final JSONObject payload) {
            if (payload == null) {
                throw new IllegalStateException("must provide a payload JSONObject");
            }
            this.payload = payload;
            this.runStartTime = System.currentTimeMillis();
        }

        @Override
        public void onFailure(Call call, IOException e) {
            long runDuration = System.currentTimeMillis() - runStartTime;
            Log.e(TAG, "POST to " + call.request().url().toString() + " failed in " + runDuration + "ms, error:\n" + Log.getStackTraceString(e) + "\npayload: " + payload.toString());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            long runDuration = System.currentTimeMillis() - runStartTime;
            boolean success = false;
            String message = "POST response from cloud in " + runDuration + "ms";
            if (response.code() == 200 && response.body() != null) {
                JSONObject jsonObject = SupportSDK.successJSONObject(response.body().string());
                if (jsonObject != null) {
                    JSONArray resultsArray = jsonObject.optJSONArray("results");
                    if (resultsArray != null) {
                        success = true;
                    }
                }
            } else if (response.code() != 200 && response.body() != null) {
                message += ", error response (code=" + response.code() + "), message: " + SupportSDK.failureMessageFromJSONData(response.body().string());
            } else {
                message += "empty/null response (code=" + response.code() + ") received from " + call.request().url().toString();
            }
            if (!success) {
                Log.w(TAG, message);
            } else {
                Log.v(TAG, message + ", POST payload succeeded: " + payload.toString());
            }
        }

    }

    /**
     * Handle background queue flushing and processing.
     */
    private class CheckQueueFlushHandler implements Runnable {

        private final long timeIntervalFlush;

        public CheckQueueFlushHandler(final long timeIntervalFlush) {
            this.timeIntervalFlush = timeIntervalFlush;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            Log.v(TAG, "check queue flush handler thread started");
            List<Check> checks = new ArrayList<>();
            while (true) {
                synchronized (lockObj_checkQueueFlushing) {
                    boolean checksToFlush = true;
                    while (checksToFlush) {
                        synchronized (lockObj_checkQueue) {
                            Check check = checkQueue.poll();
                            if (check != null) {
                                checks.add(check);
                            }
                            checksToFlush = (checkQueue.size() > 0);
                        }
                    }
                    if (checks.size() > 0) {
                        postChecksToCloud(checks);
                    }
                    lockObj_checkQueueFlushing.notifyAll();
                }
                checks.clear();
                synchronized (checkQueueFlushHandlerThread) {
                    try {
                        Log.v(TAG, "check queue flush handler thread pausing for " + timeIntervalFlush + "ms");
                        checkQueueFlushHandlerThread.wait(timeIntervalFlush);
                        Log.v(TAG, "check queue flush handler thread resuming");
                    } catch (InterruptedException e) {
                        Log.e(TAG, "check flush thread interrupted");
                    }
                }
            }
        }
    }

    /**
     * The base class for all SupportSDK checks that are processed and sent to the Relay Proactive cloud.
     */
    @SuppressWarnings({"WeakerAccess"})
    public static abstract class Check {
        private CheckType type;
        private String name;
        private CheckStatus checkStatus;
        private String checkStatusDetail;

        public Check(final CheckType type, final String name, final CheckStatus checkStatus, final String checkStatusDetail) {
            if (name == null) {
                throw new IllegalArgumentException("new check instances must have a name");
            }
            if (CheckStatus.NOT_SET == checkStatus) {
                throw new IllegalArgumentException("new check instances must have a valid status code");
            }
            this.type = type;
            this.name = name;
            this.checkStatus = checkStatus;
            this.checkStatusDetail = checkStatusDetail;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "@" + hashCode() + "{name=" + name + ",type=" + type + ",code=" + checkStatus + ",desc=" + checkStatusDetail + "}";
        }

        /**
         * @return {@link this} as a {@link JSONObject}
         * @throws JSONException on error when object is marshalled to JSON
         */
        public JSONObject toJson() throws JSONException {
            JSONObject ret = new JSONObject();
            ret.put("type", type.toString());
            ret.put("name", name);
            ret.put("status_code", checkStatus);
            ret.put("status_detail", checkStatusDetail);
            return ret;
        }

        /**
         * Represents a check status understood by the Boomtown Cloud.
         */
        public enum CheckStatus {
            NOT_SET(-1),
            OK(0),
            UNKNOWN(1),
            WARNING(2),
            CRITICAL(3);

            CheckStatus(int code) {}
        }
    }

    /**
     * A SupportSDK check of type {@link CheckType#APP_HEALTH}.
     */
    public static class CheckAppHealth extends Check {

        public CheckAppHealth(final String name, final CheckStatus checkStatus, final String checkStatusDetail) {
            super(CheckType.APP_HEALTH, name, checkStatus, checkStatusDetail);
        }
    }

    /**
     * A SupportSDK check of type {@link CheckType#APP_HARDWARE}.
     */
    public static class CheckAppHardware extends Check {

        public CheckAppHardware(final String name, final CheckStatus checkStatus, final String checkStatusDetail) {
            super(CheckType.APP_HARDWARE, name, checkStatus, checkStatusDetail);
        }
    }

    /**
     * A SupportSDK check of type {@link CheckType#APP_CLOUD}.
     */
    public static class CheckAppCloud extends Check {

        public CheckAppCloud(final String name, final CheckStatus checkStatus, final String checkStatusDetail) {
            super(CheckType.APP_CLOUD, name, checkStatus, checkStatusDetail);
        }
    }

    /**
     * A SupportSDK check of type {@link CheckType#APP_DUMP}.
     */
    public static class CheckAppDump extends Check {

        public static final String CHECK_NAME = "APP DUMP";

        public final File[] files;

        public CheckAppDump(final CheckStatus checkStatus, final String checkStatusDetail, final File[] files) {
            super(CheckType.APP_DUMP, CHECK_NAME, checkStatus, checkStatusDetail);
            this.files = files;
        }
    }

    /**
     * Represents the types of checks supported by the SDK.  {@link this#UNKNOWN} checks are not processed by the SDK = effectively ignored.
     */
    public enum CheckType {
        UNKNOWN(-1),
        APP_HEALTH(1),
        APP_HARDWARE(2),
        APP_CLOUD(3),
        APP_DUMP(4);

        CheckType(int code) {}
    }

}