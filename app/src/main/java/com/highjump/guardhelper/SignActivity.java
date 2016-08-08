package com.highjump.guardhelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class SignActivity extends AppCompatActivity implements View.OnClickListener {

    // 当前用户
    private UserData mCurrentUser;

    // 楼层
    private EditText mEditStorey;

    // 类别
    private RadioButton mRadioStreet;
    private RadioButton mRadioHigh;

    // 楼层对话框
    AlertDialog mDialogStorey;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

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

        // 单选框
        mRadioStreet = (RadioButton) findViewById(R.id.rad_street);
        mRadioHigh = (RadioButton) findViewById(R.id.rad_high);

        // 默认是街道签到
        mRadioStreet.setChecked(true);

        Button button = (Button) findViewById(R.id.but_sign);
        button.setOnClickListener(this);

        // 楼层输入框
        mEditStorey = (EditText) findViewById(R.id.edit_storey);
        // 阻止弹出键盘
        mEditStorey.setInputType(InputType.TYPE_NULL);

        // 点击这个edit, 弹出对话框
        mEditStorey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogStorey.show();
            }
        });

        // 选择楼层对话框
        final NumberPicker mPicker = new NumberPicker(this);
        mPicker.setMinValue(1);
        mPicker.setMaxValue(100);

        mDialogStorey = new AlertDialog.Builder(this)
                .setTitle("请选择楼层")
                .setView(mPicker)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 设置楼层输入框数值
                        mEditStorey.setText("" + mPicker.getValue());
                    }
                })
                .create();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_sign:
                doSign();
                break;

            default:
                break;
        }
    }

    /**
     * 签到
     */
    private void doSign() {

        // 如果正在签到，则退出
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "正在签到...");

        // 调用相应的API
        API_Manager.getInstance().signArrival(
                mCurrentUser,
                new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        mProgressDialog.dismiss();

                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.createErrorAlertDialog(SignActivity.this, Config.STR_CONNET_FAIL, e.getMessage()).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        mProgressDialog.dismiss();

                        // 获取返回数据
                        final ApiResult resultObj = new ApiResult(response.body().string());

                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (Integer.parseInt(resultObj.getResult()) < 1) {
                                        CommonUtils.createErrorAlertDialog(SignActivity.this, "签到失败！").show();
                                        return;
                                    }

                                    // 更新定位时间间隔
                                    CommonUtils.mnLocationInterval = Config.LOCATION_INTERVAL_AFTER_SIGN;

                                    // 返回主页面
                                    onBackPressed();
                                }
                                catch (Exception e) {
                                    // 解析失败
                                    CommonUtils.createErrorAlertDialog(SignActivity.this, Config.STR_PARSE_FAIL, e.getMessage()).show();
                                }
                            }
                        });
                    }
                });
    }

}
