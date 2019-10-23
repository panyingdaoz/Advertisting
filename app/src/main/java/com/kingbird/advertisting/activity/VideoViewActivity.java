package com.kingbird.advertisting.activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kingbird.advertisting.R;
import com.kingbird.advertisting.base.Banner2;
import com.kingbird.advertisting.base.Base;
import com.kingbird.advertisting.base.BaseActivity;
import com.kingbird.advertisting.base.BasePopup;
import com.kingbird.advertisting.coms.TcpServer;
import com.kingbird.advertisting.jsonbean.BaiDuAdvParam;
import com.kingbird.advertisting.jsonbean.StartParam;
import com.kingbird.advertisting.litepal.AddJingDongAdv;
import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.litepal.PlayList;
import com.kingbird.advertisting.manager.CustomActivityManager;
import com.kingbird.advertisting.manager.ExecutorServiceManager;
import com.kingbird.advertisting.manager.ProtocolManager;
import com.kingbird.advertisting.manager.SocketManager;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.receiver.NetWorkReceiver;
import com.kingbird.advertisting.service.MonitorService;
import com.kingbird.advertisting.service.StartAppService;
import com.kingbird.advertisting.utils.AreaAveragingScale;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.GlideImageLoader;
import com.kingbird.advertisting.utils.GlideUtil;
import com.kingbird.advertisting.utils.MacUtil;
import com.kingbird.advertisting.utils.NetUtil;
import com.kingbird.advertisting.utils.SpUtil;
import com.kuaifa.ad.KuaiFaClient;
import com.kuaifa.ad.entry.DeviceEntry;
import com.kuaifa.ad.entry.DeviceNetworkEntry;
import com.kuaifa.ad.entry.DeviceUDIDEntry;
import com.kuaifa.ad.entry.ScreenSizeEntry;
import com.kuaifa.ad.result.GetAdResult;
import com.kuaifa.ad.value.MaterialType;
import com.kuaifa.ad.value.NetworkOperatorType;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoTextureView;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tsy.sdk.myokhttp.MyOkHttp;
import com.tsy.sdk.myokhttp.response.DownloadResponseHandler;
import com.tsy.sdk.myokhttp.response.JsonResponseHandler;
import com.tsy.sdk.myokhttp.response.RawResponseHandler;
import com.xboot.stdcall.DataforHandle;
import com.youth.banner.BannerConfig;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import startest.ys.com.poweronoff.PowerOnOffManager;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.kingbird.advertisting.base.Base.addParameterData;
import static com.kingbird.advertisting.base.Base.checkMd5;
import static com.kingbird.advertisting.base.Base.dataQuery;
import static com.kingbird.advertisting.base.Base.fileIsExists;
import static com.kingbird.advertisting.base.Base.fileNameQuery;
import static com.kingbird.advertisting.base.Base.getQuery;
import static com.kingbird.advertisting.base.Base.getSplitScreenShow;
import static com.kingbird.advertisting.base.Base.isAccessibilitySettingsOn;
import static com.kingbird.advertisting.base.Base.readBaiDuShow;
import static com.kingbird.advertisting.base.Base.readIntervalTimeShow;
import static com.kingbird.advertisting.base.Base.readJingDongShow;
import static com.kingbird.advertisting.base.Base.readLocalFile;
import static com.kingbird.advertisting.base.Base.removeFile;
import static com.kingbird.advertisting.base.Base.setCount;
import static com.kingbird.advertisting.base.Base.setIntentData;
import static com.kingbird.advertisting.base.Base.showReport;
import static com.kingbird.advertisting.base.Base.showSum;
import static com.kingbird.advertisting.base.Base.timeTrigger;
import static com.kingbird.advertisting.base.Base.updateDownloadCount;
import static com.kingbird.advertisting.base.Base.updateDownloadState;
import static com.kingbird.advertisting.base.Base.updateDownloadState2;
import static com.kingbird.advertisting.base.Base.vipShowSum;
import static com.kingbird.advertisting.utils.Config.ADD_JINGDONG;
import static com.kingbird.advertisting.utils.Config.APK_CHECK;
import static com.kingbird.advertisting.utils.Config.APPID;
import static com.kingbird.advertisting.utils.Config.APPKEY;
import static com.kingbird.advertisting.utils.Config.BAIDU_IS_PLAYED;
import static com.kingbird.advertisting.utils.Config.CONSTANT_FIFTEEN;
import static com.kingbird.advertisting.utils.Config.CONSTANT_FIVE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_FORTY;
import static com.kingbird.advertisting.utils.Config.CONSTANT_FOUR;
import static com.kingbird.advertisting.utils.Config.CONSTANT_ONE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_ONE_THOUSAND;
import static com.kingbird.advertisting.utils.Config.CONSTANT_TEN;
import static com.kingbird.advertisting.utils.Config.CONSTANT_THREE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_TWO;
import static com.kingbird.advertisting.utils.Config.DELETE_PLAYED_MEDIA;
import static com.kingbird.advertisting.utils.Config.DEVICE_TIME;
import static com.kingbird.advertisting.utils.Config.DOMAIN_NAME;
import static com.kingbird.advertisting.utils.Config.DOMAIN_NAME2;
import static com.kingbird.advertisting.utils.Config.END_JINGDONG;
import static com.kingbird.advertisting.utils.Config.FILE_SAVE_URL;
import static com.kingbird.advertisting.utils.Config.GET_BAIDU_AD;
import static com.kingbird.advertisting.utils.Config.GET_REDPACKET_AMOUNT;
import static com.kingbird.advertisting.utils.Config.GET_START_PARAM;
import static com.kingbird.advertisting.utils.Config.ILLEGAL_LOGO_URR;
import static com.kingbird.advertisting.utils.Config.JINGDONG_APP_HOST;
import static com.kingbird.advertisting.utils.Config.JINGDONG_APP_ID;
import static com.kingbird.advertisting.utils.Config.JINGDONG_APP_KEY;
import static com.kingbird.advertisting.utils.Config.JINGDONG_CODE;
import static com.kingbird.advertisting.utils.Config.JINGDONG_CODE1;
import static com.kingbird.advertisting.utils.Config.JINGDONG_CODE2;
import static com.kingbird.advertisting.utils.Config.JINGDONG_CODE3;
import static com.kingbird.advertisting.utils.Config.JINGDONG_REPORT;
import static com.kingbird.advertisting.utils.Config.OPEN_YUNZHONG_ACTION;
import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME;
import static com.kingbird.advertisting.utils.Config.REDPACKET_AMOUNT;
import static com.kingbird.advertisting.utils.Config.ROOT_DIRECTORY_URL;
import static com.kingbird.advertisting.utils.Config.SAVE_BAIDU_LOG;
import static com.kingbird.advertisting.utils.Config.SET_DEVICE_PARAM;
import static com.kingbird.advertisting.utils.Config.START_JINGDONG;
import static com.kingbird.advertisting.utils.Config.SUCCESS;
import static com.kingbird.advertisting.utils.Config.SUCCESS2;
import static com.kingbird.advertisting.utils.Config.USER_NAME;
import static com.kingbird.advertisting.utils.Config.VIDEO_TYPE;
import static com.kingbird.advertisting.utils.Config.YI_SHENG_MODEL_3288;
import static com.kingbird.advertisting.utils.Const.AD_MATERIAL_ID;
import static com.kingbird.advertisting.utils.Const.LOGO_SIZE;
import static com.kingbird.advertisting.utils.Const.QR_SIZE;
import static com.kingbird.advertisting.utils.Const.VOICE_CONTENT;
import static com.kingbird.advertisting.utils.Const.VOICE_ENABLE;
import static com.kingbird.advertisting.utils.NetUtil.isNetConnected;
import static com.kingbird.advertisting.utils.Plog.e;
import static com.kingbird.advertisting.utils.SpUtil.readString;

//import com.mstar.android.tv.TvTimerManager;
//import com.mstar.android.tvapi.common.vo.EnumTimerPeriod;
//import com.mstar.android.tvapi.common.vo.StandardTime;

/**
 * 这是广告UI界面活动
 *
 * @author panyingdao
 * @date 2017/8/10.
 */
@SuppressWarnings("ResourceType")
public class VideoViewActivity extends BaseActivity {

    private static final String TAG = VideoViewActivity.class.getSimpleName();
    private static final String MY_BROADCAST_TAG = "tcpServerReceiver";
    private String startParamUrl;
    private String logoUrl;
    private String tissueQrCodeUrl;
    private int deviceAppType, startCount = 0;
    private int isRedPacket;
    private int jdType = 0;
    private boolean logoShow, qrCodeShow, redPacket, hasRedPacket;
    private IntentFilter filter;
    private String mUri;
    private String text;
    private String model;
    private String currentPlayId;
    private String stopRequestId;
    private int deviceScreenWidth;
    private int deviceScreenHeight;
    private int screenRatio;
    private int showSumSize;
    private int imgeCount;

    //    private PLVideoTextureView mVideoView;api files('libs/pldroid-player-2.1.8.jar')
//    private PLVideoView mVideoView;
    private PLVideoTextureView mVideoView;
    private ImageView mAppLogo;
    private ImageView mCoverView;
    private ImageView mZxing;
    private ImageView mImage;
    private ImageView mRedPacket;
    private ImageView mTissue;
    private ImageView mSignal;
    private TextView mText;
    private TextView mTissueText;
    private GifImageView mGifImage;
    private ConstraintLayout mHotelLayout;
    private RelativeLayout mProductLayout;

    private int sdk17;
    private AudioManager mAudio;
    private MonitorService monitorService;
    private GifDrawable gifFromAssets;
    private RequestOptions options;
    private RequestOptions optionsLogo;
    private RequestOptions optionsNetwork;
    private RequestOptions optionsTissue;
    private RequestOptions optionsRedPacket;
    private RequestOptions optionsImage;
    private RequestOptions optionsImage2;
    private RequestOptions optionsStandby;
    private int redPacketMoney, hasRedPacketMoney;
    private String imagePath;

    private static boolean mIsStopped = false;
    private boolean isVisible = true;
    private boolean isJdStart = true;
    private boolean mIsNewPlay = true;
    private boolean mPlayResult = true;
    private boolean mIsVip = false;
    private boolean isJson = true;
    private boolean isRequestBaiDu = true, isRequestJd;
    private boolean isPlayIntervalTimeShow;
    private int jdCount;
    private long jdQuestTime = 0;
    private int index = 0;
    private int playType;
    private String rCode;
    private String showId;
    private int count = 0;
    private String error;
    private List<PlayList> qery;
    private List<String> time;
    private MarqueeView mMarqueeView;
    private Banner2 banner;

    private static String endJdPlay;
    private static ArrayList<String> currentJdPlay;

    private List<String> names = Arrays.asList("JPG", "JPEG", "PNG", "GIF");
    private byte[] command = new byte[1];
    private byte[] number = new byte[1];

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private final MyHandler myHandler = new MyHandler(this);
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    private static ArrayList<String> deleteFailure = new ArrayList<>();
    ArrayList<Integer> showSumList = new ArrayList<>();
    ArrayList<String> downloadList = new ArrayList<>();
    ArrayList<Integer> showIntervalTimeList = new ArrayList<>();
    private MyOkHttp mMyOkHttp;
    private MediaPlayer mMediaPlayer;

    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("atcivity-schedule-pool-%d").daemon(true).build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenType = SpUtil.readInt(Const.DEVICE_APP_TYPE);
        if (screenType == CONSTANT_TWO) {
            e("上下分屏");
            setContentView(R.layout.constraint);
        } else if (screenType == CONSTANT_FOUR) {
            e("左右分屏");
            setContentView(R.layout.layouttest);
        } else {
            e("上下分屏");
            setContentView(R.layout.constraint);
        }
        e("onCreate创建");
        e("VideoTextureActivity的状态", CustomActivityManager.getInstance().getTopActivity());

        readLocalFile();
        intervalTimeInspect();
        updateShowList();
        initView();
        playInitialize();
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(context, StartAppService.class));
            }
        });
        Intent intent = new Intent(context, MonitorService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        NetWorkReceiver.register(this);

        bindReceiver();
        getScreenWidthHeigth();
        videoPlay();
        queryLitepal();
        setOnOff();
        String enable = SpUtil.readString(Const.ENABLE);
        String start = "01";
        if (start.equals(enable)) {
            startMarquee();
        }
        getStartParam();
        if (!isNetConnected(context)) {
            e("网络异常！");
            setIntentData("networkType", 4);
            networkJudgment();
        } else {
            networkJudgment();
        }
        if (!isAccessibilitySettingsOn(this)) {
            ExecutorServiceManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(R.string.accessibility_remind);
                        }
                    });
                }
            }, 2, TimeUnit.SECONDS);
        }

