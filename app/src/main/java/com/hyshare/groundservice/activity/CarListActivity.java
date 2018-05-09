package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityCarListBinding;
import com.hyshare.groundservice.map.AmapUtil;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.model.MapPoint;
import com.hyshare.groundservice.model.ViewModel;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.ToastUtil;
import com.hyshare.groundservice.view.GasPercentView;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class CarListActivity extends BaseActivity<ActivityCarListBinding> {

    BaseQuickAdapter adapter;
    private int responseCode = 0;
    MapPoint start;

    @Override
    public int setLayoutId() {
        return R.layout.activity_car_list;
    }

    @Override
    public void initUI() {
        String lat = SharedUtil.getString(context, "lat");
        String lon = SharedUtil.getString(context, "lon");
        start = new MapPoint(Double.valueOf(lat), Double.valueOf(lon));
        adapter = new BaseQuickAdapter<CarList.CarListBean, BaseViewHolder>(R.layout.item_car_list_layout) {
            @Override
            protected void convert(BaseViewHolder helper, CarList.CarListBean item) {
                helper.setText(R.id.car_number, item.getNumber());
                helper.setText(R.id.car_type, item.getBrand_name() + item.getSeat_num() + "座");
                ((GasPercentView)helper.itemView.findViewById(R.id.gas)).init(item.getRemaining_gas());
                helper.setText(R.id.car_status, setUseState(item.getUse_state()));
                MapPoint end = new MapPoint(Double.valueOf(item.getLatitude()), Double.valueOf(item.getLongitude()));
                helper.setText(R.id.distance, "距离：" + AmapUtil.format(AmapUtil.calculateLineDistance(start, end)/1000) + "公里");
                ViewModel model = setOperation(item.getClaim_state());
                helper.setText(R.id.operate_car, model.getText());
                helper.setBackgroundRes(R.id.operate_car, model.getRes());
                Glide.with(context).load(item.getImage_path()).into((ImageView) helper.getView(R.id.car_photo));
            }
        };
        mLayoutBinding.carList.setLayoutManager(new LinearLayoutManager(context));
        mLayoutBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mLayoutBinding.carList.setAdapter(adapter);
        mLayoutBinding.carList.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(context, ManageCarActivity.class);
                intent.putExtra("id", ((CarList.CarListBean)adapter.getItem(position)).getId());
                startActivity(intent);
            }
        });
        mLayoutBinding.searchInfo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String number = mLayoutBinding.searchInfo.getText().toString().trim();
                    getCarList(number);
                    return true;
                }
                return false;
            }
        });
    }

    private String setUseState(String status){
        String state = "";
        switch (status){
            case "1":
                state = "使用中";
                return state;
            case "2":
                state = "空闲中";
                return state;
        }
        return state;
    }

    private ViewModel setOperation(String status){
        ViewModel model = new ViewModel();
        switch (status){
            case "1":
                model.setText("认领");
                model.setRes(R.drawable.button_corner);
                return model;
            case "2":
                model.setText("已被认领");
                model.setRes(R.drawable.already_button_bg);
                return model;
            case "3":
                model.setText("已被认领");
                model.setRes(R.drawable.already_button_bg);
//                model.setText("取消认领");
//                model.setRes(R.drawable.cancel_button_bg);
                return model;
        }
        return model;
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        String number = "";
        getCarList(number);
    }

    private void getCarList(String number){
        showDialog();
        getApiService().getCarList(number)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<CarList>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<CarList> carListBaseModel) {
                        dismiss();
                        if (carListBaseModel.getCode() == 1){
                            if (carListBaseModel.getData() != null){
                                adapter.setNewData(carListBaseModel.getData().getRows());
                            }else {
                                ToastUtil.toast(carListBaseModel.getMessage());
                            }
                        }
                    }
                });
    }

    private int claimWorkList(String carId){
        responseCode = 0;
        Map<String, Object> param = new HashMap<>();
        param.put("car_id", carId);
        JSONObject object = new JSONObject(param);
        showDialog();
        getApiService().claimWorkList(parseRequest(object.toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<String> stringBaseModel) {
                        dismiss();
                        int code = stringBaseModel.getCode();
                        if (code == 1){
                            responseCode = 1;
                            ToastUtil.toast("车辆认领成功");

                        }else if (code == 9999){
                            logout();
                        }else ToastUtil.toast(stringBaseModel.getMessage());
                    }
                });
        return responseCode;
    }

    private int cancelWorkList(String id){
        responseCode = 0;
        showDialog();
        getApiService().cancelClaim(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                    }

                    @Override
                    public void onNext(BaseModel<String> stringBaseModel) {
                        dismiss();
                        if (stringBaseModel.getCode() == 1){
                            responseCode = 1;
                            ToastUtil.toast("已取消认领");
                        }else if (stringBaseModel.getCode() == 9999){
                            logout();
                        }else ToastUtil.toast(stringBaseModel.getMessage());
                    }
                });
        return responseCode;
    }
}
