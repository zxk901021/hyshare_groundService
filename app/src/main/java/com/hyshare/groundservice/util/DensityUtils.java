package com.hyshare.groundservice.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import java.text.DecimalFormat;

/**
 * Created by zxk on 2018/8/22.
 */

public class DensityUtils {

    private DensityUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static float getWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static float getHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * @param d
     * @param w 要保留小数位数
     * @return
     */
    public static String doublelFormat(double d, int w) {
        DecimalFormat df = null;
        switch (w) {
            case 1:
                df = new DecimalFormat("######0.0");
                break;
            case 2:
                df = new DecimalFormat("######0.00");
                break;
            case 3:
                df = new DecimalFormat("######0.000");
                break;
            case 4:
                df = new DecimalFormat("######0.0000");
                break;
            case 5:
                df = new DecimalFormat("######0.00000");
                break;
        }
        return df.format(d);
    }

    public static Double mapDoubleFormat(double d, int w) {
        DecimalFormat df = null;
        switch (w) {
            case 1:
                df = new DecimalFormat("######0.0");
                break;
            case 2:
                df = new DecimalFormat("######0.00");
                break;
            case 3:
                df = new DecimalFormat("######0.000");
                break;
            case 4:
                df = new DecimalFormat("######0.0000");
                break;
            case 5:
                df = new DecimalFormat("######0.00000");
                break;
        }
        return Double.valueOf(df.format(d));
    }
}
