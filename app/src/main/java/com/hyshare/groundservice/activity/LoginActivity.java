package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.databinding.ActivityLoginBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.LoginBean;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private String username, password;

    @Override
    public int setLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initUI() {
        if (!checkIsLogin()){
            mLayoutBinding.login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login();
                }
            });
        }else {
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private boolean checkIsLogin() {
        if (TextUtils.isEmpty(SharedUtil.getString(context, KeyConstant.TOKEN))) return false;
        else return true;
    }

    @Override
    public void initData() {

    }

    private void login() {
        username = mLayoutBinding.account.getText().toString();
        password = mLayoutBinding.password.getText().toString();
        Map<String, Object> param = new HashMap<>();
        param.put("username", username);
        param.put("password", password);
        JSONObject object = new JSONObject(param);
        getApiService().login(parseRequest(object.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<LoginBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<LoginBean> loginBeanBaseModel) {
                        if (loginBeanBaseModel.getCode() == 1) {
                            String token = loginBeanBaseModel.getData().getToken();
                            String userId = loginBeanBaseModel.getData().getUser_id();
                            SharedUtil.putString(context, KeyConstant.TOKEN, token);
                            SharedUtil.putString(context, KeyConstant.USER_ID, userId);
                            SharedUtil.putString(context, KeyConstant.ACCOUNT, username);
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.toast(loginBeanBaseModel.getMessage());
                        }
                    }
                });

    }
}
