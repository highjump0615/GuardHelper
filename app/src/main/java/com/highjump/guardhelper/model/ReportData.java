package com.highjump.guardhelper.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/7/28.
 */
public class ReportData {

    public static int REPORT_TEXT = 0;
    public static int REPORT_IMAGE = 1;
    public static int REPORT_VIDEO = 2;

    private int mnType;
    private Date mDate;

    // 0 - 上报信息, 1 - 下发信息
    private int mnSend = 0;

    private String mstrData;

    /**
     * constructor
     * @param data
     * @param type
     * @param receive - 0:上报, 1:获取
     */
    public ReportData(String data, int type, int receive) {
        mstrData = data;
        mnType = type;
        mnSend = receive;

        // 当前时间
        mDate = new Date();
    }

    // get / set
    public int isReceive() {
        return  mnSend;
    }

    public String getData() {
        return mstrData;
    }

    public int getType() {
        return mnType;
    }

    public String getTime() {
        // 时间格式化 - 换成北京时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String strTime = format.format(mDate);

        return strTime;
    }
}