//        deleteExpiredLogs(7);
//        Gdd gdd = new Gdd();
//        gdd.setPhone("15535958281");
//        gdd.setQuhao("86");
//        gdd.setType("1");
//        JSONObject post = new JSONObject();
//        JSONObject post = new JSONObject();
        Map<String,String> post = null;
        try {
            post.put("phone", "15535958281");
            post.put("quhao", "86");
            post.put("types", "1");
        KLog.e("发送的数据：："+post.toString());
        MyOkHttp myOkHttp = new MyOkHttp();
        myOkHttp.post()
                .url("http://jt.etac.io/api/registermessage/")
//                .jsonParams(post.toString())
                .params(post)
                .tag(this)
                .enqueue(new JsonResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, JSONObject response) {
                        KLog.e("doPostJSON onSuccess JSONObject2:" + response);
                    }

                    @Override
                    public void onSuccess(int statusCode, JSONArray response) {
                        KLog.e("doPostJSON onSuccess JSONArray:" + response);
                    }

                    @Override
                    public void onFailure(int statusCode, String errorMsg) {
                        KLog.e("doPostJSON onFailure:" + errorMsg);
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        setIntentData("deviceID", "00000001220");
//        TcpServer.getInstance().connect();139.196.137.120
//        UdpReceiveService.getInstance().run();
    }

    class Gdd {
        String phone;
        String quhao;
        String types;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getQuhao() {
            return quhao;
        }

        public void setQuhao(String quhao) {
            this.quhao = quhao;
        }

        public String getType() {
            return types;
        }

        public void setType(String type) {
            this.types = type;
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if (mAction != null) {
                if (MY_BROADCAST_TAG.equals(mAction)) {
                    String msg = intent.getStringExtra(MY_BROADCAST_TAG);
                    e("传过来的值", msg);
                    Message message = Message.obtain();
                    message.what = Integer.parseInt(msg);
                    message.obj = msg;
                    myHandler.sendMessage(message);
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {

        private WeakReference<VideoViewActivity> mActivity;

        private MyHandler(VideoViewActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        /**
         * @param msg msg.what的值分别表示：0：暂停1：停止2：播放3：远程控制实时播放4：清单控制播放、上一个、下一个
         *            5：字幕控制6:退出程序7:开关机控制8：音量控制
         */
        @Override
        public void handleMessage(Message msg) {
            VideoViewActivity activity = mActivity.get();
            List<Parameter> parameters = LitePal.findAll(Parameter.class);
            for (Parameter parameter : parameters) {
                String id = parameter.getDeviceId();
                if (activity != null) {
                    command[0] = (byte) 0x81;
                    number[0] = (byte) 0xA0;
                    int msgWhat = msg.what;
                    if (msgWhat < 10) {
                        handleMessage2(msg, parameter, id);
                    } else if (msgWhat > 30) {
                        handleMessage4(msg);
                    } else {
                        handleMessage3(msg, id, parameter.getrCode(), parameter);
                    }
                }
            }
        }

        private void handleMessage2(Message msg, Parameter parameter, String id) {
            switch (msg.what) {
                case 0:
                    e("远程控制暂停");
                    controlPause(parameter, id);
                    break;
                case 1:
                    e("远程控制停止");
                    stopPlay();
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 1, 1);
                    break;
                case 2:
                    e("远程控制播放");
                    controlPlay(parameter, id);
                    break;
                case 3:
                    assignPlay(parameter, id);
                    break;
                case 4:
                    int playType = SpUtil.readInt(Const.SPLIT_SCREEN);
                    e("播放类型", playType);
                    if (playType == 1) {
                        String controlNumber = SpUtil.readString(Const.CONTROL_NUMBER);
                        String ad = "AD";
                        if (ad.equals(controlNumber)) {
                            updateShowList();
                        }
                        showOperation(parameter, id);
                    } else if (playType == CONSTANT_TWO || playType == CONSTANT_THREE) {
                        String fileName = SpUtil.readString(Const.START_SCREENOR_FILE);
                        banner.setBannerPlayItem(fileName);
                    }
                    break;
                case 5:
                    subtitleControl(parameter, id);
                    break;
                case 6:
                    finish();
                    break;
                case 7:
                    setOnOff();
                    break;
                case 8:
                    int volume = SpUtil.readInt(Const.VOLUME);
                    volumeControl(mAudio, volume);
                    break;
                case 9:
                    int qrSize = SpUtil.readInt(QR_SIZE);
                    qrCodeShow2(id, qrSize);
                    appLogoInitialize();
                    break;
                case 10:
                    appLogoInitialize();
                    break;
                default:
                    break;
            }
        }

        private void showOperation(Parameter parameter, String id) {
            command[0] = (byte) 0x85;
            String controlNumber = SpUtil.readString(Const.CONTROL_NUMBER);
            if (showSumSize != 0) {
                switch (controlNumber) {
                    case "AD":
                        imgeCount = 0;
                        number[0] = (byte) 0xAD;
                        // 播放监听
                        boolean isShowPlay = SpUtil.readBoolean(Const.SHOW_PLAY);
                        e("是否播放", isShowPlay);
                        String fileName = parameter.getPlayUrl();
                        List<PlayList> querShow = Base.fileNameQuery("playId", fileName);
                        for (PlayList querShows : querShow) {
                            currentPlayId = Integer.toString(querShows.getPlayId());
                            closeAssetMusics();
                            if (isShowPlay) {
                                showPlay(fileName, false);
                            } else if (mIsNewPlay) {
                                mIsNewPlay = false;
                                showPlay(fileName, false);
                                SpUtil.writeBoolean(Const.SHOW_PLAY, false);
                            }
                        }
                        break;
                    case "B0":
                        previousShow();
                        number[0] = (byte) 0xB0;
                        break;
                    case "B1":
                        nextShow();
                        number[0] = (byte) 0xB1;
                        break;
                    default:
                        break;
                }
                controlPlayAnswer(id, parameter.getrCode(), command, number, 0, 1);
            } else {
                controlPlayAnswer(id, parameter.getrCode(), command, number, 0, 0);
            }
        }

        private void assignPlay(Parameter parameter, String id) {
            error = "82";
            command[0] = (byte) 0x82;
            number[0] = (byte) 0xA1;
            stopPlay();
            playInitialize();
            String playurl = parameter.getPlayUrl();
            e("直播地址", playurl);
            switch (parameter.getPlayType()) {
                case 1:
                    setIntentData("playUrl", playurl);
                    localPlay(playurl);
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 1, 1);
                    break;
                case 2:
                    setIntentData("playUrl", playurl);
                    localPlay(playurl);
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 2, 1);
                    break;
                case 3:
                    setIntentData("playUrl", playurl);
                    localPlay(playurl);
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 3, 1);
                    break;
                case 4:
                    String path = parameter.getFileUrl() + "/" + playurl;
                    e("本地地址", path);
                    if (fileIsExists(path)) {
                        setIntentData("playUrl", playurl);
                        localPlay(path);
                        controlPlayAnswer(id, parameter.getrCode(), command, number, 4, 1);
                    } else {
                        controlPlayAnswer(id, parameter.getrCode(), command, number, 4, 0);
                    }
                    break;
                default:
                    break;
            }
        }

        private void controlPlay(Parameter parameter, String id) {
            String videoName = ".mp4";
//            playInitialize();
            if (mIsStopped) {
                String videoPath;
                String playUrl = parameter.getPlayUrl();
                if (playUrl == null) {
                    e("获取开机路径播放");
                    assignLoop();
                } else {
                    e("获取实时路径播放");
                    if (playUrl.endsWith(videoName)) {
                        videoPath = FILE_SAVE_URL + playUrl;
                        if (fileIsExists(videoPath)) {
                            localPlay(videoPath);
                        } else {
                            controlPlayAnswer(id, parameter.getrCode(), command, number, 2, 0);
                        }
                    } else {
                        localPlay(playUrl);
                    }
                }
            } else {
                e("执行直接播放");
                mVideoView.start();
            }
            controlPlayAnswer(id, parameter.getrCode(), command, number, 2, 1);
        }

        private void controlPause(Parameter parameter, String id) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
                mIsStopped = false;
                if (!mVideoView.isPlaying()) {
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 0, 1);
                } else {
                    controlPlayAnswer(id, parameter.getrCode(), command, number, 0, 0);
                }
            }
        }

        private void subtitleControl(final Parameter parameter, final String id) {
            command[0] = (byte) 0x86;
            number[0] = (byte) 0xAB;
            String enable = SpUtil.readString(Const.ENABLE);
            switch (enable) {
                case "00":
                    stopScroll();
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().writeAnswer(id, parameter.getrCode(), command, number, true, "");
                        }
                    });
                    break;
                case "01":
                    text = SpUtil.readString(Const.CONTENT);
                    if (!TextUtils.isEmpty(text)) {
                        startMarquee();
                        ThreadManager.getInstance().doExecute(new Runnable() {
                            @Override
                            public void run() {
                                ProtocolManager.getInstance().writeAnswer(id, parameter.getrCode(), command, number, true, "");
                            }
                        });
                    } else {
                        ThreadManager.getInstance().doExecute(new Runnable() {
                            @Override
                            public void run() {
                                ProtocolManager.getInstance().writeAnswer(id, parameter.getrCode(), command, number, false, "");
                            }
                        });
                    }
                    break;
                default:
            }
        }

        private void controlPlayAnswer(final String id, final String rCode, final byte[] command, final byte[] number, final int playType, final int isResult) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    String controlNumber = SpUtil.readString(Const.CONTROL_TYPE);
                    switch (controlNumber) {
                        case "LAN":
                            e("配置软件控制回应");
                            ProtocolManager.getInstance().controlAnswer(id, rCode, command, number, playType, "0000", isResult, "client");
                            break;
                        case "INTERNET":
                            ProtocolManager.getInstance().controlAnswer(id, rCode, command, number, playType, "0000", isResult, "");
                            break;
                        default:
                            break;
                    }
                }
            });
        }

    }

    /**
     * handleMessage 3分支
     */
    private void handleMessage3(Message msg, final String id, final String rCode, Parameter parameter) {
        switch (msg.what) {
            case 11:
                showToast(R.string.splite_show);
                break;
            case 12:
                networkJudgment();
                break;
            case 13:
                isRequestBaiDu = true;
                baiDuPare(id);
                break;
            case 14:
                if (isNetConnected(context)) {
                    int qrSize = SpUtil.readInt(QR_SIZE);
                    qrCodeShow2(id, qrSize);
                }
                break;
            case 15:
                if (isNetConnected(context)) {
                    tissueQrCodeUrl = SpUtil.readString(Const.TISSUE_QRCODE_URL);
                    tissueVisibility(tissueQrCodeUrl);
                }
                break;
            case 16:
                updateSplitScreenShow();
                splitScreenImageShow();
                break;
            case 17:
                updateSplitScreenShow();
                String showId = SpUtil.readString(Const.SHOW_ID2);
                programmeControl(showId, id);
                break;
            case 18:
                String deleteFileName = SpUtil.readString(Const.START_SCREENOR_FILE);
                banner.deleteBannerPlayItem(deleteFileName);
                break;
            case 19:
                updateShowList();
                break;
            case 20:
                showIntervalTimeList = readIntervalTimeShow();
                break;
            case 21:
                int logoSize = SpUtil.readInt(LOGO_SIZE);
                int qrSize = SpUtil.readInt(QR_SIZE);
                String url = SpUtil.readString(Const.LOGO_URL);
                setLogoGlideSize(logoSize);
                logoShow(url);
                qrCodeShow2(id, qrSize);
                break;
            case 22:
                voiceControl(id, rCode, true, parameter.getPlayUrl());
                break;
            case 23:
                isRequestJd = SpUtil.readBoolean(Const.IS_START_JINGDONG);
                getAdDemo(id);
                break;
            case 24:
                inputBoxDisplay(true);
                break;
            case 25:
                appLogoInitialize();
                break;
            default:
        }
    }

    /**
     * 语音控制
     */
    private void voiceControl(final String id, final String rCode, boolean isReturn, String playPath) {
        command[0] = (byte) 0x97;
        number[0] = (byte) 0xBA;
        String enableVoice = SpUtil.readString(Const.VOICE_ENABLE);
        e("语音是否开启", enableVoice);
        switch (enableVoice) {
            case "00":
                closeAssetMusics();
                if (isReturn) {
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().writeAnswer(id, rCode, command, number, true, "");
                        }
                    });
                }
                break;
            case "01":
                String text = SpUtil.readString(Const.VOICE_CONTENT);
                String fileSuffix = null;
                if (playPath != null) {
                    fileSuffix = playPath.substring(playPath.lastIndexOf(".") + 1).toUpperCase();
                }
                if (!TextUtils.isEmpty(text)) {
                    if (!redPacket || redPacketMoney == 0) {
                        if (!VIDEO_TYPE.equals(fileSuffix)) {
//                            openAssetMusics("hongbao-tts.mp3");
                        }
                    }
                    if (isReturn) {
                        ThreadManager.getInstance().doExecute(new Runnable() {
                            @Override
                            public void run() {
                                ProtocolManager.getInstance().writeAnswer(id, rCode, command, number, true, "");
                            }
                        });
                    }
                } else {
                    if (isReturn) {
                        ThreadManager.getInstance().doExecute(new Runnable() {
                            @Override
                            public void run() {
                                ProtocolManager.getInstance().writeAnswer(id, rCode, command, number, false, "");
                            }
                        });
                    }
                }
                break;
            default:
        }
    }

    /**
     * handleMessage 4分支
     */
    private void handleMessage4(Message msg) {
        switch (msg.what) {
            case 101:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTissueText.setVisibility(View.GONE);
                        mTissue.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case 102:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(R.string.tissue_text2);
                        mTissueText.setVisibility(View.VISIBLE);
                        mTissue.setVisibility(View.GONE);
                    }
                });
                break;
            case 103:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(R.string.qrCode_failure);
                    }
                });
            case 104:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(R.string.tissue_text3);
                        mTissueText.setVisibility(View.VISIBLE);
                        mTissue.setVisibility(View.GONE);
                    }
                });
                break;
            default:
        }
    }

    /**
     * 控件初始化
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        SpUtil.writeInt(Const.RESTART_APP_TIME, 30);
        sdk17 = 1;
        context = VideoViewActivity.this;
        mAudio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        mVideoView = findViewById(R.id.VideoView);
        mMarqueeView = findViewById(R.id.MarqueeView);
        banner = findViewById(R.id.banner);
        mImage = findViewById(R.id.ImagePlay);
        mTissue = findViewById(R.id.Tissue);
        mRedPacket = findViewById(R.id.RedPacket);
        mGifImage = findViewById(R.id.GifImage);
        mZxing = findViewById(R.id.ZXing);
        mAppLogo = findViewById(R.id.AppLogo);
        mText = findViewById(R.id.DeviceId);
        mCoverView = findViewById(R.id.CoverView);
        mSignal = findViewById(R.id.Signal);
        mTissueText = findViewById(R.id.TissueText);
        mHotelLayout = findViewById(R.id.RelativeHotel);
        mProductLayout = findViewById(R.id.Product);

        optionsImage = new RequestOptions()
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        optionsImage2 = new RequestOptions();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5000L, TimeUnit.MILLISECONDS)
                .readTimeout(5000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();
        mMyOkHttp = new MyOkHttp(okHttpClient);
    }

    /**
     * 播放控件初始化
     */
    private void playInitialize() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            e("先释放再初始化！");
        }
        int codec = 0;
        List<Parameter> decode = LitePal.findAll(Parameter.class);
        for (Parameter decodes : decode) {
            switch (decodes.getDecodingWay()) {
                case 1:
                    codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
                    break;
                case 2:
                    codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE);
                    break;
                default:
                    break;
            }
        }
        AVOptions options = new AVOptions();
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        mVideoView.setAVOptions(options);

        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnInfoListener(mOnInfoListener);

        // You can also use a custom `MediaController` widget
