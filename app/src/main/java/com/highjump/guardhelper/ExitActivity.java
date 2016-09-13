package com.highjump.guardhelper;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.BitmapUtils;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

public class ExitActivity extends AppCompatActivity implements View.OnClickListener {

    public static String PARAM_REPORT_DATA = "param_report_data";

    // 当前用户
    private UserData mCurrentUser;

    // 上报数据
    private ArrayList<ReportData> maryData = new ArrayList<ReportData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

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

        // 确定按钮
        Button button = (Button) findViewById(R.id.but_exit);
        button.setOnClickListener(this);

        // 获取ReportData数据
        Intent intent = getIntent();
        if (intent.hasExtra(PARAM_REPORT_DATA)) {
            maryData = (ArrayList<ReportData>) intent.getSerializableExtra(PARAM_REPORT_DATA);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_exit:
                doExit();
                break;

            default:
                break;
        }
    }

    /**
     * 销毁数据而推出
     */
    private void doExit() {

        for (ReportData rData : maryData) {
//            Uri uriFile = rData.getUriImage();
//            String strPath = null;
//
//            if (uriFile != null) {
//                // 获取文件路径
//                Cursor cursor = this.getContentResolver().query(uriFile, null, null, null, null);
//                if (cursor.moveToNext()) {
//                    /* _data：文件的绝对路径 ，_display_name：文件名 */
//                    strPath = cursor.getString(cursor.getColumnIndex("_data"));
//                }
//            }
//            else if (rData.getType() == ReportData.REPORT_VIDEO) {
//                strPath = rData.getStringData();
//            }

            String strPath = rData.getStringData();

            // 删除文件
            if (!TextUtils.isEmpty(strPath)) {
                File file = new File(strPath);
                file.delete();
            }
        }

        ((GHApplication)getApplication()).finishApp();
    }
}
