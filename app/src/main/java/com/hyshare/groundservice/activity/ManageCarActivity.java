package com.hyshare.groundservice.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
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
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.ToastUtil;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class ManageCarActivity extends BaseActivity<ActivityManageCarBinding> implements
        GeocodeSearch.OnGeocodeSearchListener, RouteSearch.OnRouteSearchListener,
        AMapLocationListener{

    private MapView mapView;
    private String id;
    AMap map;
    private GeocodeSearch geocodeSearch;
    private RouteSearch routeSearch;

    public AMapLocationClient aMapLocationClient;
    public AMapLocationClientOption locationClientOption;

    private double lat, lon;
    AMapModel mapModel;


    @Override
    public int setLayoutId() {
        return R.layout.activity_manage_car;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView = mLayoutBinding.mapview;
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        mapModel = new AMapModel(context);
        mapModel.bindMapView(mapView);

        initMap();
        float lat = Float.valueOf(SharedUtil.getString(context, "lat"));
        float lng = Float.valueOf(SharedUtil.getString(context, "lon"));
//        mapModel.moveCameraToLatLng(new LatLng(lat, lng));
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);

    }

    private void initMap(){
        aMapLocationClient = new AMapLocationClient(context);
        locationClientOption = new AMapLocationClientOption();
        aMapLocationClient.setLocationListener(this);
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setOnceLocation(true);
        aMapLocationClient.setLocationOption(locationClientOption);
        aMapLocationClient.startLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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
        mLayoutBinding.operateWorkList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(context, StartWorkListActivity.class);
                startActivity(intent1);
            }
        });
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
//        mLayoutBinding.gas.init(context, bean.getRemaining_gas());
        mLayoutBinding.gas.setText(bean.getRemaining_gas());
        mLayoutBinding.carStatus.setText(setUseState(bean.getUse_state()));
        LatLonPoint latLng = new LatLonPoint(Double.valueOf(bean.getLatitude()), Double.valueOf(bean.getLongitude()));
        RegeocodeQuery query = new RegeocodeQuery(latLng, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        routeSearch = new RouteSearch(context);
        routeSearch.setRouteSearchListener(this);
        LatLonPoint start = new LatLonPoint(lat, lon);
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, latLng);
//        RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT );
        RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo, 0, null, null, null);
        routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
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

    private int setBg(String status) {
        int res = 0;
        switch (status) {
            case "1":
                res = R.drawable.button_corner;
                return res;
            case "2":
                res = R.drawable.already_button_bg;
                return res;
            case "3":
                res = R.drawable.cancel_button_bg;
                return res;
        }
        return res;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        mLayoutBinding.carAddress.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress());
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        driveRouteResult.getPaths();
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null){
            if (aMapLocation.getErrorCode() == 0){
                lat = aMapLocation.getLatitude();
                lon = aMapLocation.getLongitude();
                SharedUtil.putString(context, KeyConstant.LAT, String.valueOf(lat));
                SharedUtil.putString(context, KeyConstant.LAT, String.valueOf(lon));
            }else {
                lat = Double.valueOf(SharedUtil.getString(context, KeyConstant.LAT, "0"));
                lon = Double.valueOf(SharedUtil.getString(context, KeyConstant.LON, "0"));
            }
        }
    }
}
