package com.goboomtown.supportsdk.dnssd;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DNS-SD aka NSD service for Support SDK.
 */
public class BTConnectPresenceService {

    public static final String TAG = BTConnectPresenceService.class.getSimpleName();
    public static final String DNS_SD_KEY_TXT = "txt";
    public static final String NSD_SVC_NAME = "SupportSDK";
    public static final String NSD_SVC_NAME_PROTOCOL = "_supportsdk._tcp.";
    public static final int DEFAULT_NSD_SVC_PORT = 22666;
    public static final String DEVICE_OS_METADATA_KEY = "com.goboomtown.device_os";
    public static final String QUOTE = "\"";
    public static final String SPACE = " ";

    public SupportSDK   supportSDK = null;

    /**
     * Implementors can listen to advertisement events.
     */
    public interface ServiceAdvertisementListener {
        void didAdvertiseService();
        void didFailToAdvertiseService();
    }

    /**
     * singleton class instance
     */
    private static BTConnectPresenceService instance;

    /**
     *
     */
    private Context ctx;

    /**
     * DNS-SD (NSD) service broadcast name
     */
    protected String serviceName;

    /**
     * NSD svc manager
     */
    private NsdManager mNsdManager;

    /**
     * NSD svc registration listener
     */
    private NsdManager.RegistrationListener mRegistrationListener;

    /**
     * flag indicates if svc registration in progress
     */
    protected boolean svcRegistering;

    /**
     * flag indicates if svc registration finished+success
     */
    protected boolean svcRegistered;

    /**
     * Default payload data to send with each NSD broadcast.
     */
    protected Map<String, String> defaultPayloadData;

    /**
     * Custom payload data to send with NSD broadcast.  Data is not encrypted.
     */
    protected Map<String, String> customPayloadData;

    /**
     * List of keys contained in {@link this#customPayloadData} that should be
     * encrypted when broadcast.
     */
    protected List<String> customPayloadDataEncryptedVals;

    /**
     * Registered service advertisement listeners.
     */
    protected List<ServiceAdvertisementListener> advertisementListeners;

    /**
     * default c'tor
     *
     * @param context   Application context
     **/
    private BTConnectPresenceService(Context context) {
        ctx = context;
        serviceName = null;
        defaultPayloadData = buildDefaultPayloadData();
        customPayloadData = new HashMap<>();
        customPayloadDataEncryptedVals = new ArrayList<>();
        advertisementListeners = new ArrayList<>();
    }

    /**
     * Calls {@link this#BTConnectPresenceService(Context)} and adds given listener.
     *
     * @param context      Application context
     * @param listener the {@link ServiceAdvertisementListener} to add.
     */
    private BTConnectPresenceService(Context context, ServiceAdvertisementListener listener) {
        this(context);
        addAdvertisementListener(listener);
    }

    /**
     * Gets singleton instance of this class.
     *
     * @param context      Application context
     * @return  Instance of BTConnectPresenceService
     * @throws IllegalStateException with null context
     */
    public static BTConnectPresenceService getInstance(Context context) throws IllegalStateException {
        if (context == null) {
            throw new IllegalArgumentException("getInstance called with null context");
        }
        if (instance == null) {
            instance = new BTConnectPresenceService(context);
        }
        return instance;
    }

    /**
     * Gets singleton instance of this class with provided listener.
     *
     * @param context   Application context
     * @param listener the {@link ServiceAdvertisementListener} to use.
     * @return  Instance of BTConnectPresenceService
     * @throws IllegalStateException with null context
     */
    public static BTConnectPresenceService getInstance(Context context, ServiceAdvertisementListener listener) throws IllegalStateException {
        getInstance(context);
        return instance;
    }

    /**
     * @return true if NSD registration underway, false otherwise
     */
    public boolean isSvcRegistering() {
        return svcRegistering;
    }

    /**
     * @return true if NSD svc registered, false otherwise
     */
    public boolean isSvcRegistered() {
        return svcRegistered;
    }

