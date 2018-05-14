package com.hyshare.groundservice.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.hyshare.groundservice.R;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/5/7.
 */

public class DialogUtil {

    private static KProgressHUD progressHUD = null;

    private static KProgressHUD init(Context context) {
        if (progressHUD == null) {
            progressHUD = KProgressHUD.create(context);
            return progressHUD;
        } else return progressHUD;
    }

    public static void loading(Context context) {
        init(context);
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("正在加载")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
    }

    public static void dismiss() {
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

    static NumberProgressBar progressBar;
    static Timer timer;
    static int currentProgress;
    static PopupWindow pop;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showProgressDialog(Context context, String title, View parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.progress_bar_layout, null);
        progressBar = view.findViewById(R.id.number_progress);
        TextView message = view.findViewById(R.id.title);
        message.setText(title);

        progressBar.setOnProgressBarListener(new OnProgressBarListener() {
            @Override
            public void onProgressChange(int current, int max) {
                currentProgress = current;
            }
        });
        pop = new PopupWindow(view, 580, 250);
        pop.setOutsideTouchable(false);
        pop.setElevation(10);
        pop.update();
        pop.showAtLocation(parent, Gravity.CENTER, 0, 80);
//        progressDialog.setView(view);
//        progressDialog.show();
    }

    public static void startShowProgress(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                progressBar.incrementProgressBy(1);
            }
        }, 1000, 1000);
    }
    public static int getCurrentProgress(){
        return currentProgress;
    }

    public static void showMaxProgress(){
        progressBar.setProgress(100);
        pop.dismiss();
    }
}
