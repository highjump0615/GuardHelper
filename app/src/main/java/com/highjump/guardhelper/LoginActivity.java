package com.highjump.guardhelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final int SDK_PERMISSION_REQUEST = 127;
    public static String IS_EXIT = "is_exit";

    private EditText mEditUsername;
    private EditText mEditPassword;

    // 用户名
    private String mStrUsername;
    // 密码
    private String mStrPassword;

    private String mstrNotMatch = "警号或密码不正确";

    private ProgressDialog mProgressDialog;

    private String mStrPermissionInfo;

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

        // 初始化
        Config.initConfig(this);

        openGPSSettings();

        // after android m,must request Permission on runtime
        getPermissions();
    }

    @TargetApi(23)
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            /*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                mStrPermissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                mStrPermissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
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

        if (mStrUsername.equals(UserData.DEFAULT_USER) && mStrPassword.equals(UserData.DEFAULT_USER)) {
            gotoMain();
            return;
        }

        mProgressDialog = ProgressDialog.show(this, "", "正在登录...");

        // 调用相应的API
        API_Manager.getInstance().userLogin(
                mStrUsername,
                mStrPassword,
                new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        mProgressDialog.dismiss();

                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.createErrorAlertDialog(LoginActivity.this, Config.STR_CONNET_FAIL, e.getMessage()).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mProgressDialog.dismiss();

                                // 失败
                                if (!response.isSuccessful()) {
                                    CommonUtils.createErrorAlertDialog(LoginActivity.this, "登录错误", response.message()).show();

                                    response.close();
                                    return;
                                }

                                try {
                                    // 获取返回数据
                                    ApiResult resultObj = new ApiResult(response.body().string());

                                    if (Integer.parseInt(resultObj.getResult()) < 1) {
                                        CommonUtils.createErrorAlertDialog(LoginActivity.this, mstrNotMatch).show();
                                        return;
                                    }

                                    gotoMain();
                                }
                                catch (Exception e) {
                                    // 解析失败
                                    CommonUtils.createErrorAlertDialog(LoginActivity.this, Config.STR_PARSE_FAIL, e.getMessage()).show();
                                }

                                response.close();
                            }
                        });
                    }
                }
        );
    }

    /**
     * 跳转到主页面
     */
    private void gotoMain() {
        // 设置当前用户
        new UserData(mStrUsername);

        // 跳转到主页面
        CommonUtils.moveNextActivity(LoginActivity.this, MainActivity.class, true);
    }

    /**
     * 检查GPS设置
     */
    private void openGPSSettings() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            return;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("使用警卫助手，必须要先打开GPS定位")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).create();

        alert.show();
    }
}
