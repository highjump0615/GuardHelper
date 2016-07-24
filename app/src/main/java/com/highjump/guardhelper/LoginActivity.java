package com.highjump.guardhelper;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.highjump.guardhelper.utils.CommonUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String IS_EXIT = "is_exit";

    private EditText mEditUsername;
    private EditText mEditPassword;

    // 用户名
    private String mStrUsername;
    // 密码
    private String mStrPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra(LoginActivity.IS_EXIT, false)) {
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditUsername = (EditText)findViewById(R.id.edit_username);
        mEditPassword = (EditText)findViewById(R.id.edit_password);

        Button button = (Button) findViewById(R.id.but_login);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_login:
                doLogin();
                break;

            default:
                break;
        }
    }

    /**
     * 登录
     */
    private void doLogin() {
        mStrUsername = mEditUsername.getText().toString();
        mStrPassword = mEditPassword.getText().toString();

        // 检查输入是否合适
        if (TextUtils.isEmpty(mStrUsername)) {
            CommonUtils.createErrorAlertDialog(this, "请输入警号").show();
            return;
        }
        if (TextUtils.isEmpty(mStrPassword)) {
            CommonUtils.createErrorAlertDialog(this, "请输入密码").show();
            return;
        }

        // 跳转到签到页面
        CommonUtils.moveNextActivity(this, SignActivity.class, true);
    }
}
