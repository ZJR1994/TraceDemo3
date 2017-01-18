package com.app.zjr.tracedemo3.service;

import com.google.gson.Gson;

/**
 * Created by ZJR on 2016/10/14.
 */
public class GsonService {
    public static <T> T parseJson(String jsonString, Class<T> clazz) {
        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            System.out.println("解析json失败");
        }
        return t;
    }
}
