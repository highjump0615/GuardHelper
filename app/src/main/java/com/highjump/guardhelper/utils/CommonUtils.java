package com.highjump.guardhelper.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.highjump.guardhelper.R;
import com.highjump.guardhelper.api.API_Manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 2016/7/23.
 */
public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    // 当前位置
    public static BDLocation mCurrentLocation = null;

    private static MediaPlayer mMediaPlayer;

    // 获取经度
    public static double getLongitude() {
        double dLongitude = 0;

        if (mCurrentLocation != null) {
            dLongitude = mCurrentLocation.getLongitude();
        }

        return dLongitude;
    }

    // 获取纬度
    public static double getLatitude() {
        double dLatitude = 0;

        if (mCurrentLocation != null) {
            dLatitude = mCurrentLocation.getLatitude();
        }

        return dLatitude;
    }

    /**
     * 获取md5加密字符串
     * @param encTarget - 原码字符串
     * @return - 加密字符串
     */
    public static String getMD5EncryptedString(String encTarget) {
        return getMD5EncryptedString(encTarget.getBytes(), encTarget.length());
    }

    public static String getMD5EncryptedString(byte[] data, int length) {
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
            return null;
        }

        // Encryption algorithm
        mdEnc.update(data, 0, length);
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    /**
     * 创建对话框
     */
    public static Dialog createErrorAlertDialog(final Context context, String message) {
        return createErrorAlertDialog(context, "", message);
    }

    /**
     * 创建对话框
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null).create();
    }

    /**
     * 跳转到指定的activity
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass, boolean removeSource) {
        Intent intent = new Intent(source, destinationClass);

//        if (removeSource) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        }

        source.startActivity(intent);

        if (removeSource) {
            source.finish();
        }
    }

    // 播放asset的音频
    public static void playSound(Context context, String fileName) {
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd(fileName);
            long start = descriptor.getStartOffset();
            long end = descriptor.getLength();

            if (mMediaPlayer == null)
                mMediaPlayer = new MediaPlayer();
            else
                mMediaPlayer.reset();

            mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), start, end);

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                    return false;
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                    }
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {

                }
            });
            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2) {

                }
            });

            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放音频
     */
    public static void stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    /*
     * returning photoImage / video
     */
    public static File getOutputMediaFile(Context context, boolean isImageType) {
        File mediaStorageDir = getMyApplicationDirectory(context, Config.IMAGE_DIRECTORY_NAME);

        if (mediaStorageDir == null) return null;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;

        if (isImageType) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator /*+ "IMG_"*/ + timeStamp + ".jpg");
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator /*+ "VID_"*/ + timeStamp + ".3gp");
        }

        return mediaFile;
    }

    /**
     * Get path of images to be saved
     */
    public static File getMyApplicationDirectory(Context context, String directoryName) {
        String appName = context.getString(R.string.app_name);

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), appName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + appName + " directory");
                return null;
            }
        }

        mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + appName, directoryName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + directoryName + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri )
            return null;

        final String scheme = uri.getScheme();
        String data = null;

        if ( scheme == null ) {
            data = uri.getPath();
        }
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        }
        else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
