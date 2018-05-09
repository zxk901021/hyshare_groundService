package com.hyshare.groundservice.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.location.DPoint;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/9.
 */

public class MapPoint implements Parcelable{

    private double latitude;
    private double longitude;

    public MapPoint() {
    }

    public MapPoint(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public MapPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public DPoint toDPint() {
        return new DPoint(latitude, longitude);
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public LatLonPoint getLatLonPoint() {
        return new LatLonPoint(latitude, longitude);
    }

    public static List<MapPoint> convertLatLngToMapPoint(List<LatLng> latLngList) {
        if (latLngList == null) {
            return null;
        }
        List<MapPoint> result = new ArrayList<>();
        for (LatLng latLng : latLngList) {
            result.add(new MapPoint(latLng));
        }

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
    }

    protected MapPoint(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
    }

    public static final Creator<MapPoint> CREATOR = new Creator<MapPoint>() {
        @Override
        public MapPoint createFromParcel(Parcel source) {
            return new MapPoint(source);
        }

        @Override
        public MapPoint[] newArray(int size) {
            return new MapPoint[size];
        }
    };

    @Override
    public String toString() {
        return "[lat:" + latitude + ",lng:" + longitude + "]";
    }
}
