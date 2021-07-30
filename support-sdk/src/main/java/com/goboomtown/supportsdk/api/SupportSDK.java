package com.goboomtown.supportsdk.api;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.appcompat.app.*;

import android.os.*;

import android.preference.PreferenceManager;
import android.util.*;
import android.util.Base64;
import android.view.ViewGroup;

import com.goboomtown.chat.*;
import com.goboomtown.forms.model.*;
import com.goboomtown.supportsdk.*;
import com.goboomtown.supportsdk.model.*;
import com.goboomtown.supportsdk.model.Configuration;
import com.goboomtown.supportsdk.service.*;

import org.json.*;

import java.io.*;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.concurrent.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.*;

/**
 * Created by Larry Borsato on 2016-07-12.
 */
public class SupportSDK {

    private static final String TAG = SupportSDK.class.getSimpleName();
    private static final String SupportSDKHelpName = "SupportSDK";

    private static final boolean TLSv13Support = false;

    public static final String HTTP_HEADER_DOWNLOAD_TOKEN = "X-Boomtown-DownloadSessionToken";

    public  static final String KEY_DEV_MODE_ENABLED                = "com.goboomtown.supportsdk.dev_mode_enabled";
    public  static final String KEY_DEV_MODE_JSON_CONFIG            = "com.goboomtown.supportsdk.dev_mode_json_config";
    public  static final String KEY_DEV_MODE_JSON_CONFIG_DEFAULT    = "com.goboomtown.supportsdk.dev_mode_json_config_default";
    public  static final String KEY_LAST_VERSION                    = "com.goboomtown.supportsdk.last_version";

    private static final String PRODUCTION          = "Production";
    private static final String PREPROD             = "Preprod";
    private static final String INTEG               = "Integ";
    private static final String UAT                 = "UAT";
    private static final String SANDBOX             = "Sandbox";
    private static final String LOCAL_ENV           = "Local Environment";

    private static final String SERVER_PRODUCTION   = "https://api.goboomtown.com";
    private static final String SERVER_PREPROD      = "https://api.preprod.goboomtown.com";
    private static final String SERVER_INTEG        = "https://api.integ.goboomtown.com";
    private static final String SERVER_UAT          = "https://api.uat.goboomtown.com";
    private static final String SERVER_SANDBOX      = "https://api.sandbox.goboomtown.com";
    private static final String SERVER_LOCAL_ENV    = "https://api.local-env.goboomtown.com";

    public  static final String JSON_API_HOST       = "apiHost";
    public  static final String JSON_INTEGRATION_ID = "integrationId";
    public  static final String JSON_API_KEY        = "apiKey";

    public  static final Map<String, String> servers = new HashMap<String, String>() {{
        put(PRODUCTION, SERVER_PRODUCTION);
        put(PREPROD, SERVER_PREPROD);
        put(INTEG, SERVER_INTEG);
        put(UAT, SERVER_UAT);
        put(SANDBOX, SERVER_SANDBOX);
        put(LOCAL_ENV, SERVER_LOCAL_ENV);
    }};

    private static final int    REQUEST_SCREEN_CAPTURE = 100;

    public RestClient client = null;

    public static final String SDK_V1_ENDPOINT = "/sdk/v1";

    private WeakReference<Context> mContext;

    private SupportSDKListener mListener;

    public  Appearance      appearance;

    private Configuration   configuration;
    public  SessionManager  sessionManager;
    public  int             screenCapturePermissionResultCode;
    public  Intent          screenCapturePermissionData;

    public boolean isTLSv13Supported = false;

    public  Boolean isLocationPermitted = false;
    public  Boolean isCameraPermitted = false;
    public  Boolean isStoragePermitted = false;
    public  Boolean isForegroundPermitted = false;
    public  Boolean isVideoCapturePermitted = false;
    public  Boolean isAudioCapturePermitted = false;
    public  Boolean isExtStorageWriteable = false;
    public  Boolean isSystemOverlayPermitted = false;

    public int                          kbSubscreensOnStack;
    public boolean                      isKBRequested = false;
    public SupportSDKKBListener         mKBListener;
    public SupportSDKFormsListener      mFormsListener;
    public SupportSDKHistoryListener    mHistoryListener;
    public boolean                      isRetrievingKB = false;
    public boolean                      isRetrievingHistory = false;
    public boolean                      isRetrievingForms = false;
    public boolean                      isRetrievingJourneys = false;
    public ArrayList<Issue>    historyEntries = new ArrayList<>();
    private JSONArray                   supportFormsIdsJSON;
    private int                         nFormRetrievalAttempts;
    private ProgressDialog              mProgress;

    public  Locale   locale;

    /**
     * ID of provider
     */
    public String   providerId;

    /**
     * ID of member (also referred to as merchant)
     */
    public String memberID;

    /**
     * ID of member user
     */
    public String memberUserID;

    /**
     * ID of member user's location
     */
    public String memberLocationID;

    /**
     * ID of member user device
     */
    public String memberDeviceID;

    /**
     * ID of video call
     */
    public String   callId;

    public String rateableIssueId;

    private HashMap<String, String>  customerInfo;

