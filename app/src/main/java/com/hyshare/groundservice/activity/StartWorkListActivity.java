package com.hyshare.groundservice.activity;

import android.view.View;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityStartWorkListBinding;

public class StartWorkListActivity extends BaseActivity<ActivityStartWorkListBinding> implements View.OnClickListener{

    String workOrderId;

    @Override
    public int setLayoutId() {
        return R.layout.activity_start_work_list;
    }

    @Override
    public void initUI() {
        mLayoutBinding.back.setOnClickListener(this);
    }

    @Override
    public void initData() {
        workOrderId = getIntent().getStringExtra("work_id");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
        }
    }
}
