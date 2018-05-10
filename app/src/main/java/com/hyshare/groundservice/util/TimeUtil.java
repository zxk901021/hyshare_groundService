package com.hyshare.groundservice.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 * Created by Administrator on 2018/5/10.
 */

public class TimeUtil {

    public static String timeFormat(long time) {
        long current = System.currentTimeMillis()/1000;
        long second = current - time;
        long days = second / 86400;
        second = second % 86400;
        long hours = second / 3600;
        second = second % 3600;
        long minutes = second / 60;
        second = second % 60;
        if (days > 0) {
            return days + "天" + hours + "小时" + minutes + "分" + second + "秒";
        } else if (days == 0 && hours > 0) {
            return hours + "小时" + minutes + "分" + second + "秒";
        } else if (days == 0 && hours == 0 && minutes > 0) {
            return minutes + "分" + second + "秒";
        } else return second + "秒";

    }
}
