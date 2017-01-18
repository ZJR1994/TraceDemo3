package com.app.zjr.tracedemo3.data;

import cn.bmob.v3.BmobObject;

/**
 * Created by ZJR on 2016/10/31.
 */
public class Cars extends BmobObject {
    private String CarNumber;
    private String IMEI;

    public void setCarNumber(String carNumber) {
        this.CarNumber = carNumber;
    }

    public String getCarNumber() {
        return CarNumber;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMEI() {
        return IMEI;
    }

}

