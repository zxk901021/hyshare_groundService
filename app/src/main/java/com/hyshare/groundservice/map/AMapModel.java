package com.hyshare.groundservice.map;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.amap.api.fence.GeoFence;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.util.SharedUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/5/8.
 */

public class AMapModel {

    private String TAG = getClass().getSimpleName() + ":";

    public static final float DEFAULT_LEVEL = 12;

    private Context mContext;
    private int mLocationIconRes = 0;
    private AMap mAMap;

    public void onDestory() {
        mAMap = null;
    }

    public AMapModel(Context context) {
        this.mContext = context;
    }

    public AMap bindMapViewNoLocation(TextureMapView mapView) {
        if (mapView == null) {
            throw new NullPointerException(TAG + "MapView 不能为null");
        }
        mAMap = mapView.getMap();
        initNoLocationAMap(mAMap);
        mapView = null;
        return mAMap;
    }

    public AMap bindMapView(MapView mapView) {
        if (mapView == null) {
            throw new NullPointerException(TAG + "MapView 不能为null");
        }
        mAMap = mapView.getMap();
        initAMap(mAMap);
        mapView = null;
        return mAMap;
    }

    public AMap bindMapView(TextureMapView mapView) {
        if (mapView == null) {
            throw new NullPointerException(TAG + "MapView 不能为null");
        }
        mAMap = mapView.getMap();
        initAMap(mAMap);
        mapView = null;
        return mAMap;
    }

    public AMap bindMapView(TextureMapView mapView, int locationIconRes) {
        this.mLocationIconRes = locationIconRes;
        if (mapView == null) {
            throw new NullPointerException(TAG + "MapView 不能为null");
        }
        mAMap = mapView.getMap();
        initAMap(mAMap);
        mapView = null;
        return mAMap;
    }

    public AMap bindCarMapView(TextureMapView mapView) {
        if (mapView == null) {
            throw new NullPointerException(TAG + "MapView 不能为null");
        }
        mAMap = mapView.getMap();
        initCarAMap(mAMap);
        mapView = null;
        return mAMap;
    }

    public AMap getAMap() {
        return mAMap;
    }

    public void setOnMyLocationChangeListener(AMap.OnMyLocationChangeListener onMyLocationChangeListener) {
        if (mAMap != null && onMyLocationChangeListener != null) {
            mAMap.setOnMyLocationChangeListener(onMyLocationChangeListener);
        }
    }

