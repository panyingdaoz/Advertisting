package com.kingbird.advertisting.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.kingbird.advertisting.BuildConfig;
import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.utils.Plog;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME2;

/**
 * Application class
 *
 * @author Pan
 * @date 2017/8/21
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        KLog.init(BuildConfig.LOG_DEBUG);
        Plog.init(true);

//        //2018-10-31添加讯飞初始化
//        SpeechUtility.createUtility(getApplicationContext(), "appid=" + "5bd814bb");

        Context context = getApplicationContext();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(PACKAGE_NAME2));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "f1dd4dcd9f", true, strategy);

        //自定义上报参数
        List<Parameter> deviceId = LitePal.findAll(Parameter.class);
        for (Parameter deviceIds : deviceId) {
            String id = deviceIds.getDeviceId();
            if (id != null) {
                CrashReport.putUserData(this, "deviceID", id);
            }
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        LitePal.initialize(this);
        //2018-8-16 添加
        MultiDex.install(MyApplication.this);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
