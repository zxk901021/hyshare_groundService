package com.hyshare.groundservice.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyshare.groundservice.R;

/**
 * @author Administrator
 * @date 2018/5/4
 */

public class ToastUtil {

    private static Toast mToast = null;
    private static Context mContext;
    private static ToastLocation mShowLocation;
    private static String oldMsg;
    private static long oneTime = 0;
    private static long twoTime = 0;
    private static View whiteToastView;
    private static View GrayToastView;

    public static void init(Context context, ToastLocation showLocation) {
        mContext = context;
        mShowLocation = showLocation;
        whiteToastView = LayoutInflater.from(context).inflate(R.layout.toast_custom_layout, null);
        GrayToastView = LayoutInflater.from(context).inflate(R.layout.toast_gray_custom, null);
    }

    public static void toast(String s) {
        if (mToast == null) {
            //mToast = getToast(s, mShowLocation);
            mToast = getGrayCustomToast(s);
            mToast.show();
        } else {
            twoTime = System.currentTimeMillis();
            if (s.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    mToast.show();
                }
            } else {
                oldMsg = s;
                ((TextView) (mToast.getView().findViewById(R.id.message))).setText(s);
                mToast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void toast(int resId) {
        toast(mContext.getString(resId));
    }

    private static Toast getCustomToast(String message, ToastLocation showLocation) {
        Toast customToas = new Toast(mContext);
        customToas.setGravity(Gravity.CENTER, 0, 0);
        TextView tv = (TextView) whiteToastView.findViewById(R.id.message);
        tv.setText(message);
        customToas.setDuration(Toast.LENGTH_SHORT);
        customToas.setView(whiteToastView);
        return customToas;
    }

    private static Toast getGrayCustomToast(String message) {
        Toast customToas = new Toast(mContext);
        customToas.setGravity(Gravity.CENTER, 0, 0);
        TextView tv = (TextView) GrayToastView.findViewById(R.id.message);
        tv.setText(message);
        customToas.setDuration(Toast.LENGTH_SHORT);
        customToas.setView(GrayToastView);
        return customToas;
    }

    private static Toast getToast(String message, ToastLocation showLocation) {
        Toast t = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        if (showLocation == ToastLocation.CENTER) {
            t.setGravity(Gravity.CENTER, 0, 0);
            LinearLayout view = (LinearLayout) t.getView();
            TextView tv = (TextView) view.findViewById(android.R.id.message);
            tv.setTextSize(16);
            tv.setTextColor(Color.parseColor("#F05C4C"));
            tv.setGravity(Gravity.CENTER);
            int hdp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mContext.getResources().getDisplayMetrics());
            int vdp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mContext.getResources().getDisplayMetrics());
            view.setPadding(hdp, vdp, hdp, vdp);
            view.setGravity(Gravity.CENTER);
        }
        return t;
    }

    public enum ToastLocation {
        CENTER, BOTTOM
    }
}
