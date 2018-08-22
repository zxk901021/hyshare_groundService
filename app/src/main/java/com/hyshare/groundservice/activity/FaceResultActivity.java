package com.hyshare.groundservice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityFaceResultBinding;

public class FaceResultActivity extends BaseActivity<ActivityFaceResultBinding> {
    private boolean isSuccess;
    private Handler handler;

    public static void startAct(Context context, boolean isSuccess) {
        Intent intent = new Intent(context, FaceResultActivity.class);
        intent.putExtra("issuccess", isSuccess);
        context.startActivity(intent);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_face_result;
    }

    @Override
    public void initUI() {
//        mToolbarBinding.title.setText("人脸识别结果");
//        mToolbarBinding.back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        isSuccess = getIntent().getBooleanExtra("issuccess", false);

        if(isSuccess){
            handler=new Handler();
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {

                    finish();
                }
            },2000);
        }
        mLayoutBinding.setIsSuccess(isSuccess);
        mLayoutBinding.setClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        mLayoutBinding.ivFaceResult.setImageResource(isSuccess ? R.mipmap.success : R.mipmap.fail);
        mLayoutBinding.tvFaceTip.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
        mLayoutBinding.btnFaceButton.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
    }

    @Override
    public void initData() {

    }
}
