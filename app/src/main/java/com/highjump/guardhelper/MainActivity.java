package com.highjump.guardhelper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.highjump.guardhelper.adapter.MainItemAdapter;
import com.highjump.guardhelper.api.API_Manager;
import com.highjump.guardhelper.api.ApiResult;
import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.BitmapUtils;
import com.highjump.guardhelper.utils.CommonUtils;
import com.highjump.guardhelper.utils.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
    private ProgressBar mProgressExpanded;

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
    private final double IMAGE_MAX_SIZE = 4096.0;

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
        mProgressExpanded = (ProgressBar) findViewById(R.id.progress_expand);

        // 列表设置
        mRecyclerView = (RecyclerView) findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainItemAdapter(this, maryData);
        mRecyclerView.setAdapter(mAdapter);

        if (mCurrentUser.isNormalUser()) {
            // 定时请求命令
            mHandlerTimerOrder.postDelayed(mRunnableQueryOrder, Config.QUERY_ORDER_INTERVAL * 1000);
            // 定时上报位置
            mHandlerTimerLocation.postDelayed(mRunnableLocation, Config.LOCATION_INTERVAL * 1000);
        }

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

                            response.close();
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
            Intent intent = new Intent(this, ExitActivity.class);
            intent.putParcelableArrayListExtra(ExitActivity.PARAM_REPORT_DATA, maryData);
            startActivity(intent);

