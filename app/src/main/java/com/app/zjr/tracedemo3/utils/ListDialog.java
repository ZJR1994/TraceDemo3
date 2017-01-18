package com.app.zjr.tracedemo3.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.app.zjr.tracedemo3.R;
import com.app.zjr.tracedemo3.activity.MainActivity;
import com.app.zjr.tracedemo3.data.Cars;
import com.app.zjr.tracedemo3.data.HistoryTrackData;
import com.app.zjr.tracedemo3.trace.HistoryTrace;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by ZJR on 2016/10/28.
 */
public class ListDialog extends Dialog {

    public ListDialog(Context context) {
        super(context);
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    //创建一个Bmob查询对象
    BmobQuery<Cars> bmobQuery = new BmobQuery<>();
    ArrayAdapter adapter = null;

    public AlertDialog.Builder getBuilder() {
//        bmobQuery.addQueryKeys("CarNumber"); //设置指定查询列
        //执行查询
        bmobQuery.findObjects(new FindListener<Cars>() {
            @Override
            public void done(List<Cars> list, BmobException e) {
                //新建一个字符串集合
                final List<String> items = new ArrayList<String>();
                //获取所有返回的Cars对象的CarNumber值添加到items里面
                for (int i = 0; i < list.size(); i++) {
                    items.add(list.get(i).getCarNumber());
                }
                //如果没有抛出异常就填充数据弹出对话框
                if (e == null) {
                    adapter = new ArrayAdapter(MainActivity.myContext, R.layout.items, items);
                    builder.setAdapter(adapter, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.entityName = items.get(which);
                            MainActivity.entytv.setText(items.get(which));
                            Toast.makeText(MainActivity.myContext, "您选择了：" +
                                    items.get(which), Toast.LENGTH_SHORT).show();
                            MainActivity.stopRefreshThread();
                            MainActivity.client.queryEntityList(MainActivity.serviceId, items.get(which),
                                    null, 0, 0, 10, 1, MainActivity.entityListener);
                        }
                    });
                    builder.setTitle("车辆列表");
                    builder.setNegativeButton("取消", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    Toast.makeText(MainActivity.myContext, e.getMessage() + e.getErrorCode(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return builder;
    }
}
