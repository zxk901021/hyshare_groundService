package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

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

        mLayoutBinding.account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editCheck()){
                    mLayoutBinding.login.setBackgroundResource(R.drawable.shape_login_no);
                    mLayoutBinding.login.setClickable(true);
                }else {
                    mLayoutBinding.login.setBackgroundResource(R.drawable.shape_login_ok);
                    mLayoutBinding.login.setClickable(false);
                }
            }
        });

        mLayoutBinding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editCheck()){
                    mLayoutBinding.login.setBackgroundResource(R.drawable.shape_login_no);
                    mLayoutBinding.login.setClickable(true);
                }else {
                    mLayoutBinding.login.setBackgroundResource(R.drawable.shape_login_ok);
                    mLayoutBinding.login.setClickable(false);
                }
            }
        });

    }

    private boolean editCheck(){
        username = mLayoutBinding.account.getText().toString().trim();
        password = mLayoutBinding.password.getText().toString().trim();
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    private boolean checkIsLogin() {
        return !TextUtils.isEmpty(SharedUtil.getString(context, KeyConstant.TOKEN));
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
