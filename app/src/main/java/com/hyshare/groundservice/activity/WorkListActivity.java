package com.hyshare.groundservice.activity;


import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.donkingliang.labels.LabelsView;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityWorkListBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.ViewModel;
import com.hyshare.groundservice.model.WorkList;
import com.hyshare.groundservice.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class WorkListActivity extends BaseActivity<ActivityWorkListBinding> {

    BaseQuickAdapter adapter;

    @Override
    public int setLayoutId() {
        return R.layout.activity_work_list;
    }

    @Override
    public void initUI() {
        adapter = new BaseQuickAdapter<WorkList.WorkListBean, BaseViewHolder>(R.layout.item_work_list_layout) {
            @Override
            protected void convert(BaseViewHolder helper, WorkList.WorkListBean item) {
                helper.setText(R.id.car_number, item.getCar_number());
                helper.setText(R.id.start_time, item.getCreate_time());
                ViewModel model = setWorkStatus(item.getState());
                helper.setText(R.id.work_list_status, model.getText());
                helper.setTextColor(R.id.work_list_status, model.getRes());
                helper.setText(R.id.end_time, item.getEnd_time());
                String task = item.getTask();
                if (!TextUtils.isEmpty(task)) {
                    if (task.contains(",")) {
                        List<String> tasks = Arrays.asList(task.split(","));
                        ((LabelsView) helper.getView(R.id.work_task)).setLabels(tasks);
                    }else {
                        List<String> tasks = new ArrayList<>();
                        tasks.add(task);
                        ((LabelsView) helper.getView(R.id.work_task)).setLabels(tasks);
                    }
                }
            }
        };
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(context, ManageCarActivity.class);
                intent.putExtra("id", ((WorkList.WorkListBean)adapter.getItem(position)).getCar_id());
                startActivity(intent);
            }
        });
        mLayoutBinding.workList.setLayoutManager(new LinearLayoutManager(context));
        mLayoutBinding.workList.setAdapter(adapter);
        mLayoutBinding.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseTime();
            }
        });
        mLayoutBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private ViewModel setWorkStatus(String status){
        ViewModel model = new ViewModel();
        switch (status){
            case "1":
                model.setText("未开始");
                model.setRes(Color.parseColor("#F85F4A"));
                return model;
            case "2":
                model.setText("进行中");
                model.setRes(Color.parseColor("#F85F4A"));
                return model;
            case "4":
                model.setText("已取消");
                model.setRes(Color.parseColor("#CCCCCC"));
                return model;
            case "8":
                model.setText("已完成");
                model.setRes(Color.parseColor("#33E1EF"));
                return model;
        }
        return model;
    }

    private void chooseTime(){
        TimePickerView pickerView = new TimePickerBuilder(context, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String time = sdf.format(date);
                mLayoutBinding.time.setText(time);
                getWorkList(time);
            }
        }).build();
        pickerView.show();
    }

    @Override
    public void initData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String time = sdf.format(date);
        mLayoutBinding.time.setText(time);
        getWorkList(time);
    }

    private void getWorkList(String time){
        getApiService().getWorkList(time)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseModel<WorkList>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtil.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseModel<WorkList> workListBaseModel) {
                        if (workListBaseModel.getCode() == 1) {
                            if (workListBaseModel.getData() != null){
                                adapter.setNewData(workListBaseModel.getData().getRows());
                                mLayoutBinding.totalCount.setText("共计" + workListBaseModel.getData().getRows().size() + "条");
                            }else {
                                adapter.isUseEmpty(true);
                                ToastUtil.toast(workListBaseModel.getMessage());
                                mLayoutBinding.totalCount.setText("共计0条");
                            }
                        }
                    }
                });
    }
}
