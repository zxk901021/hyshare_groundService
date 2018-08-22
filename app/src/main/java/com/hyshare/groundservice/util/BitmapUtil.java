package com.hyshare.groundservice.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxk on 2018/7/25.
 */

public class BitmapUtil {

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertBitmapToString(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return android.util.Base64.encodeToString(appicon, android.util.Base64.DEFAULT);
    }

    public static byte[] convertBitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return android.util.Base64.encode(appicon, android.util.Base64.DEFAULT);
    }


    public static String convertByteToString(byte[] bitmap) {
        if (bitmap == null) {
            return null;
        }
        return android.util.Base64.encodeToString(bitmap, android.util.Base64.DEFAULT);
    }


    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToBitmap(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = android.util.Base64.decode(st, android.util.Base64.DEFAULT);
            bitmap =
                    BitmapFactory.decodeByteArray(bitmapArray, 0,
                            bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap compressBitmap(Bitmap bitmap, int size) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //    try {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
//        }catch (Exception e){
//            Log.e("wx",e.toString());
//        }

        float zoom = (float) Math.sqrt(size * 1024 / (float) out.toByteArray().length);
        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        while (out.toByteArray().length > size * 1024) {
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }

        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = null;
        matrix = null;

        return result;
    }

    /**
     * 获取图片格式
     *
     * @param path
     * @return
     */
    public static String getImageFileType(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //让图片不加载到内存中
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        String type = options.outMimeType;
        if (TextUtils.isEmpty(type)) {
            type = "未知格式";
        } else {
            type = type.substring(6, type.length());
        }

        return type;
    }

    /**
     * 获取图片格式
     *
     * @param paths
     * @return
     */
    public static List<String> getImageFileType(List<String> paths) {
        if (paths == null || paths.size() == 0) {
            return null;
        }
        ArrayList<String> resutl = new ArrayList<>();
        for (String path : paths) {
            resutl.add(getImageFileType(path));
        }

        return resutl;
    }
}
