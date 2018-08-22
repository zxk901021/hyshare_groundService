package com.hyshare.groundservice.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.foamtrace.photopicker.SelectModel;
import com.google.android.flexbox.FlexboxLayout;
import com.hyshare.groundservice.R;
import com.hyshare.groundservice.adapter.SelectImageAdapter;
import com.hyshare.groundservice.base.BaseActivity;
import com.hyshare.groundservice.constant.KeyConstant;
import com.hyshare.groundservice.databinding.ActivityCarFaultFeedbackBinding;
import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.BaseTypeModel;
import com.hyshare.groundservice.util.AlbumModule;
import com.hyshare.groundservice.util.BITMAPUtils;
import com.hyshare.groundservice.util.BitmapUtil;
import com.hyshare.groundservice.util.DensityUtils;
import com.hyshare.groundservice.util.SharedUtil;
import com.hyshare.groundservice.util.StringUtil;
import com.hyshare.groundservice.util.ToastUtil;
import com.hyshare.groundservice.view.FlexRadioGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CarFaultFeedbackActivity extends BaseActivity<ActivityCarFaultFeedbackBinding> implements View.OnClickListener{

    private boolean mProtectFromCheckedChange = false;
    private SparseBooleanArray isCollapses = new SparseBooleanArray(); //是否收缩
    private String[] faultTypeList = new String[]{"车辆损坏", "车辆未熄火", "违规停车", "门窗灯未关闭", "车辆有违章", "其他"};
    private SelectImageAdapter mSelectImageAdapter;
    private List<String> mSelectImageList;
    private List<String> mSelectImageBase64List;
    private AlbumModule mAlbumModule;
    private String plateNum;
    private String faultType;
    private String faultDetail;
    private BaseTypeModel jobTypeModel;
    private String typeId;   //选择工作的ID
    private ArrayList<String> data;
    private String carid;
    public static void startAct(Context context, String carNum, String carid) {
        Intent intent = new Intent(context, CarFaultFeedbackActivity.class);
        intent.putExtra("carNum",carNum);
        intent.putExtra("carid",carid);
        context.startActivity(intent);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_car_fault_feedback;
    }

    @Override
    public void initUI() {
        mLayoutBinding.getRoot().setBackgroundResource(R.color.white);
        mLayoutBinding.btnPost.setOnClickListener(this);
        mLayoutBinding.btnScanPlateNum.setOnClickListener(this);
        mLayoutBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        carid=getIntent().getStringExtra("carid");
        mLayoutBinding.etPlateNum.setText(getIntent().getStringExtra("carNum"));
        mSelectImageAdapter = new SelectImageAdapter(3);
        mLayoutBinding.rvImage.setAdapter(mSelectImageAdapter);
        mSelectImageList = new ArrayList<>();
        mSelectImageAdapter.setNewData(mSelectImageList);
        mSelectImageAdapter.setOnSelectImageRvClickListener(new SelectImageAdapter.OnSelectImageRvClickListener() {
            @Override
            public void clickImage() {
                mAlbumModule.openAlbumActivity((ArrayList<String>) mSelectImageList);
            }

            @Override
            public void clickAddImage() {
                mAlbumModule.openAlbumActivity((ArrayList<String>) mSelectImageList);
            }

            @Override
            public void clickDel(ArrayList<String> list, int position) {
                mSelectImageList.remove(position);
                showSelectImage(mSelectImageList);
            }
        });

        mAlbumModule = new AlbumModule().activity(CarFaultFeedbackActivity.this)
                .showCarema(false)
                .picMax(3)
                .type(SelectModel.MULTI);

    }

    @Override
    public void initData() {
        getTypeData();
    }

    private void getTypeData() {
        data = new ArrayList<>();
        data.add("车辆有违章");
        data.add("其他");
        data.add("门窗灯未关闭");
        data.add("车辆损坏");
        data.add("违规停车");
        data.add("车辆未熄火");

        jobTypeModel = new BaseTypeModel();
        List<BaseTypeModel.DataBean> dataBeans = new ArrayList<>();

        BaseTypeModel.DataBean dataBean = new BaseTypeModel.DataBean();
        dataBean.setDescription("车辆有违章");
        dataBean.setValue("5");

        BaseTypeModel.DataBean dataBean1 = new BaseTypeModel.DataBean();
        dataBean1.setDescription("其他");
        dataBean1.setValue("6");

        BaseTypeModel.DataBean dataBean2 = new BaseTypeModel.DataBean();
        dataBean2.setDescription("门窗灯未关闭");
        dataBean2.setValue("4");

        BaseTypeModel.DataBean dataBean3 = new BaseTypeModel.DataBean();
        dataBean3.setDescription("车辆损坏");
        dataBean3.setValue("1");

        BaseTypeModel.DataBean dataBean4 = new BaseTypeModel.DataBean();
        dataBean4.setDescription("违规停车");
        dataBean4.setValue("3");

        BaseTypeModel.DataBean dataBean5 = new BaseTypeModel.DataBean();
        dataBean5.setDescription("车辆未熄火");
        dataBean5.setValue("2");

        dataBeans.add(dataBean);
        dataBeans.add(dataBean1);
        dataBeans.add(dataBean2);
        dataBeans.add(dataBean3);
        dataBeans.add(dataBean4);
        dataBeans.add(dataBean5);
        jobTypeModel.setData(dataBeans);

        createRadioButton(data, mLayoutBinding.flexRg);


    }

    private void createRadioButton(List<String> filters, final FlexRadioGroup group) {

        float width = DensityUtils.getWidth(this);
        for (String filter : filters) {
            RadioButton rb = (RadioButton) getLayoutInflater().inflate(R.layout.item_label, null);
            rb.setText(filter);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams((int) ((width - DensityUtils.dp2px(this, 48)) / 3), ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = DensityUtils.dp2px(this, 6);
            lp.topMargin = margin;
            lp.bottomMargin = margin;
            lp.leftMargin = margin;
            lp.rightMargin = margin;
            rb.setLayoutParams(lp);
            group.addView(rb);

            /**
             * 下面两个监听器用于点击两次可以清除当前RadioButton的选中
             * 点击RadioButton后，{@link FlexRadioGroup#OnCheckedChangeListener}先回调，然后再回调{@link View#OnClickListener}
             * 如果当前的RadioButton已经被选中时，不会回调OnCheckedChangeListener方法，故判断没有回调该方法且当前RadioButton确实被选中时清除掉选中
             */
            group.setOnCheckedChangeListener(new FlexRadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(@IdRes int checkedId) {
                    mProtectFromCheckedChange = true;
                }
            });

            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mProtectFromCheckedChange && ((RadioButton) v).isChecked()) {
                        group.clearCheck();
                    } else {
                        mProtectFromCheckedChange = false;
                    }
                }
            });
        }
        isCollapses.put(group.getId(), false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (AlbumModule.getImages(data) != null) {
            mSelectImageList.clear();
            mSelectImageList.addAll(AlbumModule.getImages(data));
            showSelectImage(mSelectImageList);
        }
    }

    private void showSelectImage(List<String> selectImageList) {
        mLayoutBinding.tvImageNum.setText("请提供车况照片(" + mSelectImageList.size() + "/3) ");
        mSelectImageAdapter.setNewData(mSelectImageList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_post:
                attemptPost();
                break;
            case R.id.btn_scan_plate_num:
                break;
        }
    }


    private void attemptPost() {
        plateNum = mLayoutBinding.etPlateNum.getText().toString();
        faultType = mLayoutBinding.flexRg.getCheckedRadioButtonId() == -1 ?
                "" : ((RadioButton) mLayoutBinding.flexRg.findViewById(mLayoutBinding.flexRg.getCheckedRadioButtonId())).getText().toString();
        faultDetail = mLayoutBinding.etFaultDes.getText().toString();
        if (TextUtils.isEmpty(plateNum)) {
            ToastUtil.toast("请输入故障车辆的车牌号");
            return;
        }
        if (TextUtils.isEmpty(faultType)) {
            ToastUtil.toast("请选择故障类型");
            return;
        }

        showDialog();
        for(BaseTypeModel.DataBean bean:jobTypeModel.getData()){
            if(bean.getDescription().equals(faultType)){
                typeId=bean.getValue();
            }
        }
        //压缩图片
        mSelectImageBase64List=new ArrayList<>();
        if (mSelectImageList.size() > 0) {
            Observable.from(mSelectImageList)
                    .flatMap(new Func1<String, Observable<? extends Bitmap>>() {
                        @Override
                        public Observable<? extends Bitmap> call(String s) {
                            return Observable.just(BitmapFactory.decodeFile(s));
                        }
                    })
                    .flatMap(new Func1<Bitmap, Observable<? extends Bitmap>>() {
                        @Override
                        public Observable<? extends Bitmap> call(Bitmap bitmap) {
                            return Observable.just(BITMAPUtils.compressBitmap(bitmap, 40));
                        }
                    })
                    .flatMap(new Func1<Bitmap, Observable<? extends String>>() {
                        @Override
                        public Observable<? extends String> call(Bitmap bitmap1) {
                            return Observable.just(BITMAPUtils.convertBitmapToString(bitmap1));
                        }
                    })
                    .buffer(mSelectImageList.size())
                    .flatMap(new Func1<List<String>, Observable<? extends String>>() {
                        @Override
                        public Observable<? extends String> call(List<String> list) {
                            return Observable.just(StringUtil.appendWithTag(list, ","));
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(String s) {
                            post(s, StringUtil.appendWithTag(BITMAPUtils.getImageFileType(mSelectImageList), ","));
                        }
                    });

        } else {
            dismiss();
            ToastUtil.toast("请至少上传一张图片！");
//            post("", "");
        }


    }

    private void post(String images, String imageType) {
        Map<String, Object> param = new HashMap<>();
        String userId = SharedUtil.getString(context, KeyConstant.USER_ID, "");
        param.put("member_id", userId);
        param.put("car_id", carid);
        param.put("type", typeId);
        param.put("remark", faultDetail);
        param.put("images", images.split(","));
        JSONObject object = new JSONObject(param);
        getApiService().postFaults(parseRequest(object.toString()))
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
                        if (stringBaseModel.getCode()==1) {
                            ToastUtil.toast("故障上报成功");
                            finish();
                        }
                    }
                });

    }
}
