package com.highjump.guardhelper;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.highjump.guardhelper.utils.CommonUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private RelativeLayout mLayoutMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // setTitle需要放在setSupportActionBar之前才会生效
        toolbar.setTitle("警号：234598");

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

        mLayoutMore = (RelativeLayout) findViewById(R.id.layout_more);
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

            default:
                break;
        }
    }

    private void toggleMore() {
//        if (mLayoutMore.getVisibility() == View.GONE) {
            // 显示
            mLayoutMore.setVisibility(View.VISIBLE);

        mLayoutMore.animate().setInterpolator(new AccelerateInterpolator()).translationYBy(-500);
//
//            int nHeight = mLayoutMore.getHeight();
//            TranslateAnimation transAlpha = new TranslateAnimation(0, 0, 0, nHeight);
//            transAlpha.setDuration(2000);
//            transAlpha.setFillAfter(true);
//            transAlpha.setInterpolator(new AccelerateInterpolator() );
//            mLayoutMore.startAnimation(transAlpha);

//        }
//        else {
//            // 隐藏
//            mLayoutMore.setVisibility(View.GONE);
//        }
    }
}
