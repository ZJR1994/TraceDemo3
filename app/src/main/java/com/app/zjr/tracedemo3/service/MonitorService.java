package com.app.zjr.tracedemo3.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.app.zjr.tracedemo3.activity.MainActivity;

import java.util.List;

/**
 * Created by ZJR on 2016/10/9.
 */
public class MonitorService extends Service {
    public static boolean isCheck = false;
    public static boolean isRunning = false;
    private static final String SERVICE_NAME = "com.baidu.trace.LBSTraceService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("MonitorService onStartCommand");
        new Thread() {
            @Override
            public void run() {
                while (isCheck) {
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Thread sleep failed");
                    }
                }

                if (!isServiceWork(getApplicationContext(), SERVICE_NAME)) {
                    Toast.makeText(MainActivity.myContext,"轨迹服务已停止，重启轨迹服务",Toast.LENGTH_LONG).show();
                    if (MainActivity.client != null && MainActivity.trace != null) {
                        MainActivity.client.startTrace(MainActivity.trace);
                    } else {
                        Toast.makeText(MainActivity.myContext, "轨迹服务正在运行", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *判断服务是否正在运行的方法
     * @param context
     * @param serviceName
     * @return 如果返回true表示服务正在运行，如果返回false表示服务已停止
     */
    private boolean isServiceWork(Context context, String serviceName) {
        boolean isWork = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = am.getRunningServices(80);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String name = myList.get(i).service.getClassName().toString();
            if (name.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