//        mMediaController = new MediaController(this, false, true);
//        mVideoView.setMediaController(mMediaController);
    }

    /**
     * 广播绑定
     */
    private void bindReceiver() {
        filter = new IntentFilter(MY_BROADCAST_TAG);
        filter.addAction(MY_BROADCAST_TAG);
    }

    /**
     * 设置屏参获取、设定
     */
    private void getScreenWidthHeigth() {
        int screenHeightReaPacket, screenWidthReaPacket;
        Display display1 = getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display1.getSize(outSize);
        deviceScreenWidth = outSize.x;
        deviceScreenHeight = outSize.y;
//        Plog.e("屏的分辨率：", outSize.x + "*" + outSize.y);
        model = android.os.Build.MODEL;
//        String aaa2 = android.os.Build.SERIAL;  //硬件的唯一标识，唯一ID
//        Plog.e("唯一标识",aaa2);
        SpUtil.writeString(Const.DEVICE_MODEL, model);
//        Plog.e("设备型号：", model);
        BuglyLog.e("设备型号", model);
        SpUtil.writeInt(Const.SPLIT_WIDTH, deviceScreenHeight);
        SpUtil.writeInt(Const.SPLIT_HEIGTH, deviceScreenHeight);
        e("屏的分辨率：", deviceScreenWidth + "*" + deviceScreenHeight);
        if (deviceScreenWidth > deviceScreenHeight) {
            //大二维码比例为5，正常比例为6，横屏
            screenWidthReaPacket = deviceScreenHeight / 5;
            screenHeightReaPacket = deviceScreenHeight / 5;
            screenRatio = deviceScreenHeight;
            e("横屏", screenRatio);
            SpUtil.writeInt(Const.DEVICEID_TYPE, 1);
            SpUtil.writeInt(Const.SCREENOR_ORIENT, 1);
        } else {
            //门禁比例为8,正常比例为8，竖屏
            screenRatio = deviceScreenWidth;
            e("竖屏", screenRatio);
            screenWidthReaPacket = deviceScreenWidth / 6;
            screenHeightReaPacket = deviceScreenWidth / 6;
            SpUtil.writeInt(Const.DEVICEID_TYPE, 2);
        }

        int logoSize = SpUtil.readInt(LOGO_SIZE);
        setLogoGlideSize(logoSize);

        options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(deviceScreenWidth, deviceScreenHeight);
        optionsTissue = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(screenRatio / 8, screenRatio / 8);
        optionsRedPacket = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(screenWidthReaPacket, screenHeightReaPacket);
        optionsNetwork = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(screenRatio / 25, screenRatio / 25);
    }

    /**
     * logo显示大小控制
     */
    private void setLogoGlideSize(int scalingRatio) {
        int imageSize;
        if (scalingRatio == 0) {
            scalingRatio = 8;
        }
        int deviceType = SpUtil.readInt(Const.DEVICEID_TYPE);
        if (deviceType == 1) {
            imageSize = screenRatio / scalingRatio;
        } else {
            imageSize = screenRatio / (scalingRatio + 1);
        }

        optionsLogo = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .override(imageSize, imageSize);
    }

    /**
     * 节目播放
     */
    private void videoPlay() {
        setIntentData("playType", 4);
        e("当前清单数量", showSumSize);
        if (showSumSize > 0) {
            e("下标值", index);
            getJingDongAd();
            playVideo();
        } else {
            assignLoop();
        }
    }

    /**
     * 启动后检查京东广告
     */
    private void getJingDongAd() {
        int adSize = readJingDongShow().size();
        if (adSize == 0) {
            List<Parameter> parameterList = LitePal.findAll(Parameter.class);
            for (Parameter parameter : parameterList) {
                isRequestJd = SpUtil.readBoolean(Const.IS_START_JINGDONG);
                getAdDemo(parameter.getDeviceId());
            }
        }
    }

    /**
     * 设备数据查询
     */
    public void queryLitepal() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                List<Parameter> query = LitePal.findAll(Parameter.class);
                for (Parameter parameter : query) {
                    String deviceId = parameter.getDeviceId();
                    e("查询区号", parameter.getrCode());
                    String ipAddress = parameter.getIp();
                    int port = parameter.getPort();
                    int startPlayType = parameter.getStartPlayType();
                    e("查询IP", ipAddress);
                    e("查询端口", port);
                    e("查询协议类型", parameter.getProtocolType());
                    e("查询开始播放类型", startPlayType);
                    e("查询开始播放路径", parameter.getStartPlayUrl());
                    e("查询实时播放类型", parameter.getPlayType());
                    e("查询实时播放路径", parameter.getPlayUrl());
                    e("开关机是否有效", parameter.getIsResult());
                    e("开机时间", parameter.getStartHour() + ":" + parameter.getStartMinute());
                    e("关机时间", parameter.getEndHour() + ":" + parameter.getEndMinute());
                    e("心跳周期", parameter.getHeartBeat());
                    e("屏尺寸", parameter.getScreenSize());
                    e("解码方式", parameter.getDecodingWay());
                    e("应用类型", parameter.getApplicationType());
                    BuglyLog.e("设备ID", deviceId);
                    BuglyLog.e("查询IP", ipAddress);
                    BuglyLog.e("查询端口", Integer.toString(port));
                    BuglyLog.e("查询开始播放类型", Integer.toString(startPlayType));
                    BuglyLog.e("查询开始播放路径", parameter.getStartPlayUrl());
                    BuglyLog.e("查询实时播放路径", parameter.getPlayUrl());
                    BuglyLog.e("应用类型", Integer.toString(parameter.getApplicationType()));
                    UdpView.getInstance().connectUdp(ipAddress);

                    if (deviceId == null) {
                        if (isNetConnected(context)) {
                            int result = LitePal.deleteAll(Parameter.class);
                            e("删除Parameter表结果", result);
                            if (result > 0) {
                                String mac = MacUtil.getMac(context);
                                if (!TextUtils.isEmpty(mac)) {
                                    String id = mac.substring(1, 12);
                                    addParameterData(id);
                                }
                            }
                        }
                    }

                    deviceDataReport(parameter, ipAddress, port);

                    try {
                        if (deleteFailure.size() > 0) {
                            for (int i = 0; i < deleteFailure.size(); i++) {
                                programmeControl(deleteFailure.get(i), deviceId);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    /**
     * 给web上传设备参数
     */
    private void deviceDataReport(final Parameter parameter, final String ipAddress, final int port) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject userJson = setJsonUser();
                    JSONObject deviceJson = new JSONObject();
                    deviceJson.put("deviceId", parameter.getDeviceId());
                    deviceJson.put("appVersion", APK_CHECK);
                    deviceJson.put("screenWidth", deviceScreenWidth);
                    deviceJson.put("screenHeight", deviceScreenHeight);
                    deviceJson.put("ipAddress", ipAddress);
                    deviceJson.put("port", port);

                    JSONObject postJson = new JSONObject();
                    postJson.put("user", userJson);
                    postJson.put("param", deviceJson);

                    Base.webPostReport(postJson, SET_DEVICE_PARAM);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @NonNull
    private JSONObject setJsonUser() throws JSONException {
        JSONObject userJson = new JSONObject();
        userJson.put("userName", USER_NAME);
        userJson.put("appId", APPID);
        userJson.put("appKey", APPKEY);
        return userJson;
    }

    /**
     * 开关机方法
     */
    private void setOnOff() {
        List<Parameter> parameters = LitePal.findAll(Parameter.class);
        for (Parameter parameter : parameters) {
            int isResult = parameter.getIsResult();
            e("是否启用", isResult);
            int onHour = parameter.getStartHour();
            int onMinute = parameter.getStartMinute();
            int offHour = parameter.getEndHour();
            int offMinute = parameter.getEndMinute();
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            switch (isResult) {
                case 1:
                    if ("v40".equals(model)) {
                        setYnhOnOff(model, false, onHour, onMinute, offHour, offMinute, year, month, day);
                    } else if (YI_SHENG_MODEL_3288.equals(model)) {
                        PowerOnOffManager powerOnOffManager = PowerOnOffManager.getInstance(this);
                        powerOnOffManager.clearPowerOnOffTime();
                    } else if ("rk312x".equals(model)) {
                        String msg = "{checkSwitch:true,type:0,settings:[]}";
                        Intent powerOnOffTimerIntent = new Intent("com.zhsd.setting.POWER_ON_OFF_TIMER");
                        powerOnOffTimerIntent.putExtra("data", msg);
                        powerOnOffTimerIntent.putExtra("owner", "0");
                        sendBroadcast(powerOnOffTimerIntent);
                    } else {
                        settings("0", onHour, onMinute, offHour, offMinute);
                    }
                    break;
                case 2:
                    if ("v40".equals(model) || "YF_XXXG".equals(model) || "magton".equals(model) || "Allwinner-Tablet".equals(model)
                            || "QUAD-CORE T3 p1".equals(model)) {
                        setYnhOnOff(model, true, onHour, onMinute, offHour, offMinute, year, month, day);
                    } else if ("3280".equals(model) || "3288".equals(model)) {
                        settings("1", onHour, onMinute, offHour, offMinute);
                    } else if ("rk3328-box".equals(model)) {
                        setPowerOnOff(onHour, onMinute, offHour, offMinute);
                    } else if ("rk312x".equals(model) || YI_SHENG_MODEL_3288.equals(model)) {
                        setRk3128OnOff(onHour, onMinute, offHour, offMinute);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设备特殊参数获取
     */
    private void getStartParam() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                List<Parameter> parameterList = LitePal.findAll(Parameter.class);
                for (Parameter parameterLists : parameterList) {
                    final String deviceId = parameterLists.getDeviceId();
                    startParamUrl = GET_START_PARAM + deviceId;
                    e("开机获取数据路径", startParamUrl);
                    mMyOkHttp.get()
                            .url(startParamUrl)
                            .tag(this)
                            .enqueue(new RawResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, String response) {
                                    startParamParser(response, deviceId);
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {
                                    e("设备初始数据获取失败原因:" + errorMsg);
                                    startCount++;
                                    if (startCount < 3) {
                                        getStartParam();
                                    } else {
                                        try {
                                            deviceAppType = SpUtil.readInt(Const.DEVICE_APP_TYPE);
                                            logoShow = SpUtil.readBoolean(Const.LOGO_SHOW);
                                            qrCodeShow = SpUtil.readBoolean(Const.QR_CODE_SHOW);
                                            hasRedPacket = SpUtil.readBoolean(Const.HAS_READPECKET);
                                            logoUrl = SpUtil.readString(Const.LOGO_URL);
                                            tissueQrCodeUrl = SpUtil.readString(Const.TISSUE_QRCODE_URL);
                                            e("加载二维码显示");
                                            showOperation(deviceId);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                }
            }


        });
    }

    /**
     * 开机参数解析
     */
    private void startParamParser(final String response, final String deviceId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    StartParam startParam = StartParam.objectFromData(response);
                    e("获取数据结果", response + "\n数据长度" + response.length());
                    boolean requestResult = startParam.isSuccess();
                    e("访问结果", requestResult);
                    if (requestResult || response.length() > CONSTANT_FORTY) {
                        String qrCodeUrl = startParam.getQrCodeUrl();
                        e("二维码路径", qrCodeUrl);
                        logoUrl = (String) startParam.getLogoUrl();
                        e("logo路径", logoUrl);
                        String landscapeUrl = (String) startParam.getLandscapeUrl();
                        e("横屏图片路径", landscapeUrl);
                        String portraitUrl = (String) startParam.getPortraitUrl();
                        e("竖屏图片路径", portraitUrl);
                        deviceAppType = startParam.getDeviceAppType();
                        e("设备类型", deviceAppType);
                        logoShow = startParam.isLogoShow();
                        e("是否显示logo", logoShow);
                        qrCodeShow = startParam.isQrCodeShow();
                        e("是否显示二维码", qrCodeShow);
                        tissueQrCodeUrl = startParam.getTissueQRCodeUrl();
                        e("纸巾二维码", tissueQrCodeUrl);
                        hasRedPacket = startParam.isHasRedPacket();
                        e("整点红包路径", hasRedPacket);

                        SpUtil.writeString(Const.LOGO_URL, logoUrl);
                        SpUtil.writeString(Const.TISSUE_QRCODE_URL, tissueQrCodeUrl);
                        SpUtil.writeInt(Const.DEVICE_APP_TYPE, deviceAppType);
                        SpUtil.writeBoolean(Const.LOGO_SHOW, logoShow);
                        SpUtil.writeBoolean(Const.QR_CODE_SHOW, qrCodeShow);
                        SpUtil.writeBoolean(Const.HAS_READPECKET, hasRedPacket);
                        if (landscapeUrl != null) {
                            SpUtil.writeString(Const.LANDSCAPE_URL, landscapeUrl);
                            e("横存储");
                        }
                        if (portraitUrl != null) {
                            e("竖存储");
                            SpUtil.writeString(Const.PORTRAIT_URL, portraitUrl);
                        }

                        if (showSumSize == 0 && isVisible) {
                            standbyVisibility(landscapeUrl, portraitUrl);
                        }
                        showOperation(deviceId);
                    } else {
                        deviceAppType = SpUtil.readInt(Const.DEVICE_APP_TYPE);
                        logoShow = SpUtil.readBoolean(Const.LOGO_SHOW);
                        qrCodeShow = SpUtil.readBoolean(Const.QR_CODE_SHOW);
                        hasRedPacket = SpUtil.readBoolean(Const.HAS_READPECKET);
                        logoUrl = SpUtil.readString(Const.LOGO_URL);
                        tissueQrCodeUrl = SpUtil.readString(Const.TISSUE_QRCODE_URL);
                        String lUrl = SpUtil.readString(Const.LANDSCAPE_URL);
                        String pUrl = SpUtil.readString(Const.PORTRAIT_URL);

                        if (showSumSize == 0 && isVisible) {
                            standbyVisibility(lUrl, pUrl);
                        }
                        showOperation(deviceId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    e("异常原因", e.toString());
                }
            }
        });
    }

    /**
     * logo加载
     */
    private void appLogoInitialize() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = SpUtil.readString(Const.LOGO_SHOW_DATA);
                    e("logo路径", url);
                    logoShow(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 字幕加载
     */
    private void startMarquee() {
        text = SpUtil.readString(Const.CONTENT);
        if (!TextUtils.isEmpty(text)) {
            boolean isTime = text.startsWith("绑定设备验证码");
            e("字幕内容", text);
            mMarqueeView.setText(text);
            mMarqueeView.startScroll();
            mMarqueeView.setVisibility(View.VISIBLE);

            if (isTime) {
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        stopScroll();
                        SpUtil.writeString(Const.CONTENT, "");
                        e("取消验证码字幕滚动");
                    }
                }, 2, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * 字幕停止
     */
    private void stopScroll() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMarqueeView.stopScroll();
                mMarqueeView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 显示控制
     */
    private void showOperation(String deviceId) {
        if (!logoShow) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    GlideUtil.load(VideoViewActivity.this, R.drawable.app_name, optionsLogo, mAppLogo);
//                    mAppLogo.setVisibility(View.VISIBLE);
//                }
//            });
//            e("logoUrl 路径", logoUrl);
//            if (logoUrl != null) {
            final String mLogoUrl = DOMAIN_NAME2 + logoUrl;
            if (!mLogoUrl.equals(DOMAIN_NAME2) && !mLogoUrl.equals(ILLEGAL_LOGO_URR)) {
                String logoName = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
                final String saveDir = FILE_SAVE_URL + logoName;
                downloadLogo(saveDir, mLogoUrl);

                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                e("最终logo路径", mLogoUrl);
//                                    if (!mLogoUrl.equals(DOMAIN_NAME) && !mLogoUrl.equals(ILLEGAL_LOGO_URR)) {
                                GlideUtil.qrLoad(VideoViewActivity.this, saveDir, optionsLogo, mAppLogo);
                                mAppLogo.setVisibility(View.VISIBLE);
//                                    }
                            }
                        });
                    }
                }, 5, TimeUnit.SECONDS);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlideUtil.load(VideoViewActivity.this, R.drawable.app_name, optionsLogo, mAppLogo);
                        mAppLogo.setVisibility(View.VISIBLE);
                    }
                });
            }
