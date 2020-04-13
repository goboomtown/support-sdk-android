package com.goboomtown.supportsdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.minidns.util.InetAddressUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"WeakerAccess"})
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    private static final int BUFFER_SIZE_B64_ENCODE = 3 * 1024;
    private static final int BUFFER_SIZE_READ_TEXT = 4 * 1024;

    /**
     * @param file file to encode
     * @return file contents encoded as Base64
     * @throws IOException on various file errors
     */
    public static String base64Encode(final File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        // Base64 encode file contents
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE_B64_ENCODE);
        byte[] chunk = new byte[BUFFER_SIZE_B64_ENCODE];
        int len;
        while ((len = in.read(chunk)) == BUFFER_SIZE_B64_ENCODE) {
            contentBuilder.append(Base64.encodeToString(chunk, Base64.DEFAULT));
        }
        if (len > 0) {
            chunk = Arrays.copyOf(chunk, len);
            contentBuilder.append(Base64.encodeToString(chunk, Base64.DEFAULT));
        }
        in.close();
        return contentBuilder.toString();
    }

    /**
     * Load a text file given by a resource ID.
     *
     * @param context    context that provides resources by {@link Context#getResources()}}
     * @param resourceId a valid resource ID
     * @return string contents of file
     * @throws java.io.IOException on error
     * @see Utils#readRawTextFile(InputStream)
     */
    public static String readRawTextFile(final Context context, final int resourceId) throws IOException {
        return readRawTextFile(new BufferedInputStream(context.getResources().openRawResource(resourceId)));
    }

    /**
     * Load a text file given by a file handle.
     *
     * @param file file to read
     * @return string contents of file
     * @throws java.io.IOException on error
     * @see Utils#readRawTextFile(InputStream)
     */
    public static String readRawTextFile(final File file) throws java.io.IOException {
        return readRawTextFile(new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE_READ_TEXT));
    }

    /**
     * Load text file with encoding UTF8withBOM or ANSI
     *
     * @param is input stream to read from; {@link InputStream#close()} will be called before method exit
     * @return contents of file as {@link String}
     */
    public static String readRawTextFile(final InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE_READ_TEXT);
            byte[] bytes = new byte[BUFFER_SIZE_READ_TEXT];
            boolean isUTF8 = false;
            int read, count = 0;
            while ((read = is.read(bytes)) != -1) {
                if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    isUTF8 = true;
                    baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count += read;
            }
            String rawText;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                rawText = isUTF8 ? new String(baos.toByteArray(), StandardCharsets.UTF_8) : new String(baos.toByteArray());
            } else {
                rawText = isUTF8 ? new String(baos.toByteArray(), Charset.forName("UTF-8")) : new String(baos.toByteArray());
            }
            return rawText;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    /**
     * Returns MAC address of given interface name.
     *
     * @param iFaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(final String iFaceName) {
        if (iFaceName == null || iFaceName.isEmpty()) {
            return iFaceName;
        }
        String ret = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iFace : interfaces) {
                if (!iFace.getName().equalsIgnoreCase(iFaceName)) {
                    continue;
                }
                byte[] mac = iFace.getHardwareAddress();
                if (mac == null) {
                    break;
                }
                StringBuilder buf = new StringBuilder();
                for (byte macPiece : mac) {
                    buf.append(String.format("%02X:", macPiece));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                ret = buf.toString();
                break;
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (ret == null || ret.isEmpty()) {
            try {
                // linux hack
                ret = readRawTextFile(new File("/sys/class/net/" + iFaceName + "/address")).trim().toUpperCase();
            } catch (IOException ex) {
                ret = null;
            }
        }
        return ret;
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(final boolean useIPv4) {
        return Utils.getIPAddress(null, useIPv4);
    }

    /**
     * Get IP address from given (named) network interface.
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(final String iFaceName, final boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iFace : interfaces) {
                List<InetAddress> addrs = Collections.list(iFace.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if ((iFaceName == null && !addr.isLoopbackAddress()) || (iFaceName != null && iFaceName.equals(iFace.getName()))) {
                        String sAddr = addr.getHostAddress();
                        if (useIPv4) {
                            boolean isIPv4 = InetAddressUtil.isIpV4Address(sAddr);
                            if (isIPv4) {
                                return sAddr;
                            }
                        }
                        int delimIndex = sAddr.indexOf('%'); // drop ip6 zone suffix
                        return delimIndex < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delimIndex).toUpperCase();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    public static Activity getActivity(Context context) {
        while (context != null && ContextWrapper.class.isAssignableFrom(context.getClass())) {
            if (Activity.class.isAssignableFrom(context.getClass())) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    public static boolean doesObjectContainField(Object object, String fieldName) {
        Class<?> objectClass = object.getClass();
        for (Field field : objectClass.getFields()) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }
}
