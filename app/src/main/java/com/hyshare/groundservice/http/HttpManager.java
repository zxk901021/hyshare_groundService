package com.hyshare.groundservice.http;

import android.content.Context;
import android.text.TextUtils;

import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.constant.RequestUrl;
import com.hyshare.groundservice.util.SharedUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/7.
 */

public class HttpManager {

    ApiService apiService;
    OkHttpClient.Builder builder;
    String token;
    String userId;

    OkHttpClient client;
    Retrofit retrofit;

    OnRequest onRequest;

    public void init(Context context) {
        builder = new OkHttpClient.Builder();
        token = SharedUtil.getString(context, KeyConstant.TOKEN, "");
        userId = SharedUtil.getString(context, KeyConstant.USER_ID, "");
        if (!TextUtils.isEmpty(token)) {
            builder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("X-CLIENT-TOKEN", token)
                            .addHeader("X-MEMBER-ID", userId)
                            .build();
                    return chain.proceed(request);
                }
            });
        }
        client = builder.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(RequestUrl.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void request(Observable observable){

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onRequest.onError(e);
                    }

                    @Override
                    public void onNext(Object o) {
                        onRequest.onNext(o);
                    }
                });
    }

    public interface OnRequest{
        void onError(Throwable e);
        void onNext(Object o);
    }
}
