package com.kingbird.advertisting.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.kingbird.advertisting.base.Base;
import com.kingbird.advertisting.manager.CustomActivityManager;
import com.kingbird.advertisting.manager.ExecutorServiceManager;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;

import java.util.concurrent.TimeUnit;

import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME2;
import static com.kingbird.advertisting.utils.Config.YZDJ_PACKAGE_NAME;

/**
 * APP保护服务
 *
 * @author panyingdao
 * @date 2018/2/05.
 */
public class StartAppService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Plog.e("保护服务启动");

//        NetWorkReceiver.register(StartAppService.this);
        selfStartApp();
    }

    private void selfStartApp() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                int restartTime = SpUtil.readInt(Const.RESTART_APP_TIME);
                Plog.e("软件重启时间", restartTime);
                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        Plog.e("软件自动启动");
                        startReady();
                    }
                }, restartTime, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 启动APP
     */
    private void startReady() {
        Activity activityState = CustomActivityManager.getInstance().getTopActivity();
        Plog.e("VideoTextureActivity的状态", activityState);
        if (activityState != null) {
            String appPackageName = Base.getTopPackageName(activityState.toString());
            Plog.e("最上层应用包名", appPackageName);
            if (!PACKAGE_NAME2.equals(appPackageName) && !YZDJ_PACKAGE_NAME.equals(appPackageName)) {
                startApp();
                Plog.e("启动成功");
            } else {
                Plog.e("启动失败");
            }
        } else {
            startApp();
        }
    }

    private void startApp() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Plog.e("StartAppService销毁");
        ThreadManager.getInstance().shutdown();
        ExecutorServiceManager.getInstance().shutdown();
//        NetWorkReceiver.unRegister(StartAppService.this);
        stopSelf();
    }
}
