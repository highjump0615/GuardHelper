package com.highjump.guardhelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.highjump.guardhelper.utils.CommonUtils;

public class SignActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditLocation;

    private RadioButton mRadioStreet;
    private RadioButton mRadioHigh;

    // 位置
    private String mStrLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 位置
        mEditLocation = (EditText) findViewById(R.id.edit_location);

        // 单选框
        mRadioStreet = (RadioButton) findViewById(R.id.rad_street);
        mRadioHigh = (RadioButton) findViewById(R.id.rad_high);

        // 默认是街道签到
        mRadioStreet.setChecked(true);

        Button button = (Button) findViewById(R.id.but_sign);
        button.setOnClickListener(this);
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
