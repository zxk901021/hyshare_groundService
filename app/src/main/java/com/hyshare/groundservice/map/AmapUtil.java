package com.hyshare.groundservice.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.LatLonPoint;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.hyshare.groundservice.model.MapPoint;
import com.hyshare.groundservice.util.PackageUtil;
import com.hyshare.groundservice.util.ToastUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Administrator on 2018/5/9.
 */

public class AmapUtil {
    public static String TAG = "AmapUtil";

    public static void goLocalNavApp(@NonNull final Context context, final double lat, final double lng) {
        ActionSheetDialog dialog = new ActionSheetDialog(context, new String[]{"百度地图", "高德地图 "}, null);
        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        double[] latlng = GPSUtil.gcj02_To_Bd09(lat, lng);
                        goBaiduMap(context, latlng[0], latlng[1]);
                        break;
                    case 1:
                        goAmap(context, lat, lng);
                        break;
                }
            }
        });
        dialog.show();
    }

    /**
     * 跳转至高德地图导航
     */
    public static void goAmap(@NonNull Context context, double lat, double lng) {
        if (PackageUtil.isAvilible(context, "com.autonavi.minimap")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            //导航URI
            //Uri uri = Uri.parse("androidamap://navi?sourceApplication=appname&lat=" + lat + "&lon=" + lng + "&dev=0&style=2");
            //线路规划URI
            Uri uri = Uri.parse("amapuri://route/plan/?dlat=" + lat + "&dlon=" + lng + "&dev=0&t=2");
            intent.setPackage("com.autonavi.minimap");
            intent.setData(uri);
            context.startActivity(intent);
        } else {
            ToastUtil.toast("请先安装高德地图");
        }
    }

    /**
     * 跳转至百度地图导航
     */
    public static void goBaiduMap(@NonNull Context context, double lat, double lng) {
        if (PackageUtil.isAvilible(context, "com.baidu.BaiduMap")) {
            Intent i1 = new Intent();
            // 步行导航URI
//        i1.setData(Uri.parse("baidumap://map/walknavi?destination=39.91441,116.40405"));
            //线路规划URI
            i1.setData(Uri.parse("baidumap://map/direction?destination=" + lat + "," + lng + "&mode=walking"));
            context.startActivity(i1);
        } else {
            ToastUtil.toast("请先安装百度地图");
        }
    }

    public static DPoint AMapToBaiduMap(Context context, DPoint latLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.BAIDU);
        try {
            converter.coord(latLng);
            return converter.convert();
            //  LatLng desLatLng = converter.convert();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 计算两坐标点之间的距离
     * 单位：米
     *
     * @param startPoint
     * @param endPoint
     * @return
     */
    public static float calculateLineDistance(MapPoint startPoint, MapPoint endPoint) {
        return CoordinateConverter.calculateLineDistance(startPoint.toDPint(), endPoint.toDPint());
    }

    public static String format(float distance){
        BigDecimal bd = new BigDecimal(distance);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.toString();
    }


    /**
     * 获取与中心点最近的点
     *
     * @param centerPoint
     * @param pointList
     * @return
     */
    public static MapPoint getClosePoint(MapPoint centerPoint, List<MapPoint> pointList) {
        if (centerPoint == null || pointList == null || pointList.size() == 0) {
            return null;
        }
        MapPoint result = null;
        float distance = 0f;
        for (MapPoint point : pointList) {
            float d = calculateLineDistance(centerPoint, point);
            if (d > distance) {
                result = point;
                distance = d;
            }
        }

        return result;
    }

    public static Marker getCloseMarker(MapPoint centerPoint, List<Marker> markers) {
        if (centerPoint == null || markers == null || markers.size() == 0) {
            return null;
        }
        Marker result = null;
        double distance = 0f;
        for (Marker marker : markers) {
            double d = calculateLineDistance(centerPoint, new MapPoint(marker.getPosition()));
            Log.d(TAG, "getCloseMarker: " + d);
            if ((distance == 0 || d < distance) && "0".equals(marker.getSnippet())) {
                result = marker;
                distance = d;
            }
        }
        return result;
    }

    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    public static String getFriendlyTime(int second) {
        if (second > 3600) {
            int hour = second / 3600;
            int miniate = (second % 3600) / 60;
            return hour + "小时" + miniate + "分钟";
        }
        if (second >= 60) {
            int miniate = second / 60;
            return miniate + "分钟";
        }
        return second + "秒";
    }

    public static String getFriendlyLength(int lenMeter) {
        if (lenMeter > 10000) // 10 km
        {
            int dis = lenMeter / 1000;
            return dis + "公里";
        }

        if (lenMeter > 1000) {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + "公里";
        }

        if (lenMeter > 100) {
            int dis = lenMeter / 50 * 50;
            return dis + "米";
        }

        int dis = lenMeter / 10 * 10;
        if (dis == 0) {
            dis = 10;
        }
        return dis + "米";
    }

}
