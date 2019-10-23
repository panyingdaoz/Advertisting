package com.kingbird.advertisting.utils;

import com.kingbird.advertisting.base.Base;
import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.manager.ProtocolManager;
import com.kingbird.advertisting.manager.ThreadManager;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android_serialport_api.SerialPort;

import static com.kingbird.advertisting.base.Base.intentActivity;
import static com.kingbird.advertisting.base.Base.tissueCountRequest;
import static com.kingbird.advertisting.base.Base.webControlAnswer;
import static com.kingbird.advertisting.utils.Config.LACK_TISSUE;
import static com.kingbird.advertisting.utils.Config.OUT_TISSUE_FAILURE;
import static com.kingbird.advertisting.utils.Config.OUT_TISSUE_SUCCESS;
import static com.kingbird.advertisting.utils.Config.SET_CONTROL_RESULT;

/**
 * 类具体作用
 *
 * @author Pan yingdao
 * @date 2019/5/27/027.
 */
public class SerialPortUtil {
    /**
     * 标记当前串口状态(true:打开,false:关闭)
     **/
    private static boolean isFlagSerial = false;
    private static ArrayList<String> sendList = new ArrayList<>();
    private static InputStream inputStream = null;
    private static OutputStream outputStream = null;
    private static Thread receiveThread = null;

    /**
     * 打开串口
     */
    public static boolean open() {
        boolean isopen;
        if (isFlagSerial) {
            Plog.e("串口已经打开,打开失败");
            return false;
        }
        try {
            SerialPort serialPort = new SerialPort(new File("/dev/ttyS6"), 9600);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            receive();
            isopen = true;
            isFlagSerial = true;
        } catch (IOException e) {
            e.printStackTrace();
            isopen = false;
        }catch (UnsatisfiedLinkError e){
            e.printStackTrace();
            isopen = false;
        }
        return isopen;
    }

    /**
     * 关闭串口
     */
    public static boolean close() {
        if (isFlagSerial) {
            Plog.e("串口关闭失败");
            return false;
        }
        boolean isClose;
        Plog.e("关闭串口");
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            isClose = true;
            //关闭串口时，连接状态标记为false
            isFlagSerial = false;
            if (sendList.size() > 0) {
                sendList.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
            isClose = false;
        }
        return isClose;
    }

    /**
     * 发送串口指令
     */
    public static void sendPort(byte[] data) {
        if (!isFlagSerial) {
            Plog.e("串口未打开,发送失败", Base.bytes2HexString(data));
            return;
        }
        try {
            outputStream.write(data, 0, data.length);
            sendList.add(Base.bytes2HexString(data));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Plog.e("发送指令出现异常");
        }
    }

    /**
     * 接收串口数据的方法
     */
    private static void receive() {
        if (receiveThread != null && !isFlagSerial) {
            return;
        }
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                while (isFlagSerial) {
                    try {
                        byte[] readData = new byte[8];
                        if (inputStream == null) {
                            return;
                        }
                        int size = inputStream.read(readData);
                        if (size > 0 && isFlagSerial) {
                            String url;
                            String recData = Base.bytes2HexString(readData);
                            Plog.e("返回数据", recData);
                            List<Parameter> parameter = LitePal.findAll(Parameter.class);
                            for (Parameter parameters : parameter) {
                                String id = parameters.getDeviceId();
                                String rCode = parameters.getrCode();
                                byte[] sendCommand = new byte[1];
                                byte[] sendNumber = new byte[1];
                                for (int i = 0; i < sendList.size(); i++) {
                                    if (recData.equals(sendList.get(i))) {
                                        int number = (int) readData[3];
                                        if (number == 0 || number == 1 || number == 2 || number == 3 || number == 4) {
                                            Plog.e(number + 1 + "号线充电成功");
                                            sendCommand[0] = (byte) 0x91;
                                            sendNumber[0] = (byte) 0x0B;
                                            ProtocolManager.getInstance().writeAnswer(id, rCode, sendCommand, sendNumber, false, "");
                                        }
                                        sendList.remove(i);
                                    }
                                }
                                switch (recData) {
                                    case OUT_TISSUE_SUCCESS:
                                        //出纸成功
                                        if (!SpUtil.readBoolean(Const.QR_TISSUE_SHOW)) {
                                            SpUtil.writeBoolean(Const.QR_TISSUE_SHOW, true);
                                            intentActivity("101");
                                        }
                                        url = SET_CONTROL_RESULT + id + "&status=" + 2;
                                        Plog.e("成功路径", url);
                                        webControlAnswer(url);
                                        tissueCountRequest();
                                        Plog.e("出纸成功");
                                        break;
                                    case LACK_TISSUE:
                                        //缺纸
                                        url = SET_CONTROL_RESULT + id + "&status=" + -1;
                                        Plog.e("缺纸路径", url);
                                        webControlAnswer(url);
                                        intentActivity("102");
                                        break;
                                    case OUT_TISSUE_FAILURE:
                                        //设备故障
                                        url = SET_CONTROL_RESULT + id + "&status=" + -2;
                                        Plog.e("失败路径", url);
                                        webControlAnswer(url);
                                        SpUtil.writeBoolean(Const.QR_TISSUE_SHOW, false);
                                        intentActivity("104");
                                        break;
                                    default:
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
