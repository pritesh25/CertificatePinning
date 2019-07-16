package com.example.certificateissue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RetrofitMainActivity extends AppCompatActivity {

    private String tag = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testConnection();

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnection();
            }
        });

    }

    private void testConnection() {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("api.github.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
                .build();

        OkHttpClient httpClient = httpBuilder.certificatePinner(certificatePinner).build();


        Retrofit retrofit1 = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GithubService githubService = retrofit1.create(GithubService.class);

        Call<ResponseBody> call = githubService.getGithub();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    Log.d(tag, "response = " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(RetrofitMainActivity.this, "got response", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(tag, "SSL error = " + t.getMessage());
                Toast.makeText(RetrofitMainActivity.this, "SSL error = " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface GithubService {
        @GET("https://api.github.com/")
        Call<ResponseBody> getGithub();
    }
}