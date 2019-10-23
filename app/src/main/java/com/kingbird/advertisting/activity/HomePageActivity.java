package com.kingbird.advertisting.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kingbird.advertisting.R;
import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.manager.CustomActivityManager;
import com.kingbird.advertisting.manager.ExecutorServiceManager;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.GlideUtil;
import com.kingbird.advertisting.utils.MacUtil;
import com.kingbird.advertisting.utils.PermissionsUtils;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;
import com.kingbird.advertisting.utils.WifiSwitchInterface;
import com.kingbird.advertisting.utils.WifiSwitchPresenter;

import org.litepal.tablemanager.Connector;

import java.io.File;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION_CODES.M;
import static com.kingbird.advertisting.base.Base.addParameterData;
import static com.kingbird.advertisting.base.Base.isFirstStart;
import static com.kingbird.advertisting.base.Base.setIntentData;
import static com.kingbird.advertisting.utils.Config.INITIAL_HERATBEAT;
import static com.kingbird.advertisting.utils.Config.INITIAL_IP;
import static com.kingbird.advertisting.utils.Config.INITIAL_PORT;
import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME;
import static com.kingbird.advertisting.utils.Config.ROOT_DIRECTORY_URL;
import static com.kingbird.advertisting.utils.NetUtil.isNetConnected;
import static com.kingbird.advertisting.utils.Plog.e;

/**
 * 首页显示
 *
 * @author panyingdao
 * @date 2018/2/05.
 */
public class HomePageActivity extends AppCompatActivity implements WifiSwitchInterface {

    private ImageView mHomePage;
    private int screenWidth;
    private int screenHeight;
    private int count = 0;
    private boolean isExistence;
    private boolean isIntent;
    private WifiSwitchPresenter wifiSwitchpresenter;

    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    public static String actionServiceStateChange = "ACTION_SERVICE_STATE_CHANGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Plog.e("onCreate创建");
        wifiSwitchpresenter = new WifiSwitchPresenter(this, this);

