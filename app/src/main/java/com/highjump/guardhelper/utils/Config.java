package com.highjump.guardhelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.highjump.guardhelper.api.API_Manager;

/**
 * Created by Administrator on 2016/7/31.
 */
public class Config {

    // SharedPreference 有关参数
    public static final String PREF_NAME = "guard_pref";
    public static final String PREF_URL_DATA = "guard_url_data";
    public static final String PREF_URL_ORDER = "guard_url_order";
    public static final String PREF_URL_LOCATION = "guard_url_location";
    public static final String PREF_INTERVAL_QUERYORDER = "guard_interval_queryorder";
    public static final String PREF_INTERVAL_LOCATION = "guard_interval_location";

    public static final String STR_CONNET_FAIL = "连接服务器失败";
    public static final String STR_PARSE_FAIL = "解析响应数据失败";

    // 请求命令周期
    public static int QUERY_ORDER_INTERVAL = 30;

    // 上报定位周期
    public static int LOCATION_INTERVAL = 3;

    // 特殊命令号
    public static final int ORDERNO_EXIT_APP = 0;
    public static final int ORDERNO_OUT_OF_RANGE = 1;

    /**
     * App环境初始化
     * @param ctx
     */
    public static void initConfig(Context ctx) {

        // 获取Preference的参数
        SharedPreferences preferences = ctx.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);

        // 服务端地址
        API_Manager.setDataApiPath(preferences.getString(Config.PREF_URL_DATA, API_Manager.getDataApiPath()));
        API_Manager.setOrderApiPath(preferences.getString(Config.PREF_URL_ORDER, API_Manager.getOrderApiPath()));
        API_Manager.setLocationApiPath(preferences.getString(Config.PREF_URL_LOCATION, API_Manager.getLocationApiPath()));

        // 周期
        int nInterval = preferences.getInt(Config.PREF_INTERVAL_QUERYORDER, Config.QUERY_ORDER_INTERVAL);
        Config.QUERY_ORDER_INTERVAL = nInterval;

        nInterval = preferences.getInt(Config.PREF_INTERVAL_LOCATION, Config.LOCATION_INTERVAL);
        Config.LOCATION_INTERVAL = nInterval;
    }
}
