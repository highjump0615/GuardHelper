package com.highjump.guardhelper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2016/7/23.
 */
public class CommonUtils {

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

        if (removeSource) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        source.startActivity(intent);

        if (removeSource) {
            source.finish();
        }
    }
}