        initialization();
        getScreenWidthHeigth();
        versionInitialization();
    }
    /**
     * 初始化
     */
    private void versionInitialization() {
        Plog.e("系统版本号："+ Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= M) {
            PermissionsUtils.getInstance().chekPermissions(this, permissions, permissionsResult);
        } else {
            createFile();
            if (isFirstStart(HomePageActivity.this)) {
                addLitepal();
            }
            Plog.e("跳转");
            switchingActivity();
        }
    }
    /**
     * 数据库初始化
     */
    private void initialization() {
        CustomActivityManager.getInstance().setTopActivity(this);
        mHomePage = findViewById(R.id.homePage);
        count++;
        Connector.getDatabase();
    }
    /**
     * activity跳转
     */
    private void switchingActivity() {
        if (!isNetConnected(HomePageActivity.this)) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            if (screenWidth > screenHeight) {
                Plog.e("横屏");
                GlideUtil.load(HomePageActivity.this, R.drawable.openlandscape, options, mHomePage);
                mHomePage.setVisibility(View.VISIBLE);
            } else {
                Plog.e("竖屏");
                GlideUtil.load(HomePageActivity.this, R.drawable.openportrait, options, mHomePage);
                mHomePage.setVisibility(View.VISIBLE);
            }
            ExecutorServiceManager.getInstance().schedule(task, 30, TimeUnit.SECONDS);
        } else {
            Plog.e("有网直接跳转");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityIntent();
                    wifiSwitchpresenter.onDestroy();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(HomePageActivity.this, requestCode, grantResults);
    }

    /**
     * 创建数据库并添加初始值
     */
    private void addLitepal() {//添加参数表
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                //获取MAC地址
                String mac = MacUtil.getMac(HomePageActivity.this);
                if (!TextUtils.isEmpty(mac)) {
                    Plog.e("网络MAC地址："+ mac);
                    SpUtil.writeString(Const.MAC, mac);
                } else {
                    mac = getStringRandom();
                    Plog.e("随机MAC地址："+ mac);
                    SpUtil.writeString(Const.MAC, mac);
                }

                String deviceId = mac.substring(1, 12);
                if (isExistence) {
                    addParameterData(deviceId);
                } else {
                    setIntentData("deviceID", deviceId);
                    setIntentData("rCode", "1111");
                    setIntentData("ip", INITIAL_IP);
                    setIntentData("port", INITIAL_PORT);
                    setIntentData("protocolType", 1);
                    setIntentData("startPlayType", 2);
                    setIntentData("startPlayUrl", "0.mp4");
                    setIntentData("playType", 1);
                    setIntentData("playUrl", "rtmp://live.hkstv.hk.lxdns.com/live/hks");
                    setIntentData("isResult", 2);
                    setIntentData("startHour", 6);
                    setIntentData("startMinute", 0);
                    setIntentData("endHour", 23);
                    setIntentData("endMinute", 59);
                    setIntentData("isUploading", 2);
                    setIntentData("heartBeat", INITIAL_HERATBEAT);
                    setIntentData("screenSize", 1);
                    setIntentData("decodingWay", 1);
                    setIntentData("networkType", 1);
                    setIntentData("applicationType", 1);
                    setIntentData("uniqueness", "Pan");
                }
            }
        });
    }

    /**
     * 获取屏幕的分辨率
     */
    private void getScreenWidthHeigth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        wm.getDefaultDisplay().getMetrics(dm);
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        SpUtil.writeInt(Const.SPLIT_WIDTH, screenWidth);
        SpUtil.writeInt(Const.SPLIT_HEIGTH, screenHeight);
    }

    /**
     * 生成随机数字和字母,
     */
    public String getStringRandom() {
        String num = "num";
        int randomLength = 12;
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        //length为几位密码
        for (int i = 0; i < randomLength; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                val.append((char) (random.nextInt(6) + 97));
            } else if (num.equalsIgnoreCase(charOrNum)) {
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }

    /**
     * 创建文件件
     */
    private void createFile() {
        File appDir = new File(ROOT_DIRECTORY_URL + PACKAGE_NAME);
        if (!appDir.exists()) {
            boolean isSuccess = appDir.mkdirs();
            e("创建情况："+ isSuccess);
            Parameter add = new Parameter();
            add.setFileUrl(appDir.toString());
            add.save();
            isExistence = false;
        } else {
            isExistence = true;
            setIntentData("fileUrl", appDir.getAbsolutePath());
        }
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityIntent();
                }
            });
        }
    };

    /**
     * 网络加载等待图显示
     */
    private void activityIntent() {
        CustomActivityManager.getInstance().setTopActivity(null);
        if (count == 1) {
            Intent intent = new Intent(HomePageActivity.this, VideoViewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            mHomePage.setVisibility(View.GONE);
            Glide.with(HomePageActivity.this).pauseRequests();
        }
    }

    /**
     * 创建监听权限的接口对象
     */
    PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
        @Override
        public void passPermissons() {
            Plog.e("权限通过");
            createFile();
            if (isFirstStart(HomePageActivity.this)) {
                addLitepal();
            }
            switchingActivity();
        }

        @Override
        public void forbitPermissons() {
            PermissionsUtils.getInstance().chekPermissions(HomePageActivity.this, permissions, permissionsResult);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Plog.e("销毁");
        wifiSwitchpresenter.onDestroy();
        count = 0;
    }

    @Override
    public void wifiSwitchState(int state) {
        switch (state) {
            case WifiSwitchInterface.WIFI_STATE_DISABLED:
                Plog.e("WiFi 已经关闭");
                isIntent = true;
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            case WifiSwitchInterface.WIFI_STATE_DISABLING:
                Plog.e("WiFi 正在关闭");
                break;
            case WifiSwitchInterface.WIFI_STATE_ENABLED:
                if (isIntent) {
                    Plog.e("WiFi 已经打开");
                    activityIntent();
                    isIntent = false;
                }
                break;
            case WifiSwitchInterface.WIFI_STATE_ENABLING:
                Plog.e("WiFi 正在打开");
                break;
            case WifiSwitchInterface.ACTION_SERVICE_STATE_CHANGE:
                break;
            default:
        }
    }

}
