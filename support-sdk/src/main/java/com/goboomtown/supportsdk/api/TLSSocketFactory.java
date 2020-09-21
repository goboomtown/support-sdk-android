package com.goboomtown.supportsdk.api;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author fkrauthan
 */
public class TLSSocketFactory extends SSLSocketFactory {

    private static final String TAG = TLSSocketFactory.class.getSimpleName();

    public static final String SSL_CONTEXT_TLS = "TLS";

    private SSLSocketFactory internalSSLSocketFactory;

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance(SSL_CONTEXT_TLS);
        context.init(null, null, null);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

//    private Socket enableTLSOnSocket(Socket socket) {
//        if ( (socket instanceof SSLSocket) ) {
//            ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.2"});
//            // log supported sockets
//            String[] protocols = ((SSLSocket) socket).getEnabledProtocols();
//            for (String p : protocols) {
//                Log.d(TAG, "this socket supports protocol: " + p);
//            }
//        }
//        return socket;
//    }

    private Socket enableTLSOnSocket(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"});
//            ((SSLSocket) socket).setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});
            String[] protocols = ((SSLSocket) socket).getEnabledProtocols();
            for (String p : protocols) {
                Log.d(TAG, "this socket supports protocol: " + p);
            }
        }
        return socket;
    }

}