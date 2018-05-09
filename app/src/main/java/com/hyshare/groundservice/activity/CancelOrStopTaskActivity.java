package com.hyshare.groundservice.activity;

import com.hyshare.groundservice.R;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.databinding.ActivityCancelOrStopTaskBinding;

public class CancelOrStopTaskActivity extends BaseActivity<ActivityCancelOrStopTaskBinding> {

    private int mode;

    @Override
    public int setLayoutId() {
        return R.layout.activity_cancel_or_stop_task;
    }

    @Override
    public void initUI() {

    }

    @Override
    public void initData() {
        mode = getIntent().getIntExtra("mode", 0);
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
}
