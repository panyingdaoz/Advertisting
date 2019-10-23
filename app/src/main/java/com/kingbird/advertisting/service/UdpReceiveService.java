package com.kingbird.advertisting.service;


import com.kingbird.advertisting.litepal.Parameter;
import com.kingbird.advertisting.manager.ThreadManager;
import com.socks.library.KLog;

import org.litepal.LitePal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

/**
 * 局域网广播
 *
 * @author panyingdao
 * @date 2017-8-29.
 */
public class UdpReceiveService implements Runnable {
    private static UdpReceiveService instance;
    private static final String REQUEST_DEVICE_DISCOVERY = "REQUEST_DEVICE_DISCOVERY";
    private static DatagramSocket serverReceive;
    private static byte[] data = new byte[1024];
    private static DatagramPacket inpacket = new DatagramPacket(data, data.length);

    public static UdpReceiveService getInstance() {
        if (instance == null) {
            instance = new UdpReceiveService();
        }
        return instance;
    }

    @Override
    public void run() {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    serverReceive = new DatagramSocket(6999);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                try {
                    serverReceive.receive(inpacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (inpacket.getLength() != 0) {
                    String str = new String(data, 0, inpacket.getLength());
                    KLog.e("收到的广播消息", str);
                    if (REQUEST_DEVICE_DISCOVERY.equals(str)) {
                        sendData();
                    }
                }
            }
        });
    }

    private void sendData() {
        DatagramSocket serverSend;
        List<Parameter> query = LitePal.findAll(Parameter.class);
        for (Parameter parameter : query) {
            try {
                String str2 = parameter.getDeviceId();
                byte[] bytes = str2.getBytes();
//                KLog.e("得到的ID：", parameter.getDeviceID());
                KLog.e("client端IP：", inpacket.getAddress().getHostAddress());
                KLog.e("client端Port：", inpacket.getPort());
                serverSend = new DatagramSocket();
                DatagramPacket outPacket = new DatagramPacket(bytes,
                        bytes.length, inpacket.getAddress(), inpacket.getPort());
                serverSend.send(outPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
