package com.hyshare.groundservice.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.hyshare.groundservice.util.TimeUtil;
import com.hyshare.groundservice.util.ToastUtil;
import com.hyshare.groundservice.view.GasPercentView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class CarListActivity extends BaseActivity<ActivityCarListBinding> implements View.OnClickListener {

    BaseQuickAdapter adapter;
    private int responseCode = 0;
    MapPoint start;
    List<CarList.CarListBean> sumData;
    List<CarList.CarListBean> myData;
    List<CarList.CarListBean> searchData;

    private int dataMode = 0;
    private final static int DATA_MY = 1;
    private final static int DATA_SUM = 2;

    private boolean TIME_DESC = true;
    private boolean DISTANCE_DESC = false;

    private boolean IS_SEARCH = false;

    private String searchNumber;

    @Override
    public int setLayoutId() {
        return R.layout.activity_car_list;
    }

    @Override
    public void initUI() {
        sumData = new ArrayList<>();
        myData = new ArrayList<>();
        searchData = new ArrayList<>();
        dataMode = DATA_SUM;
        String lat = SharedUtil.getString(context, "lat");
        String lon = SharedUtil.getString(context, "lon");
        start = new MapPoint(Double.valueOf(lat), Double.valueOf(lon));
        adapter = new BaseQuickAdapter<CarList.CarListBean, BaseViewHolder>(R.layout.item_car_list_layout) {
            @Override
            protected void convert(BaseViewHolder helper, final CarList.CarListBean item) {
                helper.setText(R.id.car_number, item.getNumber());
                helper.setText(R.id.car_type, item.getBrand_name() + item.getSeat_num() + "座");
                ((GasPercentView) helper.itemView.findViewById(R.id.gas)).init(item.getRemaining_gas());
                helper.setTag(R.id.gas, item.getRemaining_gas());
                helper.setText(R.id.car_status, setUseState(item.getUse_state()));
                helper.setText(R.id.distance, "距离：" + AmapUtil.format(item.getDistance() / 1000) + "公里");
                ViewModel model = setOperation(item.getClaim_state());
                helper.setText(R.id.operate_car, model.getText());
                if (!TextUtils.isEmpty(item.getLast_return())) {
                    helper.setText(R.id.stay_time, "停放：" + TimeUtil.timeFormat(Long.valueOf(item.getLast_return())));
                }
                helper.setBackgroundRes(R.id.operate_car, model.getRes());
                if (model.getText().equals("已被认领")) helper.setTextColor(R.id.operate_car, Color.parseColor("#999999"));
                else helper.setTextColor(R.id.operate_car, Color.WHITE);
                Glide.with(context).load(item.getImage_path()).into((ImageView) helper.getView(R.id.car_photo));
                helper.getView(R.id.operate_car).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ("1".equals(item.getClaim_state())) {
                            claimWorkList(item.getId());
                        }
                    }
                });
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
                intent.putExtra("id", ((CarList.CarListBean) adapter.getItem(position)).getId());
                startActivity(intent);
            }
        });
        mLayoutBinding.searchInfo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND || i == EditorInfo.IME_ACTION_SEARCH) {
                    String number = mLayoutBinding.searchInfo.getText().toString().trim();
                    getCarList(number);
                    return true;
                }
                return false;
            }
        });
        mLayoutBinding.searchInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchNumber = mLayoutBinding.searchInfo.getText().toString().trim();
                findSearch();
            }
        });
        mLayoutBinding.myClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dataMode == DATA_MY) {
                    mLayoutBinding.myClaim.setTextColor(Color.parseColor("#666666"));
                    dataMode = DATA_SUM;
                    if (!TextUtils.isEmpty(searchNumber)) findSearch();
                    else setResultData(sumData);
                } else if (dataMode == DATA_SUM) {
                    mLayoutBinding.myClaim.setTextColor(Color.parseColor("#f85f4a"));
                    dataMode = DATA_MY;
                    if (!TextUtils.isEmpty(searchNumber)) findSearch();
                    else setResultData(myData);
                }
            }
        });
        mLayoutBinding.distanceOrder.setOnClickListener(this);
        mLayoutBinding.stayTimeOrder.setOnClickListener(this);
    }

    private void findSearch() {
        IS_SEARCH = !TextUtils.isEmpty(searchNumber);
        if (dataMode == DATA_MY) searchData = searchData(myData, searchNumber);
        else searchData = searchData(sumData, searchNumber);
        setResultData(searchData);
    }

    private void setResultData(List<CarList.CarListBean> data) {
        adapter.setNewData(data);
        if (data.size() > 0) mLayoutBinding.carList.smoothScrollToPosition(0);
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

    private List<CarList.CarListBean> searchData(List<CarList.CarListBean> data, String carNumber) {
        if (TextUtils.isEmpty(carNumber)) return searchData = data;
        else {
            searchData = new ArrayList<>();
            Pattern pattern = Pattern.compile(carNumber, Pattern.CASE_INSENSITIVE);
            for (CarList.CarListBean bean : data) {
                Matcher matcher = pattern.matcher(bean.getNumber());
                if (matcher.find()) searchData.add(bean);
            }
            return searchData;
        }
    }

    private ViewModel setOperation(String status) {
        ViewModel model = new ViewModel();
        switch (status) {
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

    private void getCarList(String number) {
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
                        if (carListBaseModel.getCode() == 1) {
                            if (carListBaseModel.getData() != null) {
                                sumData = carListBaseModel.getData().getRows();
                                if (sumData.size() > 0) {
                                    for (int i = 0, len = sumData.size(); i < len; i++) {
                                        MapPoint end = new MapPoint(Double.valueOf(sumData.get(i).getLatitude()), Double.valueOf(sumData.get(i).getLongitude()));
                                        float distance = AmapUtil.calculateLineDistance(start, end);
                                        sumData.get(i).setDistance(distance);
                                    }
                                    Collections.sort(sumData, sortByDistanceASC(sumData));
                                    DISTANCE_DESC = true;
                                }
                                myData = new ArrayList<>();
                                for (CarList.CarListBean bean : sumData) {
                                    if ("3".equals(bean.getClaim_state())) {
                                        myData.add(bean);
                                    }
                                }
                                if (dataMode == DATA_SUM) adapter.setNewData(sumData);
                                else if (dataMode == DATA_MY) adapter.setNewData(myData);

                            } else {
                                ToastUtil.toast(carListBaseModel.getMessage());
                            }
                        }
                    }
                });
    }

    private Comparator sortByDistanceASC(List<CarList.CarListBean> data) {
        return new Comparator<CarList.CarListBean>() {
            @Override
            public int compare(CarList.CarListBean t1, CarList.CarListBean t2) {
                if (t1.getDistance() - t2.getDistance() > 0) {
                    return 1;
                } else if (t1.getDistance() - t2.getDistance() < 0) {
                    return -1;
                } else return 0;
            }
        };
    }

    private Comparator sortByDistanceDESC(List<CarList.CarListBean> data) {
        return new Comparator<CarList.CarListBean>() {
            @Override
            public int compare(CarList.CarListBean t1, CarList.CarListBean t2) {
                if (t1.getDistance() - t2.getDistance() > 0) {
                    return -1;
                } else if (t1.getDistance() - t2.getDistance() < 0) {
                    return 1;
                } else return 0;
            }
        };
    }

    private Comparator sortByTimeASC(List<CarList.CarListBean> data) {
        return new Comparator<CarList.CarListBean>() {
            @Override
            public int compare(CarList.CarListBean t1, CarList.CarListBean t2) {
                if (!TextUtils.isEmpty(t1.getLast_return()) && !TextUtils.isEmpty(t2.getLast_return())) {
                    if (Long.valueOf(t1.getLast_return()) - Long.valueOf(t2.getLast_return()) > 0) {
                        return -1;
                    } else if (Long.valueOf(t1.getLast_return()) - Long.valueOf(t2.getLast_return()) < 0) {
                        return 1;
                    } else return 0;
                }
                return 0;
            }
        };
    }

    private Comparator sortByTimeDESC(List<CarList.CarListBean> data) {
        return new Comparator<CarList.CarListBean>() {
            @Override
            public int compare(CarList.CarListBean t1, CarList.CarListBean t2) {
                if (!TextUtils.isEmpty(t1.getLast_return()) && !TextUtils.isEmpty(t2.getLast_return())) {
                    if (Long.valueOf(t1.getLast_return()) - Long.valueOf(t2.getLast_return()) > 0) {
                        return 1;
                    } else if (Long.valueOf(t1.getLast_return()) - Long.valueOf(t2.getLast_return()) < 0) {
                        return -1;
                    } else return 0;
                }
                return 0;
            }
        };
    }

    private int claimWorkList(String carId) {
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
                        if (code == 1) {
                            responseCode = 1;
                            ToastUtil.toast("车辆认领成功");
                            getCarList("");
                        } else if (code == 9999) {
                            logout();
                        } else ToastUtil.toast(stringBaseModel.getMessage());
                    }
                });
        return responseCode;
    }

    @Override
    public void onClick(View view) {
        Drawable selected = getResources().getDrawable(R.mipmap.order_selected);
        selected.setBounds(0, 0, selected.getMinimumWidth(), selected.getMinimumHeight());
        Drawable unselected = getResources().getDrawable(R.mipmap.order_unselect);
        unselected.setBounds(0, 0, unselected.getMinimumWidth(), unselected.getMinimumHeight());
        switch (view.getId()) {
            case R.id.distance_order:
                if (DISTANCE_DESC) {
                    if (IS_SEARCH) Collections.sort(searchData, sortByDistanceDESC(searchData));
                    else {
                        if (dataMode == DATA_MY)
                            Collections.sort(myData, sortByDistanceDESC(myData));
                        else Collections.sort(sumData, sortByDistanceDESC(sumData));
                    }
                    DISTANCE_DESC = false;
                    TIME_DESC = true;
                    mLayoutBinding.distanceOrder.setCompoundDrawables(null, null, selected, null);
                    mLayoutBinding.distanceOrder.setTextColor(Color.parseColor("#f85f4a"));
                    mLayoutBinding.stayTimeOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.stayTimeOrder.setTextColor(Color.parseColor("#666666"));
                } else {
                    if (IS_SEARCH) Collections.sort(searchData, sortByDistanceASC(searchData));
                    else {
                        if (dataMode == DATA_MY)
                            Collections.sort(myData, sortByDistanceASC(myData));
                        else Collections.sort(sumData, sortByDistanceASC(sumData));
                    }
                    DISTANCE_DESC = true;
                    TIME_DESC = true;
                    mLayoutBinding.distanceOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.distanceOrder.setTextColor(Color.parseColor("#666666"));
                    mLayoutBinding.stayTimeOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.stayTimeOrder.setTextColor(Color.parseColor("#666666"));
                }
                adapter.notifyDataSetChanged();
                if (adapter.getData().size() > 0) mLayoutBinding.carList.smoothScrollToPosition(0);
                break;
            case R.id.stay_time_order:
                if (TIME_DESC) {
                    if (IS_SEARCH) Collections.sort(searchData, sortByTimeDESC(searchData));
                    else {
                        if (dataMode == DATA_MY) Collections.sort(myData, sortByTimeDESC(myData));
                        else Collections.sort(sumData, sortByTimeDESC(sumData));
                    }
                    TIME_DESC = false;
                    DISTANCE_DESC = true;
                    mLayoutBinding.distanceOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.distanceOrder.setTextColor(Color.parseColor("#666666"));
                    mLayoutBinding.stayTimeOrder.setCompoundDrawables(null, null, selected, null);
                    mLayoutBinding.stayTimeOrder.setTextColor(Color.parseColor("#f85f4a"));

                } else {
                    TIME_DESC = true;
                    DISTANCE_DESC = true;
                    if (IS_SEARCH) Collections.sort(searchData, sortByTimeASC(searchData));
                    else {
                        if (dataMode == DATA_MY) Collections.sort(myData, sortByTimeASC(myData));
                        else Collections.sort(sumData, sortByTimeASC(sumData));
                    }
                    mLayoutBinding.distanceOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.distanceOrder.setTextColor(Color.parseColor("#666666"));
                    mLayoutBinding.stayTimeOrder.setCompoundDrawables(null, null, unselected, null);
                    mLayoutBinding.stayTimeOrder.setTextColor(Color.parseColor("#666666"));
                }
                adapter.notifyDataSetChanged();
                if (adapter.getData().size() > 0) mLayoutBinding.carList.smoothScrollToPosition(0);
                break;
        }
    }
}