    public boolean chatEnabled;
    public boolean callmeEnabled;
    public boolean kbEnabled;
    public boolean websiteEnabled;
    public boolean emailEnabled;
    public boolean phoneEnabled;
    public boolean formsEnabled;
    public boolean historyEnabled;
    public boolean journeysEnabled;

    public boolean supportProactiveEnabled;
    public boolean supportScreenShareEnabled;
    public String  supportEmailAddress;
    public String  supportPhoneNumber;
    public String  supportWebsite;
    public Uri     supportWebsiteURL;
    public String  callMeButtonText;
    public String  callMeButtonConfirmation;
    public boolean supportUnavailable;
    public String  supportUnavailableSummary;

    public boolean  isV2Settings;

    private KBViewModel             kbViewModel;
    public  ArrayList<FormModel>    forms = new ArrayList<>();
    public  ArrayList<JourneyModel> journeys = new ArrayList();

    /**
     * Default member values - from app/get
     */
    public String defaultMemberID;
    public String defaultMemberUserID;
    public String defaultMemberLocationID;
    public String defaultMemberDeviceID;

    public String supportSDKVersion;
    public String osVersion;

    public boolean cloudConfigComplete;

    public String   downloadSessionToken = null;

    public SupportSDK(Context context, String jsonString, HashMap<String, String> customerInfo, SupportSDKListener listener) {
        mContext = new WeakReference<>(context);
        this.customerInfo = customerInfo;
        mListener = listener;
        cloudConfigComplete = false;
        locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        setAPIInfo();
        appearance = new Appearance(context);
        configuration = new Configuration(mContext.get(), jsonString);
        client = new RestClient(isTLSv13Supported);
        providerId = configuration.configAPIIntegrationId;
        this.customerInfo = customerInfo;
        restGetSettings();
    }


    public void reset() {
        cloudConfigComplete = false;
    }


    public boolean isProduction() {
        return configuration.configAPIHost.equalsIgnoreCase(SERVER_PRODUCTION);
    }


    public String getHost() {
        return configuration.configAPIHost;
    }


    public void checkAppVersion(String appVersion) {
        if ( appVersion!= null ) {
            if (isNewAppVersion(appVersion)) {
                configuration.enableDeveloperMode(false);
            }
        }
    }


