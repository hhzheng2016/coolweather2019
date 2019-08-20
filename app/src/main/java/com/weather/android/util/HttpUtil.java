package com.weather.android.util;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    /**
     *
     * @param address 请求地址
     * @param callback 回调处理响应
     */
    public static void sendOkHttpRequest(String address, Callback callback) {
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        Log.d(TAG, "sendOkHttpRequest: request= "+ request.toString());
        client.newCall(request).enqueue(callback);
    }
}
