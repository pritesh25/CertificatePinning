package com.example.certificateissue;

import android.net.http.X509TrustManagerExtensions;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class AsyncTaskActivity extends AppCompatActivity {

    private String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_task);

        new DemoAsyncTask().execute();

    }

    private void validatePinning(X509TrustManagerExtensions trustManagerExt, HttpsURLConnection conn, Set<String> validPins) throws SSLException {
        String certChainMsg = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            List<X509Certificate> trustedChain = trustedChain(trustManagerExt, conn);
            for (X509Certificate cert : trustedChain) {
                byte[] publicKey = cert.getPublicKey().getEncoded();
                md.update(publicKey, 0, publicKey.length);
                String pin = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                certChainMsg += "    sha256/" + pin + " : " + cert.getSubjectDN().toString() + "\n";
                if (validPins.contains(pin)) {
                    Log.d(tag, "validPins = " + validPins);
                    Log.d(tag, "pin       = " + pin);
                    return;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            Log.d(tag, "(catch error) NoSuchAlgorithmException = " + e.getMessage());
            throw new SSLException(e);
        }
        throw new SSLPeerUnverifiedException("Certificate pinning " + "failure\n  Peer certificate chain:\n" + certChainMsg);
    }

    private List<X509Certificate> trustedChain(X509TrustManagerExtensions trustManagerExt, HttpsURLConnection conn) throws SSLException {
        Certificate[] serverCerts = conn.getServerCertificates();
        X509Certificate[] untrustedCerts = Arrays.copyOf(serverCerts, serverCerts.length, X509Certificate[].class);
        String host = conn.getURL().getHost();
        try {
            return trustManagerExt.checkServerTrusted(untrustedCerts, "RSA", host);
        } catch (CertificateException e) {
            Log.d(tag, "(CertificateException) catch error = " + e.getMessage());
            throw new SSLException(e);
        }
    }

    private class DemoAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);

                // Find first X509TrustManager in the TrustManagerFactory
                X509TrustManager x509TrustManager = null;
                for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        x509TrustManager = (X509TrustManager) trustManager;
                        break;
                    }
                }
                X509TrustManagerExtensions trustManagerExt = new X509TrustManagerExtensions(x509TrustManager);
                URL url = new URL("https://www.github.com/");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.connect();
                //urlConnection.getInputStream();              //Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=
                Set<String> validPins = validPins =Collections.singleton("WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=");

                validatePinning(trustManagerExt, urlConnection, validPins);
            } catch (SSLException e) {
                Log.d(tag, "(SSLException) catch error = " + e.getMessage());
            } catch (IOException e) {
                Log.d(tag, "(IOException) catch error = " + e.getMessage());
            } catch (KeyStoreException e) {
                Log.d(tag, "(KeyStoreException) catch error = " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                Log.d(tag, "(NoSuchAlgorithmException) catch error = " + e.getMessage());
            }

            return null;
        }
    }
}