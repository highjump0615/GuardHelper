package com.highjump.guardhelper;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.highjump.guardhelper.adapter.MainItemAdapter;
import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

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

    // 请求命令的线程
    private Handler mHandlerTimerOrder = new Handler();
    private Handler mHandlerTimerLocation = new Handler();

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
        mRecyclerView = (RecyclerView) findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainItemAdapter(this, maryData);
        mRecyclerView.setAdapter(mAdapter);

        // 定时请求命令
        mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL);
        // 定时上报位置
        mHandlerTimerLocation.postDelayed(mRunnableLocation, CommonUtils.mnLocationInterval);

        // 开始定位
        initLocation();
    }

    /**
     * 请求命令线程
     */
    private Runnable mRunnableQueryOrder = new Runnable() {
        public void run() {

            // 调用请求命令API
            API_Manager.getInstance().queryOrder(
                    mCurrentUser,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            // UI线程上运行
                            runOnUiThread(new Runnable() {
                                              @Override
                                              public void run() {
                                                  Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                              }
                                          });

                            mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL);     // 安排一个Runnable对象到主线程队列中
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            // UI线程上运行
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 获取返回数据
                                    try {
                                        ApiResult resultObj = new ApiResult(response.body().string());

                                        if (Integer.parseInt(resultObj.getResult()) < 1) {
                                            Toast.makeText(MainActivity.this, "没有命令", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // 获取命令
                                        getOrder(resultObj.getNodeData("orderno"));
                                    }
                                    catch (Exception e) {
                                        // 解析失败
                                        Toast.makeText(MainActivity.this, "获取命令失败！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL);     // 安排一个Runnable对象到主线程队列中
                        }
                    }
            );

            Log.e(TAG, "onTimer");
        }
    };

    /**
     * 上报位置线程
     */
    private Runnable mRunnableLocation = new Runnable() {
        public void run() {
            // 调用请求命令API
            API_Manager.getInstance().reportLocation(
                    mCurrentUser,
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mHandlerTimerLocation.postDelayed(mRunnableLocation, CommonUtils.mnLocationInterval);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            mHandlerTimerLocation.postDelayed(mRunnableLocation, CommonUtils.mnLocationInterval);
                        }
                    }
            );
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 停止Timer
        mHandlerTimerOrder.removeCallbacks(mRunnableQueryOrder);
        mHandlerTimerLocation.removeCallbacks(mRunnableLocation);

        // 停止定位
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(mLocationListener);
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
        } else if (id == R.id.nav_setting) {
            // 跳转到设置页面
            CommonUtils.moveNextActivity(this, SettingActivity.class, false);
        } else if (id == R.id.nav_exit) {
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
        } else {
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
        ReportData data = new ReportData(strData, ReportData.REPORT_TEXT, 0);
        maryData.add(data);

        // 更新聊天列表
        mAdapter.notifyDataSetChanged();
        dismissKeyboard();

        // 清空输入框
        mEditMsg.setText("");

        // 上报数据
        API_Manager.getInstance().reportData(
                mCurrentUser,
                data,
                new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtils.createErrorAlertDialog(MainActivity.this, "Error", e.getMessage()).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // 获取返回数据
                                    ApiResult resultObj = new ApiResult(response.body().string());

                                    if (Integer.parseInt(resultObj.getResult()) < 1) {
                                        CommonUtils.createErrorAlertDialog(MainActivity.this, "上报失败").show();
                                        return;
                                    }
                                }
                                catch (Exception e) {
                                    // 解析失败
                                    CommonUtils.createErrorAlertDialog(MainActivity.this, Config.STR_PARSE_FAIL, e.getMessage()).show();
                                }
                            }
                        });
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

    /**
     * 获取下发信息
     * @param orderNo
     */
    private void getOrder(String orderNo) {

        // 如果获取不到命令号, 则退出
        if (TextUtils.isEmpty(orderNo)) {
            Toast.makeText(MainActivity.this, "请求命令失败！拿不到命令号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用获取信息API
        API_Manager.getInstance().getOrder(
                mCurrentUser,
                orderNo,
                new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 获取返回数据
                        final ApiResult resultObj = new ApiResult(response.body().string());

                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processReceive(resultObj);
                            }
                        });
                    }
                }
        );
    }

    private void processReceive(ApiResult resultObj) {

        try {
            // information
            String strInfo = resultObj.getNodeData("information");
            if (TextUtils.isEmpty(strInfo)) {
                Toast.makeText(MainActivity.this, "获取下发信息失败！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 信息类型
            int nType = Integer.parseInt(resultObj.getNodeData("datatype"));

            // 暂时处理文字信息
            if (nType != ReportData.REPORT_TEXT) {
                return;
            }

            // 创建Report模型
            ReportData data = new ReportData(strInfo, nType, 1);
            maryData.add(data);

            // 更新聊天列表
            mAdapter.notifyDataSetChanged();

            //
            // 本地推送
            //
            NotificationCompat.Builder ncBuilder = new NotificationCompat.Builder(this);
            Notification notification;
            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            final PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            notification = ncBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(strInfo)
                    .setAutoCancel(true)
                    .setContentTitle("新命令")
                    .setContentIntent(resultPendingIntent)
                    .setContentText(strInfo)
//                    .setSound(alarmSound)
//                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(100, notification);

        } catch (Exception e) {
            // 解析失败
            Toast.makeText(MainActivity.this, "获取下发信息失败！解析错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void initLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean bGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Location location = null;
        String strProvider = "";

        // 检查有没有定位方面的权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(MainActivity.this, "没有获取定位权限", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bGpsEnabled) {
            strProvider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(strProvider);
        }

        updateNewLocation(location);

        if (strProvider.length() > 0) {
            locationManager.requestLocationUpdates(strProvider, 3000/*时间间隔*/, 0, mLocationListener);
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            updateNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void updateNewLocation(Location location) {

        if (location != null) {
            CommonUtils.mCurrentLocation = location;
//            Toast.makeText(MainActivity.this, "位置：" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_LONG).show();
        }
        else {
            /*if (BuildConfig.DEBUG) {
                location = new Location("reverseGeocoded");
                location.setLongitude(115.25);
                location.setLatitude(39.26);

                CommonUtils.mCurrentLocation = location;
            }*/

//            Toast.makeText(MainActivity.this, "无法定位", Toast.LENGTH_LONG).show();
        }
    }
}