    public void setOnMarkerClickListener(AMap.OnMarkerClickListener onMarkerClickListener) {
        if (mAMap != null && onMarkerClickListener != null) {
            mAMap.setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    private void initNoLocationAMap(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(3000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色的方法。
        //修改地图的中心点位置
        float lat = Float.valueOf(SharedUtil.getString(mContext, "lat"));
        float lng = Float.valueOf(SharedUtil.getString(mContext, "lon"));
        CameraPosition cp = aMap.getCameraPosition();
        CameraPosition cpNew = CameraPosition.fromLatLngZoom(new LatLng(lat, lng), cp.zoom);
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cpNew);
        aMap.moveCamera(cu);

        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), mLocationIconRes == 0 ? R.mipmap.main_positionpoint : mLocationIconRes)));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);//关闭旋转手势
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
//                Location location;
//                if ((location = aMap.getMyLocation()) != null) {
//                    moveCameraToLatLng(new MapPoint(location.getLatitude(), location.getLongitude()));
//                }
            }
        });
    }

    private void initCarAMap(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(3000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色的方法。
        //修改地图的中心点位置
        float lat = Float.valueOf(SharedUtil.getString(mContext, "lat"));
        float lng = Float.valueOf(SharedUtil.getString(mContext, "lon"));
        CameraPosition cp = aMap.getCameraPosition();
        CameraPosition cpNew = CameraPosition.fromLatLngZoom(new LatLng(lat, lng), cp.zoom);
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cpNew);
        aMap.moveCamera(cu);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(mContext.getResources(), mLocationIconRes == 0 ? R.mipmap.main_positionpoint : mLocationIconRes)));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);//关闭旋转手势
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }


    private void initAMap(AMap aMap) {
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(3000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        // myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);

        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.strokeColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);//设置定位蓝点精度圆圈的填充颜色的方法。
        //修改地图的中心点位置
        float lat = Float.valueOf(SharedUtil.getString(mContext, "lat"));
        float lng = Float.valueOf(SharedUtil.getString(mContext, "lon"));
        CameraPosition cp = aMap.getCameraPosition();
        CameraPosition cpNew = CameraPosition.fromLatLngZoom(new LatLng(lat, lng), cp.zoom);
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cpNew);
        aMap.moveCamera(cu);

        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(mContext.getResources(),
                        mLocationIconRes == 0 ? R.mipmap.main_positionpoint : mLocationIconRes)));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);//关闭旋转手势
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
//                Location location;
//                if ((location = aMap.getMyLocation()) != null) {
//                    moveCameraToLatLng(new MapPoint(location.getLatitude(), location.getLongitude()));
//                }
            }
        });
    }

    public void startLocation() {
        if (checkNull()) {
            mAMap.setMyLocationEnabled(true);
        }
    }

    public void drawLine(ArrayList<LatLng> latLngs) {

        mAMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.argb(255, 255, 0, 0)));
    }

    public Marker drawEndMarker(Context context, LatLng latLng, int res) {
        MarkerOptions markerOption;
        markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("").snippet("");
        // View view = LayoutInflater.from(context).inflate(R.layout.marker_end, null);
        markerOption.draggable(false);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory
                .fromResource(res));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        return mAMap.addMarker(markerOption);
    }


    public Marker drawRedCarMarker(Context context, LatLng latLng, float rotate) {
        MarkerOptions markerOption;
        markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("").snippet("");
        View view = LayoutInflater.from(context).inflate(R.layout.layout_marker_car_used, null);
        view.findViewById(R.id.green_car).setVisibility(View.GONE);
        view.findViewById(R.id.red_car).setVisibility(View.VISIBLE);
        if (rotate != 0) {
            view.setRotation(rotate);

        }

        markerOption.draggable(false);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory
                .fromView(view));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        return mAMap.addMarker(markerOption);
    }

    public Marker drawUserOrderCarMarker(Context context, LatLng latLng, Float rotato) {
        MarkerOptions markerOption;
        markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("").snippet("");
        markerOption.draggable(false);//设置Marker可拖动
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.car));
        if (rotato != 0) {
            imageView.setRotation(rotato);
        }

        markerOption.icon(BitmapDescriptorFactory.fromView(imageView));
        //  markerOption.icon(BitmapDescriptorFactory
        //          .fromResource(R.mipmap.car));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果

        return mAMap.addMarker(markerOption);
    }

    public Marker drawMarker(LatLng point, View icon, String title, String snippet, boolean draggable) {
        MarkerOptions markerOption;
        markerOption = new MarkerOptions();
        markerOption.position(point);
        markerOption.title(title).snippet(snippet);
        markerOption.draggable(draggable);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory
                .fromView(icon));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        return mAMap.addMarker(markerOption);
    }

    /**
     * 添加一组markers 在地图上添一组图片标记（marker）对象，并设置是否改变地图状态以至于所有的marker对象都在当前地图可视区域范围内显示。
     *
     * @param markerOptionses
     * @param moveToCenter    设置是否改变地图状态以至于所有的marker对象都在当前地图可视区域范围内显示。
     * @return
     */
    public ArrayList<Marker> drawMarkers(ArrayList<MarkerOptions> markerOptionses, boolean moveToCenter) {
        return mAMap.addMarkers(markerOptionses, moveToCenter);
    }

    public void animShowMarker(Marker marker) {
        Animation markerAnimation = new ScaleAnimation(0, 1, 0, 1); //初始化生长效果动画
        markerAnimation.setDuration(500);  //设置动画时间 单位毫秒
        marker.setAnimation(markerAnimation);
        marker.startAnimation();
    }

    public MarkerOptions createMarkerOption(LatLng point, View icon, String title, String snippet, boolean draggable) {
        MarkerOptions markerOption;
        markerOption = new MarkerOptions();
        markerOption.position(point);
        markerOption.title(title).snippet(snippet);
        markerOption.draggable(draggable);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory
                .fromView(icon));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(false);//设置marker平贴地图效果
        return markerOption;
    }

    public View createNowNearMarkerIcon(Context context) {
        View view = null;
        view = LayoutInflater.from(context).inflate(R.layout.layout_marker_car_used, null);
        view.findViewById(R.id.green_car).setVisibility(View.VISIBLE);
        view.findViewById(R.id.red_car).setVisibility(View.GONE);
        return view;
    }

    public void drawCircle(GeoFence fence, int strokeColor, int strokeWidth, int fillColor) {
        LatLng center = new LatLng(fence.getCenter().getLatitude(),
                fence.getCenter().getLongitude());
        // 绘制一个圆形
        mAMap.addCircle(
                new CircleOptions()
                        .center(center)
                        .radius(fence.getRadius())
                        .strokeColor(strokeColor)
                        .fillColor(fillColor)
                        .strokeWidth(strokeWidth));


    }

    public void moveCameraToLatLng(LatLng mapPoint) {
        if (checkNull()) {
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(mapPoint, 12, 0, 0));
            mAMap.animateCamera(cu);
        }
    }

    public void moveCameraToMyLocation() {
        if (checkNull()) {
            if (mAMap == null) {
                return;
            }
            CameraUpdate cu = CameraUpdateFactory.newCameraPosition(new CameraPosition(getMyLocation(), getCurrentZoomLevel(), 0, 0));
            mAMap.animateCamera(cu);
        }
    }

    private boolean checkNull() {
        return mAMap != null;
    }


    public interface CarTimeResult {
        public void result(LatLonPoint startLatlon, LatLonPoint endLatlon, long time, float distance);
    }

    RouteSearch walkRouteSearch;
    RouteSearch.WalkRouteQuery walkRouteQuery;


    RouteSearch carTimerouteSearch;
    RouteSearch.DriveRouteQuery driveQuery;


    public interface CarRouteInter {
        public void ok();
    }

    public interface CarRelayIngListener {
        public void result(DrivePath drivePath);
    }

    //总距离
    public int dis;
    //总时间
    public int dur;

    public interface RouteSearchResultListener {
        public void onSuccess();

        public void onFailed();
    }

    public float getCurrentZoomLevel() {
        return mAMap != null ? mAMap.getCameraPosition().zoom : 12f;
    }

    public void zoomTo(float level) {
        if (checkNull()) {
            mAMap.animateCamera(CameraUpdateFactory.zoomTo(level));
        }
    }

    public LatLng getMyLocation() {
        LatLng mapPoint = new LatLng(0.0, 0.0);
        if (mAMap != null && mAMap.getMyLocation() != null) {
            mapPoint = new LatLng(mAMap.getMyLocation().getLatitude(), mAMap.getMyLocation().getLongitude());
        }
        return mapPoint;
    }
}
