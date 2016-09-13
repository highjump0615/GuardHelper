package com.highjump.guardhelper.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/7/28.
 */
public class ReportData implements Parcelable {

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
    private Bitmap mBmpThumb;

    // 图片属性
    private int mnWidth;
    private int mnHeight;
    private Uri mUriImage;

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
     */
    public ReportData() {
    }

    /**
     * Constructor
     * @param data 图片信息
     * @param uriImage 图片Uri, 仅限于发出图片：图库和相机
     * @param receive 0:上报, 1:获取
     */
    public ReportData(Bitmap data, int nWidth, int nHeight, Uri uriImage, String videoPath, int receive) {
        mBmpThumb = data;

        mnWidth = nWidth;
        mnHeight = nHeight;
        mUriImage = uriImage;

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
        return mBmpThumb;
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

    public void setType(int type) {
        mnType = type;
    }

    public int getStatus() {
        return mnStatus;
    }

    public Uri getUriImage() {
        return mUriImage;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mstrData);
    }

    //静态的Parcelable.Creator接口
    public static final Parcelable.Creator<ReportData> CREATOR = new Creator<ReportData>() {

        //创建出类的实例，并从Parcel中获取数据进行实例化
        public ReportData createFromParcel(Parcel source) {
            ReportData rData = new ReportData();
            rData.mstrData = source.readString();

            return rData;
        }

        public ReportData[] newArray(int size) {
            return new ReportData[size];
        }

    };
}
