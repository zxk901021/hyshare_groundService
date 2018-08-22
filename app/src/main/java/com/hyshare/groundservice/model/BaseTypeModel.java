package com.hyshare.groundservice.model;

import java.util.List;

/**
 * Created by zxk on 2018/8/22.
 */

public class BaseTypeModel extends BaseModel2{

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private int code;

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * value : 1
         * description : 教师
         */

        private String value;
        private String description;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
