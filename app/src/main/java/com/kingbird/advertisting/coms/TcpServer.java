package com.kingbird.advertisting.coms;


import android.util.Log;

import com.kingbird.advertisting.manager.ThreadManager;
import com.kingbird.advertisting.utils.Const;
import com.kingbird.advertisting.utils.Plog;
import com.kingbird.advertisting.utils.SpUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static com.kingbird.advertisting.base.Base.bytes2HexString;
import static com.kingbird.advertisting.base.Base.dataProcessing;
import static com.kingbird.advertisting.utils.Plog.e;

/**
 * Demo class 服务器
 *
 * @author panyingdao
 * @date 2017/8/24.
 */
public class TcpServer {
    private static final String TAG = "TcpServer";
    private static TcpServer instance;
    //线程监听标志位
    private boolean isListen = true;
    private ArrayList<ServerSocketThread> sST = new ArrayList<>();

    private DataInputStream is = null;
    private DataOutputStream os = null;
    private boolean mConnected;

    private TcpServer() {
    }

    //单例模式
    public static TcpServer getInstance() {
        if (instance == null) {
            instance = new TcpServer();
        }
        return instance;
    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
//            Plog.e(TAG, "run: 监听超时");
            return null;
        }
    }

    public boolean connect() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(9999);
                    e("服务器启动，端口=", serverSocket.toString());
                    //设置超时时间
                    serverSocket.setSoTimeout(5000);
                    while (isListen) {
                        Socket socket = getSocket(serverSocket);
                        if (socket != null) {
                            new ServerSocketThread(socket);
                            mConnected = true;
                        }
                    }
                    serverSocket.close();
                } catch (IOException e) {
                    Plog.e("客户端断开连接");
                    e.printStackTrace();
                    mConnected = false;
                }

            }
        });
        return mConnected;
    }

    /**
     * 数据接收监听线程
     */
    private class ServerSocketThread extends Thread {
        Socket socket = null;
        //运行状态
        private boolean isRun = true;

        ServerSocketThread(Socket socket) {
            this.socket = socket;
            String ip = socket.getInetAddress().toString();
            e(TAG, "ServerSocketThread:检测到新的客户端联入,ip:" + ip);
            try {
                socket.setSoTimeout(5000);
                os = new DataOutputStream(socket.getOutputStream());
                is = new DataInputStream(socket.getInputStream());
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            sST.add(this);
            while (isRun && !socket.isClosed() && !socket.isInputShutdown()) {
                byte[] buff = new byte[1024];
                String rcvMsg;//接收数据
                int rcvLen;//数据长度
                try {
                    if ((rcvLen = is.read(buff)) != -1) {

                        byte[] receive = new byte[rcvLen];

                        rcvMsg = new String(buff, 0, rcvLen, "utf-8");

                        System.arraycopy(buff, 0, receive, 0, rcvLen);
                        //数据处理
                        SpUtil.writeString(Const.CONTROL_TYPE, "LAN");
                        dataProcessing(receive, "client");

                        if ("QuitServer".equals(rcvMsg)) {
                            isRun = false;
                        }
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
                sST.clear();
                e(TAG, "run: 断开连接");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //数据发送方法
    public synchronized int send(byte[] data) {
        if (!mConnected) {
            return 0;
        }
        try {
            if (os != null) {
                os.write(data);
                e("要发送的数据：", bytes2HexString(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return 0;
        }
        return data.length;
    }

    public void closeSelf() {
        isListen = false;
        for (ServerSocketThread s : sST) {
            s.isRun = false;
        }
        sST.clear();
    }
}
