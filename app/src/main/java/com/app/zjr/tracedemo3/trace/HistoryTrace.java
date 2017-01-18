package com.app.zjr.tracedemo3.trace;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.app.zjr.tracedemo3.R;
import com.app.zjr.tracedemo3.activity.MainActivity;
import com.app.zjr.tracedemo3.data.HistoryTrackData;
import com.app.zjr.tracedemo3.data.Traces;
import com.app.zjr.tracedemo3.service.GsonService;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.OnTrackListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by ZJR on 2016/10/19.
 */
public class HistoryTrace {
    //开始时间
    public static int startTime = 0;
    //结束时间
    public static int endTime = 0;

    // 起点图标
    private static BitmapDescriptor bmStart;
    // 终点图标
    private static BitmapDescriptor bmEnd;

    // 起点图标覆盖物
    public static MarkerOptions startMarker = null;
    // 终点图标覆盖物
    private static MarkerOptions endMarker = null;
    // 路线覆盖物
    public static PolylineOptions polyline, playPolyline = null;

    public static MarkerOptions markerOptions = null;

    private Polyline mVirtureRoad;
    private Marker mMoveMarker;
    private Handler mHandler;

    public Thread moveThread;
    public static boolean canMove = true;
    public static boolean canPlay = true;

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 5;
    private static final double DISTANCE = 0.0001;
    /**
     * Track监听器
     */
    protected static OnTrackListener trackListener, upLoadTraceListener, playTraceListener = null;

    private MapStatusUpdate msUpdate = null;

    /**
     * 查询历史轨迹
     */
    public void queryHistoryTrace(int processed, String processOption) {
        int simpleReturn = 0; //是否返回精简结果（0：否，1：是）
        int isProcessed = processed; //是否返回纠偏轨迹（0：否，1：是）
        //开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        //结束时间
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        MainActivity.client.setOnTrackListener(trackListener);
        MainActivity.client.queryHistoryTrack(MainActivity.serviceId, MainActivity.entityName, simpleReturn,
                isProcessed, processOption, startTime, endTime, pageSize, pageIndex, trackListener);
    }

    /**
     * 上传历史轨迹
     */
    public void upLoadHistoryTrace(int processed, String processOption) {
        int simpleReturn = 0; //是否返回精简结果（0：否，1：是）
        int isProcessed = processed; //是否返回纠偏轨迹（0：否，1：是）
        //开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        //结束时间
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        MainActivity.client.setOnTrackListener(upLoadTraceListener);
        MainActivity.client.queryHistoryTrack(MainActivity.serviceId, MainActivity.entityName, simpleReturn,
                isProcessed, processOption, startTime, endTime, pageSize, pageIndex, upLoadTraceListener);
    }

    /**
     * 轨迹回放
     */
    public void playhistoryTrace(int processed, String processOption) {
        int simpleReturn = 0; //是否返回精简结果（0：否，1：是）
        int isProcessed = processed; //是否返回纠偏轨迹（0：否，1：是）
        //开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        //结束时间
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        //分页大小
        int pageSize = 1000;
        //分页索引
        int pageIndex = 1;
        MainActivity.client.setOnTrackListener(playTraceListener);
        MainActivity.client.queryHistoryTrack(MainActivity.serviceId, MainActivity.entityName, simpleReturn,
                isProcessed, processOption, startTime, endTime, pageSize, pageIndex, playTraceListener);
    }

    /**
     * 查询里程
     */
    private void queryDistance(int processed, String processOption) {

        // 是否返回纠偏后轨迹（0 : 否，1 : 是）
        int isProcessed = processed;

        // 里程补充
        String supplementMode = "driving";

        //开始时间
        if (startTime == 0) {
            startTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        }
        //结束时间
        if (endTime == 0) {
            endTime = (int) (System.currentTimeMillis() / 1000);
        }
        MainActivity.client.queryDistance(MainActivity.serviceId, MainActivity.entityName, isProcessed,
                processOption, supplementMode, startTime, endTime, trackListener);
    }

