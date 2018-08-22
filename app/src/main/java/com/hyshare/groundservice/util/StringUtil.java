package com.hyshare.groundservice.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zxk on 2018/8/22.
 */

public class StringUtil {

    public static String appendWithTag(List<String> list, String tag) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(tag);
            }
        }
        return builder.toString();
    }

    public static double getDouble(String d) {
        return TextUtils.isEmpty(d) ? 0 : Double.parseDouble(d);
    }
    public static boolean matcherEmail(String email){
        Pattern p= Pattern.compile("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?");
        Matcher m=p.matcher(email);
        return m.matches();
    }
    public static String getFormatDistance(long distance) {
//        String result = null;
//        if (distance >= 0 && distance < 1000) {
//            result = String.format("%s 米", distance);
//        } else if (distance >= 1000) {
//            double dis = distance / 1000d;
//            result = String.format("%.1f 公里", dis);
//        } else {
//            result = String.format("%s 米", distance);
//        }
//
//        return result;
        return distance + "公里";
    }

    public static String getFormatDistance(float d) {
        long distance = (long) d;
        return getFormatDistance(distance);
    }

    public static String getFormatDistance(String distance) {
        return getFormatDistance(TextUtils.isEmpty(distance) ? 0 : Float.parseFloat(distance));
    }

    /**
     * @param time 单位秒
     * @return
     */
    public static String getTime(int time) {
        if (time < 60 * 60) {
            return (time / 60) + " 分钟";
        } else {
            return (time / (60 * 60)) + " 小时 " + (time % (60 * 60) / 60) + " 分钟";
        }
    }


    /**
     * 为文本设置样式
     *
     * @param context
     * @param text
     * @param style
     * @return
     */
    public static SpannableString format(Context context, String text, int style) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new TextAppearanceSpan(context, style), 0, text.length(), 0);
        return spannableString;
    }

}
