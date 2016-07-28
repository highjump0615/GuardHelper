package com.highjump.guardhelper;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String IS_EXIT = "is_exit";

    private EditText mEditUsername;
    private EditText mEditPassword;

    // 用户名
    private String mStrUsername;
    // 密码
    private String mStrPassword;

    private String mstrNotMatch = "警号或密码不正确";

    private ProgressDialog mProgressDialog;

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


        // 如果在登陆过程当中，则退出
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "正在登录...");

        // 调用相应的API
        API_Manager.getInstance().userLogin(
                mStrUsername,
                mStrPassword,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        mProgressDialog.dismiss();
                        CommonUtils.createErrorAlertDialog(LoginActivity.this, "Error", throwable.getMessage()).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        mProgressDialog.dismiss();

                        // 获取返回数据
                        ApiResult resultObj = new ApiResult(responseString);

                        if (Integer.parseInt(resultObj.getResult()) < 1) {
                            CommonUtils.createErrorAlertDialog(LoginActivity.this, mstrNotMatch).show();
                            return;
                        }

                        // 设置当前用户
                        new UserData(mStrUsername);

                        // 跳转到签到页面
                        CommonUtils.moveNextActivity(LoginActivity.this, MainActivity.class, true);
                    }
                }
        );
    }
}
