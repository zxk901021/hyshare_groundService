package com.hyshare.groundservice.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
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
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.bumptech.glide.Glide;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.databinding.ActivityManageCarBinding;
import com.hyshare.groundservice.map.AMapModel;
import com.hyshare.groundservice.map.AmapUtil;
import com.hyshare.groundservice.map.DrivingRouteOverlay;
import com.hyshare.groundservice.map.GPSUtil;
import com.hyshare.groundservice.map.WalkRouteOverlay;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.model.MapPoint;
import com.hyshare.groundservice.model.ViewModel;
import com.hyshare.groundservice.util.DialogUtil;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.TimeUtil;
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
    private static final String warnTips = "车辆正在使用中，是否确认";
    private boolean ordered = false;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.operate_car:
                if (orderStatus == ORDER_STATE.UN_CLAIM) {
                    claimWork();
                } else if (orderStatus == ORDER_STATE.SELF_CLAIM) {
                    unClaimWork();
                } else if (orderStatus == ORDER_STATE.CANCEL_CLAIM) {
                    Intent cancelTask = new Intent(context, CancelOrStopTaskActivity.class);
                    cancelTask.putExtra("task", data.getWord_order_task());
                    cancelTask.putExtra("mode", MODE_CANCEL);
                    cancelTask.putExtra("work_id", data.getWord_order_id());
                    cancelTask.putExtra("id", data.getId());
                    cancelTask.putExtra("time", data.getWord_order_start_time());
                    cancelTask.putExtra("number", data.getNumber());
                    startActivity(cancelTask);
                }

                break;
            case R.id.operate_work_list:
                if (orderStatus == ORDER_STATE.SELF_CLAIM) {
                    Intent operateTask = new Intent(context, StartWorkListActivity.class);
                    operateTask.putExtra("work_id", data.getWord_order_id());
                    operateTask.putExtra("id", data.getId());
                    startActivity(operateTask);
                } else if (orderStatus == ORDER_STATE.CANCEL_CLAIM) {
                    Intent cancelTask = new Intent(context, CancelOrStopTaskActivity.class);
                    cancelTask.putExtra("task", data.getWord_order_task());
                    cancelTask.putExtra("mode", MODE_STOP);
                    cancelTask.putExtra("work_id", data.getWord_order_id());
                    cancelTask.putExtra("id", data.getId());
                    cancelTask.putExtra("time", data.getWord_order_start_time());
                    cancelTask.putExtra("number", data.getNumber());
                    startActivity(cancelTask);
                }

                break;
            case R.id.state_available:
                checkOrderState("1");
                break;
            case R.id.state_unavailable:
                checkOrderState("2");
                break;
            case R.id.unlock:
                checkOrderState("6");
                break;
            case R.id.lock:
                checkOrderState("7");
                break;
            case R.id.unlock_relay:
                checkOrderState("4");
                break;
            case R.id.lock_relay:
                checkOrderState("5");
                break;
            case R.id.raise_window:
                checkOrderState("8");
                break;
            case R.id.search_car:
                checkOrderState("3");
                break;
            case R.id.guide:
                AmapUtil.goLocalNavApp(context, end.getLatitude(), end.getLongitude());
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        id = intent.getStringExtra("id");
        getCarInfo(id);
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
        mLayoutBinding.stateAvailable.setOnClickListener(this);
        mLayoutBinding.stateUnavailable.setOnClickListener(this);
        mLayoutBinding.unlock.setOnClickListener(this);
        mLayoutBinding.lock.setOnClickListener(this);
        mLayoutBinding.unlockRelay.setOnClickListener(this);
        mLayoutBinding.lockRelay.setOnClickListener(this);
        mLayoutBinding.raiseWindow.setOnClickListener(this);
        mLayoutBinding.searchCar.setOnClickListener(this);
        mLayoutBinding.guide.setOnClickListener(this);
    }

    @Override
    public void initData() {
        if (!TextUtils.isEmpty(id)) {
            getCarInfo(id);
        }
    }

    private void setCarStateBg(boolean available) {
        Drawable availableSelect = getResources().getDrawable(R.mipmap.car_use);
        Drawable availableUnSelect = getResources().getDrawable(R.mipmap.car_use_unavailable);
        Drawable unAvailableSelect = getResources().getDrawable(R.mipmap.car_unused);
        Drawable unAvailableUnSelect = getResources().getDrawable(R.mipmap.car_unused_unavailable);
        availableSelect.setBounds(0, 0, availableSelect.getMinimumWidth(), availableSelect.getMinimumHeight());
        availableUnSelect.setBounds(0, 0, availableUnSelect.getMinimumWidth(), availableUnSelect.getMinimumHeight());
        unAvailableSelect.setBounds(0, 0, unAvailableSelect.getMinimumWidth(), unAvailableSelect.getMinimumHeight());
        unAvailableUnSelect.setBounds(0, 0, unAvailableUnSelect.getMinimumWidth(), unAvailableUnSelect.getMinimumHeight());
        if (available) {
            mLayoutBinding.stateAvailable.setCompoundDrawables(null, availableUnSelect, null, null);
            mLayoutBinding.stateUnavailable.setCompoundDrawables(null, unAvailableSelect, null, null);
            mLayoutBinding.stateAvailable.setClickable(false);
            mLayoutBinding.stateUnavailable.setClickable(true);
        } else {
            mLayoutBinding.stateAvailable.setCompoundDrawables(null, availableSelect, null, null);
            mLayoutBinding.stateUnavailable.setCompoundDrawables(null, unAvailableUnSelect, null, null);
            mLayoutBinding.stateAvailable.setClickable(true);
            mLayoutBinding.stateUnavailable.setClickable(false);
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
                            if (data != null) {
                                double[] location = GPSUtil.gps84_To_Gcj02(Double.valueOf(data.getLatitude()), Double.valueOf(data.getLongitude()));
                                data.setLatitude(String.valueOf(location[0]));
                                data.setLongitude(String.valueOf(location[1]));
                            }
                            bindUI(data);
                            if ("1".equals(data.getState())) {
                                setCarStateBg(true);
                            } else setCarStateBg(false);
                        } else if (carListBeanBaseModel.getCode() == 9999) {
                            logout();
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
        mLayoutBinding.operateCar.setTextColor(model.getTextColor());
        if (TextUtils.isEmpty(bean.getUser_name())) {
            mLayoutBinding.claimant.setVisibility(View.GONE);
        } else {
            mLayoutBinding.claimant.setVisibility(View.VISIBLE);
            mLayoutBinding.claimant.setText(bean.getUser_name());
        }

        if (!TextUtils.isEmpty(bean.getLast_return())) {
            mLayoutBinding.stayTime.setText("停放：" + TimeUtil.timeFormat(Long.valueOf(bean.getLast_return())));
        }

        setOperateWorkBg();
        end = new LatLonPoint(Double.valueOf(bean.getLatitude()), Double.valueOf(bean.getLongitude()));
        initMap();
        RegeocodeQuery query = new RegeocodeQuery(end, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);

    }

    private void setOperateWorkBg() {
        if (orderStatus == ORDER_STATE.SELF_CLAIM) {
            mLayoutBinding.operateWorkList.setVisibility(View.VISIBLE);
            mLayoutBinding.operateWorkList.setText("开始工单");
        } else if (orderStatus == ORDER_STATE.CANCEL_CLAIM) {
            mLayoutBinding.operateWorkList.setVisibility(View.VISIBLE);
            mLayoutBinding.operateWorkList.setText("结束工单");
        } else {
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

    private void claimWork() {
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
                        if (stringBaseModel.getCode() == 1) {
                            ToastUtil.toast("车辆认领成功");
                            orderStatus = ORDER_STATE.SELF_CLAIM;
                            getCarInfo(id);
                        } else if (stringBaseModel.getCode() == 9999) {
                            logout();
                        }
                    }
                });
    }

    /**
     * 取消认领
     */
    private void unClaimWork() {
        if (data != null) {
            String workId = data.getWord_order_id();
            if (!TextUtils.isEmpty(workId)) {
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
                                if (stringBaseModel.getCode() == 1) {
                                    ToastUtil.toast("已取消认领");
                                    orderStatus = ORDER_STATE.UN_CLAIM;
                                    getCarInfo(id);
                                } else if (stringBaseModel.getCode() == 9999) {
                                    logout();
                                } else {
                                    ToastUtil.toast(stringBaseModel.getMessage());
                                }

                            }
                        });
            }
        }
    }

    /**
     * 更改汽车状态
     */
    private void changeCarState(final String state) {
        if (orderStatus != ORDER_STATE.CANCEL_CLAIM) {
            DialogUtil.showDialog(context, "提示", "请开始工单后再进行操作", null, null);
            return;
        }
        String message = "";
        if ("1".equals(state)) {
            if (ordered) message = warnTips + "调为可用";
            else message = "调为可用";
            confirmStateDialog(message, state);
        } else if ("2".equals(state)) {
            if (ordered) message = warnTips + "调为不可用";
            else message = "调为不可用";
            confirmStateDialog(message, state);
        }
    }

    /**
     * 查询车辆是否有订单
     */
    private void checkOrderState(final String tag) {
        getApiService().checkOrderState(id)
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
                        if (stringBaseModel.getCode() == 1) {
                            ordered = "1".equals(stringBaseModel.getData());
                            if (tag.equals("1") | tag.equals("2")) {
                                changeCarState(tag);
                            } else sendCommand(tag);
                        }
                    }
                });
    }

    private void confirmStateDialog(String title, final String state) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String, Object> param = new HashMap<>();
                        param.put("state", state);
                        JSONObject object = new JSONObject(param);
                        getApiService().changeCarState(id, parseRequest(object.toString()))
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
                                        if (stringBaseModel.getCode() == 1) {
                                            setCarStateBg(state.equals("1"));
                                            ToastUtil.toast(stringBaseModel.getMessage());
                                            getCarInfo(id);
                                        } else if (stringBaseModel.getCode() == 9999) {
                                            logout();
                                        } else ToastUtil.toast(stringBaseModel.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getCarInfo(id);
                    }
                })
                .show();
    }

    private void confirmCommandDialog(final String title, final String command) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String, Object> param = new HashMap<>();
                        param.put("device_id", data.getDevice_no());
                        param.put("command", command);
                        param.put("car_id", id);
                        JSONObject object = new JSONObject(param);
                        DialogUtil.showProgressDialog(context, title + "中...", mLayoutBinding.parentView);
                        DialogUtil.startShowProgress();
                        getApiService().sendCommand(parseRequest(object.toString()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<BaseModel<String>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        DialogUtil.showMaxProgress();
                                        ToastUtil.toast(e.getMessage());
                                    }

                                    @Override
                                    public void onNext(BaseModel<String> stringBaseModel) {
                                        DialogUtil.showMaxProgress();
                                        if (stringBaseModel.getCode() == 1) {
                                            getCarInfo(id);
                                            ToastUtil.toast(stringBaseModel.getMessage());
                                        } else if (stringBaseModel.getCode() == 9999) {
                                            logout();
                                        } else ToastUtil.toast(stringBaseModel.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getCarInfo(id);
                    }
                })
                .show();
    }

    /**
     * 发送指令
     */
    private void sendCommand(String command) {
        if (orderStatus != ORDER_STATE.CANCEL_CLAIM) {
            DialogUtil.showDialog(context, "提示", "请开始工单后再进行操作", null, null);
            return;
        }
        switch (command) {
            case "3":
                if (ordered) confirmCommandDialog(warnTips + "寻车", command);
                else confirmCommandDialog("寻车", command);
                break;
            case "4":
                if (ordered) confirmCommandDialog(warnTips + "开继电器", command);
                else confirmCommandDialog("开继电器", command);
                break;
            case "5":
                if (ordered) confirmCommandDialog(warnTips + "关继电器", command);
                else confirmCommandDialog("关继电器", command);
                break;
            case "6":
                if (ordered) confirmCommandDialog(warnTips + "开锁", command);
                else confirmCommandDialog("开锁", command);
                break;
            case "7":
                if (ordered) confirmCommandDialog(warnTips + "落锁", command);
                else confirmCommandDialog("落锁", command);
                break;
            case "8":
                if (ordered) confirmCommandDialog(warnTips + "升窗", command);
                else confirmCommandDialog("升窗", command);
                break;
        }


    }

    private ViewModel setBg(String status, String orderState) {
        ViewModel model = new ViewModel();
        if ("0".equals(orderState)) {
            switch (status) {
                case "1":
                    orderStatus = ORDER_STATE.UN_CLAIM;
                    model.setText("认领");
                    model.setRes(R.drawable.button_corner);
                    model.setTextColor(Color.WHITE);
                    return model;
                case "2":
                    orderStatus = ORDER_STATE.ALREADY_CLAIM;
                    model.setText("已被认领");
                    model.setRes(R.drawable.already_button_bg);
                    model.setTextColor(Color.parseColor("#999999"));
                    return model;
            }
        } else {
            switch (orderState) {
                case "1":
                    orderStatus = ORDER_STATE.SELF_CLAIM;
                    model.setText("取消认领");
                    model.setRes(R.drawable.cancel_button_bg);
                    model.setTextColor(Color.WHITE);
                    return model;
                case "2":
                    orderStatus = ORDER_STATE.CANCEL_CLAIM;
                    model.setText("取消工单");
                    model.setRes(R.drawable.cancel_button_bg);
                    model.setTextColor(Color.WHITE);
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
        if ("2".equals(data.getUse_state()))
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_greencar)).position(new LatLng(end.getLatitude(), end.getLongitude()));
        else if ("1".equals(data.getUse_state()))
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_redcar)).position(new LatLng(end.getLatitude(), end.getLongitude()));
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
        map.clear();
        WalkPath walkPath = walkRouteResult.getPaths().get(0);
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(context, map, walkPath, start, end);
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan(-1, -1);
        MarkerOptions options = new MarkerOptions();
        if ("2".equals(data.getUse_state()))
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_greencar)).position(new LatLng(end.getLatitude(), end.getLongitude()));
        else if ("1".equals(data.getUse_state()))
            options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.main_redcar)).position(new LatLng(end.getLatitude(), end.getLongitude()));
        map.addMarker(options);
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
                if (Float.valueOf(distance) > 1f) {
                    RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
                    RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo, 0, null, null, null);
                    routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
                } else {
                    RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
                    RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
                    routeSearch.calculateWalkRouteAsyn(walkRouteQuery);
                }

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
