package com.hyshare.groundservice.model;

/**
 * Created by Administrator on 2018/5/4.
 */

public class CarCountBean {

    private int nearby_count;
    private int unavailable_count;

    private int free_count;
    private int sum_count;

    public int getNearby_count() {
        return nearby_count;
    }

    public void setNearby_count(int nearby_count) {
        this.nearby_count = nearby_count;
    }

    public int getUnavailable_count() {
        return unavailable_count;
    }

    public void setUnavailable_count(int unavailable_count) {
        this.unavailable_count = unavailable_count;
    }

    public int getFree_count() {
        return free_count;
    }

    public void setFree_count(int free_count) {
        this.free_count = free_count;
    }

    public int getSum_count() {
        return sum_count;
    }

    public void setSum_count(int sum_count) {
        this.sum_count = sum_count;
    }
}


