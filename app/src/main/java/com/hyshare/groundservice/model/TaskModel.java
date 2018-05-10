package com.hyshare.groundservice.model;

/**
 * Created by Administrator on 2018/5/10.
 */

public class TaskModel {

    private int position;
    private String task;
    private boolean checked;

    public TaskModel(int position, String task, boolean checked) {
        this.position = position;
        this.task = task;
        this.checked = checked;
    }

    public int getPostion() {
        return position;
    }

    public void setPostion(int position) {
        this.position = position;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
