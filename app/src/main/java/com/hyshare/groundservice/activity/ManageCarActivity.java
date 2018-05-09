package com.hyshare.groundservice.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.bumptech.glide.Glide;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.databinding.ActivityManageCarBinding;
import com.hyshare.groundservice.map.AMapModel;
import com.hyshare.groundservice.map.AmapUtil;
import com.hyshare.groundservice.map.DrivingRouteOverlay;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.model.MapPoint;
import com.hyshare.groundservice.model.ViewModel;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.ToastUtil;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class ManageCarActivity extends BaseActivity<ActivityManageCarBinding> implements
        GeocodeSearch.OnGeocodeSearchListener, RouteSearch.OnRouteSearchListener,
        AMapLocationListener, View.OnClickListener {

    private MapView mapView;
    private String id;
    AMap map;
    private GeocodeSearch geocodeSearch;
    private RouteSearch routeSearch;

    public AMapLocationClient aMapLocationClient;
    public AMapLocationClientOption locationClientOption;

    private double lat, lon;
    AMapModel mapModel;

    LatLonPoint start, end;
    public ORDER_STATE orderStatus;
    private CarList.CarListBean data;
    public static final int MODE_CANCEL = 1;
    public static final int MODE_STOP = 2;

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.operate_car:
                if (orderStatus == ORDER_STATE.UN_CLAIM){
                    claimWork();
                }else if (orderStatus == ORDER_STATE.SELF_CLAIM){
                    unClaimWork();
                }else if (orderStatus == ORDER_STATE.CANCEL_CLAIM){
                    Intent cancelTask = new Intent(context, CancelOrStopTaskActivity.class);
                    cancelTask.putExtra("mode", MODE_CANCEL);
                    startActivity(cancelTask);
                }

                break;
            case R.id.operate_work_list:
                if (orderStatus == ORDER_STATE.SELF_CLAIM){
                    Intent operateTask = new Intent(context, StartWorkListActivity.class);
                    operateTask.putExtra("work_id", data.getWord_order_id());
                    startActivity(operateTask);
                }else if (orderStatus == ORDER_STATE.CANCEL_CLAIM){
                    Intent cancelTask = new Intent(context, CancelOrStopTaskActivity.class);
                    cancelTask.putExtra("mode", MODE_STOP);
                    startActivity(cancelTask);
                }

                break;
        }
    }

    /**
     * UN_CLAIM 未认领
     * ALREADY_CLAIM 已被认领
     * SELF_CLAIM 自己认领
     * CANCEL_CLAIM 已开始工单
     */
    public enum ORDER_STATE {
        INIT, UN_CLAIM, ALREADY_CLAIM, SELF_CLAIM, CANCEL_CLAIM
    }


    @Override
    public int setLayoutId() {
        return R.layout.activity_manage_car;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderStatus = ORDER_STATE.INIT;
        data = new CarList.CarListBean();
        mapView = mLayoutBinding.mapview;
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        mapModel = new AMapModel(context);
        mapModel.bindMapView(mapView);

        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);

    }

    private void initMap() {
        aMapLocationClient = new AMapLocationClient(context);
        locationClientOption = new AMapLocationClientOption();
        aMapLocationClient.setLocationListener(this);
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setOnceLocation(true);
        aMapLocationClient.setLocationOption(locationClientOption);
        aMapLocationClient.startLocation();
    }

    @Override
    public void initUI() {
        final Intent intent = getIntent();
        id = intent.getStringExtra("id");
        mLayoutBinding.carInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CarInfoActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        mLayoutBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mLayoutBinding.operateWorkList.setOnClickListener(this);
        mLayoutBinding.operateCar.setOnClickListener(this);
    }

    @Override
    public void initData() {
        if (!TextUtils.isEmpty(id)) {
            getCarInfo(id);
        }
    }

    private void getCarInfo(String id) {
        getApiService().getCarInfo(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<CarList.CarListBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<CarList.CarListBean> carListBeanBaseModel) {
                        if (carListBeanBaseModel.getCode() == 1) {
                            data = carListBeanBaseModel.getData();
                            bindUI(carListBeanBaseModel.getData());
                        }
                    }
                });
    }

    private void bindUI(CarList.CarListBean bean) {
        Glide.with(context).load(bean.getImage_path()).into(mLayoutBinding.carPhoto);
        mLayoutBinding.carNumber.setText(bean.getNumber());
        mLayoutBinding.carType.setText(bean.getBrand_name() + bean.getSeat_num() + "座");
        mLayoutBinding.gas.init(bean.getRemaining_gas());
        mLayoutBinding.gas.setText(bean.getRemaining_gas());
        mLayoutBinding.carStatus.setText(setUseState(bean.getUse_state()));
        ViewModel model = setBg(bean.getClaim_state(), bean.getWord_order_state());
        mLayoutBinding.operateCar.setBackgroundResource(model.getRes());
        mLayoutBinding.operateCar.setText(model.getText());
        setOperateWorkBg();
        end = new LatLonPoint(Double.valueOf(bean.getLatitude()), Double.valueOf(bean.getLongitude()));
        initMap();
        RegeocodeQuery query = new RegeocodeQuery(end, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);

    }

    private void setOperateWorkBg(){
        if (orderStatus == ORDER_STATE.SELF_CLAIM){
            mLayoutBinding.operateWorkList.setVisibility(View.VISIBLE);
            mLayoutBinding.operateWorkList.setText("开始工单");
        }else if (orderStatus == ORDER_STATE.CANCEL_CLAIM){
            mLayoutBinding.operateWorkList.setVisibility(View.VISIBLE);
            mLayoutBinding.operateWorkList.setText("结束工单");
        }else {
            mLayoutBinding.operateWorkList.setVisibility(View.GONE);
        }
    }

    private String setUseState(String status) {
        String state = "";
        switch (status) {
            case "1":
                state = "使用中";
                return state;

            case "2":
                state = "空闲中";
                return state;
        }
        return state;
    }

    private void claimWork(){
        Map<String, Object> param = new HashMap<>();
        param.put("car_id", data.getId());
        JSONObject object = new JSONObject(param);
        getApiService().claimWorkList(parseRequest(object.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseModel<String> stringBaseModel) {
                        if (stringBaseModel.getCode() == 1){
                            ToastUtil.toast("车辆认领成功");
                            orderStatus = ORDER_STATE.SELF_CLAIM;
                            getCarInfo(id);
                        }
                    }
                });
    }

    private void unClaimWork(){
        if (data != null){
            String workId = data.getWord_order_id();
            if (!TextUtils.isEmpty(workId)){
                getApiService().cancelClaim(workId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<BaseModel<String>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                ToastUtil.toast(e.getMessage());
                            }

                            @Override
                            public void onNext(BaseModel<String> stringBaseModel) {
                                if (stringBaseModel.getCode() == 1){
                                    ToastUtil.toast("已取消认领");
                                    orderStatus = ORDER_STATE.UN_CLAIM;
                                    getCarInfo(id);
                                }else {
                                    ToastUtil.toast(stringBaseModel.getMessage());
                                }

                            }
                        });
            }
        }
    }

    private ViewModel setBg(String status, String orderState) {
        ViewModel model = new ViewModel();
        if ("0".equals(orderState)){
            switch (status) {
                case "1":
                    orderStatus = ORDER_STATE.UN_CLAIM;
                    model.setText("认领");
                    model.setRes(R.drawable.button_corner);
                    return model;
                case "2":
                    orderStatus = ORDER_STATE.ALREADY_CLAIM;
                    model.setText("已被认领");
                    model.setRes(R.drawable.already_button_bg);
                    return model;
            }
        }else {
            switch (orderState){
                case "1":
                    orderStatus = ORDER_STATE.SELF_CLAIM;
                    model.setText("取消认领");
                    model.setRes(R.drawable.cancel_button_bg);
                    return model;
                case "2":
                    orderStatus = ORDER_STATE.CANCEL_CLAIM;
                    model.setText("取消工单");
                    model.setRes(R.drawable.cancel_button_bg);
                    return model;
            }
        }

        return model;
    }

    /**
     * 根据给定的经纬度和最大结果数返回逆地理编码的结果列表。
     * 逆地理编码兴趣点返回结果最大返回数目为10，
     * 道路和交叉路口返回最大数目为3。
     *
     * @param regeocodeResult
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        mLayoutBinding.carAddress.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress());
    }

    /**
     * 根据给定的地理名称和查询城市，返回地理编码的结果列表。
     * 地理编码返回结果集默认最大返回数目为10。
     *
     * @param geocodeResult
     * @param i
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 公交换乘路径规划结果的回调方法。
     *
     * @param busRouteResult
     * @param i
     */
    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    /**
     * 驾车路径规划结果的回调方法。
     *
     * @param driveRouteResult
     * @param i
     */
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        map.clear();
        DrivePath drivePath = driveRouteResult.getPaths().get(0);
        DrivingRouteOverlay routeOverlay = new DrivingRouteOverlay(context, map, drivePath, start, end, null);
        routeOverlay.removeFromMap();
        routeOverlay.addToMap();
        routeOverlay.zoomToSpan(-1, -1);
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_greencar)).position(new LatLng(end.getLatitude(), end.getLongitude()));
        map.addMarker(options);
    }

    /**
     * 步行路径规划结果的回调方法。
     *
     * @param walkRouteResult
     * @param i
     */
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    /**
     * 骑行路径规划结果的回调方法
     *
     * @param rideRouteResult
     * @param i
     */
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 定位回调监听，当定位完成后调用此方法
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                lat = aMapLocation.getLatitude();
                lon = aMapLocation.getLongitude();
                SharedUtil.putString(context, KeyConstant.LAT, String.valueOf(lat));
                SharedUtil.putString(context, KeyConstant.LON, String.valueOf(lon));
                routeSearch = new RouteSearch(context);
                routeSearch.setRouteSearchListener(this);
                start = new LatLonPoint(lat, lon);
                String distance = AmapUtil.format(AmapUtil.calculateLineDistance(new MapPoint(lat, lon), new MapPoint(end.getLatitude(), end.getLongitude())) / 1000);
                mLayoutBinding.distance.setText("距离：" + distance + "公里");
                RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo, 0, null, null, null);
                routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
            } else {
                lat = Double.valueOf(SharedUtil.getString(context, KeyConstant.LAT, "0"));
                lon = Double.valueOf(SharedUtil.getString(context, KeyConstant.LON, "0"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        map = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }
}