//            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAppLogo.setVisibility(View.GONE);
                }
            });
        }
        if (!qrCodeShow) {
            int qrSize = SpUtil.readInt(QR_SIZE);
            qrCodeShow2(deviceId, qrSize);
        } else {
            mText.setTextSize(8);
            textZxingVisible();
        }
        if (deviceAppType == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHotelLayout.setVisibility(View.GONE);
                }
            });
        } else if (deviceAppType == 1) {
            if (tissueQrCodeUrl != null) {
                tissueVisibility(tissueQrCodeUrl);
            }
        } else if (deviceAppType == CONSTANT_TWO || deviceAppType == CONSTANT_FOUR) {
            splitScreenImageShow();
        } else if (deviceAppType == CONSTANT_THREE) {
            e("显示商品图");
            mProductLayout.setVisibility(View.VISIBLE);
        }

        if (hasRedPacket) {
            String pictureFileName = "redpacket" + deviceId + ".jpg";
            SpUtil.writeString(Const.HAS_REDPACKET_NAME, pictureFileName);
            e("文件名", pictureFileName);
            String picture = DOMAIN_NAME + "QRCode/" + pictureFileName;
//            String saveDir = FILE_SAVE_URL + pictureFileName;
            String videoPaths = FILE_SAVE_URL + pictureFileName;
            if (Base.fileIsExists(videoPaths)) {
                initTimePrompt();
            } else {
                Base.pictureDownload(context, picture, videoPaths);
                initTimePrompt();
            }
        }
    }

    /**
     * 纸巾二维码显示控制
     */
    private void tissueVisibility(String tissueQrCodeUrl) {
        try {
            final String mTissueQrCodeUrl = DOMAIN_NAME + tissueQrCodeUrl.substring(1);
            e("纸巾机二维码完整路径和是否可以显示二维码", mTissueQrCodeUrl);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProductLayout.setVisibility(View.GONE);
                    GlideUtil.qrLoad(VideoViewActivity.this, mTissueQrCodeUrl, optionsTissue, mTissue);
                    mTissue.setVisibility(View.VISIBLE);
                }
            });
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * 副屏图片控制
     */
    private void splitScreenImageShow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> arrList = getSplitScreenShow();
                int screenSize = arrList.size();
                if (screenSize > 0) {
                    e("显示副屏图片");
                    //设置图片加载器
                    banner.setImageLoader(new GlideImageLoader());
                    banner.setViewPagerIsScroll(false);
                    banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
                    banner.setIndicatorGravity(BannerConfig.CENTER);
                    banner.setDelayTime(15 * 1000);
                    //设置图片集合
                    banner.setImages(arrList);
                    //banner设置方法全部调用完毕时最后调用
                    banner.start();
                    if (mHotelLayout.getVisibility() == View.GONE) {
                        mHotelLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mHotelLayout.getVisibility() == View.VISIBLE) {
                        mHotelLayout.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * logo显示
     */
    private void logoShow(String logoUrl) {
//        e("logo路径", logoUrl);
        if (!"".equals(logoUrl) && logoUrl != null) {
            final String mLogoUrl = DOMAIN_NAME2 + logoUrl;
            if (!mLogoUrl.equals(DOMAIN_NAME2) && !mLogoUrl.equals(ILLEGAL_LOGO_URR)) {
                e("最终logo路径", mLogoUrl);
                String logoName = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
                final String saveDir = FILE_SAVE_URL + logoName;
                downloadLogo(saveDir, mLogoUrl);

                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                GlideUtil.qrLoad(VideoViewActivity.this, mLogoUrl, optionsLogo, mAppLogo);
                                GlideUtil.qrLoad(VideoViewActivity.this, saveDir, optionsLogo, mAppLogo);
                                mAppLogo.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }, 5, TimeUnit.SECONDS);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    e("即投logo显示");
                    GlideUtil.load(VideoViewActivity.this, R.drawable.app_name, optionsLogo, mAppLogo);
                    mAppLogo.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * 下载logo到本地
     */
    private void downloadLogo(String saveDir, String mLogoUrl) {
        if (!fileIsExists(saveDir)) {
            e("logo不存在开启下载");
            Base.pictureDownload(context, mLogoUrl, saveDir);
        }
    }

    /**
     * 二维码显示
     */
    private void qrCodeShow2(String deviceId, int qrSize) {
        String isId = "000", qrDeviceId;
        qrDeviceId = deviceId.substring(0, 3);
        if (isId.equals(qrDeviceId) && deviceId.length() == 11) {
            final String fileName = "qr" + deviceId + ".jpg";
            final String mZxingPath = DOMAIN_NAME + "qrcode/" + fileName;
            final String saveDir = FILE_SAVE_URL + fileName;
            Base.pictureDownload(context, mZxingPath, saveDir);
            final int imageSize;
            int deviceType = SpUtil.readInt(Const.DEVICEID_TYPE);
            if (qrSize == 0) {
                qrSize = 8;
            }
            if (deviceType == 1) {
                imageSize = screenRatio / qrSize;
            } else {
                imageSize = screenRatio / (qrSize + 1);
            }

            ExecutorServiceManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RequestOptions options = new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .fitCenter()
                                    .override(imageSize, imageSize);
                            try {
                                Bitmap bitmap = BitmapFactory.decodeFile(saveDir);
                                AreaAveragingScale averagingScale = new AreaAveragingScale(bitmap);
                                GlideUtil.qrLoad2(VideoViewActivity.this, averagingScale.getScaledBitmap
                                        (imageSize, imageSize), options, mZxing);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            mZxing.setVisibility(View.VISIBLE);
                            if (mText.getVisibility() == View.VISIBLE) {
                                mZxing.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }, 5, TimeUnit.SECONDS);
        }

        ExecutorServiceManager.getInstance().schedule(deviceIdTask, 8, TimeUnit.SECONDS);
    }

    private void settings(String state, int onHour, int onMinute, int offHour, int offMinute) {
        e("3288主板 开关机设置");
//        List<Parameter> parameter = LitePal.findAll(Parameter.class);
//        for (Parameter parameters : parameter) {
//            int isResult = parameters.getIsResult();
        String onTime = onHour + ":" + onMinute;
        String offTime = offHour + ":" + offMinute;
//            if (isResult == CONSTANT_TWO) {
        DataforHandle dh = new DataforHandle();
        dh.setonoff(this, new String[]{state, onTime, offTime});
        setTimeState(state);
//            }
//        }
    }

    private void setPowerOnOff(int onHour, int onMinute, int offHour, int offMinute) {
        Intent intent = new Intent("android.q-zheng.action.POWERONOFF");
        int[] powerOn = {onHour, onMinute};
        int[] powerOff = {offHour, offMinute};
        intent.putExtra("timeon", powerOn);
        intent.putExtra("timeoff", powerOff);
        intent.putExtra("type", 2);
        intent.putExtra("enable", true);
        sendBroadcast(intent);
        e("设置开关机时间,杜先生盒子");
    }

    private void setRk3128OnOff(int onHour, int onMinute, int offHour, int offMinute) {
        PowerOnOffManager manager = PowerOnOffManager.getInstance(this);
        int[] powerOnTime = {onHour, onMinute};
        int[] powerOffTime = {offHour, offMinute};
        int[] weekDays = {1, 1, 1, 1, 1, 1, 1};
        manager.setPowerOnOffWithWeekly(powerOnTime, powerOffTime, weekDays);
        e("3128、3288亿晟主板开关机");
    }

    private void setYnhOnOff(String model, boolean isRun, int onHour, int onMinute,
                             int offHour, int offMinute, int year, int month, int day) {
        String modelName1 = "YF_XXXG", modelName2 = "magton", modelName3 = "Allwinner-Tablet";
        int[] timeOn, timeOff;
        int startTime = onHour * 60 + onMinute;
        int endTime = offHour * 60 + offMinute;
        int time = startTime - endTime;
        e("间隔时间", time);
        if (time > CONSTANT_FIVE) {
            timeOn = new int[]{year, month, day, onHour, onMinute};
        } else {
            timeOn = new int[]{year, month, (day + 1), onHour, onMinute};
        }
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int mi = cal.get(Calendar.MINUTE);
//        int s = cal.get(Calendar.SECOND);
        long currentTime = h * 3600 + mi * 60;
        long offTime = endTime * 60;
        if (offTime < currentTime) {
            e("未来时间");
            timeOn = new int[]{year, month, (day + 1), onHour, onMinute};
            timeOff = new int[]{year, month, (day + 1), offHour, offMinute};
        } else {
            e("当天时间");
            timeOff = new int[]{year, month, day, offHour, offMinute};
        }

        if (modelName1.equals(model) || modelName3.equals(model)) {
            e("ontime:", Arrays.toString(timeOn));
            e("offtime:", Arrays.toString(timeOff));
            setOnOffTime(context, year, month, day, onHour, onMinute, offHour, offMinute);
            e("雍慧主板开关机");
        } else if (modelName2.equals(model)) {
            Intent intentMagton = new Intent("android.intent.action.gz.setpoweronoff");
            intentMagton.putExtra("timeon", timeOn);
            intentMagton.putExtra("timeoff", timeOff);
            intentMagton.putExtra("enable", isRun);
            sendBroadcast(intentMagton);
            e("视新主板开关机");
        } else {
            Intent intentV40 = new Intent("android.intent.action.gz.setpoweronoff");
            intentV40.putExtra("timeon", timeOn);
            intentV40.putExtra("timeoff", timeOff);
            intentV40.putExtra("enable", isRun);
            sendBroadcast(intentV40);
            e("音诺恒主板开关机");
        }
    }

    private void setOnOffTime(Context context, int year, int month, int day, int onHour,
                              int onMinute, int offHour, int offMinute) {
        Calendar c1 = Calendar.getInstance();
        c1.clear();
        c1.set(year, month, day, onHour, onMinute);
        long millsOn = c1.getTimeInMillis();
        Calendar c2 = Calendar.getInstance();
        c2.clear();
        c2.set(year, month, day, offHour, offMinute);
        long millsOff = c2.getTimeInMillis();

        final Calendar mCalendar = Calendar.getInstance();
        int mYear, mMonth, mDay, mHour, mMinute;
        int offmYear, offmMonth, offmDay, offmHour, offmMinute;
        long time = System.currentTimeMillis();

        e("timeOn毫秒值", millsOn);
        e("当前毫秒值", time);
        e("timeOff毫秒值", millsOff);
        long addOn = millsOn - time;
        long addOff = millsOff - time;
        e("开始间隔", addOn);
        e("结束间隔", addOff);
        mCalendar.setTimeInMillis(time + addOn);
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH);
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);

        mCalendar.setTimeInMillis(time + addOff);
        offmYear = mCalendar.get(Calendar.YEAR);
        offmMonth = mCalendar.get(Calendar.MONTH);
        offmDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        offmHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        offmMinute = mCalendar.get(Calendar.MINUTE);

        int[] timeonArray = {mYear, mMonth, mDay, mHour, mMinute};
        int[] timeoffArray = {offmYear, offmMonth, offmDay, offmHour, offmMinute};
        e("ontime:", Arrays.toString(timeonArray));
        e("offtime:", Arrays.toString(timeoffArray));

        if (addOff > 0) {
            Intent intent = new Intent("android.56iq.intent.action.setpoweronoff");
            intent.putExtra("timeon", timeonArray);
            intent.putExtra("timeoff", timeoffArray);
            intent.putExtra("enable", true);
            context.sendBroadcast(intent);
        }
    }

    /**
     * 停止视频播放
     */
    public void stopPlay() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mIsStopped = true;
        }
    }

    /**
     * 本地播放
     */
    public void localPlay(String videoPath) {
        setImageGone();
        imageGone();
        mVideoView.setVideoPath(videoPath);
        mVideoView.start();
    }

    /**
     * 循环播放
     */
    private void assignLoop() {
        List<Parameter> startPlayFile = LitePal.findAll(Parameter.class);
        for (Parameter parameter : startPlayFile) {
            String startUrl = parameter.getStartPlayUrl();
            final String videoPaths = FILE_SAVE_URL + startUrl;
            if (fileIsExists(videoPaths)) {
                imageGone();
                String fileType = startUrl.substring(startUrl.lastIndexOf(".") + 1);
                String enableVoice = SpUtil.readString(Const.VOICE_ENABLE);
                boolean isSpeakText = false;
                switch (enableVoice) {
                    case "00":
                        isSpeakText = false;
                        break;
                    case "01":
                        isSpeakText = true;
                        break;
                    default:
                }
                isVisible = false;
                videoPlay(startUrl, videoPaths, fileType, isSpeakText, false, false);
            } else {
                setImageGone();
                e("无视频或者图片资源");
                e("是否正在播放", mVideoView.isPlaying());
                if (!mVideoView.isPlaying()) {
                    stopPlay();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RequestOptions options = new RequestOptions()
                                    .centerCrop()
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE);

                            int scrren = SpUtil.readInt(Const.DEVICEID_TYPE);
                            if (scrren == 1) {
                                GlideUtil.load(VideoViewActivity.this, R.drawable.landscape, options, mCoverView);
                                mCoverView.setVisibility(View.VISIBLE);
                            } else {
                                GlideUtil.load(VideoViewActivity.this, R.drawable.portrait, options, mCoverView);
                                mCoverView.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    voiceControl(parameter.getDeviceId(), parameter.getrCode(), false, null);
                    // 2019-5-21 去掉待机图获取
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(R.string.play_remind);
                        }
                    });
                }
            }

            getJingDongAd();
        }