    /**
     * Add a listener that can handle {@link ServiceAdvertisementListener} events.
     *
     * @param listener Listener to add
     */
    public void addAdvertisementListener(ServiceAdvertisementListener listener) {
        if (listener == null || advertisementListeners.contains(listener)) {
            return;
        }
        advertisementListeners.add(listener);
    }

    /**
     * Remove a registered listener.  If listener is not registered then does nothing.
     * @param listener Listener to remove
     */
    public void removeAdvertisementListener(ServiceAdvertisementListener listener) {
        if (listener == null) {
            return;
        }
        advertisementListeners.remove(listener);
    }

    /**
     * @return The registered {@link ServiceAdvertisementListener}s.
     */
    public List<ServiceAdvertisementListener> getAdvertisementListeners() {
        return advertisementListeners;
    }

    /**
     * Clears all registered {@link ServiceAdvertisementListener}s.
     */
    public void clearAdvertisementListeners() {
        advertisementListeners.clear();
    }

    /**
     * Start NSD svc.  Service runs until tearDown() is called.
     */
    public void start() {
        int port = ctx.getResources().getInteger(R.integer.dns_sd_port);
        if (mRegistrationListener == null) {
            initializeRegistrationListener();
        }
        registerDNSSDService(port);
    }

    /**
     * Unregister NSD svc.
     */
    public void tearDown() {
        if (!svcRegistered) {
            Log.v(TAG, "DNS-SD service not registered");
            return;
        }
        if (mRegistrationListener == null) {
            Log.i(TAG, "unable to unregister DNS-SD service, listener undefined");
            return;
        }
        mNsdManager.unregisterService(mRegistrationListener);
    }

    /**
     * Register NSD service.  Exits with no action if svc registration already underway.
     *
     * @param port port to use for registering mDNS svc
     */
    protected void registerDNSSDService(int port) {
        if (isSvcRegistering()) {
            Log.v(TAG, "registerDNSSDService called while service in process of registering");
            return;
        }
        if (isSvcRegistered()) {
            Log.v(TAG, "registerDNSSDService already registered");
            return;
        }
        svcRegistering = true;
        svcRegistered = false;
        NsdServiceInfo serviceInfo = buildDNSSDServiceInfo(port);


        Log.v(TAG, "registering NSD with service info: " + serviceInfo);
        try {
            mNsdManager = (NsdManager) ctx.getSystemService(Context.NSD_SERVICE);
            mNsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        } catch (Exception e) {
            svcRegistering = false;
            Log.w(TAG, "Error starting mDNS services\n" + Log.getStackTraceString(e));
        }
        Log.v(TAG, "registerDNSSDService finished");
    }

    /**
     * Build NSD service parameters.
     *
     * @param port port to use for registering mDNS svc
     * @return hydrated NsdServiceInfo instance
     */
    protected NsdServiceInfo buildDNSSDServiceInfo(int port) {
        InetAddress hostAddy = retrieveNonLoopbackIP();
        NsdServiceInfo serviceInfo = null;
        if (hostAddy != null) {
            serviceInfo = new NsdServiceInfo();
            // svc name subject to change based on conflicts
            // with other services advertised on the same network.
            serviceInfo.setServiceName(NSD_SVC_NAME);
            serviceInfo.setServiceType(NSD_SVC_NAME_PROTOCOL);
            serviceInfo.setHost(hostAddy);
            serviceInfo.setPort(port);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appendDNSSDServiceLollipop(serviceInfo);
        }
        return serviceInfo;
    }

