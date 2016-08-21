package com.highjump.guardhelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.highjump.guardhelper.utils.CommonUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/13.
 */
public class GHApplication extends Application {

    private static final String TAG = GHApplication.class.getSimpleName();

    // 当前Activity
    private Activity mCurrentActivity = null;
    private ArrayList<Activity> maryActivity = new ArrayList<Activity>();

    // get / set
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                // 添加到activity列表
                maryActivity.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                // 通过这个就是个当前Activity
                mCurrentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mCurrentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // 添加到activity列表
                maryActivity.remove(activity);
            }
        });
    }

    /**
     * 退出app
     */
    public void finishApp() {
        for (Activity activity : maryActivity) {
            activity.finish();
        }
    }

    /**
     * 当前activity上创建对话框
     */
    public Dialog createCommonAlertDialg(String title, String message, DialogInterface.OnClickListener clickResponse) {
        return new AlertDialog.Builder(mCurrentActivity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, clickResponse)
                .create();
    }
}
