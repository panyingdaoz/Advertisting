package com.kingbird.advertisting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.kingbird.advertisting.activity.HomePageActivity;
import com.kingbird.advertisting.base.Base;
import com.kingbird.advertisting.manager.CustomActivityManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;

import static com.kingbird.advertisting.utils.Config.OPEN_YUNZHONG_ACTION;

/**
 * 开机广播接收器
 *
 * @author panyingdao
 * @date 2017/8/30.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static final String OPEN_ACTION = "android.kingbird.action.OPEN_ADVERTISTING";
    private static final String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    private static final String PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
//    private static final String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String appName;
        Plog.e("接收到的广播", intent.getAction());
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION:
                    Plog.e("VideoTextureActivity的状态", CustomActivityManager.getInstance().getTopActivity());
                    if (CustomActivityManager.getInstance().getTopActivity() == null) {
                        //后边的XXX.class就是要启动的服务
                        Intent intent1 = new Intent(context, HomePageActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
                    }
                    break;
                case OPEN_ACTION:
                    Plog.e("收到其他APP发来启动即投视播广播", intent.getAction());
                    Intent noteList = new Intent(context, HomePageActivity.class);
                    noteList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(noteList);
                    break;
                case PACKAGE_ADDED:
                    appName = SpUtil.readString(Const.UPDATE_APP_NAME);
                    Base.removeFile(appName);
                    break;
                case PACKAGE_REPLACED:
                    appName = SpUtil.readString(Const.UPDATE_APP_NAME);
                    Base.removeFile(appName);
                    break;
                case OPEN_YUNZHONG_ACTION:
                    Plog.e("收到云中杜鹃广播");
                    break;
                default:
                    break;
            }
        }
    }
}
