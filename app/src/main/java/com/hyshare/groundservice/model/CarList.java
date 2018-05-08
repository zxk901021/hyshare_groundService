package com.hyshare.groundservice.model;

import java.util.List;

/**
 * Created by Administrator on 2018/5/5.
 */

public class CarList {

    private int total;
    private List<CarListBean> rows;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CarListBean> getRows() {
        return rows;
    }

    public void setRows(List<CarListBean> rows) {
        this.rows = rows;
    }

    public static class CarListBean{
        private String id;
        private String number;
        private String device_no;
        private String image_path;
        private String user_id;
        private String use_state;
        private String state;
        private String create_time;
        private String province_id;
        private String province_name;
        private String city_id;
        private String city_name;
        private String longitude;
        private String latitude;
        private String class_id;
        private String class_name;
        private String brand_id;
        private String brand_name;
        private String brand_logo;
        private String seat_num;
        private String color_id;
        private String source_type;
        private String remaining_gas;
        private String remark;
        private String claim_state;
        private String last_return;
        private String last_move;
        private String last_repair;
        private String last_refuel;
        private String last_maintain;
        private String last_clean_time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getDevice_no() {
            return device_no;
        }

        public void setDevice_no(String device_no) {
            this.device_no = device_no;
        }

        public String getImage_path() {
            return image_path;
        }

        public void setImage_path(String image_path) {
            this.image_path = image_path;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getUse_state() {
            return use_state;
        }

        public void setUse_state(String use_state) {
            this.use_state = use_state;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getProvince_id() {
            return province_id;
        }

        public void setProvince_id(String province_id) {
            this.province_id = province_id;
        }

        public String getProvince_name() {
            return province_name;
        }

        public void setProvince_name(String province_name) {
            this.province_name = province_name;
        }

        public String getCity_id() {
            return city_id;
        }

        public void setCity_id(String city_id) {
            this.city_id = city_id;
        }

        public String getCity_name() {
            return city_name;
        }

        public void setCity_name(String city_name) {
            this.city_name = city_name;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getClass_id() {
            return class_id;
        }

        public void setClass_id(String class_id) {
            this.class_id = class_id;
        }

        public String getClass_name() {
            return class_name;
        }

        public void setClass_name(String class_name) {
            this.class_name = class_name;
        }

        public String getBrand_id() {
            return brand_id;
        }

        public void setBrand_id(String brand_id) {
            this.brand_id = brand_id;
        }

        public String getBrand_name() {
            return brand_name;
        }

        public void setBrand_name(String brand_name) {
            this.brand_name = brand_name;
        }

        public String getBrand_logo() {
            return brand_logo;
        }

        public void setBrand_logo(String brand_logo) {
            this.brand_logo = brand_logo;
        }

        public String getSeat_num() {
            return seat_num;
        }

        public void setSeat_num(String seat_num) {
            this.seat_num = seat_num;
        }

        public String getColor_id() {
            return color_id;
        }

        public void setColor_id(String color_id) {
            this.color_id = color_id;
        }

        public String getSource_type() {
            return source_type;
        }

        public void setSource_type(String source_type) {
            this.source_type = source_type;
        }

        public String getRemaining_gas() {
            return remaining_gas;
        }

        public void setRemaining_gas(String remaining_gas) {
            this.remaining_gas = remaining_gas;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getClaim_state() {
            return claim_state;
        }

        public void setClaim_state(String claim_state) {
            this.claim_state = claim_state;
        }

        public String getLast_return() {
            return last_return;
        }

        public void setLast_return(String last_return) {
            this.last_return = last_return;
        }

        public String getLast_move() {
            return last_move;
        }

        public void setLast_move(String last_move) {
            this.last_move = last_move;
        }

        public String getLast_repair() {
            return last_repair;
        }

        public void setLast_repair(String last_repair) {
            this.last_repair = last_repair;
        }

        public String getLast_refuel() {
            return last_refuel;
        }

        public void setLast_refuel(String last_refuel) {
            this.last_refuel = last_refuel;
        }

        public String getLast_maintain() {
            return last_maintain;
        }

        public void setLast_maintain(String last_maintain) {
            this.last_maintain = last_maintain;
        }

        public String getLast_clean_time() {
            return last_clean_time;
        }

        public void setLast_clean_time(String last_clean_time) {
            this.last_clean_time = last_clean_time;
        }
    }
}
