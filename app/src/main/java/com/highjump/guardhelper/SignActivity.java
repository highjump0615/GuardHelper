package com.highjump.guardhelper;

import android.app.AlertDialog;
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

import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;

public class SignActivity extends AppCompatActivity implements View.OnClickListener {

    // 当前用户
    private UserData mCurrentUser;

    // 楼层
    private EditText mEditStorey;

    // 类别
    private RadioButton mRadioStreet;
    private RadioButton mRadioHigh;

    // 位置
    private EditText mEditLocation;
    private String mStrLocation;

    // 楼层对话框
    AlertDialog mDialogStorey;

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

        // 位置
        mEditLocation = (EditText) findViewById(R.id.edit_location);

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

        // 焦点到了这个edit, 弹出对话框
        mEditStorey.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mDialogStorey.show();
                }
            }
        });

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
                        if (mEditLocation != null) {
                            // 设置楼层输入框数值
                            mEditStorey.setText("" + mPicker.getValue());
                        }
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
        mStrLocation = mEditLocation.getText().toString();

        // 检查输入是否合适
        if (TextUtils.isEmpty(mStrLocation)) {
            CommonUtils.createErrorAlertDialog(this, "请输入位置").show();
            return;
        }

        // 跳转到签到页面
        CommonUtils.moveNextActivity(this, MainActivity.class, true);
    }
}
