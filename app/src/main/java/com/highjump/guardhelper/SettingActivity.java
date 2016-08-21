package com.highjump.guardhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    // 当前用户
    private UserData mCurrentUser;

    private EditText mEditDataAddr;
    private EditText mEditOrderAddr;
    private EditText mEditLocationAddr;

    private EditText mEditIntevalOrder;
    private EditText mEditIntevalLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 获取当前用户
        mCurrentUser = UserData.currentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置back图标
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // 点击back图标
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 警号TextView
        TextView textUsername = (TextView) toolbar.findViewById(R.id.text_tb_username);
        textUsername.setText("警号：" + mCurrentUser.getUsername());

        // 输入框
        mEditDataAddr = (EditText)findViewById(R.id.edit_uploadaddr);
        mEditOrderAddr = (EditText)findViewById(R.id.edit_orderaddr);
        mEditLocationAddr = (EditText)findViewById(R.id.edit_locationaddr);
        mEditIntevalOrder = (EditText)findViewById(R.id.edit_orderinterval);
        mEditIntevalLocation = (EditText)findViewById(R.id.edit_locationinterval);

        mEditDataAddr.setText(API_Manager.getDataApiPath());
        mEditOrderAddr.setText(API_Manager.getOrderApiPath());
        mEditLocationAddr.setText(API_Manager.getLocationApiPath());
        mEditIntevalOrder.setText("" + Config.QUERY_ORDER_INTERVAL);
        mEditIntevalLocation.setText("" + Config.LOCATION_INTERVAL);

        // 确定按钮
        Button button = (Button) findViewById(R.id.but_set);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_set:
                doSetting();
                break;

            default:
                break;
        }
    }

    /**
     * 设置参数
     */
    private void doSetting() {
        String strDataAddr = mEditDataAddr.getText().toString();
        String strOrderAddr = mEditOrderAddr.getText().toString();
        String strLocationAddr = mEditLocationAddr.getText().toString();

        String strIntervalOrder = mEditIntevalOrder.getText().toString();
        String strIntervalLocation = mEditIntevalLocation.getText().toString();

        // 检查输入是否合适
        if (TextUtils.isEmpty(strDataAddr)) {
            CommonUtils.createErrorAlertDialog(this, "请输入上报数据地址").show();
            return;
        }
        if (TextUtils.isEmpty(strOrderAddr)) {
            CommonUtils.createErrorAlertDialog(this, "请输入请求命令地址").show();
            return;
        }
        if (TextUtils.isEmpty(strLocationAddr)) {
            CommonUtils.createErrorAlertDialog(this, "请输入上报定位地址").show();
            return;
        }

        if (TextUtils.isEmpty(strIntervalOrder)) {
            CommonUtils.createErrorAlertDialog(this, "请输入获取命令周期").show();
            return;
        }
        if (TextUtils.isEmpty(strIntervalLocation)) {
            CommonUtils.createErrorAlertDialog(this, "请输入上报定位周期").show();
            return;
        }

        API_Manager.setApiPath(strDataAddr, strOrderAddr, strLocationAddr);

        Config.QUERY_ORDER_INTERVAL = Integer.parseInt(strIntervalOrder);
        Config.LOCATION_INTERVAL = Integer.parseInt(strIntervalLocation);

        // Save user phone and flag of signed into NSUserDefaults
        SharedPreferences preferences = getSharedPreferences(Config.PREF_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Config.PREF_URL_DATA, strDataAddr);
        editor.putString(Config.PREF_URL_ORDER, strOrderAddr);
        editor.putString(Config.PREF_URL_LOCATION, strLocationAddr);

        editor.putInt(Config.PREF_INTERVAL_QUERYORDER, Config.QUERY_ORDER_INTERVAL);
        editor.putInt(Config.PREF_INTERVAL_LOCATION, Config.LOCATION_INTERVAL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        }).start();

        // 返回主页面
        onBackPressed();
    }

}
