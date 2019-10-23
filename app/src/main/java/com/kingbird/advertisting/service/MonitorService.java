package com.kingbird.advertisting.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.kingbird.advertisting.base.Base;
import com.kingbird.advertisting.base.UdpIoHandlerAdapter;
import com.kingbird.advertisting.litepal.Charge;
import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.litepal.PlayList;
import com.kingbird.advertisting.manager.ExecutorServiceManager;
import com.kingbird.advertisting.manager.ProtocolDao;
import com.kingbird.advertisting.manager.ProtocolManager;
import com.kingbird.advertisting.manager.SocketManager;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.manager.UdpManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.MyLocationListener;
import com.kingbird.advertisting.utils.NetUtil;
import com.kingbird.advertisting.utils.SerialPortUtil;
import com.kingbird.advertisting.utils.SpUtil;
import com.kingbird.advertisting.utils.SystemBinUtils;
import com.kuaifa.ad.KuaiFaClient;
import com.tsy.sdk.myokhttp.MyOkHttp;
import com.tsy.sdk.myokhttp.response.DownloadResponseHandler;
import com.tsy.sdk.myokhttp.response.RawResponseHandler;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.VERSION_CODES.N;
import static com.kingbird.advertisting.activity.VideoViewActivity.context;
import static com.kingbird.advertisting.base.Base.bindDealer;
import static com.kingbird.advertisting.base.Base.bytes2HexString;
import static com.kingbird.advertisting.base.Base.chargeQuery;
import static com.kingbird.advertisting.base.Base.checkMd5;
import static com.kingbird.advertisting.base.Base.convertHexToString;
import static com.kingbird.advertisting.base.Base.dataModify;
import static com.kingbird.advertisting.base.Base.dataProcessing;
import static com.kingbird.advertisting.base.Base.downloadFtpFile;
import static com.kingbird.advertisting.base.Base.getAnIntHex;
import static com.kingbird.advertisting.base.Base.getAnString;
import static com.kingbird.advertisting.base.Base.getQuery;
import static com.kingbird.advertisting.base.Base.hexToStringGbk;
import static com.kingbird.advertisting.base.Base.intentActivity;
import static com.kingbird.advertisting.base.Base.isAccessibilitySettingsOn;
import static com.kingbird.advertisting.base.Base.readBaiDuShow;
import static com.kingbird.advertisting.base.Base.reconnect;
import static com.kingbird.advertisting.base.Base.removeFile;
import static com.kingbird.advertisting.base.Base.setIntentData;
import static com.kingbird.advertisting.base.Base.setShowData;
import static com.kingbird.advertisting.base.Base.showIdQuery;
import static com.kingbird.advertisting.base.Base.showPlay;
import static com.kingbird.advertisting.base.Base.updateDownloadState;
import static com.kingbird.advertisting.base.Base.webInterface;
import static com.kingbird.advertisting.utils.Config.ADV_JINGDONG;
import static com.kingbird.advertisting.utils.Config.ADV_VOICE;
import static com.kingbird.advertisting.utils.Config.APP_DOWNLOAD;
import static com.kingbird.advertisting.utils.Config.APP_FRONT;
import static com.kingbird.advertisting.utils.Config.CONSTANT_ELEVEN;
import static com.kingbird.advertisting.utils.Config.CONSTANT_FIVE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_ONE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_TEN;
import static com.kingbird.advertisting.utils.Config.CONSTANT_THREE;
import static com.kingbird.advertisting.utils.Config.CONSTANT_TWO;
import static com.kingbird.advertisting.utils.Config.DOMAIN_NAME;
import static com.kingbird.advertisting.utils.Config.DOWNLOAD_COMPLETE;
import static com.kingbird.advertisting.utils.Config.ELECTRIFY;
import static com.kingbird.advertisting.utils.Config.FILE_DOWNLOAD;
import static com.kingbird.advertisting.utils.Config.FILE_SAVE_URL;
import static com.kingbird.advertisting.utils.Config.GET_MEDIA_TIME;
import static com.kingbird.advertisting.utils.Config.INITIAL_IP;
import static com.kingbird.advertisting.utils.Config.INITIAL_PORT;
import static com.kingbird.advertisting.utils.Config.JINGDONG_APP_ID;
import static com.kingbird.advertisting.utils.Config.JINGDONG_APP_KEY;
import static com.kingbird.advertisting.utils.Config.JINGDONG_HOST;
import static com.kingbird.advertisting.utils.Config.LOTTERY_MACHINE;
import static com.kingbird.advertisting.utils.Config.LOTTERY_MACHINE_MODEL;
import static com.kingbird.advertisting.utils.Config.NUMBER_01;
import static com.kingbird.advertisting.utils.Config.NUMBER_0B;
import static com.kingbird.advertisting.utils.Config.NUMBER_B5;
import static com.kingbird.advertisting.utils.Config.NUMBER_B6;
import static com.kingbird.advertisting.utils.Config.NUMBER_BC;
import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME;
import static com.kingbird.advertisting.utils.Config.PACKAGE_NAME2;
import static com.kingbird.advertisting.utils.Config.RK_3128;
import static com.kingbird.advertisting.utils.Config.RK_3288;
import static com.kingbird.advertisting.utils.Config.SCROLL_TEXT;
import static com.kingbird.advertisting.utils.Config.SET_LOGO_QR_SIZE;
import static com.kingbird.advertisting.utils.Config.START_FILE_DOWNLOAD;
import static com.kingbird.advertisting.utils.Config.TISSUE;
import static com.kingbird.advertisting.utils.Config.VIDEO_TYPE;
import static com.kingbird.advertisting.utils.Config.WRITE_DATA;
import static com.kingbird.advertisting.utils.Config.YIN_NUO_HENG_MODEL;
import static com.kingbird.advertisting.utils.Config.YIN_NUO_HENG_MODEL2;
import static com.kingbird.advertisting.utils.Const.DEVICE_MODEL;
import static com.kingbird.advertisting.utils.Const.IS_INTERVAL_TIME;
import static com.kingbird.advertisting.utils.Const.LOGO_SIZE;
import static com.kingbird.advertisting.utils.Const.PICTURE_FILE_NAME;
import static com.kingbird.advertisting.utils.Const.QR_SIZE;
import static com.kingbird.advertisting.utils.Plog.e;

/**
 * 通讯服务 class
 *
 * @author panyingdao
 * @date 2017/12/15.
 */
public class MonitorService extends Service implements UdpIoHandlerAdapter.UdpIoHandlerListener {
    private static final String TAG = "MonitorService";
    private BroadcastReceiver closeRec;
    private static int failedCount = 0;
    private int onFailureCount;
    private static byte[] receive;

    public LocationClient mLocationClient = null;
    private BDAbstractLocationListener myListener = new MyLocationListener();
    private boolean isLoop = true;
    private long startTimeDownload;
    private int heartBeat;
    private int heartBeatCount = 0;
    private int failCount = 0;

    private String ftpIp;
    private int port;
    private int whether;
    private String userName;
    private String passWord;
    private String ftpFileName;