    private boolean isNewAppVersion(String appVersion) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        String lastVersion = prefs.getString(SupportSDK.KEY_LAST_VERSION, null);
        if ( appVersion.equalsIgnoreCase(lastVersion) ) {
            return false;
        }
        return true;
    }


    public boolean isDeveloperMode() {
        return configuration.isDeveloperMode();
    }


    public void showProgressWithMessage(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                mProgress = ProgressDialog.show(mContext.get(), null, message, true);
            }
        });
    }

    public void hideProgress() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        });
    }


    public void reloadConfiguration(JSONObject json) {
        cloudConfigComplete = false;
        configuration.tryNewConfiguration(json);
        restGetSettings();
    }


    public void resetToDefault() {
        configuration.restoreDefaultConfiguration();
        restGetSettings();
    }


    public void enableDeveloperMode(boolean enable) {
        configuration.enableDeveloperMode(enable);
    }

    public void requestScreenCapturePermission(Activity activity) {
        int version = Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "Requesting permission to capture screen");

            Intent intent = new Intent(mContext.get(), MediaProjectionService.class);
            ContextCompat.startForegroundService(mContext.get(), intent);

            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    activity.getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            // This initiates a prompt dialog for the user to confirm screen projection.
            try {
                activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                        REQUEST_SCREEN_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean checkScreenCapturePermission(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_SCREEN_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            screenCapturePermissionResultCode = resultCode;
            screenCapturePermissionData = data;
            return true;
        }
        return false;
    }

    public Boolean isConnected() {
        if ( sessionManager!=null ) {
            return sessionManager.isConnected();
        }
        return false;
    }

    public void startScreenSharingSession(Activity activity, ViewGroup parent, String id, String token) {
        sessionManager = new SessionManager(mContext.get(), parent);
        sessionManager.mActivity = activity;
        sessionManager.isScreenCapture = true;
        sessionManager.screenCapturePermissionResultCode = screenCapturePermissionResultCode;
        sessionManager.screenCapturePermissionData = screenCapturePermissionData;
        sessionManager.entity_id = "support";
        sessionManager.entity_name = "sdk";
        Boolean rc = sessionManager.createSession(id, token);
    }


    private void setAPIInfo() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        PackageManager manager = mContext.get().getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(mContext.get().getPackageName(), 0);
            supportSDKVersion = String.format(Locale.US, "%s Build %d", info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        osVersion = String.format(Locale.US, "Android %s (API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
    }

    /**
     * @return "SupportSDK {@link this#supportSDKVersion}, {@link this#osVersion}", for ex. "Support SDK 1.11, iOS 9.7.1 [ABC-123]"
     */
    public String clientAppIdentifier() {
        return String.format(Locale.US, "%s %s, %s", SupportSDKHelpName, supportSDKVersion, osVersion);
    }


    /**
     * Encrypt a string.
     * @param data  data string to encode
     * @return  Encoded string
     */
    public String encode(String data) {
        Mac sha256_HMAC;
        String signature;
        String key = getKey(); // configuration.configPrivateKey;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                sha256_HMAC.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                signature = Base64.encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);
            } else {
                sha256_HMAC.init(new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "HmacSHA256"));
                signature = Base64.encodeToString(sha256_HMAC.doFinal(data.getBytes(Charset.forName("UTF-8"))), Base64.DEFAULT);
            }
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return signature;
    }

    /**
     * Get private encryption key
     *
     * @return {@link Configuration#configPrivateKey}
     */
    public String getKey() {
//        return configuration.configPrivateKey;
        return configuration.configAPIKey;
    }

    /**
     * HTTP POST to the SupportSDK cloud.
     *
     * @param uri URL context for POST endpoint
     * @param jsonParams JSON payload to POST
     * @param callback handler to execute on HTTP response
     */
    public void post(String uri, JSONObject jsonParams, Callback callback) {
        final Callback remoteCallback = callback;
        Callback localCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                remoteCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processHeadersForResonse(response);
                remoteCallback.onResponse(call, response);
            }
        };
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders();
        client.post(mContext.get(), requestUrl, jsonParams, localCallback);
        Log.v(TAG, "request to " + requestUrl + " sent with body " + jsonParams.toString());
    }

    /**
     * Upload an image to the SupportSDK cloud.
     *
     * @param uri URL context for POST endpoint
     * @param jsonParams JSON payload to POST
     * @param image image to upload
     * @param callback handler to execute on HTTP response
     */
    public void post(String uri, JSONObject jsonParams, Bitmap image, String name, Callback callback) {
        final Callback remoteCallback = callback;
        Callback localCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                remoteCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processHeadersForResonse(response);
                remoteCallback.onResponse(call, response);
            }
        };
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders();
        client.post(mContext.get(),requestUrl, jsonParams, image, name, localCallback);
    }

    /**
     * HTTP GET from the SupportSDK cloud.
     * @param uri URL context to GET
     * @param callback handler to execute on HTTP response
     */
    public void get(String uri, Callback callback) {
        final Callback remoteCallback = callback;
        Callback localCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                remoteCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processHeadersForResonse(response);
                remoteCallback.onResponse(call, response);
            }
        };
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders();
        client.get(mContext.get(), requestUrl, localCallback);
    }


    /**
     * HTTP GET from the SupportSDK cloud.
     * @param uri URL context to GET
     * @param params    query parameters
     * @param callback handler to execute on HTTP response
     */
    public void get(String uri, JSONObject params, Callback callback) {
        final Callback remoteCallback = callback;
        Callback localCallback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                remoteCallback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                processHeadersForResonse(response);
                remoteCallback.onResponse(call, response);
            }
        };
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders();
        client.get(mContext.get(), requestUrl, params, localCallback);
    }


    private HashMap<String, String> addHeaders() {
        HashMap<String, String> headerMap = new HashMap<>();

        headerMap.put("X-Boomtown-Token",       configuration.configPartnerToken);
        headerMap.put("X-Boomtown-Integration", configuration.configAPIIntegrationId);
        headerMap.put("X-Boomtown-Key",         configuration.configAPIKey);
        if (!cloudConfigComplete || supportProactiveEnabled) {
            headerMap.put("X-Boomtown-DeviceUUID",         configuration.configDeviceUUID);
            headerMap.put("X-Boomtown-InstallationUUID",         configuration.configInstallationUUID.toString());
            headerMap.put("X-Boomtown-DeviceMAC",         configuration.configDeviceMAC);
            headerMap.put("X-Boomtown-DeviceIP",         configuration.configDeviceIP);
        }
        headerMap.put("X-Boomtown-User-Agent", clientAppIdentifier());
        if ( downloadSessionToken != null ) {
            headerMap.put(HTTP_HEADER_DOWNLOAD_TOKEN, downloadSessionToken);
        }
        BoomtownChat.sharedInstance().httpHeaders = headerMap;
        return headerMap;
    }


    public boolean isProactiveEnabled() {
        return supportProactiveEnabled;
    }


    /**
     * @param response  Response to convert to JSON
     * @return non-null object if response is valid JSON + has a top-level "success" boolean value; null otherwise
     */
    public static JSONObject jsonObject(Response response){
        JSONObject result = null;
        try {
            if ( response != null ) {
                ResponseBody responseBody = response.body();
                if ( responseBody != null ) {
                    String responseBodyString = responseBody.string();
                    JSONObject object = new JSONObject(responseBodyString);
                    Log.d(TAG, "JSON RESULT: " + object.toString());
                    result = object;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (Exception ex1) {
            Log.d(TAG, Log.getStackTraceString(ex1));
        }
        return result;
    }


    /**
     * @param response  Response to evaluate for success
     * @return non-null object if response is valid JSON + has a top-level "success" boolean value; null otherwise
     */
    public static JSONObject successJSONObject(String response){
        JSONObject result = null;
        try {
            JSONObject object = new JSONObject(response);
            boolean success = object.optBoolean("success");
            if (success){
                result = object;
            }
            Log.d(TAG, "JSON RESULT: " + object.toString());
        } catch (JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (Exception ex1) {
            Log.d(TAG, Log.getStackTraceString(ex1));
        }
        return result;
    }

    /**
     * @param response  Response to evaluate for success
     * @return non-null object if response is valid JSON + has a top-level "success" boolean value; null otherwise
     */
    public static JSONObject successJSONObject(Response response){
        JSONObject result = null;
        try {
            if ( response != null ) {
                ResponseBody responseBody = response.body();
                if ( responseBody != null ) {
                    String responseBodyString = responseBody.string();
                    JSONObject object = new JSONObject(responseBodyString);
                    Log.d(TAG, "JSON RESULT: " + object.toString());
                    boolean success = object.optBoolean("success");
                    if (success){
                        result = object;
                    }
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (Exception ex1) {
            Log.d(TAG, Log.getStackTraceString(ex1));
        }
        return result;
    }


    /**
     * @param response  Response to evaluate for failure
     * @return non-null if response is valid JSON + has a top-level "message" string value; null otherwise
     */
    public static String failureMessageFromJSONData(String response) {
        String message = null;
        try {
            JSONObject object = new JSONObject(response);
            message = object.optString("message");
            Log.d(TAG, "JSON RESULT: " + object.toString());
        } catch (JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (Exception ex1) {
            Log.d(TAG, Log.getStackTraceString(ex1));
        }
        return message;
    }

    /**
     * @param response  Response to evaluate for failure
     * @return non-null if response is valid JSON + has a top-level "message" string value; null otherwise
     */
    public static String failureMessageFromJSONData(Response response) {
        String message = null;
        try {
            if ( response != null ) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseBodyString = responseBody.string();
                    JSONObject object = new JSONObject(responseBodyString);
                    message = object.optString("message");
                    Log.d(TAG, "JSON RESULT: " + object.toString());
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (Exception ex1) {
            Log.d(TAG, Log.getStackTraceString(ex1));
        }
        return message;
    }


    public void processHeadersForResonse(Response response) {
        String token = response.header(HTTP_HEADER_DOWNLOAD_TOKEN);
        if ( token != null ) {
            downloadSessionToken = token;
        }
    }


    /*
    {
             members_id : 'D23ATA',
             members_locations_id : 'D23ATA-CJ2',
             members_users_id : 'D23ATA-7SL',
             members_devices_id : 'H64SNC'
     }
     */
    public void restGetCustomerInformationWithInfo(HashMap<String, String> customerInfo, Callback callback) {
        if ( customerInfo==null || customerInfo.isEmpty() ) {
            return;
        }
        String uri = String.format("%s/customers/resolve", SupportSDK.SDK_V1_ENDPOINT);

        JSONObject infoJson = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            for ( String key : customerInfo.keySet() ) {
                infoJson.put(key, customerInfo.get(key));
            }
            params.put("ids", infoJson);
        } catch (JSONException e) {
            Log.e(TAG, "error when forming payload to get customer info");
            return;
        }
        if ( callback != null ) {
            this.post(uri, params, callback);
        } else {
            this.post(uri, params, new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    cloudConfigComplete = true;
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    boolean success = false;
                    String message = "";
                    if (response.code() > 199 && response.code() < 300) {
                        try {
                            ResponseBody responseBody = response.body();
                            String responseBodyString = Objects.requireNonNull(responseBody).string();
                            JSONObject jsonObject = new JSONObject(responseBodyString);
                            memberID = jsonObject.optString("members_id");
                            memberUserID = jsonObject.optString("members_users_id");
                            memberLocationID = jsonObject.optString("members_locations_id");
                            memberDeviceID = jsonObject.optString("members_devices_id");
                            saveMemberInfo();
                            success = true;
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            message = e.getLocalizedMessage();
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            message = e.getLocalizedMessage();
                        }
                    }
                    if ( !success ) {
                        cloudConfigComplete = true;
                        Log.i(TAG, "unable to get customer info, response (" + message + ")");
                    }
                }
            });
        }
    }


    private void restGetSettings() {
        String uri = String.format("%s/app/get", SupportSDK.SDK_V1_ENDPOINT);

        JSONObject params = new JSONObject();
        this.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cloudConfigComplete = false;
                disableAllComponents();
                if ( configuration.isTryingNewConfiguration ) {
                    configuration.restoreConfiguration();
                }
                if (mListener != null) {
                    mListener.supportSDKDidFailToGetSettings();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                processHeadersForResonse(response);
                boolean success = false;
                String message = "";
                if (response.code() > 199 && response.code() < 300) {
                    JSONObject jsonObject = SupportSDK.jsonObject(response);

                    if ( jsonObject != null ) {
                        success = jsonObject.optBoolean("success", false);
                        if ( success ) {
                            processSettings(jsonObject);
                        } else {
                            message = jsonObject.optString("message", "");
                        }
                    }
                }
                cloudConfigComplete = true;
                if (!success) {
                    disableAllComponents();
                    if ( configuration.isTryingNewConfiguration ) {
                        configuration.restoreConfiguration();
                    }
                    if (mListener != null) {
                        mListener.supportSDKDidFailToGetSettings();
                    }
                    Log.i(TAG, "unable to get app settings, response (" + message + ")");
                } else {
                    if ( configuration.isTryingNewConfiguration ) {
                        configuration.saveConfiguration();
                    }
                    if (mListener != null) {
                        mListener.supportSDKDidGetSettings();
                    }
                    if ( customerInfo != null ) {
                        restGetCustomerInformationWithInfo(customerInfo, null);
                    }
                }
            }
        });
    }

    private void disableAllComponents() {
        chatEnabled = false;
        callmeEnabled = false;
        kbEnabled = false;
        websiteEnabled = false;
        emailEnabled = false;
        phoneEnabled = false;
        formsEnabled = false;
        historyEnabled = false;
        journeysEnabled = false;
    }


    private void setMemberInfo(JSONObject defaultMember) {
        if (defaultMember != null) {
            defaultMemberID = defaultMember.optString("memberId");
            defaultMemberUserID = defaultMember.optString("memberUserId");
            defaultMemberLocationID = defaultMember.optString("memberLocationId");
            defaultMemberDeviceID = defaultMember.optString("memberDeviceId");

            memberID            = defaultMemberID;
            memberUserID        = defaultMemberUserID;
            memberLocationID    = defaultMemberLocationID;

            saveMemberInfo();
        }
    }


//    private void handleUnknownCustomer(String message) {
//        memberID            = defaultMemberID;
//        memberUserID        = defaultMemberUserID;
//        memberLocationID    = defaultMemberLocationID;
//        saveMemberInfo();
//    }


    public void saveMemberInfo() {
        BoomtownChat.sharedInstance().formsUserKey = memberUserID;
    }

    private void processSettings(JSONObject jsonObject) {
        JSONObject advancedConfiguration = null;
        advancedConfiguration = jsonObject.optJSONObject("advancedConfiguration");
        if ( advancedConfiguration != null ) {
            String version = advancedConfiguration.optString("version", "1");
            if ( version.equalsIgnoreCase("2") ) {
                processV2Settings(advancedConfiguration);
                return;
            }
        }
        processV1Settings(jsonObject, advancedConfiguration);
    }

    private void processV1Settings(JSONObject settings, JSONObject advanced) {
        chatEnabled = settings.optBoolean("supportChatEnabled");
        callmeEnabled = settings.optBoolean("supportCallmeEnabled");
        kbEnabled = settings.optBoolean("supportKBEnabled");
        websiteEnabled = settings.optBoolean("supportWebsiteEnabled");
        emailEnabled = settings.optBoolean("supportEmailEnabled");
        phoneEnabled = settings.optBoolean("supportPhoneEnabled");
        formsEnabled = settings.optBoolean("supportFormsEnabled");
        historyEnabled = settings.optBoolean("supportHistoryEnabled");
        journeysEnabled = settings.optBoolean("supportJourneysEnabled", true);

        appearance.chatTitle = settings.optString("chatTitle", null);
        appearance.callmeTitle = settings.optString("callmeMenuTitle", null);
        appearance.kbTitle = settings.optString("kbTitle", null);
        appearance.websiteTitle = settings.optString("webTitle", null);
        appearance.emailTitle = settings.optString("emailTitle", null);
        appearance.phoneTitle = settings.optString("phoneTitle", null);
        appearance.formsTitle = settings.optString("formsTitle", null);
        appearance.historyTitle = settings.optString("historyTitle", null);
        appearance.journeysTitle = settings.optString("journeysTitle", null);

        supportProactiveEnabled = settings.optBoolean("supportProactiveEnabled", false);
        supportScreenShareEnabled = settings.optBoolean("supportScreenShareEnabled", true);

        supportWebsite = settings.optString("supportWebsite");
        supportEmailAddress = settings.optString("supportEmail");
        supportPhoneNumber = settings.optString("supportPhone");
        callMeButtonText = settings.optString("callmeButtonText", mContext.get().getString(R.string.label_call_me));
        if (callMeButtonText.isEmpty()) {
            callMeButtonText = mContext.get().getString(R.string.label_call_me);
        }
        callMeButtonConfirmation = settings.optString("callmeButtonConfirmation", mContext.get().getString(R.string.label_call_me_confirmation));
        if (callMeButtonConfirmation.isEmpty()) {
            callMeButtonConfirmation = mContext.get().getString(R.string.label_call_me_confirmation);
        }
        supportFormsIdsJSON = settings.optJSONArray("supportForms");
        supportWebsiteURL = Uri.parse(supportWebsite); //, "http://example.com"));

        supportUnavailable = settings.optBoolean("unavailable", false);
        supportUnavailableSummary = settings.optString("unavailableSummary");

        JSONObject defaultMember = settings.optJSONObject("defaultMember");
        setMemberInfo(defaultMember);

        if ( advanced != null ) {
            appearance.configureWithJSON(advanced);
        }
    }

    private void processV2Settings(JSONObject settings) {
        isV2Settings = true;
        if ( settings == null ) {
            return;
        }
        JSONObject components = null;
        JSONObject advancedConfiguration = null;
        try {
            components = settings.getJSONObject("components");
            if ( components != null ) {
                JSONObject json = components.getJSONObject("chat");
                if ( json != null ) {
                    chatEnabled = json.optBoolean("enabled");
                    appearance.chatTitle = json.optString("title");
                }
                json = components.optJSONObject("callme");
                if ( json != null ) {
                    callmeEnabled = json.optBoolean("enabled");
                    appearance.callmeTitle = json.optString("menuTitle");
                    callMeButtonText = json.optString("callmeButtonText", mContext.get().getString(R.string.label_call_me));
                    if (callMeButtonText.isEmpty()) {
                        callMeButtonText = mContext.get().getString(R.string.label_call_me);
                    }
                    callMeButtonConfirmation = json.optString("callmeButtonConfirmation", mContext.get().getString(R.string.label_call_me_confirmation));
                    if (callMeButtonConfirmation.isEmpty()) {
                        callMeButtonConfirmation = mContext.get().getString(R.string.label_call_me_confirmation);
                    }
                }
                json = components.optJSONObject("kb");
                if ( json != null ) {
                    kbEnabled = json.optBoolean("enabled");
                    appearance.kbTitle = json.optString("title");
                }
                json = components.optJSONObject("website");
                if ( json != null ) {
                    websiteEnabled = json.optBoolean("enabled");
                    appearance.websiteTitle = json.optString("title");
                    supportWebsite = json.optString("supportWebsite");
                    supportWebsiteURL = Uri.parse(json.optString("supportWebsite", "http://example.com"));
                }
                json = components.optJSONObject("email");
                if ( json != null ) {
                    emailEnabled = json.optBoolean("enabled");
                    appearance.emailTitle = json.optString("title");
                    supportEmailAddress = json.optString("supportEmail");
                }
                json = components.optJSONObject("phone");
                if ( json != null ) {
                    phoneEnabled = json.optBoolean("enabled");
                    appearance.phoneTitle = json.getString("title");
                    supportPhoneNumber = json.optString("supportPhone");
                }
                json = components.optJSONObject("forms");
                if ( json != null ) {
                    formsEnabled = json.optBoolean("enabled");
                    appearance.formsTitle = json.optString("title");
                    supportFormsIdsJSON = json.optJSONArray("forms");
                }
                json = components.optJSONObject("history");
                if ( json != null ) {
                    historyEnabled = json.optBoolean("enabled");
                    appearance.historyTitle = json.optString("title");
                }
                json = components.optJSONObject("journeys");
                if ( json != null ) {
                    journeysEnabled = json.optBoolean("enabled");
                    appearance.journeysTitle = json.optString("title");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        supportProactiveEnabled = settings.optBoolean("supportProactiveEnabled", false);
        supportScreenShareEnabled = settings.optBoolean("supportScreenShareEnabled", true);

        supportUnavailable = settings.optBoolean("unavailable", false);
        supportUnavailableSummary = settings.optString("unavailableSummary");

        JSONObject defaultMember = settings.optJSONObject("defaultMember");
        setMemberInfo(defaultMember);

        appearance.configureWithJSON(settings);
    }


    public void getHistory() {
        if ( isRetrievingHistory ) {
            return;
        }
        isRetrievingHistory = true;
        String url = SupportSDK.SDK_V1_ENDPOINT + "/issues/history/?members_locations_id=" + memberLocationID;
        get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isRetrievingHistory = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;
                processHeadersForResonse(response);

                isRetrievingHistory = false;
                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            historyEntries.clear();
                            for ( int n=0; n<resultsJSON.length(); n++ ) {
                                try {
                                    JSONObject issueJSON = resultsJSON.getJSONObject(n);
                                    Issue issue = new Issue(issueJSON);
                                    historyEntries.add(issue);

                                    if ( mHistoryListener != null ) {
                                        mHistoryListener.supportSDKDidRetrieveHistory();
                                    }

                                    success = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            success = true;

                        } catch (JSONException e) {
//                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
//                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    public boolean hasForms() {
        return supportFormsIdsJSON!=null && supportFormsIdsJSON.length()>0;
    }

    public void getForms(final SupportSDKFormsListener formsListener) {
        if ( supportFormsIdsJSON.length() == 0 ) {
            return;
        }
        if ( isRetrievingForms ) {
            return;
        }
        mFormsListener = formsListener;
        nFormRetrievalAttempts = 0;
        forms.clear();
        for ( int n=0; n<supportFormsIdsJSON.length(); n++ ) {
            String id;
            try {
                 id = supportFormsIdsJSON.getString(n);
                 getForm(id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void getForm(String id) {
        String url = SupportSDK.SDK_V1_ENDPOINT + "/forms/get/";
        if ( id != null ) {
            url = url + "?id=" + id;
        }
        get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                checkIfFormsDone();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                processHeadersForResonse(response);
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
//                            forms.clear();
                            for ( int n=0; n<resultsJSON.length(); n++ ) {
                                FormModel formModel = new FormModel();
                                formModel.populateWithDictionary(resultsJSON.getJSONObject(n));
                                forms.add(formModel);
                            }
                            checkIfFormsDone();
                            success = true;

                        } catch (JSONException e) {
//                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
//                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }

    private void checkIfFormsDone() {
        if ( ++nFormRetrievalAttempts == this.supportFormsIdsJSON.length() ) {
            Collections.sort(forms, new OptionComparator());
            isRetrievingForms = false;
            hideProgress();
            if ( mFormsListener != null ) {
                mFormsListener.supportSDKDidRetrieveForms();;
            }
        }
    }

    public class OptionComparator implements Comparator<FormModel>
    {
        public int compare(FormModel left, FormModel right) {
            return left.name.compareTo(right.name);
        }
    }


    public void putForm(final SupportSDKFormsListener formsListener,  String id, JSONObject formJSON) {
        mFormsListener = formsListener;
        String url = SupportSDK.SDK_V1_ENDPOINT + "/forms/logs_put/?form_id=" + id;

        JSONObject formData = formJSON;
        try {
            formData.put("members_id", memberID);
            formData.put("members_users_id", memberUserID);
            formData.put("members_locations_id", memberLocationID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject params = new JSONObject();
        try {
             params.put("checklists_log", formData);
        } catch (JSONException e) {
            Log.e(TAG, "error when forming payload to get customer info");
            return;
        }

        this.post(url, params, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if ( formsListener != null ) {
                    formsListener.supportSDKFailedToUpdateForm();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    success = true;
                    if ( jsonObject.has("results") ) {
                    }
                }
                if ( success ) {
                    if ( formsListener != null ) {
                        formsListener.supportSDKDidUpdateForm();
                    }
                } else {
                    if ( formsListener != null ) {
                        formsListener.supportSDKFailedToUpdateForm();
                    }
                }
            }
        });
    }


    public FormModel currentForm() {
        if ( forms.size() > 1 ) {
            return forms.get(1);
        }
        return new FormModel();
    }



    public void getKB(final SupportSDKKBListener kbListener) {
        mKBListener = kbListener;
        if ( kbViewModel != null ) {
             if ( mKBListener != null ) {
                mKBListener.supportSDKDidRetrieveKB(kbViewModel);
            }
        }
        String url = SupportSDK.SDK_V1_ENDPOINT + "/kb/get";
        if ( isRetrievingKB ) {
            return;
        }
        isRetrievingKB = true;
        get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isRetrievingKB = false;
                hideProgress();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                isRetrievingKB = false;
                hideProgress();
                boolean success = false;
                processHeadersForResonse(response);

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");

                            kbViewModel = new KBViewModel(resultsJSON);
                            if ( mKBListener != null ) {
                                mKBListener.supportSDKDidRetrieveKB(kbViewModel);
                            }
                            success = true;
                        } catch (JSONException e) {
//                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
//                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    public void searchKB(final SupportSDKKBListener kbListener, String query) {
        String url = SupportSDK.SDK_V1_ENDPOINT + "/kb/search?query=" + query;

        get(url, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            kbViewModel = new KBViewModel();
                            kbViewModel.entries = new ArrayList<>();
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            for ( int n=0; n<resultsJSON.length(); n++ ) {
                                JSONObject entryJSON = resultsJSON.getJSONObject(n);
                                KBEntryModel entry = new KBEntryModel(entryJSON);
                                if ( entry != null ) {
                                    kbViewModel.entries.add(entry);
                                }
                            }
                            if ( kbListener != null ) {
                                kbListener.supportSDKDidSearchKB(kbViewModel);
                            }
                            success = true;
                        } catch (JSONException e) {
//                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
                    if ( kbListener != null ) {
                        kbListener.supportSDKDidFailToSearchKB();
                    }
                }
            }
        });
    }


    public KBViewModel kbViewModel() {
        return kbViewModel;
    }


    //  Journey endpoints

    /**
     * Retrieve a list of customer journeys
     */
    public void getJourneys(Callable<Void> method) {
//        if ( isRetrievingJourneys ) {
//            return;
//        }
//        isRetrievingJourneys = true;
        JSONObject params = new JSONObject();
        try {
            params.put("members_id", memberID);
        } catch (JSONException e) {
            Log.e(TAG, "error when forming payload to get customer info");
        }

        String url = SupportSDK.SDK_V1_ENDPOINT + "/journeys/data?members_id=" + memberID;
        get(url, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isRetrievingJourneys = false;
                try {
                    method.call();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                processHeadersForResonse(response);
                boolean success = false;

                JSONObject jsonObject = SupportSDK.successJSONObject(response);
                if ( jsonObject != null ) {
                    if ( jsonObject.has("results") ) {
                        try {
                            journeys.clear();
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");
                            for ( int n=0; n<resultsJSON.length(); n++ ) {
                                JourneyModel journeyModel = new JourneyModel((JSONObject)resultsJSON.get(n));
                                journeys.add(journeyModel);
                            }
                            success = true;
                        } catch (JSONException e) {
//                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
                if ( !success ) {
//                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
                isRetrievingJourneys = false;
                try {
                    method.call();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    public interface SupportSDKListener {
        void supportSDKDidFailWithError(String description, String reason);
        void supportSDKDidGetSettings();
        void supportSDKDidFailToGetSettings();
    }

    public interface SupportSDKKBListener {
        void supportSDKDidRetrieveKB(KBViewModel kbViewModel);
        void supportSDKDidSearchKB(KBViewModel kbViewModel);
        void supportSDKDidFailToSearchKB();
    }

    public interface SupportSDKFormsListener {
        void supportSDKDidRetrieveForms();
        default void supportSDKDidFailToRetrieveForms() {
            // default method implementation
        }
        void supportSDKDidRetrieveForm(FormModel form);
        void supportSDKDidUpdateForm();
        void supportSDKFailedToUpdateForm();
    }

    public interface SupportSDKHistoryListener {
        void supportSDKDidRetrieveHistory();
        default void supportSDKDidFailToRetrieveHistory() {
            // default method implementation
        }
    }


    public static JSONObject extractXmppInformation(String xmppData, String key) {
        JSONObject jsonObject = null;
        try {
            byte[] data = decrypt(key, xmppData);
            String response = new String(data);
            jsonObject = new JSONObject(response);
        } catch (Exception var4) {
            var4.printStackTrace();
        }
        return jsonObject;
    }


    private static byte[] decrypt(String key, String data) throws Exception {
        byte[] keyBytes = new byte[32];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            System.arraycopy(key.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, keyBytes.length);
        } else {
            System.arraycopy(key.getBytes(Charset.forName("UTF-8")), 0, keyBytes, 0, keyBytes.length);
        }

        byte[] encrypted = Base64.decode(data, 2);
        SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(2, skeySpec);
        return cipher.doFinal(encrypted);
    }

    private static byte[] getKey(String keyString) {
        byte[] keyBytes = new byte[32];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            System.arraycopy(keyString.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, keyBytes.length);
        } else {
            System.arraycopy(keyString.getBytes(Charset.forName("UTF-8")), 0, keyBytes, 0, keyBytes.length);
        }

        return keyBytes;
    }


    public void getPermissions(Activity activity) {
        final Queue<String> permissionRationaleDisplayQueue = new LinkedList<>();
        final List<String> permRequestsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            isLocationPermitted = false;
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionRationaleDisplayQueue.add(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                permRequestsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            isLocationPermitted = true;
        }
        if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            isCameraPermitted = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.CAMERA)) {
                permissionRationaleDisplayQueue.add(Manifest.permission.CAMERA);
            } else {
                permRequestsNeeded.add(Manifest.permission.CAMERA);
            }
        } else {
            isCameraPermitted = true;
        }
        if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isStoragePermitted = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionRationaleDisplayQueue.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                permRequestsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            isStoragePermitted = true;
        }
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {
            if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                isForegroundPermitted = false;
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.FOREGROUND_SERVICE)) {
                    permissionRationaleDisplayQueue.add(Manifest.permission.FOREGROUND_SERVICE);
                } else {
                    permRequestsNeeded.add(Manifest.permission.FOREGROUND_SERVICE);
                }
            } else {
                isForegroundPermitted = true;
            }
        } else {
            isForegroundPermitted = true;
        }

        if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            isAudioCapturePermitted = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.RECORD_AUDIO)) {
                permissionRationaleDisplayQueue.add(Manifest.permission.RECORD_AUDIO);
            } else {
                permRequestsNeeded.add(Manifest.permission.RECORD_AUDIO);
            }
        } else {
            isAudioCapturePermitted = true;
        }
        if (ContextCompat.checkSelfPermission(mContext.get(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isExtStorageWriteable = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionRationaleDisplayQueue.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                permRequestsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            isExtStorageWriteable = true;
        }
        activity.runOnUiThread(() -> {
            displayPermissionRationales(activity, permissionRationaleDisplayQueue);
            if (permRequestsNeeded.size() > 0) {
                String[] perms = new String[permRequestsNeeded.size()];
                permRequestsNeeded.toArray(perms);
                ActivityCompat.requestPermissions(activity, perms, 0);
            }
        });
    }

    protected void displayPermissionRationales(final Activity activity, Queue<String> permissionRationaleDisplayQueue) {
        if ( permissionRationaleDisplayQueue==null || permissionRationaleDisplayQueue.size()<1) {
            return;
        }
        String pr = permissionRationaleDisplayQueue.remove();
        switch (pr) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                displayLocationPermissionRationale(activity, permissionRationaleDisplayQueue);
                break;
            case Manifest.permission.CAMERA:
                displayCameraPermissionRationale(activity, permissionRationaleDisplayQueue);
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                displayStoragePermissionRationale(activity, permissionRationaleDisplayQueue);
                break;
            case Manifest.permission.FOREGROUND_SERVICE:
                displayForegroundPermissionRationale(activity, permissionRationaleDisplayQueue);
                break;
            case Manifest.permission.RECORD_AUDIO:
                // handle both perms in one rationale
                permissionRationaleDisplayQueue.remove(Manifest.permission.RECORD_AUDIO);
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                displayExtStorageRationale(activity, permissionRationaleDisplayQueue);
                break;
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                displaySystemAlertRationale(activity, permissionRationaleDisplayQueue);
                break;
            default:
                // noop
                break;
        }
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayLocationPermissionRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_location);
        builder.setMessage(R.string.text_why_location);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayCameraPermissionRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_camera);
        builder.setMessage(R.string.text_why_camera);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 2);
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayStoragePermissionRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_storage);
        builder.setMessage(R.string.text_why_storage);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayForegroundPermissionRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_foreground);
        builder.setMessage(R.string.text_why_foreground);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 2);
                    }
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displaySystemAlertRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_system_alert);
        builder.setMessage(R.string.text_why_system_alert);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                            0);
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayExtStorageRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
        builder.setTitle(R.string.text_ask_ext_storage);
        builder.setMessage(R.string.text_why_ext_storage);
        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
                (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
                (dialog, which) -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            0);
                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
                });
        builder.show();
    }


}
