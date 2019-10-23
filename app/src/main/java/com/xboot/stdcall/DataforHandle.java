package com.xboot.stdcall;

import android.content.Context;
import android.util.Log;

import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.manager.ProtocolManager;
import com.kingbird.advertisting.manager.ThreadManager;
import com.socks.library.KLog;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 开关机设置类
 *
 * @author panyingdao
 * @date 2018/3/09.
 */
public class DataforHandle {

    private static final String TAG = DataforHandle.class.getName();
    private Context mcont;


    //定义标准数组"1","12:00","13:00"
    public void setonoff(Context cont, String[] data) {

        //开机时间，关机时间，设置状态
        String ontime, offtime, state;

        mcont = cont;
        if (data == null) {
            //传值为null
            System.out.println("kong");
        } else {
            if (data.length == 3) {
                ontime = data[1];
                offtime = data[2];
                state = data[0];
                //缺一个判断状态
                try {
                    if (Integer.parseInt(state) == 0) {
                        //关闭定时开关机
                        KLog.e(TAG, "DataforHandle --- stop");
                        setPowerOnOff((byte) 0, (byte) 4, (byte) 0, (byte) 4, (byte) 0);
                    } else {
                        //
                        KLog.e(TAG, "DataforHandle --- start");
                        judge(ontime, offtime, state);
                    }
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    //状态值不为数字
                    Log.i(TAG, "DataforHandle --- State values are not for the digital");
                }
            } else {
                //数据量不对
                Log.i(TAG, "DataforHandle --- Amount of data is wrong");
            }
        }
    }

    private void judge(String onTime, String offTime, String state) {
        if (num(onTime) && num(offTime) && num(state)) {
            settings(
                    Integer.parseInt(nowtime()[0]),
                    Integer.parseInt(nowtime()[1]),
                    Integer.parseInt(onTime.split(":")[0]),
                    Integer.parseInt(onTime.split(":")[1]),
                    Integer.parseInt(offTime.split(":")[0]),
                    Integer.parseInt(offTime.split(":")[1])
            );
        } else {
            //格式有误
            KLog.e(TAG, "DataforHandle --- Presentation Error ");
        }
    }

    private String[] nowtime() {
        String now = (new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.CHINA)).format(Calendar.getInstance().getTime());
        return now.split(" ")[1].split(":");
    }

    private boolean num(String thisnum) {
        String[] num = thisnum.split(":");
        try {
            Log.i(TAG, "" + Integer.parseInt(num[0]));
            if (num.length > 1) {
                Log.i(TAG, "" + Integer.parseInt(num[1]));
            }
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //现在时间 开机时间 关机时间
    private void settings(int nowHour, int nowMin, int onHour, int onMin, int offHour, int offMin) {
        byte[] command = new byte[1];
        command[0] = (byte) 0x81;
        byte[] number = new byte[1];
        number[0] = (byte) 0x0E;
        nowHour = nowHour == 24 ? 0 : nowHour;
        //时间相等的情况
        if (offHour == onHour && offMin == onMin) {
            //时间相等设置不成功
            KLog.e(TAG, "DataforHandle --- failed to set datafor ");
        } else {
            boolean byteOff = offMin - nowMin < 0;
            boolean byteOn = onMin - offMin < 0;
            //关机参数
            int offTimeHour =
                    (offHour - nowHour < 0 ?
                            (byteOff ? (offHour - nowHour + 24 - 1) : (offHour - nowHour + 24))
                            :
                            (byteOff ? (offHour - nowHour - 1) : (offHour - nowHour))
                    );
            int offTimeMin =
                    (byteOff ? (offMin - nowMin + 60) : (offMin - nowMin));
            //开机参数
            int onTimeHour = (byteOn ? (onHour - offHour - 1) : (onHour - offHour));
            int onTimeMin = (byteOn ? (onMin - offMin + 60) : (onMin - offMin));
            offTimeHour = offTimeHour < 0 ? (offTimeHour + 24) : offTimeHour;
            onTimeHour = onTimeHour < 0 ? (onTimeHour + 24) : onTimeHour;
            String now = (new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.CHINA)).format(Calendar.getInstance().getTime());
            KLog.e(TAG, "---------------------------" + now);
            KLog.e(TAG, "For the set of parameters==" + onTimeHour + "===" + onTimeMin + "===" + offTimeHour + "===" + offTimeMin);
            KLog.e(TAG, "--------------------------- ");

            //小于3分钟设置不成功
            if (onTimeHour == 0 && onTimeMin < 3 || offTimeHour == 0 && offTimeMin < 3) {
                Log.i(TAG, "DataforHandle --- stop Time is too short to 3 minutes");
                if (mcont != null) {
                    KLog.e("设置失败,小于3分钟设置不成功");
                    onOffFailureAnswer(command, number);
                }
            } else {
                if (mcont != null) {
                    if (setPowerOnOff((byte) onTimeHour, (byte) onTimeMin, (byte) offTimeHour, (byte) offTimeMin, (byte) 3) != 0) {
                        KLog.e("设置电源开关失败");
                        onOffFailureAnswer(command, number);
                    }
                }
            }
        }
    }

    private void onOffFailureAnswer(final byte[] command, final byte[] number) {
        List<Parameter> parameter = LitePal.findAll(Parameter.class);
        for (Parameter parameters : parameter) {
            final String id = parameters.getDeviceId();
            final String rcode = parameters.getrCode();
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    if (id!=null){
                        ProtocolManager.getInstance().writeAnswer(id, rcode, command, number, false, "");
                    }
                }
            });
        }
    }

    private int setPowerOnOff(byte offTimeHour, byte offTimeMin, byte onTimeHour, byte onTimeMin, byte enable) {
        int fd, ret;
        // byte buf[] = { 0, 3, 0, 3 };
        fd = posix.open("/dev/McuCom", posix.O_RDWR, 0666);
        if (fd < 0) {
            Log.i(TAG, "DataforHandle --- stop  fd<0 ===");
            return -1;
        }
        ret = posix.poweronoff(offTimeHour, offTimeMin, onTimeHour, onTimeMin, enable, fd);
        if (ret != 0) {
            Log.i(TAG, "DataforHandle --- stop  ret!=0 ===");
            return -1;
        }
        posix.close(fd);
        return 0;
    }
}
