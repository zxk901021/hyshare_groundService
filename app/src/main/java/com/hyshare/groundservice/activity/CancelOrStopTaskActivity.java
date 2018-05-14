package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityCancelOrStopTaskBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CancelOrStopTaskActivity extends BaseActivity<ActivityCancelOrStopTaskBinding> implements View.OnClickListener {

    private int mode;
    private ArrayList<String> labes;
    private String workId;
    private String id;
    private String time;
    private String carNumber;

    @Override
    public int setLayoutId() {
        return R.layout.activity_cancel_or_stop_task;
    }

    @Override
    public void initUI() {
        String tasks = getIntent().getStringExtra("task");
        if (!TextUtils.isEmpty(tasks)){
            if (tasks.contains(",")){
                String[] taskArray = tasks.split(",");
                labes = new ArrayList<>();
                Collections.addAll(labes, taskArray);
                mLayoutBinding.workTask.setLabels(labes);
            }else {
                labes = new ArrayList<>();
                labes.add(tasks);
                mLayoutBinding.workTask.setLabels(labes);
            }
        }

        mLayoutBinding.back.setOnClickListener(this);
        mLayoutBinding.submit.setOnClickListener(this);
    }

    @Override
    public void initData() {
        mode = getIntent().getIntExtra("mode", 0);
        workId = getIntent().getStringExtra("work_id");
        id = getIntent().getStringExtra("id");
        time = getIntent().getStringExtra("time");
        carNumber = getIntent().getStringExtra("number");
        mLayoutBinding.startTime.setText(time);
        mLayoutBinding.carNumber.setText("车牌号：" + carNumber);
        initMode();
    }

    private void initMode(){
        switch (mode){
            case 1:
                mLayoutBinding.title.setText("取消工单");
                mLayoutBinding.submit.setText("立即取消");
                mLayoutBinding.remark.setHint("可备注取消原因");
                break;
            case 2:
                mLayoutBinding.title.setText("结束工单");
                mLayoutBinding.submit.setText("立即结束");
                mLayoutBinding.remark.setHint("如果多人协助完成工单等情况可备注");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.submit:
                cancelOrStopWorkOrder();
                break;
        }
    }
    private void cancelOrStopWorkOrder(){
        String state = "";
        String remark = mLayoutBinding.remark.getText().toString().trim();
        if (mode == 1) state = "4";
        else if (mode == 2) state = "8";
        Map<String, Object> param = new HashMap<>();
        param.put("state", state);
        param.put("remark", remark);
        JSONObject object = new JSONObject(param);
        getApiService().cancelWorkList(workId, parseRequest(object.toString()))
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
                            if (mode == 1){
                                ToastUtil.toast("工单已取消");
                            }else if (mode == 2){
                                ToastUtil.toast("工单已结束");
                            }
                            Intent intent = new Intent(context, ManageCarActivity.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                        }else if (stringBaseModel.getCode() == 9999){
                            logout();
                        }else ToastUtil.toast(stringBaseModel.getMessage());
                    }
                });
    }
}
