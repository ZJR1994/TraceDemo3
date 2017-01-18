package com.app.zjr.tracedemo3.data;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by ZJR on 2016/11/9.
 */
public class Traces extends BmobObject {
    public List<LatLng> ListPoints;
    public String IMEI;
    public String carNumber;
    public int startTime;
    public int endTime;

    public void setListPoints(List<LatLng> listPoints) {
        this.ListPoints = listPoints;
    }

    public List<LatLng> getListPoints() {
        return ListPoints;
    }

    public void setImei(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getEndTime() {
        return endTime;
    }
}
