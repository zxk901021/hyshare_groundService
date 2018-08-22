package com.hyshare.groundservice.presenter;

import android.content.Context;

/**
 * Created by zxk on 2018/7/25.
 */

public interface Presenter {

    Context getContext();

    void showLoading();

    void hideLoading();

    void onDo(int codeFlag, Object... objects);

    void onError();

    boolean showError();
}