    public void initOnTraceListener() {
        trackListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Looper.prepare();
                Toast.makeText(MainActivity.myContext, "轨迹查询请求失败回调信息：" + s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                showHistoryTrace(s);
            }

            @Override
            public void onQueryDistanceCallback(String s) {

            }

            @Override
            public Map onTrackAttrCallback() {
                return super.onTrackAttrCallback();
            }
        };
        upLoadTraceListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Looper.prepare();
                Toast.makeText(MainActivity.myContext, "轨迹上传失败回调信息：" + s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                doUpLoadHistoryTrace(s);
            }

        };
        playTraceListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Looper.prepare();
                Toast.makeText(MainActivity.myContext, "轨迹播放失败回调信息：" + s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                historyTraceMove(s);
            }

        };
    }

    /**
     * 显示历史轨迹
     */
    private void showHistoryTrace(String historyTrace) {

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrace, HistoryTrackData.class);
        List<LatLng> latLngList = new ArrayList<>();

        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }
            //绘制历史轨迹
            drawHistoryTrace(latLngList, historyTrackData.distance);
        }
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 轨迹回放
     *
     * @param s
     */
    private void historyTraceMove(String s) {
        HistoryTrackData historyTrackData = GsonService.parseJson(s, HistoryTrackData.class);
        final List<LatLng> latLngList = new ArrayList<>();

        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }
            playPolyline = new PolylineOptions().points(latLngList).width(10).color(Color.RED);
            mVirtureRoad = (Polyline) MainActivity.baiduMap.addOverlay(playPolyline);
            markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                    .fromResource(R.mipmap.car)).position(latLngList.get(latLngList.size() - 1)).zIndex(9);
            mMoveMarker = (Marker) MainActivity.baiduMap.addOverlay(markerOptions);
            moveThread = new Thread() {
                int i = mVirtureRoad.getPoints().size() - 1;

                public void run() {

                    while (canMove) {
                        for (i = (mVirtureRoad.getPoints().size() - 1); i >= 1; i--) {
                            final LatLng startPoint = mVirtureRoad.getPoints().get(i);
                            final LatLng endPoint = mVirtureRoad.getPoints().get(i - 1);
                            if (i == 1) {
                                canMove = false;
                                canPlay = true;
                                this.interrupt();
                            }
                            mMoveMarker
                                    .setPosition(startPoint);
                            double slope = getSlope(startPoint, endPoint);
                            //是不是正向的标示（向上设为正向）
                            boolean isReverse = (startPoint.latitude > endPoint.latitude);

                            double intercept = getInterception(slope, startPoint);

                            double xMoveDistance = isReverse ? getXMoveDistance(slope)
                                    : -1 * getXMoveDistance(slope);


                            for (double j = startPoint.latitude;
                                 !((j > endPoint.latitude) ^ isReverse);

                                 j = j
                                         - xMoveDistance) {
                                LatLng latLng = null;
                                if (slope != Double.MAX_VALUE) {
                                    latLng = new LatLng(j, (j - intercept) / slope);
                                } else {
                                    latLng = new LatLng(j, startPoint.longitude);
                                }

                                final LatLng finalLatLng = latLng;
                                mHandler = new Handler(Looper.getMainLooper());
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (MainActivity.mapView == null) {
                                            return;
                                        }
                                        // refresh marker's position
                                        mMoveMarker.setPosition(finalLatLng);
                                    }
                                });
                                try {
                                    Thread.sleep(TIME_INTERVAL);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }

            };
            moveThread.start();
        }
    }

    /**
     * 上传历史轨迹
     */

    private static void doUpLoadHistoryTrace(String historyTrace) {

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrace, HistoryTrackData.class);
        List<LatLng> latLngList = new ArrayList<>();

        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }
        }
        //存储轨迹数据到Bmob数据库的Traces表
        Traces traces = new Traces();
        traces.setListPoints(latLngList);
        traces.setCarNumber(MainActivity.entytv.getText().toString());
        traces.setStartTime(startTime);
        traces.setEndTime(endTime);
        traces.setImei(MainActivity.getImei(MainActivity.myContext));
        if (latLngList != null && latLngList.size() != 0) {
            Looper.getMainLooper();
            traces.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        Toast.makeText(MainActivity.myContext, "轨迹数据上传成功",
                                Toast.LENGTH_SHORT).show();
                    } else if (e.getErrorCode() == 401) {
                        Toast.makeText(MainActivity.myContext, "这个轨迹数据已经上传过了！",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.myContext, "轨迹数据上传失败!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(MainActivity.myContext, "轨迹数据为空！",
                    Toast.LENGTH_SHORT).show();
        }
        Looper.loop();
    }

    /**
     * 绘制历史轨迹
     */
    private void drawHistoryTrace(final List<LatLng> points, final double distance) {

        //绘制新覆盖物之前，清空地图上的所有覆盖物
        MainActivity.baiduMap.clear();
        if (points.size() == 1) {
            points.add(points.get(0));
        }
        if (points.size() == 0 || points == null) {
            Looper.prepare();
            Toast.makeText(MainActivity.myContext, "当前查无轨迹点！", Toast.LENGTH_SHORT).show();
            Looper.loop();
            resetMarker();
        } else if (points.size() > 1) {
            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder().include(llC).include(llD).build();
            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
            bmStart = BitmapDescriptorFactory.fromResource(R.mipmap.icon_start);
            bmEnd = BitmapDescriptorFactory.fromResource(R.mipmap.icon_end);
            //添加起点图标
            startMarker = new MarkerOptions().position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);
            //添加终点图标
            endMarker = new MarkerOptions().position(points.get(0)).icon(bmEnd)
                    .zIndex(9).draggable(true);
            //添加轨迹
            polyline = new PolylineOptions().width(10).color(Color.RED)
                    .points(points);
            markerOptions = new MarkerOptions();
            markerOptions.flat(true);
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.icon(BitmapDescriptorFactory
                    .fromResource(R.mipmap.icon_gcoding));
            markerOptions.position(points.get(points.size() - 1));
            addMarker();
            Looper.prepare();
            Toast.makeText(MainActivity.myContext, "当前的轨迹里程为：" + (int) distance / 1000 + "公里",
                    Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    private void addMarker() {
        if (msUpdate != null) {
            MainActivity.baiduMap.animateMapStatus(msUpdate, 2000);
        }
        if (startMarker != null) {
            MainActivity.baiduMap.addOverlay(startMarker);
        }
        if (endMarker != null) {
            MainActivity.baiduMap.addOverlay(endMarker);
        }
        if (polyline != null) {
            MainActivity.baiduMap.addOverlay(polyline);
        }
    }

    public void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

    public void cleanMap() {
        MainActivity.baiduMap.clear();
    }

}
