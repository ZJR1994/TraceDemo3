package com.app.zjr.tracedemo3.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.zjr.tracedemo3.R;
import com.app.zjr.tracedemo3.data.Cars;
import com.app.zjr.tracedemo3.data.EntityListData;
import com.app.zjr.tracedemo3.service.GsonService;
import com.app.zjr.tracedemo3.service.MonitorService;
import com.app.zjr.tracedemo3.trace.HistoryTrace;
import com.app.zjr.tracedemo3.utils.DateDialog;
import com.app.zjr.tracedemo3.utils.DateUtils;
import com.app.zjr.tracedemo3.utils.ListDialog;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class MainActivity extends AppCompatActivity {
    int gatherInterval = 5; //坐标采集周期
    int packInterval = 15; //坐标打包周期
    int protocolType = 1; //http协议类型（1：http，2：https）
    public static String entityName; //声明设备标识
    public static long serviceId = 126488; //初始化轨迹服务id
    protected int traceType = 2; //轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
    protected boolean isTraceStart = false;
    public static Context myContext; //声明一个公用的上下文
    public static MapView mapView = null;
    public LinearLayout controlBar;
    public Button traceControl, datepic, queryEntityList_btn, changeMap, upload = null;
    public static TextView entytv;
    public ImageButton loc, play = null;
    public static BaiduMap baiduMap = null;
    public static Trace trace; //实例化一个轨迹服务
    public static LBSTraceClient client; //实例化一个轨迹服务客户端
    public static OnEntityListener entityListener = null; //声明Entity监听器
    public static OnStartTraceListener startTraceListener; //启动轨迹服务监听器
    public static OnStopTraceListener stopTraceListener; //停止轨迹服务监听器

    protected static RefreshThread refreshThread = null;  //刷新地图线程以获取实时点
    private static MapStatusUpdate msUpdate = null;
    private Intent serviceIntent;
    private static BitmapDescriptor realtimeBitmap;  //图标
    private static Overlay overlay; //覆盖物基类
    private static OverlayOptions overlayOptions;  //覆盖物选型基类
    private static List<LatLng> pointList = new ArrayList<LatLng>();  //定位点的集合
    private static PolylineOptions polyline = null;  //路线覆盖物选项类

    /**
     * 要查询的轨迹的年月日
     */
    private int year = 0;
    private int month = 0;
    private int day = 0;

    private HistoryTrace historyTrace = new HistoryTrace();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myContext = getApplicationContext();
        SDKInitializer.initialize(myContext);
        setContentView(R.layout.activity_main);
        Bmob.initialize(MainActivity.this, "0d11cff7c59ff8431b55afa9980e5889");
        init();
        initListener();
        initOnEntityListener();
        historyTrace.initOnTraceListener();
        startRefreshThread(true);
        traceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MonitorService.isRunning) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("是否开启轨迹追踪");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(myContext, "轨迹服务正在开启...", Toast.LENGTH_LONG).show();
                            startTrace();
                        }
                    });
                    builder.create().show();
                } else {
                    final AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setTitle("是否停止轨迹追踪");
                    builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(myContext, "轨迹服务正在关闭...", Toast.LENGTH_LONG).show();
                            stopTrace();
                        }
                    });
                    builder2.create().show();
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MonitorService.isRunning == false) {
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(MainActivity.this);
                    builder3.setTitle("是否上传轨迹数据");
                    builder3.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder3.setPositiveButton("上传", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String processOption = "need_denoise=0,need_vacuate=0,need_mapmatch=1";
                            historyTrace.upLoadHistoryTrace(1, processOption);
                        }
                    });
                    builder3.create().show();
                }
            }
        });
        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyTrace.cleanMap();
                historyTrace.resetMarker();
                queryRealtimeLoc();
                if (refreshThread != null) {
                    startRefreshThread(true);
                }
                baiduMap.animateMapStatus(msUpdate);
                traceControl.setVisibility(View.VISIBLE);
                upload.setVisibility(View.GONE);
                controlBar.setVisibility(View.GONE);
                play.setBackgroundResource(R.drawable.play);
            }
        });
        //轨迹回放控制按钮事件处理
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (historyTrace.canPlay == true) {
                    play.setBackgroundResource(R.drawable.wait);
                    historyTrace.canPlay = false;
                    historyTrace.canMove = true;
                    String processOption = "need_denoise=0,need_vacuate=0,need_mapmatch=1";
                    historyTrace.playhistoryTrace(1, processOption);
                } else if (historyTrace.canPlay == false) {
                    play.setBackgroundResource(R.drawable.play);
                    historyTrace.canMove = false;
                    if (historyTrace.moveThread.isAlive()) {
                        historyTrace.moveThread.interrupt();
                    }
                    historyTrace.playhistoryTrace(0, null);
                }
            }
        });
        datepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryTrace();
            }
        });
        queryEntityList_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialog listDialog = new ListDialog(MainActivity.this);
                listDialog.getBuilder();
            }
        });
        changeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] maps = new String[]{"普通地图", "卫星地图", "交通图(点击开关交通图)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("地图切换");
                builder.setItems(maps, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                if (!baiduMap.isTrafficEnabled()) {
                                    baiduMap.setTrafficEnabled(true);
                                } else {
                                    baiduMap.setTrafficEnabled(false);
                                }
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        initCar();
    }

    /**
     * 登记车辆信息
     * 查询数据库是否已记录定位设备的IMEI号
     * 未记录IMEI号则弹出对话框填写一个车牌号码标识
     * IMEI号与车牌号一起存入数据库作为车辆唯一标识
     */
    private void initCar() {
        //新建一个查询
        BmobQuery<Cars> query = new BmobQuery<>();
        //设置查询条件为等于该设备的IMEI号
        query.addWhereEqualTo("IMEI", getImei(myContext));
        //执行查询
        query.findObjects(new FindListener<Cars>() {
            @Override
            public void done(List<Cars> list, BmobException e) {
                if (e == null) {
                    //如果IMEI号已被登记，提示对应的车牌号码
                    if (list.get(0).getIMEI() != null) {
                        Toast.makeText(MainActivity.this, "当前车辆为：" + list.get(0).getCarNumber(), Toast.LENGTH_SHORT).show();
                    } else { //否则弹出车牌号码填写对话框
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("请设置您的车牌号");
                        final LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View view = inflater.inflate(R.layout.carnumber_edt, null);
                        builder.setView(view);
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MainActivity.this.finish();
                            }
                        });
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText carNumber_edt = (EditText) view.findViewById(R.id.carnumber);
                                final String carNumber = carNumber_edt.getText().toString();
                                //新建一个车辆对象
                                final Cars car = new Cars();
                                //设置IMEI号
                                car.setIMEI(getImei(myContext));
                                //设置车牌号
                                car.setCarNumber(carNumber);
                                //执行存储
                                car.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e == null) {
                                            //初次打开应用车辆信息为空，设置输入的车牌号为entity标识
                                            entityName = carNumber;
                                            trace = new Trace(myContext, serviceId, entityName,
                                                    traceType);
                                            Toast.makeText(MainActivity.this, "成功存储车辆：" + car.getCarNumber(), Toast.LENGTH_SHORT).show();
                                            entytv.setText(carNumber);
                                        } else {
                                            if (e.getErrorCode() == 401) {
                                                Toast.makeText(MainActivity.this, "存储车辆失败,该IMEI号已登记！",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        builder.create().show();
                        Toast.makeText(MainActivity.this, "请为该设备设置一个车牌号！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //车辆列表为空异常处理，逻辑与IMEI号未被登记执行的逻辑相同
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("请设置您的车牌号");
                    final LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = inflater.inflate(R.layout.carnumber_edt, null);
                    builder.setView(view);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MainActivity.this.finish();
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText carNumber_edt = (EditText) view.findViewById(R.id.carnumber);
                            final String carNumber = carNumber_edt.getText().toString();
                            final Cars car = new Cars();
                            car.setIMEI(getImei(myContext));
                            car.setCarNumber(carNumber);
                            car.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e == null) {
                                        //初次打开应用车辆信息为空，设置输入的车牌号为entity标识
                                        entityName = carNumber;
                                        trace = new Trace(myContext, serviceId, entityName,
                                                traceType);
                                        Toast.makeText(MainActivity.this, "成功存储车辆：" + car.getCarNumber(), Toast.LENGTH_SHORT).show();
                                        entytv.setText(carNumber);
                                    } else {
                                        if (e.getErrorCode() == 401) {
                                            Toast.makeText(MainActivity.this, "存储车辆失败,该IMEI号已登记！",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    });
                    builder.create().show();
                    Toast.makeText(MainActivity.this, "请为该设备设置一个车牌号！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 初始化各个参数
     */
    private void init() {
        queryEntityList_btn = (Button) findViewById(R.id.entityList);
        controlBar = (LinearLayout) findViewById(R.id.controlBar);
        traceControl = (Button) findViewById(R.id.traceControl);
        upload = (Button) findViewById(R.id.upload);
        datepic = (Button) findViewById(R.id.date);
        changeMap = (Button) findViewById(R.id.changemap);
        loc = (ImageButton) findViewById(R.id.loc);
        play = (ImageButton) findViewById(R.id.play);
        mapView = (MapView) findViewById(R.id.map);
        entytv = (TextView) findViewById(R.id.entityname);
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
        mapView.removeViewAt(1);
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));
        //查询并初始化entityName为CarNumber
        BmobQuery<Cars> query = new BmobQuery<>();
        query.addWhereEqualTo("IMEI", getImei(myContext));
        query.findObjects(new FindListener<Cars>() {

            @Override
            public void done(List<Cars> list, BmobException e) {
                if (e == null) {
                    entityName = list.get(0).getCarNumber();
                    trace = new Trace(myContext, serviceId, entityName, traceType);
                    entytv.setText(entityName);
                } else {
                    Toast.makeText(MainActivity.this, "车辆列表为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        entityName = getImei(myContext);
        client = new LBSTraceClient(myContext);
        client.setInterval(gatherInterval, packInterval);
        client.setLocationMode(LocationMode.High_Accuracy);
        client.setProtocolType(protocolType);
        trace = new Trace(myContext, serviceId, entityName, traceType);
    }

    /**
     * 初始化entity状态监听器
     */
    private void initOnEntityListener() {
        //实体状态监听器
        entityListener = new OnEntityListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                Looper.prepare();
                Toast.makeText(myContext, "entity请求失败回调消息：" + s, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onQueryEntityListCallback(String s) {
                EntityListData entityListData = GsonService.parseJson(s, EntityListData.class);
                drawRealtimePoint(entityListData.getRealtimePoint());
                Log.i("结果：", s);
            }

            @Override
            public void onReceiveLocation(TraceLocation traceLocation) {
                LatLng latLng = new LatLng(traceLocation.getLatitude(), traceLocation.getLongitude());
                if (MonitorService.isRunning && MonitorService.isCheck) {
                    showRealtimeTrack(traceLocation);
                } else {
                    drawRealtimePoint(latLng);
                }
            }
        };
    }

    /**
     * 刷新地图线程（获取实时点）
     */
    protected class RefreshThread extends Thread {
        protected boolean isRefresh = true;

        @Override
        public void run() {
            Looper.prepare();
            while (isRefresh == true) {
                //查询实时位置
                queryRealtimeLoc();
                try {
                    Thread.sleep(gatherInterval * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("线程休眠失败");
                }
            }
            Looper.loop();
        }
    }

    /**
     * 启动刷新地图线程
     *
     * @param isStart
     */
    protected void startRefreshThread(boolean isStart) {
        if (refreshThread == null) {
            refreshThread = new RefreshThread();
        }
        refreshThread.isRefresh = isStart;
        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }
    }

    public static void stopRefreshThread() {
        if (refreshThread != null) {
            refreshThread.isRefresh = false;
        }
//        if (refreshThread.isAlive()) {
//            refreshThread.interrupt();
//        }
    }

    /**
     * 开启轨迹服务
     */
    public void startTrace() {
        client.startTrace(trace, startTraceListener);
        //// TODO: 2016/10/27  
        startRefreshThread(true);
        if (!MonitorService.isRunning) {
            //开启监听服务
            MonitorService.isCheck = true;
            MonitorService.isRunning = true;
            startMonitorService();
        }
    }

    /**
     * 关闭轨迹服务
     */
    public void stopTrace() {
        client.stopTrace(trace, stopTraceListener);
        //关闭监听服务
        MonitorService.isCheck = false;
        MonitorService.isRunning = false;
        if (serviceIntent != null) {
            myContext.stopService(serviceIntent);
        }
    }

    /**
     * 开启监控服务
     */
    private void startMonitorService() {
        serviceIntent = new Intent(MainActivity.this, MonitorService.class);
        myContext.startService(serviceIntent);
    }

    /**
     * 初始化监听器
     */
    public void initListener() {
        initOnStartTraceListener();
        initOnStopTraceListener();
    }

    /**
     * 初始化开启轨迹追踪服务监听器
     */
    private void initOnStartTraceListener() {
        //初始化开启轨迹服务监听器
        startTraceListener = new OnStartTraceListener() {
            //轨迹服务开启回调接口（i：消息编码，s：消息内容）
            @Override
            public void onTraceCallback(int i, String s) {
                Toast.makeText(myContext, "轨迹服务开启回调消息：" + s, Toast.LENGTH_SHORT).show();
                if (i == 0 || i == 10006 || i == 10008 || i == 10009) {
                    isTraceStart = true;
                }
            }

            @Override
            public void onTracePushCallback(byte b, String s) {
            }
        };
    }

    /**
     * 初始化停止轨迹追踪服务监听器
     */
    private void initOnStopTraceListener() {
        stopTraceListener = new OnStopTraceListener() {
            @Override
            public void onStopTraceSuccess() {
                getMainLooper();
                Toast.makeText(myContext, "停止轨迹服务成功", Toast.LENGTH_SHORT).show();
                isTraceStart = false;
                startRefreshThread(false);
                Looper.loop();
            }

            @Override
            public void onStopTraceFailed(int i, String s) {
                getMainLooper();
                Toast.makeText(myContext, "停止轨迹服务失败回调消息：" + s, Toast.LENGTH_SHORT).show();
                startRefreshThread(false);
                Looper.loop();
            }
        };
    }

    /**
     * 查询轨迹（先选择日期，再发送请求查询该日期的轨迹）
     */
    //// TODO: 2016/10/20
    private void queryTrace() {
        int date[] = null;
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        if (year == 0 && month == 0 && day == 0) {
            String curDate = DateUtils.getCurrentDate();
            date = DateUtils.getYMDArray(curDate, "-");
        }

        if (date != null) {
            year = date[0];
            month = date[1];
            day = date[2];
        }

        DateDialog dateDialog = new DateDialog(MainActivity.this, new DateDialog.PriorityListener() {
            @Override
            public void refreshPriorityUI(String sltYear, String sltMonth, String sltDay, DateDialog.CallBack back) {
                year = Integer.parseInt(sltYear);
                month = Integer.parseInt(sltMonth);
                day = Integer.parseInt(sltDay);

                //开始时间戳
                String st = year + "年" + month + "月" + day + "日0时0分0秒";
                //结束时间戳
                String et = year + "年" + month + "月" + day + "日23时59分59秒";

                historyTrace.startTime = Integer.parseInt(DateUtils.getTimeToStamp(st));
                historyTrace.endTime = Integer.parseInt(DateUtils.getTimeToStamp(et));

                back.execute();
            }
        }, new DateDialog.CallBack() {
            @Override
            public void execute() {
                Toast.makeText(MainActivity.this, "正在查询历史轨迹，请稍后...", Toast.LENGTH_SHORT).show();
                stopRefreshThread();
                String processOption = "need_denoise=0,need_vacuate=0,need_mapmatch=1";
                historyTrace.queryHistoryTrace(1, processOption);
                traceControl.setVisibility(View.GONE);
                upload.setVisibility(View.VISIBLE);
                controlBar.setVisibility(View.VISIBLE);
            }
        }, year, month, day, width, height, "选择日期", 1);
        Window window = dateDialog.getWindow();
        window.setGravity(Gravity.CENTER); //设置日期选择对话框水平居中
        dateDialog.setCancelable(true);
        dateDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        startRefreshThread(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStart() {
        startRefreshThread(true);
        super.onStart();
    }

    /**
     * 查询实时位置
     */
    public void queryRealtimeLoc() {
        client.queryRealtimeLoc(serviceId, entityListener);
    }

    /**
     * 显示实时轨迹
     *
     * @param location
     */
    protected void showRealtimeTrack(TraceLocation location) {

        if (null == refreshThread || !refreshThread.isRefresh) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {

        } else {

            LatLng latLng = new LatLng(latitude, longitude);

            if (1 == location.getCoordType()) {
                LatLng sourceLatLng = latLng;
                CoordinateConverter converter = new
                        CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(sourceLatLng);
                latLng = converter.convert();
            }

            pointList.add(latLng);
            // 绘制实时点
            drawRealtimePoint(latLng);
        }

    }

    /**
     * 绘制实时轨迹点
     *
     * @param point
     */
    protected void drawRealtimePoint(final LatLng point) {
        if (overlay != null) {
            overlay.remove();
        }
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
        msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        if (realtimeBitmap == null) {
            realtimeBitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);
        }

        overlayOptions = new MarkerOptions().position(point)
                .icon(realtimeBitmap).zIndex(9).draggable(true);

        if (pointList.size() > 2 && pointList.size() <= 10000) {
            //绘制轨迹
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }
        addMarker();
    }

    /**
     * 添加地图覆盖物
     */
    protected void addMarker() {
        if (msUpdate != null) {
            baiduMap.animateMapStatus(msUpdate);
        }
        //线路覆盖物
        if (polyline != null) {
            baiduMap.addOverlay(polyline);
        }
        //实时点覆盖物
        if (overlayOptions != null) {
            overlay = baiduMap.addOverlay(overlayOptions);
        }
    }

    /**
     * 获取设备IMEI码
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("获取IMEI码失败");
            mImei = "NULL";
        }
        return mImei;
    }
}