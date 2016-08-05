package com.highjump.guardhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.highjump.guardhelper.model.UserData;

public class ExitActivity extends AppCompatActivity implements View.OnClickListener {

    // 当前用户
    private UserData mCurrentUser;

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

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LoginActivity.IS_EXIT, true);

        startActivity(intent);
    }
}
