package com.hyshare.groundservice.application;

import android.app.Application;
import android.content.Context;

import com.hyshare.groundservice.util.ToastUtil;

/**
 * Created by Administrator on 2018/5/4.
 */

public class BaseApplication extends Application {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ToastUtil.init(context, ToastUtil.ToastLocation.CENTER);
    }
}
