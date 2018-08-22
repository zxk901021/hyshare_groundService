package com.hyshare.groundservice.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.foamtrace.photopicker.PhotoPickerActivity;
import com.foamtrace.photopicker.SelectModel;
import com.foamtrace.photopicker.intent.PhotoPickerIntent;

import java.util.ArrayList;

/**
 * Created by zxk on 2018/8/22.
 */

public class AlbumModule {

    public static final int REQUEST_CAMERA_CODE = 0x001;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 1;
    private final ArrayList<String> imagePaths = new ArrayList<>();
    private Activity mActivity;
    private SelectModel type;
    private boolean isShowCarema;
    private int pic_max;

    public AlbumModule activity(Activity activity) {
        mActivity = activity;
        return this;
    }

    public AlbumModule type(SelectModel type) {
        this.type = type;
        return this;
    }

    public AlbumModule showCarema(boolean showCarema) {
        isShowCarema = showCarema;
        return this;
    }

    public AlbumModule picMax(int pic_max) {
        this.pic_max = pic_max;
        return this;
    }

    public void openAlbumActivity(ArrayList<String> image_url) {
        //第二个参数是需要申请的权限
        if (ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE2);
            }
            //权限还没有授予，需要在这里写申请权限的代码
        }else {
            //权限已经被授予，在这里直接写要执行的相应方法即可
            PhotoPickerIntent intent = new PhotoPickerIntent(mActivity);
            intent.setSelectModel(type);
            intent.setShowCarema(isShowCarema); // 是否显示拍照， 默认false
            intent.setMaxTotal(pic_max); // 最多选择照片数量，默认为9
            intent.setSelectedPaths(image_url); // 已选中的照片地址， 用于回显选中状态
            mActivity.startActivityForResult(intent, REQUEST_CAMERA_CODE);
        }
    }

    public void openAlbumActivity(Activity activity, SelectModel type, boolean showCarema, int pic_max) {
        PhotoPickerIntent intent = new PhotoPickerIntent(activity);
        intent.setSelectModel(type);
        intent.setShowCarema(showCarema); // 是否显示拍照， 默认false
        intent.setMaxTotal(pic_max); // 最多选择照片数量，默认为9
        intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
        activity.startActivityForResult(intent, REQUEST_CAMERA_CODE);
    }

    public static ArrayList<String> getImages(Intent intent) {
        return intent == null ? null : intent.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
    }
}
