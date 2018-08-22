package com.hyshare.groundservice.face;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.hyshare.groundservice.activity.FaceResultActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

/**
 * Created by zxk on 2018/7/25.
 */

public class FaceOpenDoor {

    public static final int PAGE_INTO_LIVENESS = 100;
    public Activity baseActivity;
    public FaceInter faceInter;


    /**
     * 第一次开车门新增人脸识别逻辑
     * 人脸识别成功--》 开门
     */
    public void attemptOpenDoor(Activity baseActivity, FaceInter faceInter) {
        this.baseActivity = baseActivity;
        this.faceInter = faceInter;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(baseActivity);
        alertDialog.setTitle("开始人脸识别");
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //人脸识别操作
                attemptFaceScan();
                //  openDoor();
            }
        });
        alertDialog.show();


    }

    private void attemptFaceScan() {
        AndPermission.with(baseActivity)
                .requestCode(200)
                .permission(
                        Permission.CAMERA
                )
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (AndPermission.hasPermission(baseActivity, Permission.LOCATION)) {
                            enterFaceScanPage();
                        } else {
                            AndPermission.defaultSettingDialog(baseActivity, requestCode).show();
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (AndPermission.hasPermission(baseActivity, Permission.LOCATION)) {
                            enterFaceScanPage();
                        } else {
                            Toast.makeText(baseActivity, "请授予应用定位权限", Toast.LENGTH_SHORT).show();
                            AndPermission.defaultSettingDialog(baseActivity, requestCode).show();
                        }
                    }
                })
                .start();

    }

    private void enterFaceScanPage() {
        faceInter.gotoLiveNess();
    }


    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null) {
            return false;
        }
        if (requestCode == PAGE_INTO_LIVENESS) {
            boolean result = data.getBooleanExtra("result", false);//失败的返回
            if (result) {
                return true;
            } else {
                FaceResultActivity.startAct(baseActivity, false);
                return false;

            }
        }
        return false;
    }


    public interface FaceInter {

        public void gotoLiveNess();
    }
}
