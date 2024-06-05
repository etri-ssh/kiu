package com.example.ccp.common.retrofit;

import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private RetrofitClient(){}
    public static Retrofit initRetrofit(String url, int timeout, Converter.Factory factory){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout,TimeUnit.SECONDS)
            .writeTimeout(timeout,TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build();

        return new Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(factory)
            .client(client)
            .build();
    }

    public static Retrofit getRetrofit(String path, int timeout){return initRetrofit(path, timeout, GsonConverterFactory.create());}
    public static Retrofit getRetrofitXML(String path){return initRetrofit(path, 3, TikXmlConverterFactory.create());}
}
