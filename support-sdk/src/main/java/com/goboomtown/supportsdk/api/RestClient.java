package com.goboomtown.supportsdk.api;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.goboomtown.chat.BoomtownChat;


//import org.conscrypt.Conscrypt;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {

    public static final String TAG = RestClient.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT = 60;    // 60 seconds

    private OkHttpClient client;
    private TrustManager[] trustManagers;
    private X509TrustManager trustManager;
    private TLSSocketFactory tlsSocketFactory;
    private boolean caUnknown;
    private boolean isTLSv13Supported;

    public  HashMap<String, String> headers;


    public RestClient(boolean enableTLSv13) {
        isTLSv13Supported = enableTLSv13;
        client = createClient();
    }


    public OkHttpClient getClient() {
        return client;
    }

    /**
     * Create an HTTP client with {@link OkHttpClient.Builder}.
     *
     * @return an HTTP client instance.  By default the client will use TLS.
     */
    protected OkHttpClient createClient() {
        OkHttpClient client = null;
        OkHttpClient.Builder clientBuilder = null;
        try {
            if ( isTLSv13Supported ) {
                clientBuilder = new OkHttpClient.Builder()
                        .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS))
                        .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            } else {
                clientBuilder = new OkHttpClient.Builder()
                        .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                        .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                        .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            }

            try {
                clientBuilder.sslSocketFactory(new TLSSocketFactory(isTLSv13Supported), new InternalX509TrustManager());
            } catch (Exception e) {
                e.printStackTrace();
            }

            client = clientBuilder.build();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (client == null) {
            // build client without HTTPS
            Log.w(TAG, "using non-HTTPS rest client");
            client = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
        } else {
            Log.i(TAG, "using HTTPS rest client");
        }
        return client;
    }

    /**
     * Perform an HTTP POST.
     *
     * @param context used to get last known location
     * @param requestUrl URL to GET
     * @param jsonParams json data POSTed in body
     * @param callback concrete callback implementation to execute on response
     */
    public void post(Context context, String requestUrl, JSONObject jsonParams, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(requestUrl)
                .headers(Headers.of(addBoomtownHeaders(context)))
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams.toString()))
                .build();
        // redact password before logging
        JSONObject logParams = Utils.redactProtectedJSON(jsonParams, Utils.DEFAULT_PROTECTED_JSON_PARAMS, null);
        if (logParams != null) {
            Log.v(TAG, "POST (" + requestUrl + ") with params: " + logParams.toString());
        }
        client.newCall(request).enqueue(callback);
    }

    /**
     * Perform an HTTP multipart POST of a bitmap image.
     *
     * @param context used to get last known location
     * @param requestUrl URL to POST
     * @param jsonParams json data POSTed in body
     * @param image image to POST as multipart
     * @param name name of image
     * @param callback concrete callback implementation to execute on response
     */
    public void post(Context context, String requestUrl, JSONObject jsonParams, Bitmap image, String name, okhttp3.Callback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", name, RequestBody.create(MediaType.parse("image/jpeg"), imageData))
//                .build();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        formBuilder.addFormDataPart("file-data", name, RequestBody.create(MediaType.parse("image/jpeg"), imageData));
        if ( jsonParams != null ) {
            Iterator<String> iter = jsonParams.keys();
            while ( iter.hasNext() ) {
                String key = iter.next();
                try {
                    formBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                            RequestBody.create(null, jsonParams.getString(key)));
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }
        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(requestUrl)
                .headers(Headers.of(addBoomtownHeaders(context)))
                .post(formBody)
                .build();
        // redact password before logging
        if ( jsonParams != null ) {
            JSONObject logParams = Utils.redactProtectedJSON(jsonParams, Utils.DEFAULT_PROTECTED_JSON_PARAMS, null);
            if (logParams != null) {
                Log.v(TAG, "POST w/image (" + requestUrl + ") image length: " + imageData.length + ", with params: " + logParams.toString());
            }
        }
        client.newCall(request).enqueue(callback);
    }

    /**
     * Perform an HTTP GET.
     *
     * @param context used to get last known location
     * @param requestUrl URL to GET
     * @param callback concrete callback implementation to execute on response
     */
    public void get(Context context, String requestUrl, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(requestUrl)
                .headers(Headers.of(addBoomtownHeaders(context)))
                .build();

        Log.v(TAG, "GET (" + requestUrl + ")");
        client.newCall(request).enqueue(callback);
    }


    /**
     * Perform an HTTP GET with params.
     *
     * @param context used to get last known location
     * @param requestUrl URL to GET
     * @param params    JSONObject containing query parameters
     * @param callback concrete callback implementation to execute on response
     */
    public void get(Context context, String requestUrl, JSONObject params, okhttp3.Callback callback) {
//        Request request = new Request.Builder()
//                .url(requestUrl)
//                .headers(Headers.of(addBoomtownHeaders(context)))
//                .build();
//
//        Log.v(TAG, "GET (" + requestUrl + ")");
//        client.newCall(request).enqueue(callback);

        HttpUrl.Builder httpBuilder = HttpUrl.parse(requestUrl).newBuilder();
        Iterator<String> iterator = params.keys();
        try {
            while ( iterator.hasNext() ) {
                String key = iterator.next();
                httpBuilder.addQueryParameter(key, params.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "GET (" + requestUrl + ")");
        Request request = new Request.Builder().url(httpBuilder.build()).build();
        client.newCall(request).enqueue(callback);

    }


    /**
     *
     * @param context context needed for the system location service
     * @return  Map of headers added
     */
    protected HashMap<String, String> addBoomtownHeaders(Context context) {
        if ( headers != null ) {
            BoomtownChat.sharedInstance().httpHeaders = headers;
            return headers;
        }

        HashMap<String, String> headerMap = new HashMap<>();
        Location location = getLastKnownLocation(context);
        String lat = "";
        String lon = "";
        if (location != null) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            lat = Double.toString(latitude);
            lon = Double.toString(longitude);

        }
        headerMap.put("latitude", lat);
        headerMap.put("longitude", lon);
        String ua = Utils.buildBoomtownUserAgent(context);
        headerMap.put("Boomtown-Agent", ua);
        if ( headers != null ) {
            for (String key : headers.keySet()) {
                headerMap.put(key, headerMap.get(key));
            }
        }
        BoomtownChat.sharedInstance().httpHeaders = headerMap;
        return headerMap;
    }

    /**
     *
     * @param context context needed for the system location service
     * @return last known user location provided by system location service
     */
    protected Location getLastKnownLocation(Context context) {
        if (context == null) {
            return null;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // app should have already requested location permission
            Log.w(TAG, "location provider unavailable - app not granted location permissions");
        }
//        else {
//            if (locationManager != null) {
//                String provider = locationManager.getBestProvider(criteria, false);
//                if (provider != null) {
//                    location = locationManager.getLastKnownLocation(provider);
//                }
//            }
//        }
        return location;
    }

    /**
     * Implementors handle callbacks from {@link RestClient} http methods.
     *
     * @author fbeachler
     */
    public abstract static class BaseCallback implements okhttp3.Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.w(TAG, "failure when calling " + Utils.truncate(call.request().url().toString(), 512) + "\nexception was: " + Log.getStackTraceString(e));
//            handleFailure(call, e);
//            onFailure(call, e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            Log.i(TAG, "successful response X-Request-ID: " + response.header("X-Request-ID", Constants.NULLSTR) + ") ");
            Log.d(TAG, "response from " + Utils.truncate(call.request().url().toString(), 512));
//            handleResponse(call, response);
//            onResponse(call, response);
        }

//        public abstract void handleFailure(Call call, IOException e);
//
//        public abstract void handleResponse(Call call, Response response) throws IOException;
    }

}