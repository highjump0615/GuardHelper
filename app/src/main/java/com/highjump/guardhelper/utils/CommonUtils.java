package com.highjump.guardhelper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.highjump.guardhelper.api.API_Manager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2016/7/23.
 */
public class CommonUtils {

    // 上报位置时间间隔
    public static int mnLocationInterval = Config.LOCATION_INTERVAL_BEFORE_SIGN;

    // 当前位置
    public static Location mCurrentLocation = null;

    // 获取经度
    public static double getLongitude() {
        double dLongitude = 0;

        if (mCurrentLocation != null) {
            dLongitude = mCurrentLocation.getLongitude();
        }

        return dLongitude;
    }

    // 获取纬度
    public static double getLatitude() {
        double dLatitude = 0;

        if (mCurrentLocation != null) {
            dLatitude = mCurrentLocation.getLatitude();
        }

        return dLatitude;
    }

    /**
     * 获取md5加密字符串
     * @param encTarget - 原码字符串
     * @return - 加密字符串
     */
    public static String getMD5EncryptedString(String encTarget) {
        return getMD5EncryptedString(encTarget.getBytes(), encTarget.length());
    }

    public static String getMD5EncryptedString(byte[] data, int length) {
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
            return null;
        }

        // Encryption algorithm
        mdEnc.update(data, 0, length);
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    /**
     * 创建对话框
     */
    public static Dialog createErrorAlertDialog(final Context context, String message) {
        return createErrorAlertDialog(context, "", message);
    }

    /**
     * 创建对话框
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null).create();
    }

    /**
     * 跳转到指定的activity
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass, boolean removeSource) {
        Intent intent = new Intent(source, destinationClass);

//        if (removeSource) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        }

        source.startActivity(intent);

        if (removeSource) {
            source.finish();
        }
    }
}
