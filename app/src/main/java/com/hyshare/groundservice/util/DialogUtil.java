package com.hyshare.groundservice.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by Administrator on 2018/5/7.
 */

public class DialogUtil {

    private static KProgressHUD progressHUD = null;

    private static KProgressHUD init(Context context){
        if (progressHUD == null){
            progressHUD = KProgressHUD.create(context);
            return progressHUD;
        }else return progressHUD;
    }

    public static void loading(Context context){
        init(context);
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("正在加载")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
    }
    public static void dismiss(){
        if (progressHUD == null) return;
        else progressHUD.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void showDialog(Context context, String title, String message, DialogInterface.OnClickListener onSureClickListener, DialogInterface.OnCancelListener onCancelClickListener) {
        if (context != null && !((Activity) context).isDestroyed()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle(title + "");
            alertDialog.setMessage(message + "");
            alertDialog.setNegativeButton("确定", onSureClickListener);
            alertDialog.show();
        }
    }
}
