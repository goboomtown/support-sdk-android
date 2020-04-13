package com.goboomtown.supportsdk.api;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static final String REDACTED = "*****";
    public static final List<String> DEFAULT_PROTECTED_JSON_PARAMS = new ArrayList<>();
    static {
//        DEFAULT_PROTECTED_JSON_PARAMS.add(BoomtownAPI.JSON_KEY_PASSWORD);
    }

//    public static String getDeviceId(Context context) {
//        String deviceId = "";
////        if (deviceOS(context).contentEquals(BoomtownAPI.DEVICE_OS_POYNT)) {
////            String pushToken = AuthenticationStore.getInstance(context).getPushToken(context);
////            deviceId = (pushToken != null) ? Utils.md5(pushToken) : null;
////        } else {
////            deviceId = Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
////        }
//        return deviceId;
//    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return "";
    }

    /**
     * assemble custom user-agent string as pipe-delimited string
     * %AppId%|%AppVersionCode%|%AppVersionString%|%HardwareCat%|%HardwareSubcat%|%HardwareOS%|%HardwareOSVersion%|%SupportedVideoTypes%|%RingEnabled%
     * com.goboomtown.Boomtown|3|1.2|google|Nexus 7|android|4.4.4
     * com.goboomtown.BoomtownMember.test.stage|305000151|3.5.0|generic|C301|clover|4.4.2|twilio|ring-disabled
     * note: in iOS UA string looks like
     * Boomtown/1.0.1/iPhone/iOS/8.0.2
     *
     * @param context   Application context
     * @return custom user-agent string
     */
    public static String buildBoomtownUserAgent(Context context) {
        StringBuilder sb = new StringBuilder();
        String packageName = context.getPackageName();
        sb.append(String.format("%s|", packageName));
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(packageName, 0);
            if (info != null) {
                sb.append(String.format(Locale.US, "%d|%s|", info.versionCode, info.versionName));
            } else {
                sb.append("??|??|");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            sb.append("??|??|");
        }
        sb.append(String.format("%s|%s|", android.os.Build.BRAND, android.os.Build.MODEL));
        String device_os = deviceOS(context);
        sb.append(String.format("%s|%s", device_os, android.os.Build.VERSION.RELEASE));
//        sb.append(String.format("|%s", BoomtownAPI.sharedInstance().supportedVideoTypes));
//        sb.append(String.format("|%s", BoomtownAPI.sharedInstance().ringEnabled ? "ring-enabled" : "ring-disabled"));
        return sb.toString();
    }

    /**
     * Get name of OS running on device.
     *
     * @param context   Application context
     * @return device OS name
     */
    public static String deviceOS(Context context) {
        return "android";
    }


    /**
     * Convenience method for {@link Utils#truncate(String, int)} with len = 30.
     * @param in    String to truncate
     * @return  the truncated string
     */
    public static String truncate(final String in) {
        return truncate(in, 30);
    }

    /**
     * Convenience method for {@link Utils#truncate(String, int, String)} with {@link Constants#DEFAULT_ELLIPSIS}.
     * @param in    string to truncate
     * @param len   length to truncate
     * @return  the truncated string
     */
    public static String truncate(final String in, final int len) {
        return truncate(in, len, Constants.DEFAULT_ELLIPSIS);
    }

    /**
     * @param in string to truncate
     * @param len length to truncate, inclusive of ellipsis length
     * @param ellipsis string to append to truncated string
     * @return new instance of truncated string
     */
    public static String truncate(final String in, final int len, final String ellipsis) {
        if (in == null) {
            return null;
        }
        if (in.length() < 1) {
            return "";
        }
        if (in.length() < len) {
            return in;
        }
        String el = (ellipsis == null ? Constants.BLANK : ellipsis);
        return in.substring(0, len - 1 - el.length()) + el;
    }

    /**
     * Redact json data for certain "protected" keys.  The default string
     * used to redact is {@link this#REDACTED}.
     *
     * @param jsonToRedact - the JSON to redact
     * @param protectedKeys - the keys that should be redacted
     * @param redactString - the redaction string used to replace protected data
     * @return the redacted JSON
     */
    public static JSONObject redactProtectedJSON(JSONObject jsonToRedact, List<String> protectedKeys, String redactString) {
        JSONObject ret = null;
        try {
            ret = new JSONObject(jsonToRedact.toString());
            Iterator<String> it = jsonToRedact.keys();
            String replace = redactString;
            if (replace == null) {
                replace = REDACTED;
            }
            while (it.hasNext()) {
                String key = it.next();
                for (String protectedKey : protectedKeys) {
                    if (key.trim().toLowerCase().contains(protectedKey)) {
                        ret.put(key, replace);
                    }
                }
            }
        } catch (JSONException e) {
            // couldn't redact, log error
            Log.i(TAG, "unexpected error occurred attempting to redact json params:\n" + Log.getStackTraceString(e));
        }
        return ret;
    }

    /**
     * @param inches number of inches to test again
     * @param act    weak reference to provide {@link Activity#getWindowManager()}.
     * @return true if device screen is larger than inches; false otherwise
     */
    public static boolean isTabletWiderThan(float inches, WeakReference<Activity> act) {
        boolean isTablet = false;
        if (act == null || act.get() == null) {
            return false;
        }
        Display display = act.get().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        if (diagonalInches >= inches) {
            isTablet = true;
        }
        return isTablet;
    }

    /**
     * Set given view to {@link View#INVISIBLE}.
     *
     * @param view view to set
     */
    public static void hideView(View view) {
        if (view == null) {
            return;
        }
        view.setVisibility(View.INVISIBLE);
    }

    /**
     * Set given view to {@link View#VISIBLE}.
     *
     * @param view view to set
     */
    public static void showView(View view) {
        if (view == null) {
            return;
        }
        view.setVisibility(View.VISIBLE);
    }


}