    private int orderNumber;
    private String condition;
    private String startTime;
    private String endTime;
    private int duration;
    private int playType;
    private String fileName;
    private int account;
    private String showName;
    private String pictureName;
    private int showId;
    private int member;
    private int vip;
    private int splitScreen;
    private int redPacket;
    private int isBaiDu;
    private int isIntervalTime;
    private String fileMd5;
    private static ArrayList<String> intervalTimeArray = new ArrayList<>();
    byte[] sendCommand = new byte[1];
    byte[] sendNumber = new byte[1];

    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = new ServiceBinder();
        e("onBind", Toast.LENGTH_LONG);
        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        List<Parameter> parameters = LitePal.findAll(Parameter.class);
        for (Parameter parameter : parameters) {
            heartBeat = parameter.getHeartBeat();
            e("通讯心跳周期", heartBeat);
            if (2 == parameter.getProtocolType()) {
                connectUDP(parameter);
            } else {
                doReceiveDataFromServer();
                initHeartBeat();//心跳
            }
            screenHeartBeatDemo(parameter.getDeviceId());
        }


        initLocation();//定位

        closeRec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SocketManager.getInstance().close();
                stopSelf();
            }
        };

        IntentFilter filter = new IntentFilter(Const.EXIT_APP);
        registerReceiver(closeRec, filter);

        String model = SpUtil.readString(DEVICE_MODEL);
        if (YIN_NUO_HENG_MODEL.equals(model) || YIN_NUO_HENG_MODEL2.equals(model)) {
            if (!SerialPortUtil.open()) {
                SerialPortUtil.open();
            }
            List<Charge> charge = LitePal.findAll(Charge.class);
            for (Charge charges : charge) {
                if (charges.getTime() != 0 || charges.getNewAddTime() != 0) {
                    openChargeListen();
                }
            }
        } else if (RK_3128.equals(model)) {
            List<Charge> charge = LitePal.findAll(Charge.class);
            for (Charge charges : charge) {
                if (charges.getTime() != 0 || charges.getNewAddTime() != 0) {
                    openChargeListen();
                }
            }
        }

    }

    /**
     * UDP连接
     */
    private void connectUDP(Parameter parameter) {
        isLoop = false;
        UdpManager.setIp(parameter.getIp());
        UdpManager.setPort(parameter.getPort());
        //初始化UDP mina相关
        if (UdpManager.getInstance().getConnectorUdp() == null) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    e("udp连接情况", UdpManager.getInstance().connectUdp(new
                            UdpIoHandlerAdapter(MonitorService.this)));
//                    if (SocketManager.getInstance().getSocket() != null) {
//                        SocketManager.getInstance().close();
//                    }
                }
            });
        }
    }

    private void doReceiveDataFromServer() {
        if (isLoop) {
            ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (SocketManager.getInstance().getSocket() != null) {
                        SpUtil.writeString(Const.NET_TYPE, "tcp");
                        receive = SocketManager.getInstance().receive();
                        rcvDataProcessing(receive);
                    }
                }
            }, 0, 10, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 接收数据
     */
    public void rcvDataProcessing(byte[] data) {
        List<Parameter> parameter = LitePal.findAll(Parameter.class);
        for (final Parameter parameters : parameter) {
            if (data != null) {
                int dataLength = data.length;
                e("接收数据长度", dataLength);
                if (dataLength == 22) {
                    dealWithHeartBeat(data);
                    heartBeatCount = 0;
                    failCount = 0;
                }
                if (dataLength == 20) {
                    remoteRestart(parameters);
                }
                if (dataLength > 22) {
                    int rcvLength = (dataLength - 20);
                    byte[] validData = new byte[rcvLength];
                    System.arraycopy(data, 20, validData, 0, rcvLength);
                    byte[] command = ProtocolManager.getInstance().parseParameter(validData, 1, 1);
                    byte[] number = ProtocolManager.getInstance().parseParameter(validData, 4, 1);
                    String commands = bytes2HexString(command);
                    String numbers = bytes2HexString(number);
                    String id = convertHexToString(getAnString(data, 2, 11));
                    String deviceId = parameters.getDeviceId();
                    String rCode = parameters.getrCode();
                    e("服务器请求数据", bytes2HexString(validData));
                    e("服务器请求ID", id);
                    e("本地ID", deviceId);
                    e("命令", commands);
                    e("编号", numbers);
                    if (id.equals(parameters.getDeviceId())) {
                        SpUtil.writeString(Const.CONTROL_TYPE, "INTERNET");
                        List<String> commandArray = Arrays.asList(FILE_DOWNLOAD, SCROLL_TEXT, APP_DOWNLOAD, START_FILE_DOWNLOAD, ELECTRIFY,
                                SET_LOGO_QR_SIZE, ADV_VOICE, WRITE_DATA, NUMBER_0B, TISSUE, ADV_JINGDONG);
                        if (commandArray.contains(commands)) {
                            controlCommand(parameters, validData, command, number, commands, numbers, deviceId, rCode);
                        } else {
                            dataProcessing(validData, "");
                        }
                    }
                    if ("90".equals(commands) && "01".equals(numbers)) {
                        ProtocolManager.getInstance().readAnswer
                                (deviceId, rCode, command, number, deviceId, "");
                    }
                }
            }
        }
    }

    /**
     * 指令控制
     */
    private void controlCommand(Parameter parameters, byte[] validData, byte[] command, byte[] number, String commands, String numbers, String deviceId, String rCode) {
        switch (commands) {
            case FILE_DOWNLOAD:
                if (NUMBER_B5.equals(numbers)) {
                    e("文件下载");
                    httpFileDownload(validData, command, number, parameters, commands, numbers);
                } else {
                    ftpDownload(validData, parameters);
                }
                break;
            case SCROLL_TEXT:
                subtitlePaly(validData, deviceId, rCode);
                break;
            case APP_DOWNLOAD:
                if (NUMBER_B5.equals(numbers)) {
                    httpFileDownload(validData, command, number, parameters, commands, numbers);
                } else {
                    appDownload(validData, parameters);
                }
                break;
            case START_FILE_DOWNLOAD:
                if (NUMBER_B6.equals(numbers)) {
                    httpFileDownload(validData, command, number, parameters, commands, numbers);
                }
                break;
            case ELECTRIFY:
                String mode = SpUtil.readString(DEVICE_MODEL);
                if (RK_3128.equals(mode)) {
                    e("10寸屏充电");
                    controlCharge2(validData);
                } else {
                    controlCharge(validData);
                }
                break;
            case SET_LOGO_QR_SIZE:
                logoQr(validData, deviceId, rCode);
                break;
            case ADV_VOICE:
                voiceAnalysis(validData);
                break;
            case WRITE_DATA:
                if (numbers.equals(NUMBER_0B)) {
                    e("开始节目加数据解析");
                    try {
                        parseShow(validData, parameters);
                        if (isBaiDu != 1 && isIntervalTime != 1) {
                            setDeviceShow(parameters);
                            sendCommand[0] = (byte) 0x91;
                            sendNumber[0] = (byte) 0x0B;
                            ProtocolManager.getInstance().internetShowAnswer(parameters.getDeviceId(),
                                    parameters.getrCode(), sendCommand, sendNumber, orderNumber, showId, 2, "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        e("清单数据解析异常", e.toString());
                    }
                } else {
                    dataProcessing(validData, "");
                }
                break;
            case TISSUE:
                byte[] sendData = ProtocolDao.openChargeData(6);
                e("出纸数组", bytes2HexString(sendData));
                SerialPortUtil.sendPort(sendData);
                break;
            case ADV_JINGDONG:
                if (numbers.equals(NUMBER_BC)) {
                    String isStart = getAnString(validData, 6, 1);
                    e("是否启动京东广告", isStart);
                    if (NUMBER_01.equals(isStart)) {
                        SpUtil.writeBoolean(Const.IS_START_JINGDONG, true);
                    } else {
                        SpUtil.writeBoolean(Const.IS_START_JINGDONG, false);
                    }
                    Base.intentActivity("23");
                }
                break;
            default:
        }
    }

    /**
     * logo、二维码显示大小控制
     */
    private void logoQr(byte[] validData, String deviceID, String rCode) {
        String number = "B8", number2 = "B9";
        e("数据", bytes2HexString(validData));
        String logoNumber = getAnString(validData, 4, 1);
        String qrNumber = getAnString(validData, 7, 1);
        int logoSize = getAnIntHex(validData, 6, 1, 16);
        int qrSize = getAnIntHex(validData, 9, 1, 16);
        e("控制logo显示编号：", logoNumber + ";" + qrNumber);
        e("logo、qr显示比例", logoSize + ";" + qrSize);
        if (number.equals(logoNumber) && number2.equals(qrNumber)) {
            SpUtil.writeInt(LOGO_SIZE, logoSize);
            SpUtil.writeInt(QR_SIZE, qrSize);
            intentActivity("21");
            byte[] commandLogo = new byte[1];
            commandLogo[0] = (byte) 0x96;
            byte[] numberLogo = new byte[1];
            numberLogo[0] = (byte) 0xB8;
            controlAnswer(commandLogo, numberLogo, deviceID, rCode);
        }
    }

    /**
     * 手机充电
     */
    private void controlCharge(final byte[] validData) {
        final int chargeNumber = getAnIntHex(validData, 5, 1, 16);
        int chargeTime = getAnIntHex(validData, 6, 2, 16);
        e("充电线序号", chargeNumber);
        e("充电时间", chargeTime);
        ArrayList<Integer> chargeList = new ArrayList<>();
        List<Charge> queryCharge = LitePal.findAll(Charge.class);
        if (queryCharge.size() > 0) {
            for (Charge charge1 : queryCharge) {
                chargeList.add(charge1.getChargeNumber());
            }
        }

        e("充电序号集合", chargeList);
        if (chargeList.contains(chargeNumber)) {
            List<Charge> queryTime = chargeQuery("chargeTime", chargeNumber);
            for (Charge queryTimes : queryTime) {
//                List<Charge> queryNewAddTime = chargeQuery("newAddTime", chargeNumber);
//                for (Charge queryNewAddTimes : queryNewAddTime) {
                int time = queryTimes.getTime();
                e("充电时间", time);
                if (time != 0) {
                    List<Charge> queryStartTime = chargeQuery("startTime", chargeNumber);
                    for (Charge queryStartTimes : queryStartTime) {
                        int consumeTime = getSystemTime() - queryStartTimes.getStartTime();
                        e("已充电时间", consumeTime / 60);
                        int surplusTime = time - (consumeTime / 60);
                        e("剩余充电时间", surplusTime);
                        int newTime = chargeTime - surplusTime;
                        e("补充的充电时间", newTime);
                        setChargeParameter("newAddTime", newTime, chargeNumber);
                    }
//                        int newAddTime = queryNewAddTimes.getNewAddTime() + chargeTime;
//                        setChargeParameter("newAddTime", newAddTime, chargeNumber);
                } else {
                    e("设置充电时间", chargeTime);
                    setChargeParameter("chargeTime", chargeTime, chargeNumber);
                    int startTime = getSystemTime();
                    e("设置充电开始时间", startTime);
                    setChargeParameter("startTime", startTime, chargeNumber);
                    setChargeParameter("state", 0, chargeNumber);
                }
//                }
            }
        } else {
            e("新增充电数据");
            chargeSave(chargeNumber, chargeTime);
        }

        controlPort(1, chargeNumber);

//        chargingTimingCheck();
        openChargeListen();

    }

    /**
     * 手机充电
     */
    private void controlCharge2(final byte[] validData) {
        final int chargeNumber = getAnIntHex(validData, 5, 1, 16);
        int chargeTime = getAnIntHex(validData, 6, 2, 16);
        e("充电线序号", chargeNumber);
        e("充电时间", chargeTime);
        ArrayList<Integer> chargeList = new ArrayList<>();
        List<Charge> queryCharge = LitePal.findAll(Charge.class);
        if (queryCharge.size() > 0) {
            for (Charge charge1 : queryCharge) {
                chargeList.add(charge1.getChargeNumber());
            }
        }

        e("充电序号集合", chargeList);
        if (chargeList.contains(chargeNumber)) {
            List<Charge> queryTime = chargeQuery("chargeTime", chargeNumber);
            for (Charge queryTimes : queryTime) {
                int time = queryTimes.getTime();
                e("充电时间", time);
                if (time != 0) {
                    List<Charge> queryStartTime = chargeQuery("startTime", chargeNumber);
                    for (Charge queryStartTimes : queryStartTime) {
                        int consumeTime = getSystemTime() - queryStartTimes.getStartTime();
                        e("已充电时间", consumeTime / 60);
                        int surplusTime = time - (consumeTime / 60);
                        e("剩余充电时间", surplusTime);
                        int newTime = chargeTime - surplusTime;
                        e("补充的充电时间", newTime);
                        setChargeParameter("newAddTime", newTime, chargeNumber);
                    }
                } else {
                    e("设置充电时间", chargeTime);
                    setChargeParameter("chargeTime", chargeTime, chargeNumber);
                    int startTime = getSystemTime();
                    e("设置充电开始时间", startTime);
                    setChargeParameter("startTime", startTime, chargeNumber);
                    setChargeParameter("state", 0, chargeNumber);
                }
            }
        } else {
            e("新增充电数据");
            chargeSave(chargeNumber, chargeTime);
        }

        controlPort3128(1, chargeNumber);

        openChargeListen();

    }

    /**
     * 清单数据解析
     */
    private void parseShow(byte[] receive, Parameter parameter) {
        orderNumber = getAnIntHex(receive, 6, 2, 16);
        e("序号", orderNumber);
        condition = getAnString(receive, 8, 1);
        e("限定条件", condition);
        int startYear = getAnIntHex(receive, 9, 2, 16);
        int startMonths = getAnIntHex(receive, 11, 1, 16);
        int startDay = getAnIntHex(receive, 12, 1, 16);
        int startHour = getAnIntHex(receive, 13, 1, 16);
        int startMinute = getAnIntHex(receive, 14, 1, 16);
        int startSecond = getAnIntHex(receive, 15, 1, 16);
        startTime = (startYear + "-" + startMonths + "-" + startDay + " " + startHour + ":" + startMinute + ":" + startSecond);
        e("起始时间", startTime);
        int endYear = getAnIntHex(receive, 16, 2, 16);
        int endMonths = getAnIntHex(receive, 18, 1, 16);
        int endDay = getAnIntHex(receive, 19, 1, 16);
        int endHour = getAnIntHex(receive, 20, 1, 16);
        int endMinute = getAnIntHex(receive, 21, 1, 16);
        int endSecond = getAnIntHex(receive, 22, 1, 16);
        endTime = (endYear + "-" + endMonths + "-" + endDay + " " + endHour + ":" + endMinute + ":" + endSecond);
        e("终止时间", endTime);
        duration = getAnIntHex(receive, 23, 2, 16);
        e("设置播放次数", duration);
        playType = getAnIntHex(receive, 25, 1, 16);
        e("播放类型", playType);
        int fileLength = getAnIntHex(receive, 26, 1, 16);
        e("文件长度", fileLength);
        try {
            fileName = java.net.URLDecoder.decode(hexToStringGbk(getAnString(receive, 27, fileLength)), "UTF-8");
            e("文件名", fileName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        account = getAnIntHex(receive, 27 + fileLength, 4, 16);
        e("商家账号", account);
        int showLength = getAnIntHex(receive, 31 + fileLength, 1, 16);
        showName = hexToStringGbk(getAnString(receive, 32 + fileLength, showLength));
        e("节目名称", showName);
        showId = getAnIntHex(receive, 32 + fileLength + showLength, 4, 16);
        e("节目ID", showId);
        member = getAnIntHex(receive, 36 + fileLength + showLength, 1, 16);
        e("会员类型", member);
        vip = getAnIntHex(receive, 37 + fileLength + showLength, 1, 16);
        e("是否是VIP播放", vip);
        splitScreen = getAnIntHex(receive, 38 + fileLength + showLength, 1, 16);
        e("是否是分屏广告", splitScreen);
        SpUtil.writeInt(Const.SPLIT_SCREEN, splitScreen);
        redPacket = getAnIntHex(receive, 39 + fileLength + showLength, 1, 16);
        SpUtil.writeInt(Const.REDPACKET, redPacket);
        e("是普通广告：1 还是红包广告：2", redPacket);
        if (redPacket == CONSTANT_TWO) {
            pictureName = "redpacket_" + parameter.getDeviceId() + "_" + showId + ".jpg";
        } else {
            pictureName = "";
        }
        isBaiDu = getAnIntHex(receive, 40 + fileLength + showLength, 1, 16);
        SpUtil.writeInt(Const.IS_BAIDU, isBaiDu);
        e("是否是百度广告", isBaiDu);
        if (isBaiDu == CONSTANT_ONE) {
            int count = readBaiDuShow().size();
            if (count > 0) {
                do {
                    count--;
                    int isDelete = LitePal.deleteAll(PlayList.class, "isBaiDu = ?", "1");
                    e("百度广告清单删除结果", isDelete);
                } while (count != 0);
            }
            intentActivity("13");
        }
        isIntervalTime = getAnIntHex(receive, 41 + fileLength + showLength, 1, 16);
        SpUtil.writeInt(Const.IS_INTERVAL_TIME, isIntervalTime);
        e("是否是时段广告", isIntervalTime);
        if (isIntervalTime == CONSTANT_ONE) {
            intervalTimeAnalysis(parameter);
        }
        fileMd5 = getAnString(receive, 42 + fileLength + showLength, 16);
        e("md5值", fileMd5);
        int lG = 58 + fileLength + showLength;
        parseShowRecursion(receive, lG);
    }

    /**
     * 粘包后的下载处理
     */
    private void parseShowRecursion(byte[] totalData, int showLength) {
        int length = totalData.length - showLength;
        e("剩余数据长度", length);
        if (length > CONSTANT_FIVE) {
            try {
                byte[] data = new byte[length - 1];
                System.arraycopy(totalData, showLength + 1, data, 0, length - 1);
                e("还剩余参数", bytes2HexString(data));
                rcvDataProcessing(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清单设置
     */
    public void setDeviceShow(Parameter parameter) {
        sendCommand[0] = (byte) 0x91;
        sendNumber[0] = (byte) 0x0B;
        boolean isNew = true;
        int baiDushowId = 0;
        int lastPlayId;
        PlayList playList = new PlayList();
        if (showId > 0) {
            List<PlayList> mpList = LitePal.findAll(PlayList.class);
            int playListSize = mpList.size();
            if (playListSize == 0) {
                lastPlayId = playListSize;
            } else {
                PlayList lastNews = LitePal.findLast(PlayList.class);
                lastPlayId = lastNews.getPlayId();
            }
            e("最终的节目播放ID", lastPlayId);
            e("数据库大小", playListSize);
            if (mpList.isEmpty()) {
                setShowData(lastPlayId + 1, condition, startTime, endTime, duration, playType, fileName, account, showName,
                        showId, member, vip, splitScreen, redPacket, pictureName, isBaiDu, baiDushowId, isIntervalTime, intervalTimeArray, fileMd5, playList);
                playList.save();
            }
            for (PlayList mpLists : mpList) {
                //2018-8-22 改为节目ID
                if (showId == mpLists.getShowId()) {
                    dataModify(lastPlayId, condition, startTime, endTime, duration, playType, fileName, account, showName, showId,
                            member, vip, splitScreen, redPacket, pictureName, isBaiDu, baiDushowId, isIntervalTime, fileMd5);
                    isNew = false;
                }
            }
            if (isNew) {
                setShowData(lastPlayId + 1, condition, startTime, endTime, duration, playType, fileName, account, showName,
                        showId, member, vip, splitScreen, redPacket, pictureName, isBaiDu, baiDushowId, isIntervalTime, intervalTimeArray, fileMd5, playList);
                playList.save();
            }

            if (isBaiDu != 1 && isIntervalTime != 1 && splitScreen != CONSTANT_TWO && playType == 1) {
                liveFor(parameter);
            }
        }
    }

    /**
     * 时段广告时间获取
     */
    public void intervalTimeAnalysis(final Parameter parameter) {
        String url = GET_MEDIA_TIME + showId;
        MyOkHttp myOkHttp = new MyOkHttp();
        myOkHttp.get()
                .url(url)
                .tag(context)
                .enqueue(new RawResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, String response) {
                        e("获取的数据:" + response);
                        intervalTimeList(response, parameter);
                    }

                    @Override
                    public void onFailure(int statusCode, String errorMsg) {
                        e("时段广告时间获取失败原因:" + errorMsg);
                    }
                });
    }

    /**
     * 时段广告时间解析
     */
    private void intervalTimeList(String response, final Parameter parameter) {
        int intervalTimeCount = 0;
        String success = "success", time;
        try {
            JSONObject json = new JSONObject(response);
            if (json.getBoolean(success)) {
                JSONArray deviceTimes = json.getJSONArray("times");
                e("时间数组内容", deviceTimes);
                for (int i = 0; i < deviceTimes.length(); i++) {
                    IntervalTime deviceTime = new IntervalTime();
                    deviceTime.mediaId = deviceTimes.getJSONObject(i).getString("mediaId");
                    deviceTime.startTime = deviceTimes.getJSONObject(i).getString("startTime");
                    deviceTime.endTime = deviceTimes.getJSONObject(i).getString("endTime");
                    e("开始时间", deviceTime.startTime);
                    e("结束时间", deviceTime.endTime);
                    e("节目ID", deviceTime.mediaId);
                    time = deviceTime.startTime + "/" + deviceTime.endTime;
                    intervalTimeArray.add(time);
                }
                e("时段集合", intervalTimeArray);
                if (intervalTimeArray.size() > 0) {
                    setDeviceShow(parameter);
                    intervalTimeCount++;
                    if (intervalTimeCount == 1) {
                        sendCommand[0] = (byte) 0x91;
                        sendNumber[0] = (byte) 0x0B;
                        ThreadManager.getInstance().doExecute(new Runnable() {
                            @Override
                            public void run() {
                                e("节目设置完成回应");
                                ProtocolManager.getInstance().internetShowAnswer(parameter.getDeviceId(), parameter.getrCode(), sendCommand, sendNumber, orderNumber, showId, 2, "");
                            }
                        });
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            e("解析出错原因", e.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 内部匿名时段播放时间类
     */
    static class IntervalTime {
        String mediaId;
        String startTime;
        String endTime;
    }

    /**
     * 直播
     */
    private void liveFor(Parameter parameter) {
        List<PlayList> newsList = showIdQuery("fileName", Integer.toString(showId));
        for (PlayList book1 : newsList) {
            if (fileName.equals(book1.getFileName())) {
                if (playType == 1) {
                    String startTime = null, endTime = null;
                    Date startDate, endDate;
                    Date currentDate = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

                    List<PlayList> qery = getQuery("startTime", "fileName", fileName);
                    for (PlayList start : qery) {
                        startTime = start.getStartTime();
                    }
                    List<PlayList> qeryEnd = getQuery("endTime", "fileName", fileName);
                    for (PlayList end : qeryEnd) {
                        endTime = end.getEndTime();
                    }

                    try {
                        startDate = format.parse(startTime);
                        endDate = format.parse(endTime);
                        e("起始时间", startTime);
                        e("现在时间", format.format(currentDate));
                        e("终止时间", endTime);
                        if (startDate.before(currentDate) && endDate.after(currentDate)) {
                            e("可以播放");
                            showPlay(fileName, "4");
                        }
                    } catch (ParseException e) {
                        e("异常", e.toString());
                        e.printStackTrace();
                    }
                }
                SpUtil.writeInt(Const.SHOW_ID, showId);
                e("直播清单回应");
                ProtocolManager.getInstance().internetShowAnswer(parameter.getDeviceId(), parameter.getrCode(), sendCommand, sendNumber, orderNumber, showId, 2, "");
                break;
            } else {
                ProtocolManager.getInstance().internetShowAnswer(parameter.getDeviceId(), parameter.getrCode(), sendCommand, sendNumber, orderNumber, showId, 0, "");
            }
        }
    }

    /**
     * 文件下载
     */
    private void httpFileDownload(byte[] validData, final byte[] command, final byte[] number, final Parameter parameter, final String commands, final String numbers) {
        try {
            onFailureCount = 0;
            int rcvLengths = getAnIntHex(validData, 2, 2, 16);
            final String url = java.net.URLDecoder.decode(hexToStringGbk(getAnString(validData, 6, rcvLengths - 2)), "UTF-8");
            e("路径和长度", url + "\n" + rcvLengths);
            final String fileName = url.substring(url.lastIndexOf("/") + 1);
//            String url = DOMAIN_NAME + "Upload/" + fileName;
//            Plog.e("IP路径", url);
            final String saveDir = FILE_SAVE_URL + fileName;
//            String fileMd5;
            List<PlayList> querShow = Base.fileNameQuery("showId", fileName);
            for (PlayList querShows : querShow) {
                byte[] commandArrays = new byte[1];
                commandArrays[0] = (byte) 0x80;
                byte[] numberArray = new byte[1];
                numberArray[0] = (byte) 0xB5;
                String showId = Integer.toString(querShows.getShowId());
                ProtocolManager.getInstance().fileDownloadAnswer(parameter.getDeviceId(), parameter.getrCode(), commandArrays[0], numberArray[0], showId, false);
            }
            if (Base.fileIsExists(saveDir)) {
//                List<PlayList> queryMd5 = fileNameQuery("fileMd5", fileName);
//                for (PlayList queryMd5s : queryMd5) {
//                    fileMd5 = queryMd5s.getFileMd5();
//                    if (fileMd5 != null) {
//                        Plog.e("获取到的文件md5值", fileMd5);
//                        File file = new File(saveDir);
//                        if (!fileMd5.equals(FileUtils.getFileMD5(file))) {
//                            SpUtil.writeString(Const.FILE_NAME, fileName);
//                            okHttpDownload(command, number, parameter, commands, url, saveDir, numbers, fileName);
//                        } else {
//                            Plog.e("文件存在无需下载");
//                        }
//                    }
//                }
                if (!checkMd5(fileName)){
                    SpUtil.writeString(Const.FILE_NAME, fileName);
                    removeFile(fileName);
                    okHttpDownload(command, number, parameter, commands, url, saveDir, numbers, fileName);
                }else {
                    e("文件存在无需下载");
                }
            } else {
                SpUtil.writeString(Const.FILE_NAME, fileName);
                okHttpDownload(command, number, parameter, commands, url, saveDir, numbers, fileName);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件下载
     */
    private void okHttpDownload(final byte[] command, final byte[] number, final Parameter parameter, final String commands,
                                final String url, final String saveDir, final String numbers, final String fileName) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5000L, TimeUnit.MILLISECONDS)
                .readTimeout(600000L, TimeUnit.MILLISECONDS)
                .build();
        MyOkHttp mMyOkHttp = new MyOkHttp(okHttpClient);
        mMyOkHttp.download()
                .url(url)
                .filePath(saveDir)
                .tag(this)
                .enqueue(new DownloadResponseHandler() {
                    @Override
                    public void onStart(long totalBytes) {
                        startTimeDownload = System.currentTimeMillis();
                        e("doDownload onStart");
                    }

                    @Override
                    public void onFinish(File downloadFile) {
                        downloadFinish(downloadFile, commands, parameter, numbers, command, number);
                    }

                    @Override
                    public void onProgress(long currentBytes, long totalBytes) {
                    }

                    @Override
                    public void onFailure(String error) {
                        e("doDownload onFailure:" + error);
                        e("保存路径", saveDir);
                        Base.removeFile(fileName);
                        onFailureCount++;
                        if (onFailureCount <= CONSTANT_TEN) {
                            okHttpDownload(command, number, parameter, commands, url, saveDir, numbers, fileName);
                        }
                    }
                });
    }

    /**
     * 文件下载完成处理
     */
    private void downloadFinish(File downloadFile, String commands, final Parameter parameter, String numbers, final byte[] command, final byte[] number) {
        String fileName = downloadFile.toString().substring(downloadFile.toString().lastIndexOf("/") + 1);
        e("doDownload onFinish:", (System.currentTimeMillis() - startTimeDownload) / 1000, fileName);
        e("下载类型", commands);
        if (checkMd5(fileName)){
            switch (commands) {
                case FILE_DOWNLOAD:
                    updateDownloadState(fileName);

                    List<PlayList> showList = getQuery("showId", "fileName", fileName);
                    for (PlayList showLists : showList) {
                        int showId = showLists.getShowId();
                        String url = DOWNLOAD_COMPLETE + parameter.getDeviceId() + "&mediaId=" + showId;
                        webInterface(url);

                        int red = SpUtil.readInt(Const.REDPACKET);
                        e("红包值", red);
                        if (showLists.getRedPacket() == 2 || red == 2) {
                            String pictureFileName = "redpacket_" + parameter.getDeviceId() + "_" + showId + ".jpg";
                            SpUtil.writeString(PICTURE_FILE_NAME, pictureFileName);
                            String picture = DOMAIN_NAME + "RedPacket/" + pictureFileName;
                            final String saveDir = FILE_SAVE_URL + pictureFileName;
                            e("红包图片名字", pictureFileName);
                            pictureDownload(picture, saveDir);
                        }
                    }
                    int screen = SpUtil.readInt(Const.SPLIT_SCREEN);
                    int interval = SpUtil.readInt(IS_INTERVAL_TIME);
                    if (interval == 1) {
                        intentActivity("20");
                    }
                    if (screen == CONSTANT_TWO || screen == CONSTANT_THREE) {
                        showPlay(fileName, "16");
                    } else {
                        int isBaiDu = SpUtil.readInt(Const.IS_BAIDU);
                        //只有不是百度广告才发送展示通知，CONSTANT_ONE==1边上百度广告
                        if (isBaiDu != CONSTANT_ONE) {
                            String startUpNumber = "B6";
                            if (startUpNumber.equals(numbers)) {
                                setIntentData("startPlayUrl", fileName);
                                startAPP();
                            } else {
                                String strSuffix = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
                                e("下载文件的后缀", strSuffix);
                                if (VIDEO_TYPE.equals(strSuffix)) {
                                    SpUtil.writeBoolean(Const.SHOW_PLAY, true);
                                } else {
                                    SpUtil.writeBoolean(Const.SHOW_PLAY, false);
                                }
                                showPlay(fileName, "4");
                            }
                        }
                    }
                    break;
                case START_FILE_DOWNLOAD:
                    setIntentData("startPlayUrl", fileName);
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().fileDownloadAnswer(parameter.getDeviceId(), parameter.getrCode(), command[0], number[0], "0000", true);
                        }
                    });
                    break;
                default:
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            ProtocolManager.getInstance().fileDownloadAnswer(parameter.getDeviceId(), parameter.getrCode(), command[0], number[0], "0000", true);
                        }
                    });
                    appUpdate(fileName);
                    break;
            }
        }else {
            e("文件不完整移除文件");
            removeFile(fileName);
        }
    }

    /**
     * 图片下载
     */
    public void pictureDownload(final String url, final String saveDir) {
        MyOkHttp mMyOkHttp = new MyOkHttp();
        mMyOkHttp.download()
                .url(url)
                .filePath(saveDir)
                .tag(this)
                .enqueue(new DownloadResponseHandler() {
                    @Override
                    public void onStart(long totalBytes) {
                        startTimeDownload = System.currentTimeMillis();
                        e("doDownload onStart");
                    }

                    @Override
                    public void onFinish(File downloadFile) {
                        String fileName = downloadFile.toString().substring(downloadFile.toString().lastIndexOf("/") + 1);
                        e("doDownload onFinish:", (System.currentTimeMillis() - startTimeDownload) / 1000, fileName);
                    }

                    @Override
                    public void onProgress(long currentBytes, long totalBytes) {
                    }

                    @Override
                    public void onFailure(String error) {
                        e(TAG, "doDownload onFailure:" + error);
                    }
                });
    }

    /**
     * 远程重启
     */
    private void remoteRestart(Parameter parameters) {
        String command = bytes2HexString(ProtocolManager.getInstance().parseParameter(receive, 17, 1));
        if (APP_FRONT.equals(command)) {
            e("远程启动APP");
            SpUtil.writeString(Const.NET_TYPE, "tcp");
            ProtocolManager.getInstance().restartTerminalAnswer(parameters.getDeviceId(), parameters.getrCode());
            startAPP();
        }
    }

    /**
     * 软件重启
     */
    private void startAPP() {
        Intent mStartActivity = new Intent();
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        }
        System.exit(0);
    }

    /**
     * 字幕处理
     */
    private void subtitlePaly(byte[] data, final String deviceId, final String rCode) {
        int rcvLength = 8, contenttype;
        String mianContent;
        String enable = getAnString(data, 6, 1);
        SpUtil.writeString(Const.ENABLE, enable);
        e("是否开启滚动字幕", enable);
        if (data.length > rcvLength) {
            int recLength = getAnIntHex(data, 8, 2, 16);
            contenttype = getAnIntHex(data, data.length - 2, 1, 16);
            e("字幕类型", contenttype);
            if (contenttype == 1) {
                if (data.length > CONSTANT_ELEVEN) {
                    mianContent = hexToStringGbk(getAnString(data, 10, recLength));
                    if (!TextUtils.isEmpty(mianContent)) {
                        SpUtil.writeString(Const.CONTENT, mianContent);
                    }
                }
                intentActivity("5");
            } else if (contenttype == CONSTANT_TWO) {
                intentActivity("11");
                byte[] command = new byte[1];
                command[0] = (byte) 0x86;
                byte[] number = new byte[1];
                number[0] = (byte) 0xAB;
                controlAnswer(command, number, deviceId, rCode);
            }
        }
    }

    /**
     * 控制结果回应
     */
    private void controlAnswer(final byte[] command, final byte[] number, final String deviceId, final String rCode) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                ProtocolManager.getInstance().writeAnswer(deviceId, rCode, command, number, true, "");
            }
        });
    }

    /**
     * 语音控制
     */
    private void voiceAnalysis(byte[] data) {
        int rcvLength = 8;
        String mianContent, noNumber = "00";
        String enable = getAnString(data, 6, 1);
        int recL = getAnIntHex(data, 8, 1, 16);
        SpUtil.writeString(Const.VOICE_ENABLE, enable);
        if (data.length >= rcvLength) {
            if (noNumber.equals(enable)) {
                intentActivity("22");
            } else {
                if (data.length > CONSTANT_ELEVEN) {
                    e("内容长度", recL);
                    mianContent = hexToStringGbk(getAnString(data, 9, recL));
                    e("语音内容", mianContent);
                    if (!TextUtils.isEmpty(mianContent)) {
                        SpUtil.writeString(Const.VOICE_CONTENT, mianContent);
                        intentActivity("22");
                    }
                }
            }
        }
    }

    /**
     * 充电开关控制
     */
    private void controlPort(int index, int chargeNumber) {
        byte[] data;
        if (index == 1) {
            data = ProtocolDao.openChargeData(chargeNumber);
            e("开始充电", chargeNumber);
        } else {
            e("结束充电", chargeNumber);
            data = ProtocolDao.closeChargeData(chargeNumber);
        }

        SerialPortUtil.sendPort(data);
    }

    /**
     * 充电开关控制
     */
    private void controlPort3128(int index, int chargeNumber) {
        if (index == 1) {
            e("开始充电", chargeNumber);
            if (chargeNumber == 1) {
                e("执行1号线通电结果", SystemBinUtils.run("echo 1 > /sys/devices/usb_control.23/output_charger01"));
            } else {
                e("执行2号线通电结果", SystemBinUtils.run("echo 1 > /sys/devices/usb_control.23/output_charger02"));
            }
        } else {
            e("开始断电", chargeNumber);
            if (chargeNumber == 1) {
                e("执行1号线断电结果", SystemBinUtils.run("echo 0 > /sys/devices/usb_control.23/output_charger01"));
            } else {
                e("执行2号线断电结果", SystemBinUtils.run("echo 0 > /sys/devices/usb_control.23/output_charger02"));
            }
        }

    }

    /**
     * 充电监听
     */
    private void openChargeListen() {
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, timeFilter);
        e("开启充电监听");
    }

    /**
     * 内部分钟监听广播
     */
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                e("我每分钟检查一次");
                int ocunt = 0;
                List<Charge> query = LitePal.findAll(Charge.class);
                for (Charge querys : query) {
                    int chargeNumber = querys.getChargeNumber();
                    int time = getTime(chargeNumber);
                    if (time == 0) {
                        ocunt++;
                    }
                    if (ocunt == query.size()) {
                        e("5个口没有充电了");
                        unregisterReceiver(mTimeReceiver);
                    }
                    e("充电时间", time);
                    List<Charge> queryStartTime = chargeQuery("startTime", chargeNumber);
                    for (Charge queryStartTimes : queryStartTime) {
                        List<Charge> queryNewAddTime = chargeQuery("newAddTime", chargeNumber);
                        for (Charge queryNewAddTimes : queryNewAddTime) {
                            int newTime = queryNewAddTimes.getNewAddTime() * 60;
                            e("续充时间", newTime);
                            int startTime = queryStartTimes.getStartTime();
                            int totalTime = time + newTime;
                            int endTime = getSystemTime();
                            int value = endTime - startTime;
                            e("当前系统时间", endTime);
                            e("开始充电时间", startTime);
                            e("总的充电时间", totalTime);
                            e("时间差", value);
                            if (value > totalTime) {
                                List<Charge> queryState = chargeQuery("state", chargeNumber);
                                for (Charge queryStates : queryState) {
                                    if (queryStates.getState() == 0) {
                                        String mode = SpUtil.readString(DEVICE_MODEL);
                                        if ("rk312x".equals(mode)) {
                                            controlPort3128(2, chargeNumber);
                                        } else {
                                            controlPort(2, chargeNumber);
                                        }
                                        setChargeParameter("newAddTime", 0, chargeNumber);
                                        setChargeParameter("chargeTime", 0, chargeNumber);
                                        setChargeParameter("state", 1, chargeNumber);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * 充电数据保存
     */
    private void chargeSave(int chargeNumber, int chargeTime) {
        Charge charge = new Charge();
        charge.setChargeNumber(chargeNumber);
        charge.setTime(chargeTime);
        charge.setStartTime(getSystemTime());
        charge.setState(0);
        charge.setNewAddTime(0);
        charge.save();
    }

    /**
     * 获取当前系统时间
     */
    private int getSystemTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 3600;
        int minute = calendar.get(Calendar.MINUTE) * 60;
        int second = calendar.get(Calendar.SECOND);
        return hour + minute + second;
    }

    /**
     * 获取充电时间
     */
    private int getTime(int chargeNumber) {
        int date = 0;
        List<Charge> query = LitePal.select("chargeTime").where("chargeNumber" + " = ?", Integer.toString(chargeNumber)).find(Charge.class);
        for (Charge querys : query) {
            date = querys.getTime() * 60;
        }
        return date;
    }

    /**
     * 修改充电数据
     */
    private void setChargeParameter(String string, int newData, int chargeNumber) {
        ContentValues values = new ContentValues();
        values.put(string, newData);
        LitePal.updateAll(Charge.class, values, "chargeNumber = ?", Integer.toString(chargeNumber));
    }

    /**
     *  ftp 文件下载
     */
    private void ftpDownload(byte[] receive, Parameter parameter) {
        ftpParse(receive);

        download(parameter);

        if (whether == 1) {
            showPlay(ftpFileName, "4");
        }
    }

    /**
     *  ftp app下载
     */

    public void appDownload(byte[] receive, Parameter parameter) {
        new RevAnalysis(receive).invoke();

        download(parameter);

        appUpdate(ftpFileName);
    }

    /**
     * app更新
     */
    private void appUpdate(String fileName) {
        SpUtil.writeString(Const.UPDATE_APP_NAME, fileName);
        //2018-12-14 修改R.string.new_package
        String filePath = FILE_SAVE_URL + fileName;
        e("安装包路径", filePath);
        if (PACKAGE_NAME2.equals(NetUtil.getApkInfo(MonitorService.this, filePath))) {
//                && APP_VERSION.equals(NetUtil.getApkVersion(MonitorService.this, filePath))) {
            //360智能安装
            onSmartInstall(filePath);
        } else {
            Base.removeFile(fileName);
        }
    }

    /**
     * 智能安装
     *
     * @param apkPath 安装包路径
     */
    public void onSmartInstall(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            Toast.makeText(this, "请选择安装包！", Toast.LENGTH_SHORT).show();
            return;
        }
        String model = SpUtil.readString(DEVICE_MODEL);
        if (YIN_NUO_HENG_MODEL.equals(model) || RK_3288.equals(model)) {
            installApp(apkPath, this);
        } else if (!isAccessibilitySettingsOn(this)) {
            e("辅助功能没有打开");
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else {
            e("主板root情况", isRoot());
//            if (CHARGE_MACHINE_MODEL.equals(model) && Build.VERSION.SDK_INT == M) {
//                Plog.e("静默安装", installSilent(apkPath));
////                installSilent(apkPath);
//            } else {
                installApp(apkPath, MonitorService.this);
//            }
        }
    }

    /**
     * 程序安装
     */
    public void installApp(String apkPath, Context context) {
        SpUtil.writeBoolean(Const.APP_NAME, true);
        File localFile = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= N) {
            e("高版本");
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            Uri filePath = FileProvider.getUriForFile(context, "com.kingbird.advertisting.fileprovider", localFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(filePath, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(localFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * install slient
     */
    public static int installSilent2(String filePath) {
        File file = new File(filePath);
        if (filePath.length() == 0 || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }

//        String[] args = {"pm", "install", "-r", filePath};
        String[] args = {"pm", "install", "-t", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        java.lang.Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        String success = "Success", success2 = "success";
        // TODO should add memory is not enough here
        if (successMsg.toString().contains(success) || successMsg.toString().contains(success2)) {
            result = 0;
        } else {
            result = 2;
        }
        e("successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }

    public static boolean installSilent(String apkPath) {
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = new ProcessBuilder("pm", "install", "-i", PACKAGE_NAME, "-r", apkPath).start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        e("result", "" + errorMsg.toString());
        e(errorMsg.toString() + "  " + successMsg);
        //如果含有“success”单词则认为安装成功
        String success = "success";
        return successMsg.toString().equalsIgnoreCase(success);
    }

    /**
     * 判断手机是否拥有Root权限。
     *
     * @return 有root权限返回true，否则返回false。
     */
    public boolean isRoot() {
        boolean bool = false;
        try {
            bool = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * ftp文件下载
     */
    private void download(Parameter parameter) {
        String ftpHost = ftpIp.substring(6);
        String ftpUserName = userName;
        String ftpPassword = passWord;
        int ftpPort = port;
        String ftpPath = ftpIp + "/" + ftpFileName;
        final String localPath = parameter.getFileUrl();

        downloadFtpFile(ftpHost, ftpUserName, ftpPassword, ftpPort, ftpPath, localPath, ftpFileName, "MonitorService");
    }

    /**
     * ftp参数解析
     *
     * @param receive 接收数组
     */
    private void ftpParse(byte[] receive) {
        RevAnalysis revAnalysis = new RevAnalysis(receive).invoke();
        int ipLength = revAnalysis.getIpLength();
        int portLength = revAnalysis.getPortLength();
        int userNameLength = revAnalysis.getUserNameLength();
        int passWordLength = revAnalysis.getPassWordLength();
        int fileNameLength = revAnalysis.getFileNameLength();
        int whetherLength = getAnIntHex(receive, 15 + ipLength + portLength + userNameLength + passWordLength + fileNameLength, 1, 16);
        byte[] data9 = new byte[whetherLength];
        System.arraycopy(receive, 16 + ipLength + portLength + userNameLength + passWordLength + fileNameLength, data9, 0, 1);
        whether = Integer.parseInt(bytes2HexString(data9));
        int showId = getAnIntHex(receive, 19 + ipLength + portLength + userNameLength + passWordLength + fileNameLength, 4, 16);
        SpUtil.writeInt(Const.SHOW_ID, showId);
    }

    /**
     * 百度定位初始化
     */
    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(2 * 60 * 1000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /**
     * 心跳
     */
    private void initHeartBeat() {
        if (isLoop) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            List<Parameter> parameters = LitePal.findAll(Parameter.class);
                            for (Parameter parameter : parameters) {
                                heartBeat = parameter.getHeartBeat();
                                e("最新通讯心跳周期", heartBeat);
                                if (SocketManager.getInstance().getSocket() != null) {
                                    SpUtil.writeString(Const.NET_TYPE, "tcp");
                                    ProtocolManager.getInstance().sendHeartBeat();
                                    heartBeatCount = 0;
                                    failCount++;
                                    if (failCount > CONSTANT_ONE) {
                                        e("心跳超过1次没有收到回复，开始进行重连");
                                        reconnect();
                                    }
                                } else {
                                    e("send heartBeat failed cause by bad net work");
                                    heartBeatCount++;
                                    e("心跳连接失败次数", heartBeatCount);
                                    if (heartBeatCount > CONSTANT_TWO) {
                                        setIntentData("ip", INITIAL_IP);
                                        setIntentData("port", INITIAL_PORT);
                                    }
                                    reconnect();
                                }
                            }
                            String model = SpUtil.readString(DEVICE_MODEL);
                            if (LOTTERY_MACHINE_MODEL.equals(model)) {
                                try {
                                    if (!getProcess()) {
                                        e("彩票软件没有运行");
                                        PackageManager packageManager = getPackageManager();
                                        Intent intent = packageManager.getLaunchIntentForPackage(LOTTERY_MACHINE);
                                        if (intent == null) {
                                            Toast.makeText(MonitorService.this, "当前软件还没有安装，请先安装此软件！", Toast.LENGTH_LONG).show();
                                        } else {
                                            startActivity(intent);
                                        }
                                    } else {
                                        e("彩票软件在运行");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            boolean result = SpUtil.readBoolean(Const.BIND_DEALER_RESULT);
                            if (!result) {
                                String dealerCode = SpUtil.readString(Const.DEALER_CODE);
                                if (!"".equals(dealerCode)) {
                                    bindDealer(MonitorService.this, dealerCode);
                                }
                            }

                        }
                    }, 1, heartBeat, TimeUnit.SECONDS);
                }
            });
        }
    }

    /**
     * 屏幕心跳-demo
     */
    private static void screenHeartBeatDemo(final String deviceId) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        KuaiFaClient client = new KuaiFaClient(JINGDONG_APP_ID, JINGDONG_APP_KEY, JINGDONG_HOST,
                                true);
                        client.screenHeartBeat(deviceId, 0);
                    }
                }, 1, 10, TimeUnit.MINUTES);
            }
        });
    }

    /**
     * 获取进程
     */
    private boolean getProcess() {
        boolean isClsRunning = false;

        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess process : processes) {
            String processName = process.name;
            if (processName.equals(LOTTERY_MACHINE)) {
                isClsRunning = true;
                break;
            }
        }
        return isClsRunning;
    }

    /**
     * UDP心跳
     */
    private void initHeartBeatUdp() {
        ExecutorServiceManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                e("UDP心跳准备");
                SpUtil.writeString(Const.NET_TYPE, "udp");
                ProtocolManager.getInstance().sendHeartBeat();
            }
        }, 1, heartBeat, TimeUnit.SECONDS);
    }

    /**
     * 心跳校验
     *
     * @param receive 接收数据
     */
    private void dealWithHeartBeat(byte[] receive) {
        int count = 3;
        if (receive != null && receive.length == ProtocolDao.HEARTBEAT_DATA_LENGTH) {
            boolean b = ProtocolManager.getInstance().checkHeartBeat(receive);
            if (!b) {
                //判断失败次数，如果为3重新初始化socket
                failedCount++;
                e(TAG, "失败次数" + failedCount);
                if (failedCount == count) {
                    failedCount = 0;
                    //初始化socket
                    e(TAG, "准备初始化socket");
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            SocketManager.getInstance().close();
                            SocketManager.getInstance().connect();
                        }
                    });
                } else {
                    e(TAG, "再次发送心跳");
                    ThreadManager.getInstance().doExecute(new Runnable() {
                        @Override
                        public void run() {
                            SpUtil.writeString(Const.NET_TYPE, "tcp");
                            ProtocolManager.getInstance().sendHeartBeat();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) {
        e("exceptionCaught", throwable.getLocalizedMessage());
    }

    @Override
    public void messageReceived(IoSession ioSession, byte[] data) {
        e("messageReceived", bytes2HexString(data));
        SpUtil.writeString(Const.NET_TYPE, "udp");
        rcvDataProcessing(data);
    }

    @Override
    public void sessionClosed(IoSession ioSession) {
        e("sessionClosed", "sessionClosed");
    }

    @Override
    public void sessionCreated(IoSession ioSession) {
        e("sessionCreated", "sessionCreated");
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) {
        e("sessionIdle", "sessionIdle");
    }

    @Override
    public void sessionOpened(IoSession ioSession) {
        e("sessionOpened", "sessionOpened");
        initHeartBeatUdp();
    }

    @Override
    public void messageSent(IoSession ioSession, Object object) {
        e("messageSent", "messageSent");
    }

    private class RevAnalysis {
        private byte[] receive;
        private int ipLength;
        private int portLength;
        private int userNameLength;
        private int passWordLength;
        private int fileNameLength;

        private RevAnalysis(byte... receive) {
            this.receive = receive;
        }

        private int getIpLength() {
            return ipLength;
        }

        private int getPortLength() {
            return portLength;
        }

        private int getUserNameLength() {
            return userNameLength;
        }

        private int getPassWordLength() {
            return passWordLength;
        }

        private int getFileNameLength() {
            return fileNameLength;
        }

        private RevAnalysis invoke() {
            ipLength = getAnIntHex(receive, 5, 1, 16);
            byte[] ftp = new byte[ipLength];
            System.arraycopy(receive, 6, ftp, 0, ipLength);
            ftpIp = convertHexToString(bytes2HexString(ftp));
//        e("ftp地址(IP):", ftpIP);

            portLength = getAnIntHex(receive, 7 + ipLength, 1, 16);
            byte[] data1 = new byte[portLength];
            System.arraycopy(receive, 8 + ipLength, data1, 0, portLength);
            port = Integer.parseInt(bytes2HexString(data1), 16);
//        e("端口:", port);

            userNameLength = getAnIntHex(receive, 9 + ipLength + portLength, 1, 16);
            byte[] data3 = new byte[userNameLength];
            System.arraycopy(receive, 10 + ipLength + portLength, data3, 0, userNameLength);
            userName = convertHexToString(bytes2HexString(data3));
//        e("用户名:", userName);

            passWordLength = getAnIntHex(receive, 11 + ipLength + portLength + userNameLength, 1, 16);
            byte[] data5 = new byte[passWordLength];
            System.arraycopy(receive, 12 + ipLength + portLength + userNameLength, data5, 0, passWordLength);
            passWord = convertHexToString(bytes2HexString(data5));
//        e("密码:", passWord);

            fileNameLength = getAnIntHex(receive, 13 + ipLength + portLength + userNameLength + passWordLength, 1, 16);
            byte[] data7 = new byte[fileNameLength];
            System.arraycopy(receive, 14 + ipLength + portLength + userNameLength + passWordLength, data7, 0, fileNameLength);
            ftpFileName = hexToStringGbk(bytes2HexString(data7));
            e("文件名:", ftpFileName);
            return this;
        }
    }

    public class ServiceBinder extends Binder {
        public MonitorService getService() {
            return MonitorService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(closeRec);
        closeRec = null;
        mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        ThreadManager.getInstance().shutdown();
        ExecutorServiceManager.getInstance().shutdown();
        UdpManager.getInstance().closeUdp();
        SerialPortUtil.close();
        e("销毁广播");
    }
}
