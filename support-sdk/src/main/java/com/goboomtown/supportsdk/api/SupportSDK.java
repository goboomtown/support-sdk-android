package com.goboomtown.supportsdk.api;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IInterface;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;

import com.goboomtown.supportsdk.BuildConfig;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.fragment.KBExpandableListAdapter;
import com.goboomtown.supportsdk.model.Configuration;
import com.goboomtown.supportsdk.model.KBViewModel;
import com.goboomtown.supportsdk.service.MediaProjectionService;
import com.goboomtown.supportsdk.view.SupportView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Larry Borsato on 2016-07-12.
 */
public class SupportSDK
    implements POSConnectorBase.POSConnectorListener {

    private static final String TAG = SupportSDK.class.getSimpleName();
    private static final String SupportSDKHelpName = "SupportSDK";

    private static final int    REQUEST_SCREEN_CAPTURE = 100;

    public static String SupportSDKBaseURL  =  "https://api.goboomtown.com";

//    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//            .build();
    public RestClient client = null;

    public static String kEndpoint          = "/api/v2";
    public static String kV2kEndpoint       = "/api/v2";
    public static String kV3Endpoint        = "/api/v3";
    public static String kSDKV1Endpoint     = "/sdk/v1";

    private WeakReference<Context> mContext;

    private SupportSDKListener mListener;

    private Configuration   configuration;
    public  SessionManager  sessionManager;
    public SupportView supportView;
    public  int             screenCapturePermissionResultCode;
    public  Intent          screenCapturePermissionData;

    public Boolean isLocationPermitted = false;
    public Boolean isCameraPermitted = false;
    public Boolean isStoragePermitted = false;
    public Boolean isForegroundPermitted = false;
    public Boolean isVideoCapturePermitted = false;
    public Boolean isAudioCapturePermitted = false;
    public Boolean isExtStorageWriteable = false;
    public Boolean isSystemOverlayPermitted = false;

    public Locale   locale;

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

    private HashMap<String, String>  mCustomerInfo;

    public boolean supportProactiveEnabled;
    public boolean supportScreenShareEnabled;
    public String  supportEmailAddress;
    public String  supportPhoneNumber;
    public String  supportWebsite;
    public Uri     supportWebsiteURL;
    public boolean showKnowledgeBase;
    public boolean showSupportEmail;
    public boolean showSupportPhone;
    public boolean showSupportWebsite;
    public boolean showSupportCallMe;
    public String  callMeButtonText;
    public String  callMeButtonConfirmation;
    public boolean supportUnavailable;
    public String  supportUnavailableSummary;

    private KBViewModel  kbViewModel;

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

    public SupportSDK(Context context, SupportSDKListener listener) {
        mContext = new WeakReference<>(context);
        mListener = listener;
        cloudConfigComplete = false;
        configuration = new Configuration(mContext.get());
        locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        setAPIInfo();

        POSConnector posConnector = new POSConnector(mContext.get(), this);
        posConnector.getAccount();
    }


    @Override
    public void posConnectorDidRetrieveAccount(Object merchant) {

    }

    @Override
    public void posConnectorDidToFailRetrieveAccount(String message) {

    }


    /**
     * Load SupportSDK configuration.
     *
     * @param configResourceId raw JSON resource
     * @param customerInfo list of possible customer identification keys and values
     */
    public void loadConfiguration(int configResourceId, HashMap<String, String> customerInfo) {
//        if ( customerId == null ) {
//            Log.e(TAG, "Unable to load configuration. You must set the SupportSDK.customerId with a valid customerId.");
//            if (mListener != null) {
//                mListener.supportSDKDidFailWithError(mContext.get().getString(R.string.error_unable_to_get_customer_info), mContext.get().getString(R.string.error_invalid_customer_id));
//            }
//            return;
//        }
        cloudConfigComplete = false;
        try {
            boolean loaded = configuration.read(configResourceId);
            if (loaded) {
                InputStream customCAFile = null;
                if (configuration.configAPIHost.contains(".integ.")) {
                    customCAFile = mContext.get().getAssets().open("api_integ_thinkrelay_com.crt");
                } else if (configuration.configAPIHost.contains(".uat.")) {
                    customCAFile = mContext.get().getAssets().open("api_uat_thinkrelay_com.crt");
                } else if (configuration.configAPIHost.contains(".boom.loc") || configuration.configAPIHost.contains(".local-env.")) {
                    customCAFile = mContext.get().getAssets().open("boomloc.crt");
                }
                client = new RestClient(customCAFile);
                providerId = configuration.configAPIIntegrationId;
                mCustomerInfo = customerInfo;
                restGetSettings();
            } else {
                if (mListener != null) {
                    mListener.supportSDKDidFailWithError(mContext.get().getString(R.string.error_unable_to_load_configuration), mContext.get().getString(R.string.error_could_not_load_file_with_resource_id) + configResourceId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void requestScreenCapturePermission(Activity activity) {
        int version = Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "Requesting permission to capture screen");

            Intent intent = new Intent(mContext.get(), MediaProjectionService.class);
            ContextCompat.startForegroundService(mContext.get(), intent);

            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            // This initiates a prompt dialog for the user to confirm screen projection.
            activity.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_SCREEN_CAPTURE);
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

//    public void startVideoChatSession(BoomtownComm call) {
//        if (call == null || call.video_conference_id == null || call.video_conference_id.isEmpty()) {
//            String sb = getString(com.goboomtown.core.R.string.no_active_call) + " " +
//                    getString(com.goboomtown.core.R.string.msg_unable_to_join_conference);
//            showErrorMessage(getString(R.string.unable_to_start_boomtown_video_support), sb);
//            return;
//        }
//        Intent intent = new Intent(this, VideoActivity.class);
//        intent.putExtra(VideoSettings.AvsSessionId, call.video_conference_id);
//        intent.putExtra(VideoSettings.AvsSessionDisplayName, String.format("%s %s", tech.first_name, tech.last_name));
//        startActivityForResult(intent, BaseToolBoxActivity.REQUEST_VIDEOCHAT);
//    }


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

    public String hostname() {
        return configuration.configAPIHost;
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
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders(uri);
        client.post(mContext.get(), requestUrl, jsonParams, callback);
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
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders(uri);
        client.post(mContext.get(),requestUrl, jsonParams, image, name, callback);
    }

    /**
     * HTTP GET from the SupportSDK cloud.
     * @param uri URL context to GET
     * @param callback handler to execute on HTTP response
     */
    public void get(String uri, Callback callback) {
        String requestUrl = String.format("%s%s", configuration.configAPIHost, uri);
        client.headers = addHeaders(uri);
        client.get(mContext.get(), requestUrl, callback);
    }

    private HashMap<String, String> addHeaders(String uri) {
        HashMap<String, String> headerMap = new HashMap<>();

        String iso8601Date 			= iso8601Date();
        String canonicalizedResource = String.format("%s:%s", uri, iso8601Date);
        String signature 			= null;
        try {
            signature = encode(canonicalizedResource);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        headerMap.put("X-Boomtown-Date",        iso8601Date);
        headerMap.put("X-Boomtown-Token",       configuration.configPartnerToken);
        headerMap.put("X-Boomtown-Signature",   signature);
        headerMap.put("X-Boomtown-Integration", configuration.configAPIIntegrationId);
        headerMap.put("X-Boomtown-Key",         configuration.configAPIKey);
        if (!cloudConfigComplete || supportProactiveEnabled) {
            headerMap.put("X-Boomtown-DeviceUUID",         configuration.configDeviceUUID);
            headerMap.put("X-Boomtown-InstallationUUID",         configuration.configInstallationUUID.toString());
            headerMap.put("X-Boomtown-DeviceMAC",         configuration.configDeviceMAC);
            headerMap.put("X-Boomtown-DeviceIP",         configuration.configDeviceIP);
        }
        headerMap.put("X-Boomtown-User-Agent", clientAppIdentifier());
        return headerMap;
    }

    private String iso8601Date() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

        // Use UTC as the default time zone.
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Calendar c = Calendar.getInstance();
        return dateFormat.format(c.getTime());
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


    /*
    {
             members_id : 'D23ATA',
             members_locations_id : 'D23ATA-CJ2',
             members_users_id : 'D23ATA-7SL',
             members_devices_id : 'H64SNC'
     }
     */
    public void restGetCustomerInformationWithInfo(HashMap<String, String> customerInfo, Callback callback) {
        String uri = String.format("%s/customers/resolve", SupportSDK.kSDKV1Endpoint);

        JSONObject infoJson = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            for ( String key : customerInfo.keySet() ) {
                infoJson.put(key, customerInfo.get(key));
            }
            params.put("ids", infoJson);
        } catch (JSONException e) {
            Log.e(TAG, "error when forming payload to get customer info");
            handleUnknownCustomer(e.getLocalizedMessage());
            return;
        }
        if ( callback != null ) {
            this.post(uri, params, callback);
        } else {
            this.post(uri, params, new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    cloudConfigComplete = true;
                    handleUnknownCustomer(e.getLocalizedMessage());
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
                        handleUnknownCustomer(message);
                        Log.i(TAG, "unable to get customer info, response (" + message + ")");
                    }
                }
            });
        }
    }


    private void handleUnknownCustomer(String message) {
//        if (mListener != null) {
//            mListener.supportSDKDidFailWithError(mContext.get().getString(R.string.error_unable_to_get_customer_info), message);
//        }
        memberID            = defaultMemberID;
        memberUserID        = defaultMemberUserID;
        memberLocationID    = defaultMemberLocationID;
    }


    /*
     Printing description of response:
     {
         altPartnerTeams =     (
         );
         apiHost = "https://api.integ.thinkrelay.com";
         callmeButtonText = "Call Me";
         "current_server_time" = "2020-01-22 13:36:37";
         defaultMember =     {
             memberDeviceId = J3B3BZ;
             memberId = D23ATA;
             memberLocationId = "D23ATA-CJ2";
             memberUserId = "D23ATA-7SL";
         };
         integrationEnabled = 1;
         integrationId = 92PZPN;
         partnerAvatar = "https://api1.integ.thinkrelay.com/api/v2/avatar/partners/H3F/100,100";
         partnerTeam = "H3F-YEN";
         routeTo = "";
         status = 200;
         success = 1;
         supportCallmeEnabled = 1;
         supportEmail = "smarttechsupport@goboomtown.com";
         supportEmailEnabled = 1;
         supportPhone = "+1 (419) 690-1610";
         supportPhoneEnabled = 1;
         supportProactiveEnabled = 1;
         supportScreenShareEnabled = 1;
         supportVideoEnabled = "<null>";
         supportWebsite = "https://www.goboomtown.com/";
         supportWebsiteEnabled = 1;
         unavailable = 0;
         unavailableSummary = "<null>";
     }
     */


    private void restGetSettings() {
        String uri = String.format("%s/app/get", SupportSDK.kSDKV1Endpoint);

        JSONObject params = new JSONObject();
        this.get(uri, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cloudConfigComplete = true;
                if (mListener != null) {
                    mListener.supportSDKDidFailWithError(mContext.get().getString(R.string.error_unable_to_get_settings), e.getLocalizedMessage());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean success = false;
                String message = "";
                if (response.code() > 199 && response.code() < 300) {
                    JSONObject jsonObject = SupportSDK.jsonObject(response);
                    if ( jsonObject != null ) {
                        success = jsonObject.optBoolean("success", false);
                        if ( success ) {
                            showKnowledgeBase = true;
                            supportProactiveEnabled = jsonObject.optBoolean("supportProactiveEnabled", false);
                            supportScreenShareEnabled = jsonObject.optBoolean("supportScreenShareEnabled", true);
                            supportWebsite = jsonObject.optString("supportWebsite");
                            supportEmailAddress = jsonObject.optString("supportEmail");
                            supportPhoneNumber = jsonObject.optString("supportPhone");
                            showSupportWebsite = jsonObject.optBoolean("supportWebsiteEnabled");
                            showSupportEmail = jsonObject.optBoolean("supportEmailEnabled");
                            showSupportPhone = jsonObject.optBoolean("supportPhoneEnabled");
                            showSupportCallMe = jsonObject.optBoolean("supportCallmeEnabled");
                            callMeButtonText = jsonObject.optString("callmeButtonText", mContext.get().getString(R.string.label_call_me));
                            if (callMeButtonText.isEmpty()) {
                                callMeButtonText = mContext.get().getString(R.string.label_call_me);
                            }
                            callMeButtonConfirmation = jsonObject.optString("callmeButtonConfirmation", mContext.get().getString(R.string.label_call_me_confirmation));
                            if (callMeButtonConfirmation.isEmpty()) {
                                callMeButtonConfirmation = mContext.get().getString(R.string.label_call_me_confirmation);
                            }

                            supportWebsiteURL = Uri.parse(jsonObject.optString("supportWebsite", "http://example.com"));
                            supportUnavailable = jsonObject.optBoolean("unavailable", false);
                            supportUnavailableSummary = jsonObject.optString("unavailableSummary");
                            JSONObject defaultMember = jsonObject.optJSONObject("defaultMember");
                            if (defaultMember != null) {
                                defaultMemberID = defaultMember.optString("memberId");
                                defaultMemberUserID = defaultMember.optString("memberUserId");
                                defaultMemberLocationID = defaultMember.optString("memberLocationId");
                                defaultMemberDeviceID = defaultMember.optString("memberDeviceId");
                            }
                        } else {
                            message = jsonObject.optString("message", "");
                        }
                    }
                }
                cloudConfigComplete = true;
                if (!success) {
                    if (mListener != null) {
                        mListener.supportSDKDidFailWithError(mContext.get().getString(R.string.error_unable_to_get_settings), message);
                    }
                    Log.i(TAG, "unable to get app settings, response (" + message + ")");
                } else {
                    if (mListener != null) {
                        mListener.supportSDKDidGetSettings();
                    }
                    getKB(null);
                    if ( mCustomerInfo != null ) {
                        restGetCustomerInformationWithInfo(mCustomerInfo, null);
                    }
                }
            }
        });
    }


    public void getKB(final SupportSDKKBListener kbListener) {
        String url = SupportSDK.kSDKV1Endpoint + "/kb/get";

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
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");

                            kbViewModel = new KBViewModel(resultsJSON);
                            if ( kbListener != null ) {
                                kbListener.supportSDKDidRetrieveKB(kbViewModel);
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
        String url = SupportSDK.kSDKV1Endpoint + "/kb/search?query=" + query;

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
                            JSONArray resultsJSON = jsonObject.getJSONArray("results");

                            kbViewModel = new KBViewModel(resultsJSON);
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
//                    warn(getString(R.string.app_name), getString(R.string.warn_unable_to_retrieve_kb));
                }
            }
        });
    }


    public KBViewModel kbViewModel() {
        return kbViewModel;
    }


    public interface SupportSDKListener {
        void supportSDKDidFailWithError(String description, String reason);
        void supportSDKDidGetSettings();
    }

    public interface SupportSDKKBListener {
        void supportSDKDidRetrieveKB(KBViewModel kbViewModel);
        void supportSDKDidSearchKB(KBViewModel kbViewModel);
        void supportSDKDidFailToSearchKB();
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


    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES/ECB/NoPadding");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(clear);
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

//        if (ContextCompat.checkSelfPermission(mContext.get(),
//                Manifest.permission.CAPTURE_VIDEO_OUTPUT) != PackageManager.PERMISSION_GRANTED) {
//            isVideoCapturePermitted = false;
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                    Manifest.permission.CAPTURE_VIDEO_OUTPUT)) {
//                permissionRationaleDisplayQueue.add(Manifest.permission.CAPTURE_VIDEO_OUTPUT);
//            } else {
//                permRequestsNeeded.add(Manifest.permission.CAPTURE_VIDEO_OUTPUT);
//            }
//        } else {
//            isVideoCapturePermitted = true;
//        }
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
//        if (ContextCompat.checkSelfPermission(mContext.get(),
//                Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
//            isSystemOverlayPermitted = false;
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                    Manifest.permission.SYSTEM_ALERT_WINDOW)) {
//                permissionRationaleDisplayQueue.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
//            } else {
//                permRequestsNeeded.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
//            }
//        } else {
//            isSystemOverlayPermitted = true;
//        }
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
//            case Manifest.permission.CAPTURE_VIDEO_OUTPUT:
//                displayAVCapturePermissionRationale(activity, permissionRationaleDisplayQueue);
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

    /**
     * Show an explanation to the user *asynchronously* -- don't block
     * this thread waiting for the user's response! After the user
     * sees the explanation, try again to request the permission.
     *
     * @param permissionRationaleDisplayQueue   Queue to add permission rationales to
     */
    protected void displayAVCapturePermissionRationale(final Activity activity, final Queue<String> permissionRationaleDisplayQueue) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.get());
//        builder.setTitle(R.string.text_ask_av_capture);
//        builder.setMessage(R.string.text_why_av_capture);
//        builder.setNegativeButton(mContext.get().getString(R.string.text_no),
//                (dialog, which) -> dialog.dismiss());
//        builder.setPositiveButton(mContext.get().getString(R.string.text_yes),
//                (dialog, which) -> {
//                    dialog.dismiss();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        ActivityCompat.requestPermissions(activity,
//                                new String[]{Manifest.permission.CAPTURE_VIDEO_OUTPUT, Manifest.permission.RECORD_AUDIO},
//                                0);
//                    }
//                    displayPermissionRationales(activity, permissionRationaleDisplayQueue);
//                });
//        builder.show();
    }



}