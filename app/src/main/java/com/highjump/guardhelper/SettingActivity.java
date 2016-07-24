package com.highjump.guardhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.highjump.guardhelper.utils.CommonUtils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditDataAddr;
    private EditText mEditOrderAddr;

    // 上报数据地址
    private String mStrDataAddr;
    // 请求命令地址
    private String mStrOrderAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

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

        // 输入框
        mEditDataAddr = (EditText)findViewById(R.id.edit_uploadaddr);
        mEditOrderAddr = (EditText)findViewById(R.id.edit_orderaddr);

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
        mStrDataAddr = mEditDataAddr.getText().toString();
        mStrOrderAddr = mEditOrderAddr.getText().toString();

        // 检查输入是否合适
        if (TextUtils.isEmpty(mStrDataAddr)) {
            CommonUtils.createErrorAlertDialog(this, "请输入上报数据地址").show();
            return;
        }
        if (TextUtils.isEmpty(mStrOrderAddr)) {
            CommonUtils.createErrorAlertDialog(this, "请输入请求命令地址").show();
            return;
        }

        // 返回主页面
        onBackPressed();
    }

}
