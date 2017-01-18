package com.app.zjr.tracedemo3.data;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * Created by ZJR on 2016/12/7.
 */
public class EntityListData {
    public int status; // 状态码，0为成功
    public int size; // 返回结果条数，该页返回了几条数据
    public int total; // 符合条件结果条数，一共有几条符合条件的数据
    public List<Entities> entities;
    public String message; // 响应信息,对status的中文描述

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private class Entities {
        public String create_time; // 创建时间 格式化时间 该时间为服务端时间
        public String modify_time; // 修改时间
        public RealTimePoint realtime_point; // 实时轨迹信息

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getModify_time() {
            return modify_time;
        }

        public void setModify_time(String modify_time) {
            this.modify_time = modify_time;
        }

        public RealTimePoint getRealtime_point() {
            return realtime_point;
        }

        public void setRealtime_point(RealTimePoint realtime_point) {
            this.realtime_point = realtime_point;
        }

        public class RealTimePoint {
            public List<Double> location;// 经纬度 Array 百度加密坐标
            public String loc_time;// 该track实时点的上传时间 UNIX时间戳 该时间为用户上传的时间

            public List<Double> getLocation() {
                return location;
            }

            public void setLocation(List<Double> location) {
                this.location = location;
            }

            public String getLoc_time() {
                return loc_time;
            }

            public void setLoc_time(String loc_time) {
                this.loc_time = loc_time;
            }

        }
    }

    public LatLng getRealtimePoint() {

        if (entities == null || entities.get(0) == null || entities.get(0).realtime_point == null) {
            return null;
        }
        List<Double> location = entities.get(0).realtime_point.location;
        if (Math.abs(location.get(0) - 0.0) < 0.01 && Math.abs(location.get(1) - 0.0) < 0.01) {
            return null;
        } else {
            LatLng latLng = new LatLng(location.get(1), location.get(0));
            return latLng;
        }
    }
}
