package com.highjump.guardhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
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
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.highjump.guardhelper.adapter.MainItemAdapter;
import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // 打开相机相关值
    private static final int CAMERA_IMAGE_REQUEST_CODE = 100;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 101;
    private static final int CAMERA_VIDEO_REQUEST_CODE = 102;

    public static final int MEDIA_TYPE_IMAGE = 0;

    // 百度定位
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private EditText mEditMsg;

    // 当前用户
    private UserData mCurrentUser;

    // 输入框更多
    private RelativeLayout mLayoutMore;

    // 聊天列表
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    // 扩大图片
    private RelativeLayout mLayoutExpanded;
    private ImageViewTouch mImgviewExpanded;
    private RelativeLayout mLayoutVideoExpanded;
    private VideoView mVdoviewExpanded;

    private View mViewExpanded;

    private Rect mStartBounds = new Rect();
    private float mfStartScale;

    public MainItemAdapter mAdapter;

    // 上报下发信息
    private ArrayList<ReportData> maryData = new ArrayList<ReportData>();

    // 请求命令的线程
    private Handler mHandlerTimerOrder = new Handler();
    private Handler mHandlerTimerLocation = new Handler();

    private Handler mHandlerUpload = new Handler();
    private Handler mHandlerUploadApi = new Handler();

    // 图片显示大小和存储大小
    private Uri mFileUri;
    private final double IMAGE_BUBBLE_SIZE = 200.0;
    private final double IMAGE_MAX_SIZE = 1024.0;

    /**
     * Hold a reference to the current animator, so that it can be canceled mid-way.
     */
    private AnimatorSet mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;

    // 振动器
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取当前用户
        mCurrentUser = UserData.currentUser();

        // 如果没有用户登录、则跳转到登录页面
        if (mCurrentUser == null) {
            CommonUtils.moveNextActivity(this, LoginActivity.class, true);
            return;
        }

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

        // 更多输入
        mLayoutMore = (RelativeLayout) findViewById(R.id.layout_more);
        button = (Button) findViewById(R.id.but_camera);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.but_gallery);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.but_video);
        button.setOnClickListener(this);

        // 扩大图片
        mLayoutExpanded = (RelativeLayout) findViewById(R.id.layout_expanded);
        mLayoutExpanded.setOnClickListener(this);

        mImgviewExpanded = (ImageViewTouch) findViewById(R.id.imgview_expanded);
        // set the default image display type
        mImgviewExpanded.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        mLayoutVideoExpanded = (RelativeLayout) findViewById(R.id.layout_video_expanded);
        mVdoviewExpanded = (VideoView) findViewById(R.id.video_expanded);
        mVdoviewExpanded.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.seekTo(1);
                mp.start();
            }
        });
        mVdoviewExpanded.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(1);
            }
        });


        // 列表设置
        mRecyclerView = (RecyclerView) findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainItemAdapter(this, maryData);
        mRecyclerView.setAdapter(mAdapter);

        // 定时请求命令
        mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL * 1000);
        // 定时上报位置
        mHandlerTimerLocation.postDelayed(mRunnableLocation, Config.LOCATION_INTERVAL * 1000);

        // 开始定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数

        initLocation();

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        // 振动器
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
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
//                            runOnUiThread(new Runnable() {
//                                              @Override
//                                              public void run() {
//                                                  Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                              }
//                                          });

                            mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL * 1000);     // 安排一个Runnable对象到主线程队列中
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
//                                            Toast.makeText(MainActivity.this, "没有命令", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // 获取命令
                                        getOrder(resultObj.getNodeData("orderno"));
                                    }
                                    catch (Exception e) {
                                        // 解析失败
//                                        Toast.makeText(MainActivity.this, "获取命令失败！", Toast.LENGTH_SHORT).show();
                                    }

                                    response.close();
                                }
                            });

                            mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL * 1000);     // 安排一个Runnable对象到主线程队列中
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
                            mHandlerTimerLocation.postDelayed(mRunnableLocation, Config.LOCATION_INTERVAL * 1000);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            mHandlerTimerLocation.postDelayed(mRunnableLocation, Config.LOCATION_INTERVAL * 1000);
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
        if (mLocationClient != null) {
            mLocationClient.stop();
        }

        // 清空notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // 如果侧栏打开了，先把它关掉
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        // 如果图片扩大代开了，先把它关掉
        else if (mLayoutExpanded.getVisibility() == View.VISIBLE) {
            closeExpanded();
        }
        // 跳转到推出页面
        else {
            CommonUtils.moveNextActivity(this, ExitActivity.class, false);
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

        // 0.5s后收起侧栏，以便防止卡住现象
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START, false);
                    }
                });
            }
        };
        new Timer().schedule(task, 500);

        return true;
    }

    /**
     * Creating file uri to store photoImage/video
     */
    public Uri getOutputMediaFileUri(int type) {
        File fileMedia = CommonUtils.getOutputMediaFile(this, type == MEDIA_TYPE_IMAGE);

        if (fileMedia != null) {
            return Uri.fromFile(fileMedia);
        }
        else {
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_more:
                toggleMore();
                break;

            case R.id.layout_expanded:
                closeExpanded();
                break;

            case R.id.but_send:
                doSend();
                break;

            case R.id.but_camera:
                mFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

                // 打开相机
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

                    startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
                }
                catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }

                toggleMore();

                break;

            case R.id.but_gallery:

                // 打开相册
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);

                toggleMore();

                break;

            case R.id.but_video:
                try {
                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    // set video quality
                    // 1- for high quality video, 0 - for mms quality video
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);

                    startActivityForResult(intent, CAMERA_VIDEO_REQUEST_CODE);
                }
                catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }

                toggleMore();

                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        Bitmap imgSend = null;
        Uri uriFile = null;

        // 通过相机获取图片
        if (requestCode == CAMERA_IMAGE_REQUEST_CODE) {
            uriFile = mFileUri;
//            imgSend = (Bitmap) data.getExtras().get("data");  // 这只是thumbname
        }
        // 通过相册获取图片
        else if (requestCode == GALLERY_IMAGE_REQUEST_CODE) {
            if (data == null) {
                return;
            }

            uriFile = data.getData();
        }

        String strVideoPath = null;

        if (requestCode == CAMERA_VIDEO_REQUEST_CODE) {
            // 处理录像
            Uri uriVideo = data.getData();
            Cursor cursor=this.getContentResolver().query(uriVideo, null, null, null, null);
            if (cursor.moveToNext()) {
                /* _data：文件的绝对路径 ，_display_name：文件名 */
                strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
                imgSend = CommonUtils.getVideoThumbnail(strVideoPath);
            }
        }
        else {
            // 获取图片
            try {
                InputStream stream = getContentResolver().openInputStream(uriFile);
                imgSend = BitmapFactory.decodeStream(stream);
                stream.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 处理图片
        if (imgSend == null) {
            return;
        }

        // 缩小大小
        double dWidth = imgSend.getWidth();
        double dHeight = imgSend.getHeight();

        // 决定图片的实际大小
        double dScale = IMAGE_MAX_SIZE / (dWidth > dHeight ? dWidth : dHeight);
        dScale = dScale < 1.0 ? dScale : 1.0;
        int nWidth = (int) (dWidth * dScale);
        int nHeight = (int) (dHeight * dScale);

        imgSend = Bitmap.createScaledBitmap(imgSend, nWidth, nHeight, false);

        // 决定图片的聊天列表大小
        dScale = IMAGE_BUBBLE_SIZE / (dWidth > dHeight ? dWidth : dHeight);
        dScale = dScale < 1.0 ? dScale : 1.0;
        nWidth = (int) (dWidth * dScale);
        nHeight = (int) (dHeight * dScale);

        // 创建上报数据模型
        final ReportData rData = new ReportData(imgSend, nWidth, nHeight, strVideoPath, 0);

        final int nIndex = maryData.size();
        maryData.add(rData);

        // 更新聊天列表
        mAdapter.notifyDataSetChanged();

        // encode图片需要时间，所以在另一个线程里做
        mHandlerUpload.postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadReportData(rData, nIndex);
            }
        }, 50);
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

        // 创建上报数据模型
        ReportData data = new ReportData(strData, 0);

        final int nIndex = maryData.size();
        maryData.add(data);

        // 更新聊天列表
        mAdapter.notifyDataSetChanged();
        dismissKeyboard();

        uploadReportData(data, nIndex);

        // 清空输入框
        mEditMsg.setText("");
    }

    /**
     * 上传数据
     * @param data - 数据模型
     * @param index - 索引
     */
    private void uploadReportData(final ReportData data, final int index) {

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
                                // 更新该气泡视图
                                data.setStatus(ReportData.STATUS_FAILED);
                                mAdapter.notifyItemChanged(index);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        // UI线程上运行
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int nStatus = ReportData.STATUS_FAILED;

                                try {
                                    // 获取返回数据
                                    ApiResult resultObj = new ApiResult(response.body().string());

                                    if (Integer.parseInt(resultObj.getResult()) == 1) {
                                        nStatus = ReportData.STATUS_SUCCESS;
                                    }
                                }
                                catch (Exception e) {
                                    // 解析失败
                                    CommonUtils.createErrorAlertDialog(MainActivity.this, Config.STR_PARSE_FAIL, e.getMessage()).show();
                                }

                                // 更新该气泡视图
                                data.setStatus(nStatus);
                                mAdapter.notifyItemChanged(index);

                                response.close();
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
//            Toast.makeText(MainActivity.this, "请求命令失败！拿不到命令号", Toast.LENGTH_SHORT).show();
            return;
        }

        //
        // 处理特殊命令
        //
        try {
            int nOrderNo = Integer.parseInt(orderNo);

            switch (nOrderNo) {
                case Config.ORDERNO_EXIT_APP:       // 退出app
                    ((GHApplication)getApplication()).finishApp();
                    return;

                case Config.ORDERNO_OUT_OF_RANGE:   // 超出警戒范围
                    ((GHApplication)getApplication()).createCommonAlertDialg(
                            "超出警戒范围",
                            "请速返回自己位置",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CommonUtils.stopSound();
                                }
                            }
                    ).show();
                    mVibrator.vibrate(1000);    // 振动1000ms
                    CommonUtils.playSound(this, "police_siren.mp3");

                    return;
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // 调用获取信息API
        API_Manager.getInstance().getOrder(
                mCurrentUser,
                orderNo,
                new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        // UI线程上运行
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
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

                        response.close();
                    }
                }
        );
    }

    /**
     * 处理服务端返回结果
     * @param resultObj
     */
    private void processReceive(ApiResult resultObj) {

        try {
            // information
            String strInfo = resultObj.getNodeData("information");
            if (TextUtils.isEmpty(strInfo)) {
//                Toast.makeText(MainActivity.this, "获取下发信息失败！", Toast.LENGTH_SHORT).show();
                return;
            }

            // 信息类型
            int nType = Integer.parseInt(resultObj.getNodeData("datatype"));

            // 创建Report模型
            if (nType == ReportData.REPORT_TEXT) {
                ReportData data = new ReportData(strInfo, 1);
                maryData.add(data);
            }
            else if (nType == ReportData.REPORT_IMAGE) {
                Bitmap bm = CommonUtils.base64ToBitmap(strInfo);

                // 缩小大小
                double dWidth = bm.getWidth();
                double dHeight = bm.getHeight();

                // 决定图片的聊天列表大小
                double dScale = IMAGE_BUBBLE_SIZE / (dWidth > dHeight ? dWidth : dHeight);
                dScale = dScale < 1.0 ? dScale : 1.0;
                int nWidth = (int) (dWidth * dScale);
                int nHeight = (int) (dHeight * dScale);

                ReportData data = new ReportData(bm, nWidth, nHeight, null, 1);
                maryData.add(data);
            }

            // 更新聊天列表
            mAdapter.notifyDataSetChanged();

            GHApplication app = (GHApplication) getApplication();
            if (app.getCurrentActivity() != this) {
                //
                // 本地推送, 在聊天页面上不发通知
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
            }
            else {
                // 如果已经在这个页面，只振动提示
                mVibrator.vibrate(500);
            }

        } catch (Exception e) {
            // 解析失败
//            Toast.makeText(MainActivity.this, "获取下发信息失败！解析错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开始定位
     */
    private void initLocation() {

        LocationClientOption option = new LocationClientOption();

        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");

        int span = 3000;

        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(span);

        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(false);

        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);

        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setLocationNotify(true);

        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(false);

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(false);

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(true);

        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);

        //可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setEnableSimulateGps(false);

        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            // 成功
            if (location.getLocType() == BDLocation.TypeGpsLocation ||
                location.getLocType() == BDLocation.TypeNetWorkLocation ||
                location.getLocType() == BDLocation.TypeOffLineLocation) {

                CommonUtils.mCurrentLocation = location;
            }
/*
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\ndescription : ");// *****对应的定位类型说明*****
            sb.append(location.getLocTypeDescription());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
            Log.i("BaiduLocationApiDem", sb.toString());
*/
        }
    }

    /**
     * 聊天列表上的图片扩大到整个画面，
     * @param thumbView - 来源View
     * @param data - 数据对象
     */
    public void zoomImageFromThumb(View thumbView, ReportData data) {
        Bitmap bm = data.getBitmapData();

        if (bm == null) {
            return;
        }

        if (data.getType() == ReportData.REPORT_IMAGE) {
            mImgviewExpanded.setImageBitmap(bm);

            mImgviewExpanded.setVisibility(View.VISIBLE);
            mLayoutVideoExpanded.setVisibility(View.GONE);

            mViewExpanded = mImgviewExpanded;
        }
        else if (data.getType() == ReportData.REPORT_VIDEO) {
            mVdoviewExpanded.setVideoPath(data.getStringData());

            mImgviewExpanded.setVisibility(View.GONE);
            mLayoutVideoExpanded.setVisibility(View.VISIBLE);

            mViewExpanded = mLayoutVideoExpanded;
        }

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(mStartBounds);
        findViewById(R.id.drawer_layout).getGlobalVisibleRect(finalBounds, globalOffset);
        mStartBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        if ((float) finalBounds.width() / finalBounds.height() > (float) mStartBounds.width() / mStartBounds.height()) {
            // Extend start bounds horizontally
            mfStartScale = (float) mStartBounds.height() / finalBounds.height();
            float startWidth = mfStartScale * finalBounds.width();
            float deltaWidth = (startWidth - mStartBounds.width()) / 2;
            mStartBounds.left -= deltaWidth;
            mStartBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            mfStartScale = (float) mStartBounds.width() / finalBounds.width();
            float startHeight = mfStartScale * finalBounds.height();
            float deltaHeight = (startHeight - mStartBounds.height()) / 2;
            mStartBounds.top -= deltaHeight;
            mStartBounds.bottom += deltaHeight;
        }

        // show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        mLayoutExpanded.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mViewExpanded.setPivotX(0f);
        mViewExpanded.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mViewExpanded, View.X, mStartBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.Y, mStartBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.SCALE_X, mfStartScale, 1f))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.SCALE_Y, mfStartScale, 1f))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.ALPHA, 0f, 1f))
                .with(ObjectAnimator.ofFloat(mLayoutExpanded, View.ALPHA, 0f, 1f));

        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    /**
     * 关闭扩大图片
     */
    private void closeExpanded() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // 播放终止
        mVdoviewExpanded.stopPlayback();

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(mViewExpanded, View.X, mStartBounds.left))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.Y,mStartBounds.top))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.SCALE_X, mfStartScale))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.SCALE_Y, mfStartScale))
                .with(ObjectAnimator.ofFloat(mViewExpanded, View.ALPHA, 1f, 0f))
                .with(ObjectAnimator.ofFloat(mLayoutExpanded, View.ALPHA, 1f, 0f));

        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLayoutExpanded.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mLayoutExpanded.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();

        mCurrentAnimator = set;
    }
}
