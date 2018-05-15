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

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    private static NumberProgressBar progressBar;
    private static Timer timer;
    private static PopupWindow pop;
    private static TimerTask task;
    private final static int MODE_LOADING = 1;
    private final static int MODE_FINISHED = 2;
    private static int currentMode = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showProgressDialog(Context context, String title, View parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.progress_bar_layout, null);
        progressBar = view.findViewById(R.id.number_progress);
        TextView message = view.findViewById(R.id.title);
        message.setText(title);

        pop = new PopupWindow(view, 580, 250);
        pop.setOutsideTouchable(false);
        pop.setElevation(10);
        pop.update();
        pop.showAtLocation(parent, Gravity.CENTER, 0, 80);

        progressBar.setOnProgressBarListener(new OnProgressBarListener() {
            @Override
            public void onProgressChange(int current, int max) {
                if (current >= 100) {
                    if (currentMode == MODE_FINISHED) {
                        pop.dismiss();
                        task.cancel();
                        timer.cancel();
                        progressBar.setOnProgressBarListener(null);
                        currentMode = MODE_LOADING;
                    }
                }
            }
        });
    }

    public static void startShowProgress() {
        currentMode = MODE_LOADING;
        timer = new Timer();
        timer.purge();
        task = new TimerTask() {
            @Override
            public void run() {
                Observable.just(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Integer>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Integer integer) {
                                progressBar.incrementProgressBy(1);
                            }
                        });
            }
        };
        timer.schedule(task, 500, 100);

    }

    public static void showMaxProgress() {
        currentMode = MODE_FINISHED;
        task.cancel();
        timer.purge();
        task = new TimerTask() {
            @Override
            public void run() {
                Observable.just(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Integer>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Integer integer) {
                                progressBar.incrementProgressBy(1);
                            }
                        });
            }
        };
        timer.schedule(task, 0, 10);


    }
}
