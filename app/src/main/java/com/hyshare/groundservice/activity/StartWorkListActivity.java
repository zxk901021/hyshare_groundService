package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityStartWorkListBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.TaskModel;
import com.hyshare.groundservice.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StartWorkListActivity extends BaseActivity<ActivityStartWorkListBinding> implements View.OnClickListener {

    String workOrderId;
    String id;
    List<TaskModel> tasks;
    BaseQuickAdapter adapter;

    @Override
    public int setLayoutId() {
        return R.layout.activity_start_work_list;
    }

    @Override
    public void initUI() {
        mLayoutBinding.back.setOnClickListener(this);
        adapter = new BaseQuickAdapter<TaskModel, BaseViewHolder>(R.layout.item_task) {
            @Override
            protected void convert(BaseViewHolder helper, TaskModel item) {
                helper.setText(R.id.task, item.getTask());
            }
        };
        mLayoutBinding.taskList.setLayoutManager(new GridLayoutManager(context, 3));
        adapter.bindToRecyclerView(mLayoutBinding.taskList);
        mLayoutBinding.taskList.setAdapter(adapter);
        mLayoutBinding.submit.setOnClickListener(this);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                TextView task = view.findViewById(R.id.task);
                if (tasks.get(position).isChecked()) {
                    tasks.get(position).setChecked(false);
                    task.setBackgroundResource(R.drawable.task_unselected_bg);
                    task.setTextColor(Color.parseColor("#333333"));
                } else {
                    tasks.get(position).setChecked(true);
                    task.setBackgroundResource(R.drawable.task_selected_bg);
                    task.setTextColor(Color.parseColor("#ffffff"));
                }

            }
        });
    }

    @Override
    public void initData() {
        tasks = new ArrayList<>();
        tasks.add(new TaskModel(0, "挪车", false));
        tasks.add(new TaskModel(1, "加油", false));
        tasks.add(new TaskModel(2, "洗车", false));
        tasks.add(new TaskModel(3, "处理违章", false));
        tasks.add(new TaskModel(4, "维修车辆", false));
        tasks.add(new TaskModel(5, "处理肇事", false));
        tasks.add(new TaskModel(6, "保养", false));
        tasks.add(new TaskModel(7, "其他", false));
        adapter.setNewData(tasks);
        workOrderId = getIntent().getStringExtra("work_id");
        id = getIntent().getStringExtra("id");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.submit:
                List<String> remarks = new ArrayList<>();
                for (TaskModel task: tasks) {
                    if (task.isChecked()){
                        remarks.add(task.getTask());
                    }
                }
                StringBuilder sb = new StringBuilder();
                if (remarks.size() > 0){
                    for (int i = 0, len = remarks.size(); i < len; i ++){
                        sb.append(remarks.get(i)).append(",");
                    }
                    String tasks = sb.substring(0, sb.length() - 1);
                    startWorks(workOrderId, tasks);
                }

                break;
        }
    }

    private void startWorks(String ordersId, String tasks){
        Map<String, Object> param = new HashMap<>();
        param.put("task", tasks);
        JSONObject object = new JSONObject(param);
        getApiService().startWork(ordersId, parseRequest(object.toString()))
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
                            ToastUtil.toast("工单已开始");
                            Intent intent = new Intent(context, ManageCarActivity.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                            finish();
                        }else ToastUtil.toast(stringBaseModel.getMessage());
                    }
                });
    }
}
