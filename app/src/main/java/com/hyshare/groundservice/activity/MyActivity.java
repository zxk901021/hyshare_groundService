package com.hyshare.groundservice.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;

/**
 * @author Administrator
 */
public class MyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }

    @Override
    public int setLayoutId() {
        return 0;
    }

    @Override
    public void initUI() {

    }

    @Override
    public void initData() {

    }
}
