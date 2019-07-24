package com.example.certificateissue;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class ProjectCertificateActivity extends AppCompatActivity {

    private String tag = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_certificate);

        new DemoAsyncTask().execute();

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
                    if (hostname.contains("github.com")) {
                        Log.d(tag, "if executed , hostname = " + hostname);
                        return true;
                    } else {
                        Log.d(tag, "else executed");
                    }

                    return hv.verify("github.com", session);
                }
            };


            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url1 = new URL("https://api.github.com");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url1.openConnection();
            urlConnection.setHostnameVerifier(hostnameVerifier);
            urlConnection.setSSLSocketFactory(new xxtlsskt());

            urlConnection.setRequestMethod("GET");

            //https://stackoverflow.com/questions/29558759/what-is-the-use-of-httpurlconnection-classs-setdooutput-setdoinput-methods
            //urlConnection.setDoInput(true);
            //urlConnection.setDoOutput(true);

            //below line for POST method

            /*OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write("");
            writer.flush();
            writer.close();
            os.close();*/

            // POST PARAMETERS
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                int response = urlConnection.getResponseCode();
                Log.d(tag, "if response code = " + response);
            } else {
                int response = urlConnection.getResponseCode();
                Log.d(tag, "else response code = " + response);
            }

            InputStream inputStream1 = urlConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream1));

            String s1, response = null;
            while ((s1 = buffer.readLine()) != null) {
                response = s1;
            }

            Log.d(tag, "response = " + response);

            urlConnection.disconnect();


        } catch (SSLException e) {
            Log.d(tag, "(SSLException) catch error = " + e.getMessage());
        } catch (IOException e) {
            Log.d(tag, "(IOException) catch error = " + e.getMessage());
        } catch (KeyStoreException e) {
            Log.d(tag, "(KeyStoreException) catch error = " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d(tag, "(NoSuchAlgorithmException) catch error = " + e.getMessage());
        } catch (CertificateException e) {
            Log.d(tag, "(CertificateException) catch error = " + e.getMessage());
        } catch (KeyManagementException e) {
            Log.d(tag, "(KeyManagementException) catch error = " + e.getMessage());
        }
    }

    private class DemoAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            postSSL();

            return null;
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
