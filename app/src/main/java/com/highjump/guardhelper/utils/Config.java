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

    public static final String STR_CONNET_FAIL = "连接服务器失败";
    public static final String STR_PARSE_FAIL = "解析响应数据失败";

    // 定时请求命令间隔(ms)
    public static final int QUERY_ORDER_INTERVAL = 30000;
    public static final int LOCATION_INTERVAL_BEFORE_SIGN = 5000;
    public static final int LOCATION_INTERVAL_AFTER_SIGN = 3000;

    /**
     * App环境初始化
     * @param ctx
     */
    public static void initConfig(Context ctx) {

        // 获取Preference的参数
        SharedPreferences preferences = ctx.getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        API_Manager.setDataApiPath(preferences.getString(Config.PREF_URL_DATA, API_Manager.getDataApiPath()));
        API_Manager.setOrderApiPath(preferences.getString(Config.PREF_URL_ORDER, API_Manager.getOrderApiPath()));
    }
}
