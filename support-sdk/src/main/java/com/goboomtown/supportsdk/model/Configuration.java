package com.goboomtown.supportsdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.goboomtown.supportsdk.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;


public class Configuration {

    private static final String TAG = Configuration.class.getSimpleName();

    private static final String SDK_PREFS_NAME = "prefs_supports_sdk";
    private static final String PREFS_KEY_SDK_DEVICE_UUID = "SDK_DEVICE_UUID";

    private static final String kKeyIntegrationId       = "integrationId";
    private static final String kKeyAPIKey              = "apiKey";
    private static final String kKeyPartnerToken        = "partnerToken";
    private static final String kKeyPrivateKey          = "privateKey";
    private static final String kKeyPartnerTeam         = "partnerTeam";
    private static final String kKeyAPIHost             = "apiHost";
    private static final String kKeyButtonURL           = "buttonURL";

    private WeakReference<Context> mContext;

    public String   configAPIHost;
    public String   configAPIIntegrationId;
    public String   configAPIKey;
    public String   configPartnerToken;
    public String   configPrivateKey;
    public String   configPartnerTeam;
    public String   configButtonURL;
    public String   configDeviceUUID;
    public UUID     configInstallationUUID;
    public String   configDeviceMAC;
    public String   configDeviceIP;

    public Configuration(Context context) {
        mContext = new WeakReference<>(context);
    }

    public boolean read(int configResourceId) throws IllegalStateException, IOException {
        String jsonString = Utils.readRawTextFile(mContext.get(), configResourceId);
        return read(jsonString);
    }

    public boolean read(String jsonString) throws IllegalStateException, IOException {
        JSONObject result;
        try {
            result = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.w(TAG, "string " + jsonString + " is not JSON (" + jsonString + ")\n" + Log.getStackTraceString(e));
            throw new IllegalStateException(e);
        }
        configAPIHost           = result.optString(kKeyAPIHost);
        configAPIHost = changeDomain(configAPIHost);
        configPartnerToken      = result.optString(kKeyPartnerToken);
        configPrivateKey        = result.optString(kKeyPrivateKey);
        configAPIIntegrationId  = result.optString(kKeyIntegrationId);
        configAPIKey            = result.optString(kKeyAPIKey);
        configPartnerTeam       = result.optString(kKeyPartnerTeam);
        configButtonURL         = result.optString(kKeyButtonURL);
        configDeviceUUID = Settings.Secure.ANDROID_ID;
        configInstallationUUID = generateInstallationUUID();
        configDeviceMAC = Utils.getMACAddress("wlan0");
        configDeviceIP = Utils.getIPAddress(true);
        return configPartnerToken != null &&
                configPrivateKey != null &&
                configAPIHost != null &&
                configAPIKey != null &&
                configAPIIntegrationId != null;
    }

    private String changeDomain(String host) {
        String newHost = host.replaceAll(".thinkrelay.com", ".goboomtown.com");
        return newHost;
    }

    /**
     * @see <a href="https://developer.android.com/training/articles/user-data-ids">https://developer.android.com/training/articles/user-data-ids</a>
     * @see <a href="https://en.proft.me/2017/06/13/how-get-unique-id-identify-android-devices/">https://en.proft.me/2017/06/13/how-get-unique-id-identify-android-devices/</a>
     * @see <a href="https://groups.google.com/forum/#!topic/android-developers/U4mOUI-rRPY">https://groups.google.com/forum/#!topic/android-developers/U4mOUI-rRPY</a>
     *
     * @return UUID stored in shared prefs; if none exists then it is generated
     */
    private UUID generateInstallationUUID() {
        SharedPreferences sharedpreferences = mContext.get().getSharedPreferences(SDK_PREFS_NAME, Context.MODE_PRIVATE);
        String prefsUUID = sharedpreferences.getString(PREFS_KEY_SDK_DEVICE_UUID, null);
        UUID ret;
        if (prefsUUID == null) {
            ret = UUID.randomUUID();
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(PREFS_KEY_SDK_DEVICE_UUID, ret.toString());
            editor.apply();
        } else {
            ret = UUID.fromString(prefsUUID);
        }
        return ret;
    }

}
