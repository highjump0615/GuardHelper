package com.highjump.guardhelper.model;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/7/28.
 */
public class ReportData {

    // 状态
    public static int STATUS_INPROGRESS = 0;
    public static int STATUS_SUCCESS = 1;
    public static int STATUS_FAILED = 2;
    private int mnStatus = STATUS_INPROGRESS;

    public static int REPORT_TEXT = 0;
    public static int REPORT_IMAGE = 1;
    public static int REPORT_VIDEO = 2;

    private int mnType;
    private Date mDate;

    // 0 - 上报信息, 1 - 下发信息
    private int mnSend = 0;

    private String mstrData;
    private Bitmap mBitmap;

    // 图片属性
    private int mnWidth;
    private int mnHeight;

    /**
     * constructor
     * @param data : 文字信息
     * @param receive - 0:上报, 1:获取
     */
    public ReportData(String data, int receive) {
        mstrData = data;
        mnType = REPORT_TEXT;
        mnSend = receive;

        // 当前时间
        mDate = new Date();
    }

    /**
     * Constructor
     * @param data - 图片信息
     * @param receive - 0:上报, 1:获取
     */
    public ReportData(Bitmap data, int nWidth, int nHeight, String videoPath, int receive) {
        mBitmap = data;

        mnWidth = nWidth;
        mnHeight = nHeight;

        mnSend = receive;

        // 根据videoPath来判断是图片或视频
        if (TextUtils.isEmpty(videoPath)) {
            mnType = REPORT_IMAGE;
        }
        else {
            mnType = REPORT_VIDEO;
            mstrData = videoPath;
        }

        // 当前时间
        mDate = new Date();
    }

    // get / set
    public int isReceive() {
        return  mnSend;
    }

    public String getStringData() {
        return mstrData;
    }

    public Bitmap getBitmapData() {
        return mBitmap;
    }

    public int getWidth() {
        return mnWidth;
    }

    public int getHeight() {
        return mnHeight;
    }

    public int getType() {
        return mnType;
    }

    public int getStatus() {
        return mnStatus;
    }

    public void setStatus(int value) {
        mnStatus = value;
    }

    public String getTime() {
        // 时间格式化 - 换成北京时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String strTime = format.format(mDate);

        return strTime;
    }
}