//            CommonUtils.moveNextActivity(this, ExitActivity.class, false);
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
        int nImgWidth = 0, nImgHeight = 0;

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

        String strFilePath = null;

        if (requestCode == CAMERA_VIDEO_REQUEST_CODE) {
            // 处理录像
            Uri uriVideo = data.getData();
            Cursor cursor=this.getContentResolver().query(uriVideo, null, null, null, null);
            if (cursor.moveToNext()) {
                /* _data：文件的绝对路径 ，_display_name：文件名 */
                strFilePath = CommonUtils.getRealFilePath(this, uriVideo);
                imgSend = BitmapUtils.getVideoThumbnail(strFilePath);

                double dWidth = imgSend.getWidth();
                double dHeight = imgSend.getHeight();

                // 决定图片的聊天列表大小
                double dScale = IMAGE_BUBBLE_SIZE / (dWidth > dHeight ? dWidth : dHeight);
                dScale = dScale < 1.0 ? dScale : 1.0;
                nImgWidth = (int) (dWidth * dScale);
                nImgHeight = (int) (dHeight * dScale);

                // 创建Thumnail图
                imgSend = BitmapUtils.createScaleBitmap(imgSend, nImgWidth, nImgHeight);
            }
        }
        else {
            // 获取图片
            try {
                InputStream stream = getContentResolver().openInputStream(uriFile);

                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(stream, null, options);

                // 决定图片的聊天列表大小
                double dScale = IMAGE_BUBBLE_SIZE / (options.outWidth > options.outHeight ? options.outWidth : options.outHeight);
                dScale = dScale < 1.0 ? dScale : 1.0;
                int nThumbWidth = (int) (options.outWidth * dScale);
                int nThumbHeight = (int) (options.outHeight * dScale);

                // Calculate inSampleSize
                options.inSampleSize = BitmapUtils.calculateInSampleSize(options, nThumbWidth, nThumbHeight);

                // reset input stream
                stream = getContentResolver().openInputStream(uriFile);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                imgSend = BitmapFactory.decodeStream(stream, null, options);

                stream.close();

                nImgWidth = nThumbWidth;
                nImgHeight = nThumbHeight;

                // 进一步得到目标大小的缩略图
                imgSend = BitmapUtils.createScaleBitmap(imgSend, nImgWidth, nImgHeight);

                strFilePath = CommonUtils.getRealFilePath(this, uriFile);
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

//        // 缩小大小
//        double dWidth = imgSend.getWidth();
//        double dHeight = imgSend.getHeight();
//
//        // 决定图片的实际大小
//        double dScale = IMAGE_MAX_SIZE / (dWidth > dHeight ? dWidth : dHeight);
//        dScale = dScale < 1.0 ? dScale : 1.0;
//        int nWidth = (int) (dWidth * dScale);
//        int nHeight = (int) (dHeight * dScale);
//
//        imgSend = Bitmap.createScaledBitmap(imgSend, nWidth, nHeight, false);
//
//        // 决定图片的聊天列表大小
//        dScale = IMAGE_BUBBLE_SIZE / (dWidth > dHeight ? dWidth : dHeight);
//        dScale = dScale < 1.0 ? dScale : 1.0;
//        nWidth = (int) (dWidth * dScale);
//        nHeight = (int) (dHeight * dScale);

        // 创建上报数据模型
        final ReportData rData = new ReportData(imgSend, nImgWidth, nImgHeight, uriFile, strFilePath, 0);

        // 设置类型
        if (requestCode == CAMERA_VIDEO_REQUEST_CODE) {
            rData.setType(ReportData.REPORT_VIDEO);
        }
        else {
            rData.setType(ReportData.REPORT_IMAGE);
        }

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

        // 滚动到最底
        mLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), maryData.size());
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

        // 滚动到最底
        mLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), maryData.size());
    }

    /**
     * 上传数据
     * @param data - 数据模型
     * @param index - 索引
     */
    private void uploadReportData(final ReportData data, final int index) {

        if (!mCurrentUser.isNormalUser()) {
            return;
        }

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
//            strInfo = "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";

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
                Bitmap bm = BitmapUtils.base64ToBitmap(strInfo);

                // 将图片保存到文件
                File fileImage = CommonUtils.getOutputMediaFile(this, true);
                Uri uriFile = null;

                if (fileImage != null) {
                    FileOutputStream outStream = new FileOutputStream(fileImage);
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

                    outStream.flush();
                    outStream.close();

                    uriFile = Uri.fromFile(fileImage);
                }

                // 缩小大小
                double dWidth = bm.getWidth();
                double dHeight = bm.getHeight();

                // 决定图片的聊天列表大小
                double dScale = IMAGE_BUBBLE_SIZE / (dWidth > dHeight ? dWidth : dHeight);
                dScale = dScale < 1.0 ? dScale : 1.0;
                int nWidth = (int) (dWidth * dScale);
                int nHeight = (int) (dHeight * dScale);
                bm = BitmapUtils.createScaleBitmap(bm, nWidth, nHeight);

                ReportData data = new ReportData(bm, nWidth, nHeight, uriFile, null, 1);
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

            // 滚动到最底
            mLayoutManager.smoothScrollToPosition(mRecyclerView, new RecyclerView.State(), maryData.size());

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

            // 显示进度条
            mProgressExpanded.setVisibility(View.VISIBLE);

            // 加载原图片
            BitmapLoadTask task = new BitmapLoadTask(mImgviewExpanded, mProgressExpanded);
            task.execute(data.getUriImage());
        }
        else if (data.getType() == ReportData.REPORT_VIDEO) {
            mVdoviewExpanded.setVideoPath(data.getStringData());

            mImgviewExpanded.setVisibility(View.GONE);
            mLayoutVideoExpanded.setVisibility(View.VISIBLE);

            mViewExpanded = mLayoutVideoExpanded;

            // 隐藏进度条
            mProgressExpanded.setVisibility(View.GONE);
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

    /**
     * Class for loading original bitmap
     */
    private class BitmapLoadTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> mRefImageview;
        private final WeakReference<ProgressBar> mRefProgress;
        private Uri mUriFile;

        public BitmapLoadTask(ImageView imageView, ProgressBar progress) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mRefImageview = new WeakReference<ImageView>(imageView);
            mRefProgress = new WeakReference<ProgressBar>(progress);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... params) {
            mUriFile = params[0];

            Bitmap bmpImage = null;

            if (mUriFile != null) {
                // 加载图片文件
                try {
                    InputStream stream = getContentResolver().openInputStream(mUriFile);
                    bmpImage = BitmapFactory.decodeStream(stream);

                    stream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bmpImage;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mRefImageview != null && bitmap != null) {
                final ImageView imageView = mRefImageview.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }

            // 隐藏进度条
            final ProgressBar progressBar = mRefProgress.get();
            if (progressBar != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
