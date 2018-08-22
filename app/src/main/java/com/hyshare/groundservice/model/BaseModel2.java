package com.hyshare.groundservice.model;


import com.google.gson.Gson;

/**
 * Created by zxk on 2018/8/22.
 */

public class BaseModel2 implements Model{

    private static final Gson mJson = new Gson();

    public int status;
    public String out_txt;
    public int code;
    public  String message;
    @Override
    public boolean isOk() {
        return status == 1;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isRemoteLogin() {
        return code == 9999;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
