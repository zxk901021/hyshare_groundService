package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivitySettingBinding;
import com.hyshare.groundservice.util.SharedUtil;

public class SettingActivity extends BaseActivity<ActivitySettingBinding> implements View.OnClickListener{

    @Override
    public int setLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initUI() {

        mLayoutBinding.back.setOnClickListener(this);
        mLayoutBinding.logout.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.logout:
                Intent logout = new Intent(context, LoginActivity.class);
                startActivity(logout);
                SharedUtil.clearAll(context);
                finish();
                break;
        }
    }
}
