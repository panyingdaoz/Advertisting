package com.kingbird.advertisting.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.kingbird.advertisting.manager.ExecutorServiceManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基类  在API大于等于19时，自动隐藏和显示导航条的状态栏
 *
 * @author panyingdao
 * @date 2017/12/12.
 */
public class BaseActivity extends AppCompatActivity {

    private MyBaseActivityBroad baseActivityBrod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //让视频全屏播放
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        //屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        registerReceiver();
    }
    /**
     * 注册
     */
    private void registerReceiver() {
        baseActivityBrod = new MyBaseActivityBroad();
        IntentFilter intentFilter = new IntentFilter(Const.EXITAPP);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(baseActivityBrod, intentFilter);
    }

    /**
     * 吐司提示
     */
    protected Toast toast;

    @SuppressLint("ShowToast")
    public void showToast(int msg) {
        if (toast != null) {
            toast.setText(msg);
        } else {
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    @SuppressLint("ShowToast")
    public void showToast(String msg) {
        if (toast != null) {
            toast.setText(msg);
        } else {
            toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        }
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (baseActivityBrod != null) {
            try {
                unregisterReceiver(baseActivityBrod);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  广播监听
     */
    public class MyBaseActivityBroad extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(final Context context, Intent intent) {
            String intentAction = intent.getAction();
            Plog.e("接收到的广播", intentAction);
            int closeAll = intent.getIntExtra("closeAll", 0);
            if (closeAll == 1) {
                Plog.e("销毁BaseActivity");
                finish();
            }
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    ExecutorServiceManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            Plog.e("软件自动后台切前台");
                            moveTaskToFront(context);
                        }
                    }, 15, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     *  应用后台切入前台
     */
    private void moveTaskToFront(Context context) {
       //获取ActivityManager
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        //获得当前运行的task(任务)
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            //找到本应用的 task，并将它切换到前台
            if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
    }

}