//        if (!isNetConnected(VideoViewActivity.this)) {
//            Plog.e("网络异常！");    http://testapi.jdmomedia.com/ad/track?id=3230333331303439&aid=14514&event=1
//            showToast("网络异常！"); http://testapi.jdmomedia.com/ad/track?id=3230333331303838&aid=14514&event=1
//            setIntentData("networkType", 4);
//            networkJudgment();
//        }
    }

    /**
     * 下标复位
     */
    private void indexReset() {
        if (index > showSumSize || index == showSumSize) {
            e("下标复位");
            index = 0;
        }
    }

    /**
     * 播放类型比对
     */
    private void localShowPlay(String playListId) {
        try {
            qery = dataQuery("condition", playListId);
            for (PlayList conditions : qery) {
                String playType = conditions.getCondition();
                e("播放类型", playType);
                switch (playType) {
                    case "00":
                        qery = dataQuery("duration", playListId);
                        for (PlayList duration : qery) {
                            int queryDuration = duration.getDuration();
                            e("设定播放次数", queryDuration);
                            int count = getCurrentCount(playListId);
                            e("已播放次数", count);
                            if (count >= queryDuration) {
                                deleteShow(playListId);
                            } else if (count < duration.getDuration()) {
                                videoPath(playListId);
                            }
                        }
                        break;
                    case "01":
                        timePlay(playListId);
                        break;
                    case "90":
                        e("删除无效节目清单");
                        deleteShow(playListId);
                        break;
                    case "80":
                        e("删除无效节目清单");
                        deleteShow(playListId);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 时段广告比对处理
     */
    private void intervalTimeInspect() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                showIntervalTimeList = readIntervalTimeShow();
                int size = showIntervalTimeList.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        String playId = Integer.toString(showIntervalTimeList.get(i));
                        qery = dataQuery("condition", playId);
                        for (PlayList conditions : qery) {
                            String playType = conditions.getCondition();
                            switch (playType) {
                                case "00":
                                    qery = dataQuery("duration", playId);
                                    for (PlayList duration : qery) {
                                        int queryDuration = duration.getDuration();
                                        e("设定播放次数", queryDuration);
                                        int count = getCurrentCount(playId);
                                        e("已播放次数", count);
                                        if (count >= queryDuration) {
                                            deleteAnswer(playId);
                                        }
                                    }
                                    break;
                                case "01":
                                    String startTime = null, endTime = null;
                                    Date startDate, endDate;
                                    try {
                                        Date currentDate = new Date();
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                                        qery = dataQuery("startTime", playId);
                                        for (PlayList start : qery) {
                                            startTime = start.getStartTime();
                                        }
                                        qery = dataQuery("endTime", playId);
                                        for (PlayList end : qery) {
                                            endTime = end.getEndTime();
                                        }
                                        startDate = format.parse(startTime);
                                        endDate = format.parse(endTime);
                                        if (startDate.before(currentDate) && endDate.after(currentDate)) {
                                            e("当前时段广告可以播放");
                                        } else if (currentDate.before(startDate)) {
                                            e("当前时段广告时间未到，不处理");
                                        } else {
                                            deleteAnswer(playId);
                                        }
                                    } catch (ParseException e) {
                                        e("异常", e.toString());
                                        e.printStackTrace();
                                    } catch (IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 时间播放比对
     */
    private void timePlay(final String playListId) {
        String startTime = null, endTime = null;
        Date startDate, endDate;
        try {
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

            qery = dataQuery("startTime", playListId);
            for (PlayList start : qery) {
                startTime = start.getStartTime();
            }
            qery = dataQuery("endTime", playListId);
            for (PlayList end : qery) {
                endTime = end.getEndTime();
            }
            startDate = format.parse(startTime);
            endDate = format.parse(endTime);
            e("起始时间", startTime);
            e("现在时间", format.format(currentDate));
            e("终止时间", endTime);
            if (startDate.before(currentDate) && endDate.after(currentDate)) {
                e("可以播放");
                videoPath(playListId);
            } else if (currentDate.before(startDate)) {
                e("时间未到，播放下一个");
                addSelf();
                String playListIds = showSumList.get(index).toString();
                localShowPlay(playListIds);
            } else {
                if (isNetConnected(VideoViewActivity.this)) {
                    deleteShow(playListId);
                } else {
                    e("网络异常继续播放");
                    videoPath(playListId);
                }
            }
        } catch (ParseException e) {
            e("异常", e.toString());
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (StackOverflowError e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除清单
     */
    private void deleteShow(String playListId) {

        deleteAnswer(playListId);

        updateShowList();
        e("当前集合大小", showSumSize);
        e("当前下标", index);
        if (showSumSize > 0) {
            if (index > showSumSize || index == showSumSize) {
                e("下标等于集合大小,或者大于");
                index = 0;
            } else if (showSumSize == 1) {
                index = 0;
            }
            try {
                String showIdNew = showSumList.get(index).toString();
                localShowPlay(showIdNew);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else if (showSumSize == 0) {
            e("没有可播放清单");
            assignLoop();
//            setIntentData("playType", 4);
        }
    }

    /**
     * 节目集合更新
     */
    private void updateShowList() {
        showSumList = showSum();
        showSumSize = showSumList.size();
    }

    /**
     * 删除回应
     */
    private void deleteAnswer(final String playListId) {
        List<PlayList> qery = dataQuery("fileName", playListId);
        for (PlayList filename : qery) {
            String deleteFileName = filename.getFileName();
            e("要删除本地文件,和播放ID", deleteFileName, playListId);
            List<PlayList> baiDu = dataQuery("isBaiDu", playListId);
            for (PlayList baiDus : baiDu) {
                List<PlayList> splitShow = dataQuery("screenType", playListId);
                for (PlayList splitShows : splitShow) {
                    List<Parameter> parameter = LitePal.findAll(Parameter.class);
                    for (Parameter parameters : parameter) {
                        String deviceId = parameters.getDeviceId();
                        int adType = baiDus.getIsBaiDu();
                        if (adType == CONSTANT_ONE) {
                            int isDelete = LitePal.deleteAll(PlayList.class, "playId = ?", playListId);
                            e("百度广告清单删除结果", isDelete);
                            if (isDelete > 0) {
//                                String server = SpUtil.readString(Const.CONTROL_TYPE);
//                                Base.deleteFile(deleteFileName, server);
                                updateShowList();
                                e("当前广告数量", showSumSize);
                                if (showSumSize == 0) {
                                    baiDuPare(deviceId);
                                }
                            }
                        } else if (adType == CONSTANT_TWO) {
                            int isDelete = LitePal.deleteAll(PlayList.class, "playId = ?", playListId);
                            e("" + "京东广告清单删除结果", isDelete);
                            if (isDelete > 0) {
                                updateShowList();
                            }
                        } else if (splitShows.getScreenType() == 2) {
                            deleteSplitScreenShow(deviceId, playListId, deleteFileName);
                        } else {
                            List<PlayList> jiTouAdv = dataQuery("showId", playListId);
                            for (PlayList jiTouAdvs : jiTouAdv) {
                                String showId = Integer.toString(jiTouAdvs.getShowId());
                                int isDelete = LitePal.deleteAll(PlayList.class, "playId = ?", playListId);
                                e("即投云媒广告清单删除结果", isDelete);
                                if (isDelete > 0) {
                                    updateShowList();
                                    String server = SpUtil.readString(Const.CONTROL_TYPE);
                                    Base.deleteFile(deleteFileName, server);

                                    programmeControl(showId, deviceId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除分屏节目
     */
    private void deleteSplitScreenShow(String deviceId, String playListId, String deleteFileName) {
        List<String> splitScreenShow = getSplitScreenShow();
        for (int i = deleteFailure.size() - 1; i >= 0; i--) {
            String fileUrl = ROOT_DIRECTORY_URL + PACKAGE_NAME + deleteFileName;
            String showFile = splitScreenShow.get(i);
            e("删除文件名", fileUrl);
            e("分屏文件名", showFile);
            if (showFile.equals(fileUrl)) {
                int isDelete = LitePal.deleteAll(PlayList.class, "playId = ?", playListId);
                e("分屏广告清单删除结果", isDelete);
                if (isDelete > 0) {
                    splitScreenShow.remove(i);
                    String server = SpUtil.readString(Const.CONTROL_TYPE);
                    Base.deleteFile(deleteFileName, server);

                    programmeControl(showId, deviceId);
                }
            }
        }
        updateSplitScreenShow();
    }

    /**
     * 更新分屏节目
     */
    private void updateSplitScreenShow() {
        List<String> splitScreenShow2 = getSplitScreenShow();
        banner.update(splitScreenShow2);
        if (splitScreenShow2.size() == 0 && mHotelLayout.getVisibility() == View.VISIBLE) {
            e("副屏广告为空，取消分屏");
            mHotelLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 删除云端节目数据
     */
    private void programmeControl(final String showId, final String deviceId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                String url = DELETE_PLAYED_MEDIA + deviceId + "&mediaId=" + showId;
                mMyOkHttp.get()
                        .url(url)
                        .tag(this)
                        .enqueue(new RawResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, String response) {
                                e("获取删除节目返回数据:" + response);
                                try {
                                    JSONObject jsonString = new JSONObject(response);
                                    if (jsonString.getBoolean(SUCCESS)) {
                                        if (deleteFailure.size() > 0) {
                                            noDeleteShowId(deleteFailure.get(0));
                                        }
                                    } else {
                                        e("删除云数据库失败");
                                        deleteFailure.add(showId);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, String errorMsg) {
                                e("失败原因:" + errorMsg);
                                deleteFailure.add(showId);
                            }
                        });
            }
        });
    }

    /**
     * 节目删除失败统计
     *
     * @param showId 节目ID
     */
    private void noDeleteShowId(String showId) {
        for (int i = deleteFailure.size() - 1; i >= 0; i--) {
            if (showId.equals(deleteFailure.get(i))) {
                deleteFailure.remove(i);
            }
        }
    }

    /**
     * 获取当前节目播放次数
     *
     * @param playListId 节目ID
     */
    private int getCurrentCount(String playListId) {
        int count = 0;
        qery = dataQuery("count", playListId);
        for (PlayList qerys : qery) {
            count = qerys.getCount();
        }
        return count;
    }

    /**
     * 删除播放失败处理
     */
    private void deletePlayFailure(String videoPath) {
        int maximum = 5;
        count++;
        if (count > maximum) {
            qery = getQuery("playId", "fileName", videoPath);
            for (PlayList showId : qery) {
                int deleteCount = LitePal.deleteAll(PlayList.class, "playId = ?", Integer.toString(showId.getShowId()));
                e("被删除数 " + deleteCount);
                if (deleteCount > 0) {
                    updateShowList();
                }
            }
        }
    }

    /**
     * 开启节目播放
     */
    private void playVideo() {
        List<Parameter> parameter = LitePal.findAll(Parameter.class);
        for (final Parameter parameters : parameter) {
            e("要播放的文件：" + mUri);
//            Plog.e("要播放的文件：" + mUri);
            if (mUri == null) {
                mUri = parameters.getPlayUrl();
                e("获取到的的文件名" + mUri);
                isJdStart = false;
            }
            try {
                showPlay(mUri, true);
                setIntentData("playUrl", mUri);
                e("isJson结果: " + isJson);
                if (showSumSize > 0 && isJson) {
                    String peakPath = DEVICE_TIME + parameters.getDeviceId();
                    if (isNetConnected(VideoViewActivity.this)) {
                        webPeakTime(peakPath, parameters);
                    } else {
                        String peakData = SpUtil.readString(Const.VIP_SHOW_DATA);
                        e("没有网络：" + peakData);
                        jsonToObj(peakData, parameters);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 网络状况判断
     */
    private void networkJudgment() {
        List<Parameter> networkType = LitePal.findAll(Parameter.class);
        for (Parameter networkTypes : networkType) {
            final int type = networkTypes.getNetworkType();
            e("当前网络类型：" + type);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (type == 1) {
                        mSignal.setVisibility(View.VISIBLE);
                        GlideUtil.load(VideoViewActivity.this, R.drawable.moblie, optionsNetwork, mSignal);
                    } else if (type == 2) {
                        GlideUtil.load(VideoViewActivity.this, R.drawable.wifi, optionsNetwork, mSignal);
                    } else if (type == 3) {
                        GlideUtil.load(VideoViewActivity.this, R.drawable.ethernet, optionsNetwork, mSignal);
                    } else {
                        GlideUtil.load(VideoViewActivity.this, R.drawable.nonetwork, optionsNetwork, mSignal);
                    }
                    mSignal.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    /**
     * 节目播放
     */
    private void showPlay(@NonNull final String videoPath, boolean isSpeakText) {
        final String videoPaths = FILE_SAVE_URL + videoPath;
        String fileType = videoPath.substring(videoPath.lastIndexOf(".") + 1);
        //红包广告操作
        redPacketOperation(videoPath);

        if (fileIsExists(videoPaths)) {
            videoPlay(videoPath, videoPaths, fileType, isSpeakText, true, true);
        } else {
            List<PlayList> playType = getQuery("playType", "fileName", videoPath);
            e("数组：" + playType.size());
            for (PlayList playTypes : playType) {
                final int isLive = playTypes.getPlayType();
                e("文件类型：" + isLive);
                if (1 == isLive || 2 == isLive || 3 == isLive) {
                    e("直播地址和类型：" + videoPath + ", " + isLive);
                    count = 0;
                    setImageGone();
                    imageGone();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsNewPlay = true;
                            mVideoView.setVisibility(View.VISIBLE);
                            mVideoView.setVideoPath(videoPath);
                        }
                    });
                } else {
                    e("重新下载文件");
                    if (showSumSize > 0) {
                        if (!downloadList.contains(videoPath)) {
                            downloadList.add(videoPath);
                            fileMissingDownload(videoPath);
                        }
                        isJson = false;
                        nextShow();
                    }
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            List<Parameter> idList = LitePal.findAll(Parameter.class);
                            for (Parameter idLists : idList) {
                                command[0] = (byte) 0x85;
                                number[0] = (byte) 0xAD;
                                ProtocolManager.getInstance().controlAnswer(idLists.getDeviceId(), idLists.getrCode(),
                                        command, number, isLive, showId, 0, "");
                            }
                        }
                    });
                }
            }
            if (playType.size() == 0 && showSumSize > 0) {
                e("文件错误！");
                nextShow();
            }
            if (showSumSize == 0) {
                assignLoop();
            }
        }
    }

    /**
     * 广告文件重新下载
     */
    private void fileMissingDownload(String videoPath) {
        if (isNetConnected(VideoViewActivity.this)) {

            removeFile(videoPath);

            List<PlayList> countList = fileNameQuery("downloadCount", videoPath);
            for (PlayList countLists : countList) {
                final int count = countLists.getDownloadCount();
                e("下载次数", count);
                if (count > CONSTANT_TEN) {
                    int delete = LitePal.deleteAll(PlayList.class, "fileName = ?", videoPath);
                    e("删除结果：" + delete);
                } else {
                    String url = DOMAIN_NAME + "Upload/" + videoPath;
                    String saveDir = FILE_SAVE_URL + videoPath;
                    mMyOkHttp.download()
                            .url(url)
                            .filePath(saveDir)
                            .tag(this)
                            .enqueue(new DownloadResponseHandler() {
                                @Override
                                public void onFinish(File downloadFile) {
                                    String fileName = downloadFile.toString().substring(downloadFile.toString().lastIndexOf("/") + 1);
                                    e("下载成功：" + fileName);
                                    downloadList.remove(fileName);
                                    if (checkMd5(fileName)) {
                                        updateDownloadState(fileName);
                                        updateDownloadCount(count + 1, fileName);
                                    } else {
                                        e("文件不完整移除");
                                        removeFile(fileName);
                                    }
                                }

                                @Override
                                public void onProgress(long currentBytes, long totalBytes) {
                                }

                                @Override
                                public void onFailure(String errorMsg) {
                                    e("失败原因：" + errorMsg);
                                }
                            });
                }
            }
        } else {
            e("当前网络不可用！");
        }
    }

    /**
     * 节目播放
     */
    private void videoPlay(@NonNull String videoPath, final String videoPaths, String
            fileType, boolean isSpeakText, boolean isRedPacket, boolean isStart) {
        String videoType = "MP4";
        if (isRedPacket) {
//            List<PlayList> redPacket1 = fileNameQuery("redPacket", videoPath);
            e("播放是的currentPlayId：" + currentPlayId);
            if (currentPlayId != null && isJdStart) {
                List<PlayList> advType = dataQuery("isBaiDu", currentPlayId);
                for (PlayList advTypes : advType) {
                    int type = advTypes.getIsBaiDu();
                    e("广告类型：" + type);
                    jdType = type;
                    if (type == 2) {
                        playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, 1, isStart);
                        List<PlayList> requestIdList = dataQuery("showName", currentPlayId);
                        for (PlayList requestIdLists : requestIdList) {
                            List<PlayList> startJdUrl = dataQuery("jdStartUrl", currentPlayId);
                            for (PlayList startJdUrls : startJdUrl) {
                                List<PlayList> stopJdUrl = dataQuery("jdStopUrl", currentPlayId);
                                for (PlayList stopJdUrls : stopJdUrl) {
                                    List<PlayList> jdUrlList = dataQuery("jdUrlList", currentPlayId);
                                    for (PlayList jdUrlLists : jdUrlList) {
                                        endJdPlay = stopJdUrls.getJdStopUrl();
                                        currentJdPlay = jdUrlLists.getJdUrlList();
                                    }
                                }
                                setCount(currentPlayId, 1);
                                String requestId = requestIdLists.getShowName();
                                e("获取到的mediaID start：" + requestId);
                                stopRequestId = requestId;
                                String url = START_JINGDONG + "?requestId=" + requestId + "&ticks=" + System.currentTimeMillis() / 1000;
                                e("上报路径 tart：" + url);
                                e("上报路径 京东 tart：" + startJdUrls.getJdStartUrl());
                                startJingDong(url);
                                startJingDong(startJdUrls.getJdStartUrl());
                            }
                        }
                    } else if (type == 0) {
                        List<PlayList> redPacket1 = dataQuery("redPacket", currentPlayId);
                        for (PlayList redPackets : redPacket1) {
                            final int redPacket = redPackets.getRedPacket();
                            playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, redPacket, isStart);
                        }
                    } else if (type == 1) {
                        playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, 1, isStart);
                    }
                }
                if (advType.size() == 0) {
                    e("获取类型失败");
                    playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, 1, isStart);
                }
            } else if (!isJdStart) {
                e("软件启动播放");
                playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, 1, isStart);
            }
        } else {
            playControl(videoPath, videoPaths, fileType, isSpeakText, videoType, 1, isStart);
        }
    }

    /**
     * 节目播放控制
     */
    private void playControl(@NonNull String videoPath, final String videoPaths, String
            fileType, boolean isSpeakText, String videoType, final int redPacket, boolean isStart) {
        String videoType2 = "MOV";
        if (videoType.contains(fileType.toUpperCase()) || videoType2.contains(fileType.toUpperCase())) {
            e("本地视频");
            setImageGone();
            imageGone();
            count = 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imgeCount = 0;
                    if (redPacket != CONSTANT_TWO || redPacketMoney == 0) {
                        closeAssetMusics();
                    }
                    playInitialize();
                    mVideoView.setVideoPath(videoPaths);
                    mVideoView.start();
                    if (mVideoView.getVisibility() == View.GONE) {
                        mVideoView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (names.contains(fileType.toUpperCase())) {
            e("图片");
            count = 0;
            imagePlay(videoPath, fileType, isStart);
            if (isSpeakText) {
                String text = SpUtil.readString(VOICE_CONTENT);
                e("获取的text：" + text + ", " + isRedPacket);
                if (!TextUtils.isEmpty(text)) {
                    e("文件：" + videoPath);
                    if (redPacket != CONSTANT_TWO || redPacketMoney == 0) {
                        imgeCount++;
                        e("imgeCount：" + imgeCount);
                        if (imgeCount == 1) {
                            e("开始");
                            openAssetMusics("hongbao-tts.mp3");
                        } else if (imgeCount > 1 && imgeCount != CONSTANT_TWO) {
                            e("开始");
                            imgeCount = 0;
                            openAssetMusics("hongbao-tts.mp3");
                        }
                    } else {
                        closeAssetMusics();
                    }
                }
            } else {
                closeAssetMusics();
            }

        } else {
            deletePlayFailure(videoPath);
            nextShow();
        }
    }

    /**
     * 图片播放
     */
    private void imagePlay(final String picPath, final String fileType,
                           final boolean isStart) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int deviceScreen;
                try {
//                    playInitialize();
                    mVideoView.setVisibility(View.INVISIBLE);
                    mVideoView.setVisibility(View.GONE);
                    if (mVideoView.isPlaying()) {
                        mVideoView.stopPlayback();
                    }
                    String imageType = "GIF";
                    //本地文件
                    File file = new File(ROOT_DIRECTORY_URL + PACKAGE_NAME, picPath);
                    if (!isStart) {
                        setImageGone();
                        e("加载启动画面");
                        GlideUtil.loadPlay3(VideoViewActivity.this, file, optionsImage2, mCoverView);
                        mCoverView.setVisibility(View.VISIBLE);
                    } else {
                        if (imageType.contains(fileType.toUpperCase())) {
                            e("动静态图");
                            setImageGone();
                            imageGone();
                            gifFromAssets = new GifDrawable(file);
                            mGifImage.setImageDrawable(gifFromAssets);
                            gifFromAssets.start();
                            mGifImage.setVisibility(View.VISIBLE);
                            mImage.setVisibility(View.VISIBLE);
                        } else {
                            imageGone();
                            int deviceType = SpUtil.readInt(Const.DEVICEID_TYPE);
                            int screenType = SpUtil.readInt(Const.DEVICE_APP_TYPE);
                            int bannerSize = banner.getBannerSize();
                            boolean existed = (screenType == CONSTANT_TWO && bannerSize > 0) || (screenType == CONSTANT_FOUR && bannerSize > 0);
                            if (existed) {
                                if (deviceType == 1) {
                                    deviceScreen = Double.valueOf(deviceScreenWidth * 0.8).intValue();
                                } else {
                                    deviceScreen = Double.valueOf(deviceScreenHeight * 0.8).intValue();
                                }
                                e("改变后的宽度：" + deviceScreen);
                            } else {
                                if (deviceType == 1) {
                                    deviceScreen = deviceScreenWidth;
                                } else {
                                    deviceScreen = deviceScreenHeight;
                                }
                            }
                            FrameLayout.LayoutParams pImage = (FrameLayout.LayoutParams) mImage.getLayoutParams();
                            if (deviceType == 1) {
                                pImage.width = deviceScreen;
                                pImage.height = deviceScreenHeight;
                                e("横屏");
                                GlideUtil.loadPlay3(VideoViewActivity.this, file, optionsImage2, mImage);
                            } else {
                                pImage.width = deviceScreenWidth;
                                pImage.height = deviceScreen;
                                GlideUtil.loadPlay3(VideoViewActivity.this, file, optionsImage2, mImage);
                            }
                            mImage.setLayoutParams(pImage);
                            mImage.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    e("图片异常情况：" + e.toString());
                }
            }
        });
        //创建并执行在给定延迟后启用的一次性操作。
        if (isStart) {
            cdt.start();
        }
    }

    /**
     * 图片定时
     */
    CountDownTimer cdt = new CountDownTimer(15 * 1000, 15 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            mIsNewPlay = true;
            boolean isPlay = true;
            e("定时完成，视频播放状态：" + mVideoView.isPlaying());

            jingDongReport();

            if (mVideoView.isPlaying()) {
                isPlay = false;
            }
            e("是否能够跳转：" + isPlay);
            if (isPlay) {
                if (showSumSize > 0) {
                    e("播放清单文件");
                    playVideo();
                } else {
                    e("清单为空");
                    assignLoop();
                }
            }
        }
    };

    /**
     * 图片控件隐藏
     */
    private void imageGone() {
        if (mGifImage.getVisibility() == View.VISIBLE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    e("mGifImage动态图隐藏");
                    mGifImage.setVisibility(View.GONE);
                    if (gifFromAssets.isRunning()) {
                        gifFromAssets.stop();
                    }
                }
            });
        }
        if (mCoverView.getVisibility() == View.VISIBLE) {
            e("隐藏待机画面");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCoverView.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * 图片播放控件隐藏
     */
    private void setImageGone() {
        if (mImage.getVisibility() == View.VISIBLE) {
            e("mImage静态图隐藏");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //新增2018-6-4-16:55
                    mImage.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * VIP节目时间获取
     */
    public void webPeakTime(final String url, final Parameter parameter) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                mMyOkHttp.get()
                        .url(url)
                        .tag(this)
                        .enqueue(new RawResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, String response) {
                                e("获取VIP节目时间数据:" + response);
                                try {
                                    JSONObject json = new JSONObject(response);
                                    if (json.getBoolean(SUCCESS)) {
                                        SpUtil.writeString(Const.VIP_SHOW_DATA, response);
                                        jsonToObj(response, parameter);
                                    } else {
                                        e("获取数据失败");
                                        String vipShowData = SpUtil.readString(Const.VIP_SHOW_DATA);
                                        jsonToObj(vipShowData, parameter);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, String errorMsg) {
                                e("VIP参数获取失败原因:" + errorMsg);
                                String vipShowData = SpUtil.readString(Const.VIP_SHOW_DATA);
                                jsonToObj(vipShowData, parameter);
                            }
                        });
            }
        });
    }

    /**
     * 无节目图片展示
     */
    private void standbyVisibility(String landscapeUrl, String portraitUrl) {
        if (showSumSize == 0) {
            int scrren = SpUtil.readInt(Const.DEVICEID_TYPE);
            if (scrren == 1) {
                if (landscapeUrl != null) {
                    imagePath = DOMAIN_NAME2 + landscapeUrl;
                    optionsStandby = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .override(1920, 1080);
                }
            } else {
                if (portraitUrl != null) {
                    imagePath = DOMAIN_NAME2 + portraitUrl;
                    optionsStandby = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .override(1080, 1920);
                }
            }
            if (imagePath != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e("最终路径：" + imagePath);

                        GlideUtil.load(VideoViewActivity.this, imagePath, optionsStandby, mCoverView);
                        mCoverView.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            e("广告不为空");
        }
    }

    /**
     * VIP时间解析
     */
    int intervalCount = 0;

    public void jsonToObj(final String jsonString, final Parameter parameter) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                boolean isPeak = false;
                String byTime = "byTime";
                try {
                    JSONObject json = new JSONObject(jsonString);
                    e("VIP时间解析的结果：" + json.getBoolean(byTime));
                    if (json.getBoolean(byTime)) {
                        setIntentData("isPeakTimes", true);
                        JSONArray deviceTimes = json.getJSONArray("deviceTimes");
                        for (int i = 0; i < deviceTimes.length(); i++) {
                            PeakParameter deviceTime = new PeakParameter();
                            deviceTime.startTime = deviceTimes.getJSONObject(i).getString("startTime").substring(11);
                            deviceTime.endTime = deviceTimes.getJSONObject(i).getString("endTime").substring(11);
                            isPeak = timeTrigger(deviceTime.startTime, deviceTime.endTime);
                            if (isPeak) {
                                break;
                            }
                        }
                        e("是否开启VIP播放：" + isPeak);
                        if (isPeak && parameter.getIsPeakTimes() && vipShowSum().size() > 0) {
                            int vipLength = vipShowSum().size() - 1;
                            if (index > vipLength) {
                                index = 0;
                            } else if (index < 0) {
                                index = 0;
                            }
                            mIsVip = true;
                            e("开启VIP播放以及当前下标：" + index);
//                            e("查询到的VIP节目："+ vipShowSum());
                            String playListId = vipShowSum().get(index).toString();
                            localShowPlay(playListId);
                        } else {
                            e("获取非VIP时段播放的节目");
                            nonTimeAdvertising();
                        }
                    } else {
                        e("isPeakTimes获取非VIP时段播放的节目");
                        nonTimeAdvertising();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取时段时间
     */
    private void nonTimeAdvertising() {
        String playListIds;
        indexReset();
        playListIds = getShowPlayId();
        e("第一次获取的播放ID：" + playListIds + ", " + showSumSize);
        int size = showIntervalTimeList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                String playId2 = Integer.toString(showIntervalTimeList.get(i));
                List<PlayList> interval = dataQuery("intervalTimeList", playId2);
                for (PlayList intervals : interval) {
                    String fileName = getFileName(playId2);
                    String saveDir = FILE_SAVE_URL + fileName;
                    if (fileIsExists(saveDir)) {
                        time = intervals.getIntervalTimeList();
                    } else {
                        fileInspect(fileName);
                    }
                }

                inspectIntervalTime2();
                e("比较结果：" + isPlayIntervalTimeShow);
                if (isPlayIntervalTimeShow) {
                    e("时段广告,跳出循环");
                    intervalCount++;
                    if (intervalCount == 1) {
                        playListIds = playId2;
                    }
                    break;
                }
            }
            if (!isPlayIntervalTimeShow) {
                e("时段比较失败");
                intervalCount = 0;
            }
        }

        e("最后确定的广告ID：" + playListIds);
        if (playListIds == null && showSumSize > 0) {
            nonTimeAdvertising();
        } else {
            localShowPlay(playListIds);
        }
        mIsVip = false;
    }

    private void fileInspect(String fileName) {
        if (!downloadList.contains(fileName)) {
            downloadList.add(fileName);
            e("任务未经存在");
            fileMissingDownload(fileName);
        }
    }

    /**
     * 获取播放ID
     */
    private String getShowPlayId() {
        String playListIds = null;
        indexReset();
        String playId = showSumList.get(index).toString();
        String fileName = getFileName(playId);
        String saveDir = FILE_SAVE_URL + fileName;
        if (fileIsExists(saveDir) && checkMd5(fileName)) {
            playListIds = playId;
        } else {
            addSelf();
            getShowPlayId();
            if (fileName != null) {
                fileInspect(fileName);
            }
        }
        return playListIds;
    }

    /**
     * 获取下载状态
     */
    private String getFileName(String playId) {
        String fileName = null;
        try {
            List<PlayList> queryFileName = dataQuery("fileName", playId);
            for (PlayList queryFileNames : queryFileName) {
                fileName = queryFileNames.getFileName();
                e("获取文件名：" + fileName);
            }
        } catch (StackOverflowError e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 红包广告控制
     *
     * @param videoPath 路径
     */
    private void redPacketOperation(@NonNull final String videoPath) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                List<PlayList> redPacket1 = fileNameQuery("redPacket", videoPath);
                for (PlayList redPackets : redPacket1) {
                    isRedPacket = redPackets.getRedPacket();
                    e("当前是否是红包广告：" + isRedPacket);
                    List<PlayList> pictureName2 = fileNameQuery("pictureName", videoPath);
                    for (PlayList pictureName2s : pictureName2) {
                        final String pictureName = pictureName2s.getPictureName();
                        if (isRedPacket == CONSTANT_TWO) {
                            List<PlayList> showId = fileNameQuery("showId", videoPath);
                            for (PlayList showIds : showId) {
                                String url = REDPACKET_AMOUNT + showIds.getShowId();
                                mMyOkHttp.get()
                                        .url(url)
                                        .tag(this)
                                        .enqueue(new RawResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, String response) {
                                                e("获取的数据:" + response);
                                                try {
                                                    String urlDecoder = java.net.URLDecoder.decode(response, "UTF-8");
                                                    JSONObject json = new JSONObject(urlDecoder);
                                                    if (json.getBoolean("success")) {
                                                        redPacket = json.getBoolean("hasRedPacket");
                                                        redPacketMoney = json.getInt("amount");
                                                        e("是否还有红包：" + redPacket);
                                                        e("红包金额：" + redPacketMoney);
                                                        if (redPacketMoney == 0 || !redPacket) {
                                                            String videoPaths = FILE_SAVE_URL + pictureName;
                                                            mRedPacket.setVisibility(View.GONE);
                                                            if (fileIsExists(videoPaths)) {
                                                                e("删除红包二维码");
                                                                Base.deleteFile(pictureName, "");
                                                            }
                                                            closeAssetMusics();
                                                        } else {
                                                            rePacketVisibility(pictureName);
                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(int statusCode, String errorMsg) {
                                                e("红包数据失败原因:" + errorMsg);
                                                redPacketMoney = 0;
                                                rePacketVisibility(pictureName);
                                            }
                                        });
                            }
                        } else {
                            if ("00".equals(SpUtil.readString(VOICE_ENABLE))) {
                                closeAssetMusics();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRedPacket.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * 红包二维码展示
     */
    private void rePacketVisibility(String pictureName) {
        e("红包图和金额：" + pictureName + ", " + redPacketMoney);
        if (pictureName != null && redPacketMoney != 0) {
            final File file = new File(ROOT_DIRECTORY_URL + PACKAGE_NAME, pictureName);
            if (fileIsExists(FILE_SAVE_URL + pictureName)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GlideUtil.loadPlay3(VideoViewActivity.this, file, optionsRedPacket, mRedPacket);
                        mRedPacket.setVisibility(View.VISIBLE);
                    }
                });

                openAssetMusics("hongbao-tts.mp3");
            } else {
                String picture = DOMAIN_NAME + "RedPacket/" + pictureName;
                final String saveDir = FILE_SAVE_URL + pictureName;
                MonitorService monitorService = new MonitorService();
                monitorService.pictureDownload(picture, saveDir);
            }
        } else {
            e("当前红包金额：" + redPacketMoney);
            if (!redPacket) {
                closeAssetMusics();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRedPacket.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    /**
     * 获取节目播放文件
     */
    private void videoPath(final String playShowId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    e("获取播放地址方式和下标：" + mIsVip + "，" + index);
                    List<PlayList> baiDu = dataQuery("isBaiDu", playShowId);
                    for (PlayList baiDus : baiDu) {
                        List<PlayList> intervalTime = dataQuery("intervalTime", playShowId);
                        for (PlayList intervalTimes : intervalTime) {
                            List<Parameter> parameter = LitePal.findAll(Parameter.class);
                            for (Parameter parameters : parameter) {
                                int isBaiDu = baiDus.getIsBaiDu();
                                int timeInterval = intervalTimes.getIntervalTime();
                                final String deviceId = parameters.getDeviceId();
                                rCode = parameters.getrCode();
                                e("协议类型：" + parameters.getProtocolType());
                                currentPlayId = playShowId;
                                e("当前播放ID：" + playShowId);
                                isJdStart = true;
                                if (parameters.getProtocolType() == 2) {
                                    SpUtil.writeString(Const.NET_TYPE, "udp");
                                }
                                if (isBaiDu == 1) {
                                    e("百度广告");
                                    baiDuPare(deviceId);
                                } else if (timeInterval == 1) {
                                    e("时段广告");
                                    showUpdate(deviceId, playShowId, rCode);
                                } else if (isBaiDu == CONSTANT_TWO) {
                                    e("京东广告");
//                                    showUpdate(deviceId, playShowId, rCode, false);
                                    getAdDemo(deviceId);
                                } else {
                                    e("普通广告");
                                    showUpdate(deviceId, playShowId, rCode);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    e("异常原因：" + e.toString());
                }
            }
        });
    }

    /**
     * 时段广告时间检查
     */
    private void inspectIntervalTime2() {
        try {
            ArrayList<Boolean> timeList = new ArrayList<>();
            int timeSize = time.size();
            if (timeSize > 0) {
                for (int i = 0; i < timeSize; i++) {
                    String getTime = time.get(i);
                    String startTime = getTime.substring(0, getTime.lastIndexOf("/"));
                    String endTime = getTime.substring(getTime.lastIndexOf("/") + 1);
                    boolean result = timeTrigger(startTime, endTime);
                    timeList.add(result);
                }
                int resultCountSize = timeList.size();
                for (int i = 0; i < resultCountSize; i++) {
                    if (timeList.get(i)) {
                        isPlayIntervalTimeShow = true;
                        e("成功");
                        break;
                    } else {
                        isPlayIntervalTimeShow = false;
                    }
                }
            } else {
                isPlayIntervalTimeShow = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 节目更新
     */
    private void showUpdate(String deviceId, String playId, String rCode) {
        int playCount = 0;
        List<PlayList> fileName = dataQuery("fileName", playId);
        for (PlayList filenames : fileName) {
            playCount = getCurrentCount(playId);
            e("设置前已播放的次数：" + playId + " ," + playCount);
            playCount = playCount + 1;
            mUri = filenames.getFileName();
            setCount(playId, playCount);

            e("即将播放文件", mUri);
        }
        addSelf();
        showReport(deviceId, rCode, playId, playCount);
    }

    /**
     * 打开assets下的mp3文件
     */
    private void openAssetMusics(final String mp3FileName) {
        //播放 assets/a2.mp3 音乐文件
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaPlayer = new MediaPlayer();
                    AssetFileDescriptor fd = getAssets().openFd(mp3FileName);
                    mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                    mMediaPlayer.prepare();
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 停止语音播放
     */
    private void closeAssetMusics() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                            try {
                                mMediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 百度广告参数请求
     */
    private void baiDuPare(final String deviceId) {
        if (isRequestBaiDu) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    String url = GET_BAIDU_AD + deviceId;
                    mMyOkHttp.get()
                            .url(url)
                            .tag(this)
                            .enqueue(new RawResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, String response) {
                                    e("获取百度数据:" + response);
                                    baiDuDataParser(deviceId, response);
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {
                                    e("获取百度数据失败原因:" + errorMsg);
                                    baiDuPare(deviceId);
                                }
                            });
                }
            });
        }
    }

    /**
     * 百度广告上报信息解析，上报播放次数
     */
    private void baiDuDataParser(String deviceId, String response) {
        int lastPlayId;
        try {
            BaiDuAdvParam baiduAdvParam = BaiDuAdvParam.objectFromData(response);
            boolean baiDuRequestResult = baiduAdvParam.isSuccess();
            e("数据获取结果", baiDuRequestResult);
            if (baiDuRequestResult || response.length() > CONSTANT_FORTY) {
                isRequestBaiDu = true;
                int baiDuId = baiduAdvParam.getAdvInfo().getId();
                e("百度广告ID", baiDuId);
                String imageUrl = baiduAdvParam.getAdvInfo().getImageUrl().get(0);
                String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                e("文件路径", imageUrl);
                e("播放图片名字", imageName);

                PlayList playList = new PlayList();
                List<PlayList> mpList = LitePal.findAll(PlayList.class);
                if (mpList.size() > 0) {
                    PlayList lastNews = LitePal.findLast(PlayList.class);
                    lastPlayId = lastNews.getPlayId();
                } else {
                    lastPlayId = 0;
                }

                addBaiDuShow(lastPlayId, baiDuId, imageName, playList);

                String localFile = FILE_SAVE_URL + imageName;
                if (!fileIsExists(localFile)) {
                    e("本地没有文件下载");

                    baiDuFileDownload(imageUrl, imageName, lastPlayId + 1);
                } else {
                    e("文件已经存在，跳过文件下载");
                    updateDownloadState(imageName);
                    int count = showSum().size();
                    if (count == 1) {
                        setCount(Integer.toString(lastPlayId + 1), 1);
                        showPlay(imageName, true);
                    }
                }

                for (int i = 0; i < baiduAdvParam.getAdvInfo().getWinNoticeUrl().size(); i++) {
                    String winNoticeUrl = baiduAdvParam.getAdvInfo().getWinNoticeUrl().get(i);
                    e("循环到的上报路径", winNoticeUrl);
                    Base.webInterface(winNoticeUrl);
                }

                String baiDuIsPlayedPath = BAIDU_IS_PLAYED + baiDuId;
                Base.webInterface(baiDuIsPlayedPath);

                int thirdMonitorUrlSize = baiduAdvParam.getAdvInfo().getThirdMonitorUrl().size();
                for (int i = 0; i < thirdMonitorUrlSize; i++) {
                    String thirdMonitorUrl = baiduAdvParam.getAdvInfo().getThirdMonitorUrl().get(i);
                    e("循环到的第三方监控地址", thirdMonitorUrl);
                    Base.webInterface(thirdMonitorUrl);
                }
            } else {
                String error = "-1";
                String errorCode = Integer.toString(baiduAdvParam.getErrorCode());
                e("获取百度数据失败！,错误码是 =", errorCode);
                if (error.equals(errorCode)) {
                    e("继续申请百度广告");
                    baiDuPare(deviceId);
                } else {
                    isRequestBaiDu = false;
                    webSaveBaiDuLog(deviceId, errorCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            baiDuPare(deviceId);
            e("百度数据异常原因", e.toString());
        }
    }

    /**
     * 添加百度广告清单
     */
    private void addBaiDuShow(int lastPlayId, int baiDuId, String imageName, PlayList
            playList) {
        e("当前数据库最后一条数据播放ID", lastPlayId);
        int baiDuShow = readBaiDuShow().size();
        e("百度广告清单大小", baiDuShow);
        if (baiDuShow <= 1) {
            e("添加");
            Base.setShowData(lastPlayId + 1, "00", "2018-12-14 13:48:55", "2018-12-14 13:48:55", 1, 6, imageName,
                    1111, "百度广告", 0, 3, 2, 1, 1, "没有", 1, 0,
                    baiDuId, null, null, playList);
            playList.save();
        }
    }

    /**
     * 百度广告获取失败返回
     */
    private void webSaveBaiDuLog(String deviceId, String errorCode) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());

            JSONObject userJson = setJsonUser();

            JSONObject logJson = new JSONObject();
            logJson.put("deviceId", deviceId);
            logJson.put("errorCode", errorCode);
            logJson.put("requestDate", simpleDateFormat.format(date));

            JSONObject postJson = new JSONObject();
            postJson.put("user", userJson);
            postJson.put("log", logJson);

            e("百度失败日志数据", postJson.toString());
            //与params不共存 以jsonParams优先
            mMyOkHttp.post()
                    .url(SAVE_BAIDU_LOG)
                    .jsonParams(postJson.toString())
                    .tag(this)
                    .enqueue(new JsonResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject response) {
                            e("doPostJSON onSuccess JSONObject:" + response);
                        }

                        @Override
                        public void onSuccess(int statusCode, JSONArray response) {
                            e("doPostJSON onSuccess JSONArray:" + response);
                        }

                        @Override
                        public void onFailure(int statusCode, String errorMsg) {
                            e("doPostJSON onFailure:" + errorMsg);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 百度文件下载
     */
    private void baiDuFileDownload(final String imageUrlPath, final String imageName,
                                   final int playId) {
        e("图片下载路径", imageUrlPath);
        String saveDir = FILE_SAVE_URL + imageName;
        mMyOkHttp.download()
                .url(imageUrlPath)
                .filePath(saveDir)
                .tag(this).enqueue(new DownloadResponseHandler() {
            @Override
            public void onFinish(File downloadFile) {
                e("百度广告下载完成,加播放ID", imageName + "; " + playId);

                updateDownloadState(imageName);
                e("清单大小", showSumSize);
                jingDongPaly(imageName, playId, showSumSize);
            }

            @Override
            public void onProgress(long currentBytes, long totalBytes) {
            }

            @Override
            public void onFailure(String errorMsg) {
                e("百度广告下载完成失败，原因", errorMsg);
                baiDuFileDownload(imageUrlPath, imageName, playId);
            }
        });
    }

    /**
     * 获取广告-demo
     */
    private void getAdDemo(final String deviceId) {
        if (isRequestJd) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    KuaiFaClient client = new KuaiFaClient(JINGDONG_APP_ID, JINGDONG_APP_KEY, JINGDONG_APP_HOST, true);
                    ScreenSizeEntry size = new ScreenSizeEntry(1920, 1080);
                    String mac = readString(Const.MAC);
                    e("mac地址", mac);
                    //获取当前时间
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                    Date date = new Date(System.currentTimeMillis());
                    DeviceUDIDEntry udid = new DeviceUDIDEntry("9774d56d682e549c",
                            mac, simpleDateFormat.format(date), deviceId);
                    DeviceEntry device = new DeviceEntry(udid, size);

                    DeviceNetworkEntry network = new
                            DeviceNetworkEntry(NetUtil.getNetworkState(VideoViewActivity.this),
                            NetworkOperatorType.MOBILE);
                    String requestId = UUID.randomUUID().toString().replace("-", "");
                    GetAdResult result = client.getAd(device, network, 15, MaterialType.ALL, requestId);
                    int code = result.getCode();
                    String msg = result.getMsg();
                    e("code返回值", code);
                    e("msg返回值", msg);
                    if (code == 0 && SUCCESS2.equals(msg)) {
                        e("获取广告成功");
                        jdRequestSuccess(result, requestId, deviceId);
                    } else if (code == JINGDONG_CODE1 || code == JINGDONG_CODE2) {
                        e("错误信息", code);
                        jdCount++;
                        if (jdCount < CONSTANT_THREE) {
                            getAdDemo(deviceId);
                        } else {
                            if (jdTimeTest()) {
                                jdCount++;
                                if (jdCount <= CONSTANT_FORTY) {
                                    scheduleGetJd(deviceId, 15);
                                } else if (jdCount <= CONSTANT_ONE_THOUSAND) {
                                    scheduleGetJd(deviceId, 60);
                                }
                            }
                        }
                    } else if (code == JINGDONG_CODE3) {
                        if (jdTimeTest()) {
                            jdCount++;
                            if (jdCount <= CONSTANT_FORTY) {
                                scheduleGetJd(deviceId, 15);
                            } else if (jdCount <= CONSTANT_ONE_THOUSAND) {
                                scheduleGetJd(deviceId, 60);
                            }
                        }
                    } else if (!msg.equals(SUCCESS2)) {
                        getAdDemo(deviceId);
                    }
                }
            });
        }
    }

    /**
     * 解析获取到的京东广告
     */
    private void jdRequestSuccess(GetAdResult result, String requestId, String deviceId) {
        String startJdPlay = null, endJdPlay = null;
        ArrayList<String> currentJdPlay = null;
        int lastPlayId;
        try {
            jdCount = 0;
            int adKey = result.getData().ad_key;
            int showTime = result.getData().material.show_time;
            String title = result.getData().material.title;
            String md5 = result.getData().material.md5;
            int height = result.getData().material.height;
            int width = result.getData().material.width;
            String url = result.getData().material.url;
            String type = result.getData().material.type;
            ArrayList<ArrayList<String>> adTracking = result.getData().ad_tracking;
            if (adTracking.size() > 0) {
                startJdPlay = result.getData().ad_tracking.get(0).get(0);
                endJdPlay = result.getData().ad_tracking.get(1).get(0);
                currentJdPlay = result.getData().ad_tracking.get(2);
            }
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            e("京东文件名", fileName);

            PlayList playList = new PlayList();
            List<PlayList> mpList = LitePal.findAll(PlayList.class);
            if (mpList.size() > 0) {
                PlayList lastNews = LitePal.findLast(PlayList.class);
                lastPlayId = lastNews.getPlayId();
            } else {
                lastPlayId = 0;
            }
            addJingDongShow(adKey, url, lastPlayId, requestId, fileName, md5, startJdPlay, endJdPlay, currentJdPlay, playList);
            addJingDongWeb(adKey, showTime, title, md5, height, width, url, type, requestId, deviceId);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 京东广告请求时间校验
     */
    private boolean jdTimeTest() {
        boolean isTime;
        long jdQuestTime2 = System.currentTimeMillis() / 1000;
        long jdTime = jdQuestTime2 - jdQuestTime;
        e("每次请求时间差", jdTime);
        if (jdTime >= CONSTANT_FIFTEEN) {
            jdQuestTime = System.currentTimeMillis() / 1000;
            isTime = true;
        } else {
            isTime = false;
        }
        return isTime;
    }

    /**
     * 延时请求京东广告
     */
    private void scheduleGetJd(final String deviceId, int time) {
        ExecutorServiceManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getAdDemo(deviceId);
            }
        }, time, TimeUnit.SECONDS);
    }

    /**
     * 删除京东广告
     */
    private void jdDelete(String playListId) {
        qery = dataQuery("duration", playListId);
        for (PlayList duration : qery) {
            int queryDuration = duration.getDuration();
            e("设定播放次数", queryDuration);
            int count = getCurrentCount(playListId);
            e("已播放次数", count);
            if (count >= queryDuration) {
                deleteAnswer(playListId);
            }
        }
    }

    /**
     * 京东广告信息上传即投云媒数据库
     */
    private void addJingDongWeb(final int adKey, final int showTime, final String title,
                                final String md5, final int height,
                                final int width, final String url, final String type, final String requestId,
                                final String deviceId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                AddJingDongAdv add = new AddJingDongAdv();
                add.setRequestId(requestId);
                add.setAdKey(adKey);
                add.setShowTime(showTime);
                add.setTitle(title);
                add.setMd5(md5);
                add.setHeight(height);
                add.setWidth(width);
                add.setUrl(url);
                add.setType(type);
                add.setDeviceId(deviceId);
                e("上传web数据", JSON.toJSONString(add));
                mMyOkHttp.post()
                        .url(ADD_JINGDONG)
                        .jsonParams(JSON.toJSONString(add))
                        .tag(this)
                        .enqueue(new JsonResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject response) {
                                e("京东广告数据上报web成功1:" + response);
                            }

                            @Override
                            public void onSuccess(int statusCode, JSONArray response) {
                                e("京东广告数据上报web成功2" + response);
                            }

                            @Override
                            public void onFailure(int statusCode, String errorMsg) {
                                e("doPostJSON onFailure:" + errorMsg);
                            }
                        });
            }
        });
    }

    /**
     * 添加京东广告清单
     */
    private void addJingDongShow(int adKey, String url, int lastPlayId, String
            requestId, String imageName, String md5, String startUrl,
                                 String stopUrl, ArrayList<String> jdUrl, PlayList playList) {
        e("当前数据库最后一条数据播放ID", lastPlayId);
        int jingDongShow = readJingDongShow().size();
        e("京东广告清单大小", jingDongShow);
        Base.setJdShowData(lastPlayId + 1, "00", "2018-12-14 13:48:55", "2018-12-14 13:48:55", 1, 6, imageName,
                1111, requestId, 0, 3, 2, 1, 1, "没有", 2, 1,
                0, null, md5.toUpperCase(), startUrl, stopUrl, jdUrl, playList);
        playList.save();
        e("添加");

        updateShowList();

        String saveDir = FILE_SAVE_URL + imageName;
        int adKeyDownload = SpUtil.readInt(AD_MATERIAL_ID);
        if (adKey != adKeyDownload) {
            SpUtil.writeInt(AD_MATERIAL_ID, adKey);
            fileDownload(url, imageName, lastPlayId + 1);
        } else {
            if (fileIsExists(saveDir)) {
                e("新增后清单大小" + jingDongShow + "和播放状态" + mVideoView.isPlaying());
                if (showSumSize == 1 && (!mVideoView.isPlaying())) {
                    e("新增已存在立即播放");
                    setCount(Integer.toString(lastPlayId + 1), 1);
                    currentPlayId = Integer.toString(lastPlayId + 1);
                    isJdStart = true;
                    showPlay(imageName, true);
                }
            } else {
                SpUtil.writeInt(AD_MATERIAL_ID, adKey);
                fileDownload(url, imageName, lastPlayId + 1);
            }
        }
        if (jingDongShow > CONSTANT_TWO) {
            e("多余删除");
            jdDelete(Integer.toString(readJingDongShow().get(0)));
        }
    }

    /**
     * 京东开始播放上报
     */
    private void startJingDong(final String url) {
        if (isRequestJd && url != null) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    mMyOkHttp.get()
                            .url(url)
                            .tag(this)
                            .enqueue(new RawResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, String response) {
                                    e("开始播放上报成功:" + response);
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {
                                    e("开始播放上报成功失败原因:" + errorMsg);
                                    if (statusCode < JINGDONG_CODE) {
                                        e("成功");
                                    } else if (statusCode == JINGDONG_REPORT) {
                                        e("超过规定上报时间");
                                    } else {
                                        startJingDong(url);
                                    }
                                }
                            });
                }
            });
        }
    }

    /**
     * 京东结束播放上报
     */
    private void endJingDong(final String url) {
        e("上报路径 stop", url);
        if (isRequestJd && url != null) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    mMyOkHttp.get()
                            .url(url)
                            .tag(this)
                            .enqueue(new RawResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, String response) {
                                    e("结束播放上报成功:" + statusCode);
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {
                                    e("结束播放上报失败原因:" + errorMsg);
                                    if (statusCode < JINGDONG_CODE) {
                                        e("成功");
                                    } else if (statusCode == JINGDONG_REPORT) {
                                        e("超过规定上报时间");
                                    } else {
                                        endJingDong(url);
                                    }
                                }
                            });
                }
            });
        }
    }

    /**
     * 京东监播上报
     */
    private void playJingDong(final String url) {
        if (isRequestJd) {
            e("京东播放上报", url);
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    mMyOkHttp.get()
                            .url(url)
                            .tag(this)
                            .enqueue(new RawResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, String response) {
                                    e("京东监播上报成功:" + response);
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {
                                    e("京东监播上报失败原因:" + errorMsg);
                                    e("京东监播上报失败状态:" + statusCode);
                                    if (statusCode < JINGDONG_CODE) {
                                        e("成功");
                                    } else if (statusCode == JINGDONG_REPORT) {
                                        e("超过规定上报时间");
                                    } else {
                                        playJingDong(url);
                                    }
                                }
                            });
                }
            });
        }
    }

    /**
     * 京东播放结束上报
     */
    private void jingDongReport() {
        try {
            if (jdType == CONSTANT_TWO) {
                String url = END_JINGDONG + "?requestId=" + stopRequestId + "&ticks=" + System.currentTimeMillis() / 1000;
                endJingDong(url);
                endJingDong(endJdPlay);
                for (int i = 0; i < currentJdPlay.size(); i++) {
                    playJingDong(currentJdPlay.get(i));
                }

                if (readJingDongShow().size() > 1) {
                    int isDelete = LitePal.deleteAll(PlayList.class, "jdStopUrl = ?", endJdPlay);
                    e("删除京东广告清单结果", isDelete);
                    if (isDelete > 0) {
                        updateShowList();
                    }
                } else if (!isRequestJd) {
                    int isDelete = LitePal.deleteAll(PlayList.class, "jdStopUrl = ?", endJdPlay);
                    if (isDelete > 0) {
                        updateShowList();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 京东文件下载
     */
    private void fileDownload(final String imageUrlPath, final String imageName, final int playId) {
        String saveDir = FILE_SAVE_URL + imageName;
        mMyOkHttp.download()
                .url(imageUrlPath)
                .filePath(saveDir)
                .tag(this).enqueue(new DownloadResponseHandler() {
            @Override
            public void onFinish(File downloadFile) {
                e("京东广告下载完成,加播放ID", imageName + "; " + playId);
                if (checkMd5(imageName)) {
                    updateDownloadState2(imageName);
                    e("京东加入后清单大小", showSumSize);
                    jingDongPaly(imageName, playId, showSumSize);
                }
            }

            @Override
            public void onProgress(long currentBytes, long totalBytes) {
            }

            @Override
            public void onFailure(String errorMsg) {
                e("京东广告下载完成失败，原因", errorMsg);
                fileDownload(imageUrlPath, imageName, playId);
            }
        });
    }

    /**
     * 京东广告立即播放
     */
    private void jingDongPaly(final String imageName, final int playId, int showSumSize) {
        if (showSumSize == 1) {
            Parameter playUrl = new Parameter();
            playUrl.setPlayUrl(imageName);
            playUrl.updateAll("uniqueness = ?", "Pan");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    e("立即播放京东广告");
                    setCount(Integer.toString(playId), 1);
                    currentPlayId = Integer.toString(playId);
                    isJdStart = true;
                    showPlay(imageName, true);
                }
            });
        }
    }

    /**
     * 下标处理
     */
    private void addSelf() {
        index = index + 1;
        if (mIsVip) {
            if (vipShowSum().size() == index || index > vipShowSum().size()) {
                index = 0;
            }
        } else {
            if (showSumSize == index || index > showSumSize) {
                index = 0;
            }
        }
    }

    /**
     * 上一个节目
     */
    private void previousShow() {
        int vipSize = vipShowSum().size();
        int listSize = 2;
        e("播放上一个", index);
        e("当前清单大小", showSumSize);

        if (index == 0) {
            if (showSumSize == listSize || vipSize == listSize) {
                index = 1;
            } else {
                if (mIsVip) {
                    index = vipSize - 3;
                } else {
                    index = showSumSize - 3;
                }
            }
        } else if (index == 1) {
            if (showSumSize == listSize || vipSize == listSize) {
                index = 0;
            } else {
                if (mIsVip) {
                    index = vipSize - 2;
                } else {
                    index = showSumSize - 2;
                }
            }
        } else if (index == listSize) {
            if (mIsVip) {
                index = vipSize - 1;
            } else {
                index = showSumSize - 1;
            }
        } else if (index > listSize) {
            index = index - 3;
        }
        if (index < 0) {
            index = 0;
        }
        String playListId = getShowId();
        String fileName = getFileName(playListId);
        String saveDir = FILE_SAVE_URL + fileName;
        if (fileIsExists(saveDir)) {
            localShowPlay(playListId);
            playVideo();
        } else {
            e("文件不存在跳转上一个");
            previousShow();
        }
    }

    /**
     * 获取节目ID
     */
    private String getShowId() {
        try {
            if (mIsVip) {
                showId = vipShowSum().get(index).toString();
            } else {
                showId = showSumList.get(index).toString();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return showId;
    }

    /**
     * 下一个节目
     */
    private void nextShow() {
        if (showSumSize != 0) {
            e("播放下一个");
            indexReset();
            index = index - 1;
            if (index < 0) {
                if (mIsVip) {
                    index = vipShowSum().size() - 1;
                } else {
                    index = showSumSize - 1;
                }
            }
            String playListId = getShowId();
            String fileName = getFileName(playListId);
            String saveDir = FILE_SAVE_URL + fileName;
            if (fileIsExists(saveDir)) {
                localShowPlay(playListId);
                mUri = fileName;
                playVideo();
                isJson = true;
            } else {
                e("文件不存在跳转下一个");
                nextShow();
            }
        } else {
            assignLoop();
        }
    }

    /**
     * 设备音量控制，最大级别15，最小级别0
     */
    private void volumeControl(AudioManager mAudio, int volume) {
        int current = mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
        e("当前的音量值", current);
        e("设置的音量值", volume);
        mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        volumeControlAnswer();
    }

    /**
     * 设备音量控制回应
     */
    private void volumeControlAnswer() {
        command[0] = (byte) 0x81;
        number[0] = (byte) 0xB4;
        String controlNumber = SpUtil.readString(Const.CONTROL_TYPE);
        List<Parameter> parameter = LitePal.findAll(Parameter.class);
        for (Parameter parameters : parameter) {
            final String id = parameters.getDeviceId();
            final String rcode = parameters.getrCode();
            switch (controlNumber) {
                case "LAN":
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().writeAnswer(id, rcode, command, number, true, "client");
                        }
                    });
                    break;
                case "INTERNET":
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().writeAnswer(id, rcode, command, number, true, "");
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 3288主板设置时间状态
     */
    public void setTimeState(String state) {
        SharedPreferences spf = this.getSharedPreferences("timer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString("timer_state", state);
        editor.apply();
    }

    /**
     * 整点报时
     */
    private void initTimePrompt() {
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, timeFilter);
        isTimeReceiver = true;
        e("开启整点红包");
    }

    boolean isTimeReceiver = false;
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int minNumber = 59;
            final Calendar cal = Calendar.getInstance();
            final int min = cal.get(Calendar.MINUTE);
//            Plog.e("分：", min);
//            if (min == 0 || min == 10 || min == 20 || min == 30 || min == 40 || min == 50) {
            if (min == 0) {
                e("是否还有整点红包", hasRedPacket);
                if (hasRedPacket) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String hasRedPacketName = SpUtil.readString(Const.HAS_REDPACKET_NAME);
                            String hasRedPacketNamePath = FILE_SAVE_URL + hasRedPacketName;
                            e("整点红包图片路径", hasRedPacketNamePath);
                            if (fileIsExists(hasRedPacketNamePath)) {
                                imagePlay(hasRedPacketName, "JPG", true);
                                openAssetMusics("zhengdian-tts");
                                if (mVideoView.isPlaying()) {
                                    mVideoView.stopPlayback();
                                }
                                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeAssetMusics();
                                    }
                                }, 15, TimeUnit.SECONDS);
                            }
                        }
                    });
                }