    /**
     * Append TXT attribute (name/val pair) to serviceInfo instance.
     * The value used for TXT is provided by {@link this#buildTXTDataPayload()}.
     *
     * @param serviceInfo   Information to add to attributes
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void appendDNSSDServiceLollipop(NsdServiceInfo serviceInfo) {
        if (serviceInfo == null) {
            return;
        }
        try {
            serviceInfo.setAttribute(DNS_SD_KEY_TXT, buildTXTDataPayload());
        } catch (Exception|Error e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * Initialize NSD svc listener.
     */
    protected void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                svcRegistering = false;
                svcRegistered = true;
                // log success
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("DSNSD service registration success!  serviceInfo {}");
                if (serviceInfo != null) {
                    logMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    logMsg.append("=null");
                }
                Log.i(TAG, logMsg.toString());
                // notify listeners
                notifyAdvertisementListenersDidAdvertiseService();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // failed registration
                svcRegistering = false;
                svcRegistered = false;
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("DSNSD service registration failed, serviceInfo {}");
                if (serviceInfo != null) {
                    logMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    logMsg.append("=null");
                }
                logMsg.append(", errorCode=" + errorCode);
                Log.e(TAG, logMsg.toString());
                // notify listeners
                notifyAdvertisementListenersDidFailToAdvertiseService();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                // service unregistered.  Only happens when NsdManager.unregisterService() invoked with this listener.
                svcRegistering = false;
                svcRegistered = false;
                if (serviceInfo == null) {
                    Log.w(TAG, "oops!  DSNSD service unregistered but service info is empty!");
                    return;
                }
                Log.i(TAG, "DSNSD service unregistered, serviceInfo.name=" + serviceInfo.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // failed unregistration
                svcRegistering = false;
                svcRegistered = false;
                StringBuilder errMsg = new StringBuilder();
                errMsg.append("DSNSD service un-registration failed, serviceInfo {}");
                if (serviceInfo != null) {
                    errMsg.append(" name=").append(serviceInfo.getServiceName())
                            .append(", port=")
                            .append(serviceInfo.getPort())
                            .append(", type=")
                            .append(serviceInfo.getServiceType())
                            .append(", host=" + serviceInfo.getHost());
                } else {
                    errMsg.append("=null");
                }
                errMsg.append(", errorCode=" + errorCode);
                Log.e(TAG, errMsg.toString());
            }
        };
    }

    /**
     * Retrieve a non-loopback IP address assigned to this device.  Goes through all network
     * interfaces, and all IP addresses assigned to each interface.
     *
     * @return the first non-loopback, IPv4 address assigned to a network interface on the device
     */
    public static InetAddress retrieveNonLoopbackIP() {
        InetAddress hostAddy = null;
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements() && hostAddy == null; ) {
            if ( en != null ) {
                while (en.hasMoreElements() && hostAddy == null) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && !inetAddress.isLinkLocalAddress()
                                && inetAddress.isSiteLocalAddress()) {
                            Log.v(TAG, "network scan got host address=" + inetAddress.getHostAddress() + "\n");
                            hostAddy = inetAddress;
                            break;
                        }

                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("LOG_TAG", ex.toString());
        }
        return hostAddy;
    }

    protected Map<String,String> buildDefaultPayloadData() {
        Map<String, String> ret = new HashMap<>();
        String packageName = ctx.getPackageName();
        ret.put("product_name", ctx.getString(R.string.app_name));
        ret.put("bundle_id", packageName);
        PackageInfo info = null;
        try {
            info = ctx.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (info != null) {
            ret.put("version", info.versionName);
        } else {
            ret.put("version", "??");
        }
        ret.put("device", android.os.Build.MODEL);
        ret.put("manufacturer", android.os.Build.BRAND);
        // ret.put("os", android.os.Build.VERSION.RELEASE);
        ret.put("os", BTConnectPresenceService.deviceOS(ctx));
        return ret;
    }

    private void notifyAdvertisementListenersDidAdvertiseService() {
        if (advertisementListeners == null) {
            return;
        }
        for (int i = 0; i < advertisementListeners.size(); i++) {
            advertisementListeners.get(i).didAdvertiseService();
        }
    }

    private void notifyAdvertisementListenersDidFailToAdvertiseService() {
        if (advertisementListeners == null) {
            return;
        }
        for (int i = 0; i < advertisementListeners.size(); i++) {
            advertisementListeners.get(i).didFailToAdvertiseService();
        }
    }

    /**
     * Convenience methods for {@link this#addCustomPayloadData(String, String, boolean)} with encrypted = true.
     *
     * @param key   payload key
     * @param value payload value
     */
    public void addCustomPayloadData(String key, String value) throws IllegalArgumentException {
        addCustomPayloadData(key, value, true);
    }

    /**
     * Add name/value pair to data sent in NSD payload.
     *
     * @param key key to add
     * @param value value for given key
     * @param encrypt flag to encrypt value
     */
    public void addCustomPayloadData(String key, String value, boolean encrypt) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("attempt to add null key with value=" + value);
        }
        customPayloadData.put(key, value);
        if (encrypt  && !customPayloadDataEncryptedVals.contains(key)) {
            customPayloadDataEncryptedVals.add(key);
        }
    }

    /**
     * Remove a key/val pair from custom payload data.
     *
     * @param key   Key to indicate type of payload data to remove
     */
    public void removeCustomPayloadData(String key) {
        if (key == null) {
            return;
        }
        customPayloadData.remove(key);
        for (String k : customPayloadDataEncryptedVals) {
            if (key.equals(k)) {
                customPayloadDataEncryptedVals.remove(k);
                break;
            }
        }
    }

    /**
     * Set name/value pair to have encrypted value during broadcast.
     *
     * @param key key to encrypt when broadcasting data
     * @param encrypt   encrypt if true
     */
    public void setCustomPayloadDataEncrypted(String key, boolean encrypt) throws IllegalArgumentException {
        if (!customPayloadData.containsKey(key)) {
            throw new IllegalArgumentException("attempt to add null key");
        }
        customPayloadDataEncryptedVals.remove(key);
        if (encrypt) {
            customPayloadDataEncryptedVals.add(key);
        }
    }

    /**
     * assemble payload string as name/value pairs
     * "build=1" "device=android" "bundle_id=com.goboomtown.BoomtownConnectPaySTAGE" "manufacturer=Apple" "os=iOS 10.3.2" "version=3.2.2" "product_name=ConnectSTAGE"
     *
     * @return custom user-agent string
     */
    private String buildTXTDataPayload() {
        StringBuilder sb = new StringBuilder();
        // build default data
        for (String k : defaultPayloadData.keySet()) {
            sb.append(QUOTE);
            sb.append(k);
            sb.append("=");
            sb.append(defaultPayloadData.get(k));
            sb.append(QUOTE);
            sb.append(SPACE);
        }
        // build custom data
        // public/non-encrypted vals first
        for (String k : customPayloadData.keySet()) {
            if (!customPayloadDataEncryptedVals.contains(k)) {
                sb.append(QUOTE);
                sb.append(k);
                sb.append("=");
                sb.append(customPayloadData.get(k));
                sb.append(QUOTE);
                sb.append(SPACE);
            }
        }
        String k;
        for (int i = 0; i < customPayloadDataEncryptedVals.size(); i++) {
            sb.append(QUOTE);
            k = customPayloadDataEncryptedVals.get(i);
            sb.append(k);
            sb.append("=");
            try {
                sb.append(supportSDK.encode(Objects.requireNonNull(customPayloadData.get(k))));
            } catch ( NullPointerException e) {
                Log.getStackTraceString(e);
            } catch (Exception e) {
                Log.e(TAG, "error encrypting custom name/val pair for key=" + k
                        + ".  Stacktrace:\n" + Log.getStackTraceString(e));
            }
            sb.append(QUOTE);
            sb.append(SPACE);
        }
        return sb.toString().trim();
    }

    /**
     * Get name of OS running on device.
     *
     * @param context   Application context
     * @return device OS name
     */
    public static String deviceOS(Context context) {
        String device_os = null;
        try {
            String package_name = context.getPackageName();
            if (package_name != null) {
                ApplicationInfo info = context.getPackageManager().getApplicationInfo(package_name, PackageManager.GET_META_DATA);
//                if ( info instanceof ApplicationInfo && info.metaData instanceof Bundle) {
                if ( info!=null && info.metaData!=null ) {
                    device_os = info.metaData.getString(DEVICE_OS_METADATA_KEY);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "cannot get metadata from AndroidManifest\n" + Log.getStackTraceString(e));
        }
        if (device_os == null) {
            device_os = "android";
        }
        return device_os;
    }
}
