package com.example.wind.mycomic.utils;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HttpsUtil {
    /**
     * 有安全證書的SSLContext
     *
     * @return SSLContext
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static InputStream ssl_asset_stream;

    public static SSLContext getSSLContextWithCer() throws NoSuchAlgorithmException, IOException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        // 實例化SSLContext
        SSLContext sslContext = SSLContext.getInstance("SSL");

        // 從assets中加載證書
        // 在HTTPS通訊中最常用的是cer/crt和pem
        InputStream inStream = ssl_asset_stream;

        /*
         * X.509 標準規定了證書可以包含什麼信息，並說明了記錄信息的方法 常見的X.509證書格式包括：
         * cer/crt是用於存放證書，它是2進制形式存放的，不含私鑰。
         * pem跟crt/cer的區別是它以Ascii來表示，可以用於存放證書或私鑰。
         */
        // 證書工廠
        CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
        Certificate cer = cerFactory.generateCertificate(inStream);

        // 密鑰庫
        /*
         * Pkcs12也是證書格式 PKCS#12是“個人信息交換語法”。它可以用來將x.509的證書和證書對應的私鑰打包，進行交換。
         */
        KeyStore keyStory = KeyStore.getInstance("PKCS12");
//		 keyStory.load(inStream, "123456".toCharArray());
        keyStory.load(null, null);
        // 加載證書到密鑰庫中
        keyStory.setCertificateEntry("tsl", cer);

        // 密鑰管理器
        KeyManagerFactory kMFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kMFactory.init(keyStory, null);
        // 信任管理器
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmFactory.init(keyStory);

        // 初始化sslContext
        sslContext.init(kMFactory.getKeyManagers(), tmFactory.getTrustManagers(), new SecureRandom());
        inStream.close();
        return sslContext;

    }

    /**
     * 沒有安全證書的SSLContext
     *
     * @return SSLContext
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @SuppressLint("TrulyRandom")
    public static SSLContext getSSLContextWithoutCer() throws NoSuchAlgorithmException, KeyManagementException {
        // 實例化SSLContext
        // 這裡參數可以用TSL 也可以用SSL
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { trustManagers }, new SecureRandom());
        return sslContext;

    }

    private static TrustManager trustManagers = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };
    /**
     * 驗證主機名
     */
    public static HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    };
}