package com.hyshare.groundservice.http;

import com.hyshare.groundservice.model.BaseModel;
import com.hyshare.groundservice.model.CarCountBean;
import com.hyshare.groundservice.model.CarList;
import com.hyshare.groundservice.model.LoginBean;
import com.hyshare.groundservice.model.WorkList;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2018/5/4.
 */

public interface ApiService {

    @POST("v1/Sites/FictitiousLogin")
    Observable<BaseModel<LoginBean>> login(@Body RequestBody requestBody);

    @POST("v1/groundservice/GroundCars/CarCount")
    Observable<BaseModel<CarCountBean>> getCarCount(@Body RequestBody requestBody);

    @GET("v1/groundservice/WordOrders/?")
    Observable<BaseModel<WorkList>> getWorkList(@Query("create_time") String time);

    @GET("v1/groundservice/GroundCars/?")
    Observable<BaseModel<CarList>> getCarList(@Query("number") String number);

    @GET("v1/groundservice/GroundCars/{id}")
    Observable<BaseModel<CarList.CarListBean>> getCarInfo(@Path("id") String id);

    @PUT("v1/groundservice/WordOrders/{id}/End")
    Observable<BaseModel<String>> cancelWorkList(@Path("id") String id, @Body RequestBody requestBody);

    @POST("v1/groundservice/WordOrders/")
    Observable<BaseModel<String>> claimWorkList(@Body RequestBody requestBody);

    @DELETE("v1/groundservice/WordOrders/{id}")
    Observable<BaseModel<String>> cancelClaim(@Path("id") String id);

    @PUT("v1/groundservice/WordOrders/{id}/Start")
    Observable<BaseModel<String>> startWork(@Path("id") String id, @Body RequestBody requestBody);

    @PUT("v1/groundservice/GroundCars/{id}")
    Observable<BaseModel<String>> changeCarState(@Path("id") String id, @Body RequestBody requestBody);

    @POST("v1/groundservice/GroundCars/Command/")
    Observable<BaseModel<String>> sendCommand(@Body RequestBody requestBody);

    @GET("v1/groundservice/GroundCars/{id}/isOrderByCarId")
    Observable<BaseModel<String>> checkOrderState(@Path("id") String id);

    @POST("http://api.hyshare.cn/v1/mobile/Faces/FaceVerify")
    Observable<BaseModel<String>> faceVerify(@Body RequestBody requestBody);

    @POST("/v1/groundservice/faults")
    Observable<BaseModel<String>> postFaults(@Body RequestBody requestBody);

}
