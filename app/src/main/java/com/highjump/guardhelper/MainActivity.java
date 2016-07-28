package com.highjump.guardhelper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.highjump.guardhelper.adapter.MainItemAdapter;
import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private EditText mEditMsg;

    // 当前用户
    private UserData mCurrentUser;

    // 输入框更多
    private RelativeLayout mLayoutMore;

    // 聊天列表
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    public MainItemAdapter mAdapter;

    // 上报下发信息
    private ArrayList<ReportData> maryData = new ArrayList<ReportData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取当前用户
        mCurrentUser = UserData.currentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // setTitle需要放在setSupportActionBar之前才会生效
        toolbar.setTitle("警号：" + mCurrentUser.getUsername());

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 输入框
        ImageView imgView = (ImageView) findViewById(R.id.imgview_more);
        imgView.setOnClickListener(this);
        mEditMsg = (EditText) findViewById(R.id.edit_msg);
        Button button = (Button) findViewById(R.id.but_send);
        button.setOnClickListener(this);

        // 焦点到了这个edit, 隐藏更多区
        mEditMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mLayoutMore.setVisibility(View.GONE);
                }
            }
        });

        // 点击这个edit, 隐藏更多区
        mEditMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayoutMore.setVisibility(View.GONE);
            }
        });

        mLayoutMore = (RelativeLayout) findViewById(R.id.layout_more);

        // 列表设置
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainItemAdapter(this, maryData);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign) {
            // 跳转到签到页面
            CommonUtils.moveNextActivity(this, SignActivity.class, false);
        }
        else if (id == R.id.nav_setting) {
            // 跳转到设置页面
            CommonUtils.moveNextActivity(this, SettingActivity.class, false);
        }
        else if (id == R.id.nav_exit) {
            // 跳转到退出页面
            CommonUtils.moveNextActivity(this, ExitActivity.class, false);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_more:
                toggleMore();
                break;

            case R.id.but_send:
                doSend();
                break;

            default:
                break;
        }
    }

    /**
     * 显示/隐藏更多区
     */
    private void toggleMore() {
        if (mLayoutMore.getVisibility() == View.GONE) {
            // 显示
            mLayoutMore.setVisibility(View.VISIBLE);

            // 隐藏键盘
            dismissKeyboard();
        }
        else {
            // 隐藏
            mLayoutMore.setVisibility(View.GONE);
        }
    }

    /**
     * 上报文字信息
     */
    private void doSend() {
        // 获取输入框的文字信息
        String strData = mEditMsg.getText().toString();

        // 如果输入空, 则退出
        if (TextUtils.isEmpty(strData)) {
            return;
        }

        // 创建Report模型
        ReportData data = new ReportData(strData, ReportData.REPORT_TEXT);
        maryData.add(data);

        // 更新聊天列表
        mAdapter.notifyDataSetChanged();
        dismissKeyboard();

        // 清空输入框
        mEditMsg.setText("");

        // 上报数据
        API_Manager.getInstance().reportData(
                mCurrentUser.getUsername(),
                data,
                new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        CommonUtils.createErrorAlertDialog(MainActivity.this, "Error", throwable.getMessage()).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        // 获取返回数据
                        ApiResult resultObj = new ApiResult(responseString);

                        if (Integer.parseInt(resultObj.getResult()) < 1) {
                            CommonUtils.createErrorAlertDialog(MainActivity.this, "上报失败").show();
                            return;
                        }
                    }
                });
    }

    /**
     * 收起软键盘
     */
    public void dismissKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
