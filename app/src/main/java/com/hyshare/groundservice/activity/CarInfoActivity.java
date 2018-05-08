package com.hyshare.groundservice.activity;

import android.view.View;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityCarInfoBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.util.ToastUtil;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CarInfoActivity extends BaseActivity<ActivityCarInfoBinding> {

    @Override
    public int setLayoutId() {
        return R.layout.activity_car_info;
    }

    @Override
    public void initUI() {
        mLayoutBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        String id = getIntent().getStringExtra("id");
        getCarInfo(id);
    }

    private void getCarInfo(String id){
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
                        if (carListBeanBaseModel.getCode() == 1){
                            bindUI(carListBeanBaseModel.getData());
                        }
                    }
                });
    }

    private void  bindUI(CarList.CarListBean data){
        mLayoutBinding.carNumber.setText("车牌号：" + data.getNumber());
        mLayoutBinding.carGas.init(data.getRemaining_gas());
        mLayoutBinding.carGas.setText(data.getRemaining_gas());
        mLayoutBinding.lastGas.setText(data.getLast_refuel());
        mLayoutBinding.lastMaintain.setText(data.getLast_maintain());
        mLayoutBinding.lastMoveCar.setText(data.getLast_move());
        mLayoutBinding.lastRepair.setText(data.getLast_repair());
        mLayoutBinding.lastWashCar.setText(data.getLast_clean_time());
    }
}
