package com.hyshare.groundservice.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.gyf.barlibrary.ImmersionBar;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.databinding.ActivityMainBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarCountBean;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> implements AMapLocationListener, View.OnClickListener{

    public AMapLocationClient aMapLocationClient;
    public AMapLocationClientOption locationClientOption;

    private double lat, lon;

    String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};

    List<String> permissionDenyList;

    @Override
    public int setLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initUI() {
        String account = SharedUtil.getString(context, KeyConstant.ACCOUNT);
        mLayoutBinding.userAccount.setText(account);
        getPermission();
        mLayoutBinding.manageCar.setOnClickListener(this);
        mLayoutBinding.myWorkList.setOnClickListener(this);
        ImmersionBar.with(this).statusBarView(R.id.top_view).statusBarDarkFont(false).init();
        mLayoutBinding.setting.setOnClickListener(this);
    }

    private void getPermission(){
        permissionDenyList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (int i = 0, len = permissions.length; i < len; i ++){
                if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED){
                    permissionDenyList.add(permissions[i]);
                }else {
                    initMap();
                }
            }
            if (!permissionDenyList.isEmpty()){
                String[] permissions = permissionDenyList.toArray(new String[permissionDenyList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 100);
            }
        }else {
            initMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 100){
            int temp = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                    if (showRequestPermission) {
                        ToastUtil.toast("权限未申请");
                    }
                }else temp ++;
            }
            if (temp == permissionDenyList.size()){
                initMap();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    public void initData() {

    }

    private void getCarCount(){
        HashMap<String, Object> param = new HashMap<>();
        param.put("lon" , String.valueOf(lon));
        param.put("lat" , String.valueOf(lat));
        JSONObject object = new JSONObject(param);
        getApiService().getCarCount(parseRequest(object.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<CarCountBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<CarCountBean> carCountBeanBaseModel) {
                        if (carCountBeanBaseModel.getCode() == 1){
                            mLayoutBinding.totalCount.setText(carCountBeanBaseModel.getData().getSum_count() + "");
                            mLayoutBinding.nearCount.setText(carCountBeanBaseModel.getData().getNearby_count() + "");
                            mLayoutBinding.freeCount.setText(carCountBeanBaseModel.getData().getFree_count() + "");
                            mLayoutBinding.unusedCount.setText(carCountBeanBaseModel.getData().getUnavailable_count() + "");
                        }else if (carCountBeanBaseModel.getCode() == 9999){
                            logout();
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null){
            if (aMapLocation.getErrorCode() == 0){
                lat = aMapLocation.getLatitude();
                lon = aMapLocation.getLongitude();
                SharedUtil.putString(context, KeyConstant.LAT, String.valueOf(lat));
                SharedUtil.putString(context, KeyConstant.LON, String.valueOf(lon));
                getCarCount();
            }else {
                lat = Double.valueOf(SharedUtil.getString(context, KeyConstant.LAT, "0"));
                lon = Double.valueOf(SharedUtil.getString(context, KeyConstant.LON, "0"));
                getCarCount();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.manage_car:
                Intent intent = new Intent(context, CarListActivity.class);
                startActivity(intent);
                break;

            case R.id.my_work_list:
                Intent intent1 = new Intent(context, WorkListActivity.class);
                startActivity(intent1);
                break;
            case R.id.setting:
                Intent intent2 = new Intent(context, SettingActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
