package com.goboomtown.supportsdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.goboomtown.supportsdk.api.SupportSDK;
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

    public JSONObject   configJSON;
    public String       configAPIHost;
    public String       configAPIIntegrationId;
    public String       configAPIKey;
    public String       configPartnerToken;
    public String       configPrivateKey;
    public String       configPartnerTeam;
    public String       configButtonURL;
    public String       configDeviceUUID;
    public UUID         configInstallationUUID;
    public String       configDeviceMAC;
    public String       configDeviceIP;

    public String       jsonString;
    public JSONObject   json;

    public boolean      isTryingNewConfiguration;
    public JSONObject   backupConfiguration;


    public Configuration(Context context) {
        mContext = new WeakReference<>(context);
    }

    public Configuration(Context context, String jsonString) {
        mContext = new WeakReference<>(context);
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
            saveDefaultConfiguraton(json);
            JSONObject savedConfiguration = getSavedConfiguration();
            populate(savedConfiguration!=null ? savedConfiguration : json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public boolean read(String jsonString) throws IllegalStateException, IOException {
        try {
            this.jsonString = jsonString;
            json = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.w(TAG, "string " + jsonString + " is not JSON (" + jsonString + ")\n" + Log.getStackTraceString(e));
            throw new IllegalStateException(e);
        }
        populate(json);
        return configPartnerToken != null &&
                configPrivateKey != null &&
                configAPIHost != null &&
                configAPIKey != null &&
                configAPIIntegrationId != null;
    }

    private void populate(JSONObject json) {
        configJSON              = json;
        configAPIHost           = json.optString(kKeyAPIHost);
        configAPIHost           = changeDomain(configAPIHost);
        configPartnerToken      = json.optString(kKeyPartnerToken);
        configPrivateKey        = json.optString(kKeyPrivateKey);
        configAPIIntegrationId  = json.optString(kKeyIntegrationId);
        configAPIKey            = json.optString(kKeyAPIKey);
        configPartnerTeam       = json.optString(kKeyPartnerTeam);
        configButtonURL         = json.optString(kKeyButtonURL);
        configDeviceUUID        = Settings.Secure.ANDROID_ID;
        configInstallationUUID  = generateInstallationUUID();
        configDeviceMAC         = Utils.getMACAddress("wlan0");
        configDeviceIP          = Utils.getIPAddress(true);
    }

    private String changeDomain(String host) {
        String newHost = host.replaceAll(".thinkrelay.com", ".goboomtown.com");
        return newHost;
    }


    public void tryNewConfiguration(JSONObject configuration) {
        isTryingNewConfiguration = true;
        backupConfiguration = json;
        populate(configuration);
    }


    public void restoreConfiguration() {
        populate(backupConfiguration);
        backupConfiguration = null;
        isTryingNewConfiguration = false;
    }


    public void restoreDefaultConfiguration() {
        JSONObject defaultConfiguration = getDefaultConfiguration();
        populate(defaultConfiguration);
        backupConfiguration = null;
        isTryingNewConfiguration = false;
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

    public void enableDeveloperMode(boolean enable) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SupportSDK.KEY_DEV_MODE_ENABLED, enable);
        editor.apply();
        if ( !enable ) {
            clearSavedConfiguration();
        }
    }


    public boolean isDeveloperMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        return prefs.getBoolean(SupportSDK.KEY_DEV_MODE_ENABLED, false);
    }

    public void saveConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        SharedPreferences.Editor editor = prefs.edit();
        String jsonString = configJSON.toString();
        editor.putString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG, jsonString);
        editor.apply();
    }


    public void clearSavedConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(SupportSDK.KEY_DEV_MODE_JSON_CONFIG);
        editor.apply();
    }


    public JSONObject getSavedConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        String jsonString = prefs.getString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG, null);
        if ( jsonString != null ) {
            try {
                JSONObject json = new JSONObject(jsonString);
                return json;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public JSONObject getDefaultConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        String jsonString = prefs.getString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG_DEFAULT, "{}");
        try {
            JSONObject json = new JSONObject(jsonString);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void saveDefaultConfiguraton(JSONObject json) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.get());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG_DEFAULT, json.toString());
        editor.apply();
    }



}
