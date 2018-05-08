package com.hyshare.groundservice.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.gyf.barlibrary.ImmersionBar;
import com.hyshare.groundservice.activity.LoginActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.constant.RequestUrl;
import com.hyshare.groundservice.http.ApiService;
import com.hyshare.groundservice.http.OnRequest;
import com.hyshare.groundservice.util.DialogUtil;
import com.hyshare.groundservice.util.SharedUtil;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public abstract class BaseActivity<T extends ViewDataBinding> extends AppCompatActivity {

    ApiService apiService;
    protected T mLayoutBinding;
    protected Context context;
    private KProgressHUD progressHUD;
    private OnRequest onRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mLayoutBinding = DataBindingUtil.setContentView(this, setLayoutId());
        initDialog();
        initHttp();
        initStatusBar();
        initUI();
        initData();
    }

    private void initDialog() {
        progressHUD = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("正在加载")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
    }

    protected void showDialog() {
        if (progressHUD != null) {
            progressHUD.show();
        } else {
            initDialog();
            progressHUD.show();
        }
    }

    protected void dismiss() {
        if (progressHUD != null) {
            progressHUD.dismiss();
        }
    }

    private void initHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        final String token = SharedUtil.getString(context, KeyConstant.TOKEN, "");
        final String userId = SharedUtil.getString(context, KeyConstant.USER_ID, "");
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
        OkHttpClient client = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RequestUrl.BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void initStatusBar() {
        ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .transparentNavigationBar()
                .init();
    }

    public void request(Observable<Object> observable) {
        showDialog();
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                        onRequest.onError(e);
                    }

                    @Override
                    public void onNext(Object o) {
                        dismiss();
                        onRequest.onNext(o);
                    }
                });
    }

    public abstract int setLayoutId();

    public abstract void initUI();

    public abstract void initData();

    public ApiService getApiService() {
        return apiService;
    }

    public RequestBody parseRequest(String json) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }

    protected void logout() {
        DialogUtil.showDialog(context, "提示", "当前账户已被别的设备登录，请重新登录！",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(context, LoginActivity.class));
                        SharedUtil.clearAll(context);
                        finish();
                    }
                }, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        System.exit(0);
                    }
                });
    }
}
