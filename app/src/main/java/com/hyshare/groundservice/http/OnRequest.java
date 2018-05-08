package com.hyshare.groundservice.http;

/**
 * Created by Administrator on 2018/5/7.
 */

public interface OnRequest {

    void onError(Throwable e);

    void onNext(Object o);
}
