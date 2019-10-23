# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\admin\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 腾讯分析混淆
-keep class com.tencent.qcloud.core.** { *;}
-keep class bolts.** { *;}
-keep class com.tencent.tac.** { *;}
-keep class com.tencent.stat.*{*;}
-keep class com.tencent.mid.*{*;}
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.*

#腾讯崩溃混淆
-dontwarn com.tencent.bugly.**
-keep class com.tencent.bugly.** { *;}

#七牛混淆
-keep class com.pili.pldroid.player.** { *; }
-keep class com.qiniu.qplayer.mediaEngine.MediaPlayer{*;}

#myokhttp混淆
#myokhttp
-dontwarn com.tsy.sdk.myokhttp.**
-keep class com.tsy.sdk.myokhttp.**{*;}

#myokhttp混淆
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

#myokhttp混淆
#okio
-dontwarn okio.**
-keep class okio.**{*;}

#myokhttp混淆
-dontwarn com.franmontiel.persistentcookiejar.**
-keep class com.franmontiel.persistentcookiejar.**{*;}

#myokhttp混淆
#gson
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#Glide混淆
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#Liteal数据库
-keep class org.litepal.** {*;}
-keep class * extends org.litepal.crud.DataSupport {*;}
-keep class * extends org.litepal.crud.LitePalSupport {*;}

#项目混淆打包配置
-dontwarn org.slf4j.**
-dontwarn org.apache.mina.**
-keep class com.kingbird.advertisting.**
-keep class com.kingbird.advertisting.base.Banner2.**
# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#2019-1-3 添加 public class *extends
#-keep public class * extends java.lang.annotation.Annotation.** {*;}
#-keep public class java.lang.annotation.Annotation{*;}
-keep class java.lang.annotation.Annotation.**
-dontwarn java.lang.annotation.Annotation

#apache混淆
-dontwarn org.apache.**
-keep class org.apache.** { *;}

#解决Gson 解析中代码混淆后解析失败问题
# removes such information by default, so configure it to keep all of it. 
-keepattributes Signature 
# Gson specific classes 
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; } 
# Application classes that will be serialized/deserialized over Gson 
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.kingbird.advertisting.jsonbean.** { *; }

#中南时创电视混淆
-dontwarn  com.mstar.android.**

#讯飞语音
-keep class com.iflytek.**{*;}
-keepattributes Signature

# FastJson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
#京东
-keep class com.kuaifa.ad.** { *; }
#处理 Caused by: java.lang.RuntimeException: Job failed, see logs for details、
#Caused by: java.io.IOException: Please correct the above warnings first. 异常
#-dontwarn com.apptutti.**
#-dontwarn com.dataeye.**

