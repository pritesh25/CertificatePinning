package com.example.certificateissue;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class ProjectCertificateActivity extends AppCompatActivity {

    private String tag = this.getClass().getSimpleName();
    private String domainname = "https://api.github.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_certificate);

        postSSL();
    }

    private void postSSL() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new BufferedInputStream(getAssets().open("github.crt"));

            Certificate ca = cf.generateCertificate(inputStream);
            inputStream.close();

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                    if (hostname.contains("github.com"))
                        return true;

                    return hv.verify("github.com", session);
                }
            };


            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url1 = new URL("https://github.com/");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url1.openConnection();
            urlConnection.setHostnameVerifier(hostnameVerifier);
            urlConnection.setSSLSocketFactory(new xxtlsskt());

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("");
            writer.flush();
            writer.close();
            os.close();

            // POST PARAMETERS
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                int response = urlConnection.getResponseCode();
            }

            InputStream inputStream1 = urlConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream1));

            String s1, response = null;
            while ((s1 = buffer.readLine()) != null) {
                response = s1;
            }

            Log.d(tag, "response = " + response);

        } catch (Exception e) {
            Log.d(tag, "(Exception) catch error = " + e.getMessage());
        }
    }

/*    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }*/

}
