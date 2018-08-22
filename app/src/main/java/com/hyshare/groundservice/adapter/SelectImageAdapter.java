package com.hyshare.groundservice.adapter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hyshare.groundservice.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zxk on 2018/8/22.
 */

public class SelectImageAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private ArrayList<String> mImageList;
    private int mLimit;
    private boolean isAddDef;
    private OnSelectImageRvClickListener mOnSelectImageRvClickListener;

    public SelectImageAdapter(int limit) {
        super(R.layout.item_add_image);
        mImageList = new ArrayList<>();
        this.mLimit = limit;

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mOnSelectImageRvClickListener == null) {
                    return;
                }
                if (isAddDef && position == mImageList.size() - 1) {
                    mOnSelectImageRvClickListener.clickAddImage();
                } else {
                    mOnSelectImageRvClickListener.clickImage();
                }
            }
        });

        setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (mOnSelectImageRvClickListener == null) {
                    return;
                }
                if (view.getId() == R.id.btn_add_image_del) {
                    mOnSelectImageRvClickListener.clickDel((ArrayList<String>) mImageList, position);
                }
            }
        });


    }

    @Override
    public void addData(@NonNull Collection<? extends String> newData) {
        if (newData instanceof List && newData != null) {
            mImageList.addAll(((List) newData).subList(0, mLimit - mImageList.size()));
            super.setNewData(mImageList);
        }
    }

    @Override
    public void setNewData(List<String> data) {
        if (data != null) {
            mImageList.clear();
            if (data.size() >= mLimit) {
                mImageList.addAll(data.subList(0, mLimit));
                isAddDef = false;
            } else {
                mImageList.addAll(data);
                mImageList.add(" ");
                isAddDef = true;
            }
            super.setNewData(mImageList);
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        Log.d(TAG, "convert: " + item);
        ImageView img = helper.getView(R.id.iv_add_image);
        if (isAddDef && helper.getPosition() == mImageList.size() - 1) {
            img.setImageResource(R.mipmap.trouble_img);
            helper.setVisible(R.id.btn_add_image_del, false);
        } else {
            Glide.with(helper.getConvertView().getContext()).load(item).into(img);
            helper.setVisible(R.id.btn_add_image_del, true);
        }
        helper.addOnClickListener(R.id.btn_add_image_del);
    }

    public void setOnSelectImageRvClickListener(OnSelectImageRvClickListener onSelectImageRvClickListener) {
        mOnSelectImageRvClickListener = onSelectImageRvClickListener;
    }

    public interface OnSelectImageRvClickListener {
        void clickImage();

        void clickAddImage();

        void clickDel(ArrayList<String> list, int position);
    }
}