//            } else if (min == minNumber || min == 9 || min == 19 || min == 29 || min == 39 || min == 49) {
            } else if (min == minNumber) {
                redPacketPrepare(cal);
            }
        }

        /**
         * 整点红包准备
         */
        private void redPacketPrepare(final Calendar cal) {
            List<Parameter> deviceId = LitePal.findAll(Parameter.class);
            for (Parameter deviceIds : deviceId) {
                final String url = GET_REDPACKET_AMOUNT + deviceIds.getDeviceId();
                ThreadManager.getInstance().doExecute(new Runnable() {
                    @Override
                    public void run() {
                        mMyOkHttp.get()
                                .url(url)
                                .tag(this)
                                .enqueue(new RawResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, String response) {
                                        e("获取整点红包的数据:" + response);
                                        try {
                                            String urlDecoder = URLDecoder.decode(response, "UTF-8");
                                            JSONObject json = new JSONObject(urlDecoder);
                                            if (json.getBoolean("success")) {
                                                hasRedPacket = json.getBoolean("hasRedPacket");
                                                hasRedPacketMoney = json.getInt("amount");
                                                SpUtil.writeBoolean(Const.HAS_READPECKET, hasRedPacket);
                                                SpUtil.writeInt(Const.HAS_REDPACKET_MONEY, hasRedPacketMoney);
                                                int second = cal.get(Calendar.SECOND);
                                                hasRedPacketTips(second);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int statusCode, String errorMsg) {
                                        e("失败原因:" + errorMsg);
                                        hasRedPacket = SpUtil.readBoolean(Const.HAS_READPECKET);
                                        hasRedPacketMoney = SpUtil.readInt(Const.HAS_REDPACKET_MONEY);
                                        int second = cal.get(Calendar.SECOND);
                                        hasRedPacketTips(second);
                                    }
                                });
                    }
                });
            }
        }
    };

    /**
     * 整点红包提示
     */
    private void hasRedPacketTips(int second) {
        if (hasRedPacketMoney != 0 && hasRedPacket) {
            e("第几秒", second);
            String hasRedPacketName = SpUtil.readString(Const.HAS_REDPACKET_NAME);
            String hasRedPacketNamePath = FILE_SAVE_URL + hasRedPacketName;
            e("整点红包图片路径", hasRedPacketNamePath);
            if (fileIsExists(hasRedPacketNamePath)) {
                ExecutorServiceManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        e("开始整点红包播放");
                        openAssetMusics("zhengdian2-tts.mp3");
                    }
                }, 30, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 内部匿名时段播放时间类
     */
    class PeakParameter {
        String startTime;
        String endTime;
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (!mVideoView.isPlaying()) {
                nextShow();
                e("由于网络异常跳转");
            }
        }
    };

    TimerTask deviceIdTask = new TimerTask() {
        @Override
        public void run() {
            boolean isDeviceId = SpUtil.readBoolean(Const.ISSUCCEED);
            String buttonName = SpUtil.readString(Const.ZXING_BUTTON);
            String button = "ZXing";
            e("二维码加载结果和控件名", isDeviceId + "__" + buttonName);
            if (!isDeviceId && button.equals(buttonName)) {
                textZxingVisible();
            }
        }
    };

    /**
     * ID号显示控制
     */
    private void textZxingVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<Parameter> parameter = LitePal.findAll(Parameter.class);
                for (Parameter parameters : parameter) {
                    mZxing.setVisibility(View.GONE);
                    mText.setText(parameters.getDeviceId());
                    mText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            inputBoxDisplay(false);
        }
        return false;
    }

    /**
     * 弹出输入窗
     */
    private void inputBoxDisplay(boolean isFirstStart) {
        e("弹窗");
        int width;
        Display display = getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        if (outSize.x > outSize.y) {
            width = outSize.y / 4;
        } else {
            width = outSize.x / 4;
        }
        SpUtil.writeBoolean(Const.BASE_POPUP_TYPE, isFirstStart);
        new BasePopup(this, width, isFirstStart).showPopupWindow();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        e("我是home键");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        e("点击了屏幕");
        Intent intent = new Intent();
        intent.setAction(OPEN_YUNZHONG_ACTION);
        sendBroadcast(intent);
        return super.onTouchEvent(event);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            monitorService = ((MonitorService.ServiceBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            monitorService = null;
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            e("横屏", String.valueOf(Configuration.ORIENTATION_LANDSCAPE));
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            e("竖屏", String.valueOf(Configuration.ORIENTATION_PORTRAIT));
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        e("onPause");
//        mMediaController.getWindow().dismiss();
        mVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        e("onResume");
        CustomActivityManager.getInstance().setTopActivity(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver, filter);
        mVideoView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        e("onStop");
//        mMarqueeView.stopScroll();
//        LocalBroadcastManager.getInstance(context).unregisterReceiver(myBroadcastReceiver);
//        SocketManager.getInstance().close();
//        TcpServer.getInstance().closeSelf();
//        ThreadManager.getInstance().shutdown();
//        ExecutorServiceManager.getInstance().shutdown();
//        executorService.shutdown();
//        if (isTimeReceiver) {
//            unregisterReceiver(mTimeReceiver);
//        }
        //2018-12-3新增 2019-6-10去掉
//        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        e("销毁");
        Intent intent = new Intent(Const.EXITAPP);
        intent.putExtra("closeAll", 1);
        sendBroadcast(intent);
        context = null;
        try {
            this.unbindService(conn);
            mVideoView.stopPlayback();
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            mMarqueeView.stopScroll();
            LocalBroadcastManager.getInstance(context).unregisterReceiver(myBroadcastReceiver);
            SocketManager.getInstance().close();
            TcpServer.getInstance().closeSelf();
            ThreadManager.getInstance().shutdown();
            ExecutorServiceManager.getInstance().shutdown();
            executorService.shutdown();
            if (isTimeReceiver) {
                unregisterReceiver(mTimeReceiver);
            }

            mMyOkHttp.cancel(context);
            GlideUtil.glieClear(this, mAppLogo);
            GlideUtil.glieClear(this, mCoverView);
            GlideUtil.glieClear(this, mZxing);
            GlideUtil.glieClear(this, mSignal);
            GlideUtil.glieClear(this, mImage);
            GlideUtil.glieClear(this, mRedPacket);
            GlideUtil.glieClear(this, mGifImage);
            GlideUtil.glieClear(this, mTissue);
            NetWorkReceiver.unRegister(this);
        } catch (Exception e) {
            e("异常原因", e.toString());
        }
        System.exit(0);
        android.os.Process.killProcess(Process.myPid());
    }

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            e("Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    // SDK will do reconnecting automatically
                    e("网络异常 !");
                    List<Parameter> parameterIo = LitePal.findAll(Parameter.class);
                    for (Parameter parameters : parameterIo) {
                        if (parameters.getPlayType() == 1) {
                            ExecutorServiceManager.getInstance().schedule(task, 10, TimeUnit.SECONDS);
                        }
                    }
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    e("播放器打开失败 !");
                    plOnErrorCodeOpenFailed();
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    e("拖动加载失败 !");
                    break;
                default:
                    e("未知错误 !");
                    break;
            }
            return true;
        }
    };

    /**
     * 播放异常处理
     */
    private void plOnErrorCodeOpenFailed() {
        List<Parameter> parameter = LitePal.findAll(Parameter.class);
        for (final Parameter parameters : parameter) {
            final String deviceId = parameters.getDeviceId();
            rCode = parameters.getrCode();
            playType = parameters.getPlayType();
            String fileName = parameters.getPlayUrl();
            command[0] = (byte) 0x85;
            number[0] = (byte) 0xAD;
            List<PlayList> query = getQuery("showId", "fileName", fileName);
            for (PlayList querys : query) {
                showId = Integer.toString(querys.getShowId());
            }
            if (parameters.getPlayType() == 4 && parameters.getStartPlayType() == 2) {
                e("执行跳转播放");
                e("清单下标", index);
                if (showSumSize == 0) {
                    assignLoop();
                } else {
                    playVideo();
                }
                Base.removeFile(fileName);
                ThreadManager.getInstance().doExecute(new Runnable() {
                    @Override
                    public void run() {
                        ProtocolManager.getInstance().controlAnswer(deviceId, rCode,
                                command, number, playType, showId, 15, "");
                    }
                });
            } else if (parameters.getPlayType() != 4 && 2 == parameters.getStartPlayType()) {
                if (showSumSize == 0) {
                    assignLoop();
                } else {
                    playVideo();
                }
                ThreadManager.getInstance().doExecute(new Runnable() {
                    @Override
                    public void run() {
                        ProtocolManager.getInstance().controlAnswer(deviceId, rCode,
                                command, number, playType, showId, 0, "");
                    }
                });
            }
            if ("82".equals(error)) {
                command[0] = (byte) 0x82;
                number[0] = (byte) 0xA1;
                ThreadManager.getInstance().doExecute(new Runnable() {
                    @Override
                    public void run() {
                        if (4 == parameters.getPlayType()) {
                            ProtocolManager.getInstance().controlAnswer(deviceId, rCode,
                                    command, number, playType, showId, 15, "");
                        } else if ((parameters.getPlayType() != 4)) {
                            ProtocolManager.getInstance().controlAnswer(deviceId, rCode,
                                    command, number, playType, showId, 0, "");
                        }
                    }
                });
            }
        }
    }

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            e("Play Completed !");

            jingDongReport();

            mIsNewPlay = true;
            if (showSumSize == 0) {
                System.gc();
                setIntentData("playType", 4);
                assignLoop();
            } else {
                System.gc();
                playVideo();
            }
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            e("onVideoSizeChanged: width = " + width + ", height = " + height);
//            mVideoView.start();
            e("onVideoSizeChanged执行播放");
            mPlayResult = true;
            ExecutorServiceManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    if (mPlayResult) {
                        e("暂停后再次播放");
                        mVideoView.start();
                    }
                }
            }, 2, TimeUnit.SECONDS);
        }
    };

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    mPlayResult = false;
                    e("First video render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    mPlayResult = false;
                    e(TAG, "First audio render time: " + extra + "ms");
                    if (Build.VERSION.SDK_INT == JELLY_BEAN_MR1) {
                        List<Parameter> parameter = LitePal.findAll(Parameter.class);
                        for (Parameter parameters : parameter) {
                            String videoPath = parameters.getPlayUrl();
                            if (videoPath != null && sdk17 == 1) {
                                e("4.2.2版本黑屏，重新播放");
                                sdk17 = 2;
                                mVideoView.stopPlayback();
                                playInitialize();
                                mVideoView.setVideoPath(FILE_SAVE_URL + videoPath);
                                mVideoView.start();
                            }
                        }
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    e(TAG, "Hardware decoding failure, switching software decoding!");
                    int codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_HW_DECODE);
                    AVOptions options = new AVOptions();
                    options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    e(TAG, "Connected !");
                    break;
                default:
                    break;
            }
        }
    };
}
