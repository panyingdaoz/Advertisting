//package com.kingbird.advertisting.utils;
//
//import android.content.Context;
//import android.os.Bundle;
//
//import com.iflytek.cloud.InitListener;
//import com.iflytek.cloud.SpeechConstant;
//import com.iflytek.cloud.SpeechError;
//import com.iflytek.cloud.SpeechSynthesizer;
//import com.iflytek.cloud.SynthesizerListener;
//import com.socks.library.KLog;
//
///**
// * 讯飞语音工具类
// *
// * @author panyingdao
// * @date 2018/10/31.
// */
//public class AudioUtils {
//
//    private static AudioUtils audioUtils;
//    private SpeechSynthesizer mySynthesizer;
//
//    private AudioUtils() {
//    }
//
//    /**
//     * 单例
//     * 创建时间: 2018/10/31
//     */
//    public static AudioUtils getInstance() {
//        if (audioUtils == null) {
//            synchronized (AudioUtils.class) {
//                if (audioUtils == null) {
//                    audioUtils = new AudioUtils();
//                }
//            }
//        }
//        return audioUtils;
//    }
//
//    private InitListener myInitListener = new InitListener() {
//        @Override
//        public void onInit(int code) {
//            KLog.e("mySynthesiezer:", "InitListener init() code = " + code);
//        }
//    };
//
//    /**
//     * 描述:初始化语音配置
//     * 创建时间: 2018/10/31
//     */
//    public void init(Context context) {
//        //处理语音合成关键类
//        mySynthesizer = SpeechSynthesizer.createSynthesizer(context, myInitListener);
//        //设置发音人
//        mySynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
//        //设置合成语速  最大100
//        mySynthesizer.setParameter(SpeechConstant.SPEED, "65");
//        //设置音调  最大100
//        mySynthesizer.setParameter(SpeechConstant.PITCH, "50");
//        //设置音量  最大100
//        mySynthesizer.setParameter(SpeechConstant.VOLUME, "100");
//    }
//
//    /**
//     * 描述:根据传入的文本转换音频并播放
//     * 创建时间: 2018/10/31
//     */
//
//    public void speakText(final String content) {
////        int code = mySynthesizer.startSpeaking(content, new SynthesizerListener() {
//        mySynthesizer.startSpeaking(content, new SynthesizerListener() {
//            @Override
//            public void onSpeakBegin() {
//            }
//
//            @Override
//            public void onBufferProgress(int i, int i1, int i2, String s) {
//            }
//
//            @Override
//            public void onSpeakPaused() {
//            }
//
//            @Override
//            public void onSpeakResumed() {
//            }
//
//            @Override
//            public void onSpeakProgress(int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onCompleted(SpeechError speechError) {
//                KLog.e("红包语音播放完成！");
////                startSpeak(content);
//                speakText(content);
//            }
//
//            @Override
//            public void onEvent(int i, int i1, int i2, Bundle bundle) {
//            }
//        });
//    }
//
//    public void stopSpeak(String str) {
//        KLog.e(str + "调用" + "语音停止");
//        mySynthesizer.stopSpeaking();
//    }
//
////    private void startSpeak(final String content) {
////        speakText(content);
////        count++;
////        if (count < Config.CONSTANT_TEN) {
//////            speakText(content);
////        } else {
////            KLog.e("语音停止");
//////            stopSpeak();
//////            stopRedPacketSpeak();
////            count = 0;
////        }
////    }
//}