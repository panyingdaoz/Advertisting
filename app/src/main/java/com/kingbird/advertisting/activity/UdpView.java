package com.kingbird.advertisting.activity;

import com.kingbird.advertisting.base.UdpIoHandlerAdapter;
import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.manager.UdpManager;
import com.kingbird.advertisting.utils.Plog;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import static com.kingbird.advertisting.base.Base.bytes2HexString;

//import com.socks.library.KLog;

/**
 * 类具体作用
 *
 * @author Administrator
 * @date 2019/2/20/020.
 */
public class UdpView implements UdpIoHandlerAdapter.UdpIoHandlerListener {

    private UdpView() {
    }

    private static class HolderClass {
        private final static UdpView INSTANCE = new UdpView();
    }

    public static UdpView getInstance() {
        return UdpView.HolderClass.INSTANCE;
    }

    /**
     * 连接
     */
    public void connectUdp(String ip) {
        UdpManager.setIp(ip);
        UdpManager.setPort(9031);
        //初始化UDP mina相关
        if (UdpManager.getInstance().getConnectorUdp() == null) {
            ThreadManager.getInstance().doExecute(new Runnable() {
                @Override
                public void run() {
                    UdpManager.getInstance().connectUdp(new
                            UdpIoHandlerAdapter(UdpView.this));
                }
            });
        }
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) {

    }

    @Override
    public void messageReceived(IoSession ioSession, byte[] data) {
        Plog.e("messageReceived", bytes2HexString(data));
    }

    @Override
    public void sessionClosed(IoSession ioSession) {

    }

    @Override
    public void sessionCreated(IoSession ioSession) {

    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) {

    }

    @Override
    public void sessionOpened(IoSession ioSession) {

    }

    @Override
    public void messageSent(IoSession ioSession, Object object) {
        Plog.e("messageSent", "messageSent");
    }
}
