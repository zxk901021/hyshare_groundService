package com.hyshare.groundservice.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyshare.groundservice.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/7.
 */

public class GasPercentView extends LinearLayout {

    View percent1, percent2, percent3, percent4, percent5, percent6, percent7, percent8, percent9, percent10;
    TextView percentTotal;
    List<View> percents;
    int percent;

    public void init(String percentCount) {
        if (!TextUtils.isEmpty(percentCount)){
            percent = (int) Math.round(Double.valueOf(percentCount)/10);
        }
        setPercentImg(percent);
        setText(percentCount);
    }

    public GasPercentView(Context context) {
        super(context);
    }

    public void setPercentImg(int percent) {
        percents = new ArrayList<>();
        percents.add(percent1);
        percents.add(percent2);
        percents.add(percent3);
        percents.add(percent4);
        percents.add(percent5);
        percents.add(percent6);
        percents.add(percent7);
        percents.add(percent8);
        percents.add(percent9);
        percents.add(percent10);
        if (percent > 10) {
            return;
        }
        if (percent < 0) {
            return;
        }
        for (int i = 0; i < 10; i++) {
            if (i < percent){
                percents.get(i).setBackgroundResource(R.mipmap.percent_yes);
            }else percents.get(i).setBackgroundResource(R.mipmap.percent_no);
        }
    }

    public void setText(String text) {
        percentTotal.setText(subZeroAndDot(text) + "%" );
    }

    public GasPercentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.gas_percent_layout, this);
        percent1 = findViewById(R.id.percent_1);
        percent2 = findViewById(R.id.percent_2);
        percent3 = findViewById(R.id.percent_3);
        percent4 = findViewById(R.id.percent_4);
        percent5 = findViewById(R.id.percent_5);
        percent6 = findViewById(R.id.percent_6);
        percent7 = findViewById(R.id.percent_7);
        percent8 = findViewById(R.id.percent_8);
        percent9 = findViewById(R.id.percent_9);
        percent10 = findViewById(R.id.percent_10);
        percentTotal = findViewById(R.id.total_percent);
    }

    public GasPercentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GasPercentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String subZeroAndDot(String s){
        if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }
}
