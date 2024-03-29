package com.kingbird.advertisting.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kingbird.advertisting.manager.ThreadManager;
import com.socks.library.KLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import static com.kingbird.advertisting.utils.Config.CONSTANT_ONE;
import static com.kingbird.advertisting.utils.Config.MY_LOG_URL;

/**
 * 说明：
 *
 * @author Pan Yingdao
 * @time : 2019/9/24/024
 */
public class Plog {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String NULL_TIPS = "Log with null object";
    private static final String TAG = "Plog";
    private static final String SUFFIX = ".java";
    private static final int V = 0x1;
    private static final int D = 0x2;
    private static final int I = 0x3;
    private static final int W = 0x4;
    private static final int E = 0x5;
    private static final int A = 0x6;
    private static final int J = 0x7;
    private static final int X = 0x8;
    private static boolean DEBUG = true;
    private static String mGlobalTag;
    private static boolean mIsGlobalTagEmpty = true;

    /**
     * init Plog
     */
    public synchronized static void init(boolean debug) {
        DEBUG = debug;
        //7 day expired
        deleteExpiredLogs(3);
    }

    /**
     * init Plog
     */
    public synchronized static void init(boolean debug, @Nullable String tag) {
        DEBUG = debug;
        mGlobalTag = tag;
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag);
        //7 day expired
        deleteExpiredLogs(7);
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... objects) {
        printLog(V, tag, objects);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(String tag, Object... objects) {
        printLog(D, tag, objects);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... objects) {
        printLog(I, tag, objects);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... objects) {
        printLog(W, tag, objects);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... objects) {
        printLog(E, tag, objects);
    }

    public static void a(Object msg) {
        printLog(A, null, msg);
    }

    public static void a(String tag, Object... objects) {
        printLog(A, tag, objects);
    }

    public static void json(String jsonFormat) {
        printLog(J, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(J, tag, jsonFormat);
    }

    public static void xml(String xml) {
        printLog(X, null, xml);
    }

    private static void printLog(int type, String tagStr, Object... objects) {
        if (!DEBUG) {
            return;
        }
        String[] contents = wrapperContent(tagStr, objects);
//        KLog.e("内容数组：" + Arrays.toString(contents));
        String tag = contents[0];
//        KLog.e("原始内容：" + JSON.toJSONString(objects));
//        KLog.e("原始tag内容：" + tagStr);
//        KLog.e("tag内容：" + tag);
        String msg = contents[1];
//        KLog.e("msg内容：" + msg);
        String headString = contents[2];
//        KLog.e("headString内容：" + headString);
//        if (type==D||type==E){
//            print(type, tagStr, headString, Arrays.toString(objects));
//        }
        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
            case A:
//                KLog.e("执行默认");
                printDefault(type, tag, headString, headString + msg);
                break;
            case J:
                printJson(tag, msg, headString);
                break;
            case X:
                printXml(tag, msg, headString);
                break;
            default:
        }
    }

    private static String[] wrapperContent2(String tagStr, Object... objects) {
        final int stackTraceIndex = 5;
        final String suffix = ".java";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[stackTraceIndex];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + suffix;
        }
        if (className.contains("$")) {
            className = className.split("\\$")[0] + suffix;
        }
        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String tag = (tagStr == null ? className : tagStr);
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
        return new String[]{tag, msg, headString};
    }

    private static String[] wrapperContent(String tagStr, Object... objects) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[5];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }

        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String tag = (tagStr == null ? className : tagStr);

        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG;
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag;
        }

        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {
        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append("Param").append("[").append(i).append("]").append(" = ").append("null").append("\n");
                } else {
                    stringBuilder.append("Param").append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? "null" : object.toString();
        }
    }

    private static void printDefault(int type, String tag, String headString, String msg) {
        int index = 0;
        int maxLength = 4000;
        int countOfSub = msg.length() / maxLength;
        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + maxLength);
                print(type, tag, headString, sub);
                index += maxLength;
            }
            print(type, tag, headString, msg.substring(index));
        } else {
            print(type, tag, headString, msg);
        }
    }

    private static void print(int type, String tag, String headString, String msg) {

        switch (type) {
            case V:
//                log2File("V", tag, msg);
                Log.v(tag, msg);
                break;
            case D:
                log2File2("D", tag, msg, headString, getLogFileName(new Date()));
                Log.d(tag, msg);
                break;
            case I:
//                log2File("I", tag, msg);
                Log.i(tag, msg);
                break;
            case W:
//                log2File("W", tag, msg);
                Log.w(tag, msg);
                break;
            case E:
//                log2File("E", tag, msg, null);
                String paths = MY_LOG_URL + getLogFileName2(new Date());
                File file = new File(paths);
                if (!file.exists()) {
                    KLog.e("创建log: " + file.mkdirs());
                }
                KLog.e("路径：" + paths);
                String lastFileName = listFileSortByModifyTime(paths);
                file = new File(paths + lastFileName);
                if (file.exists()) {
                    long fileSize = file.length() / 1024 / 1024;
//                KLog.e("文件大小：" + file.length() + "; " + fileSize);
                    if (fileSize >= CONSTANT_ONE) {
//                    if (fileSize >= CONSTANT_TEN) {
//                        log2File3("E", paths, msg, headString, getLogFileName3(new Date(), getFiles(paths, new ArrayList<File>()).size() + 1));
                        lastFileName = getLogFileName3(new Date(), getFiles(paths, new ArrayList<File>()).size() + 1);
                    }
                }
//                KLog.e("最新修改文件名：" + orderByDate(paths));
//                KLog.e("最新修改文件名2：" + orderByDate(MY_LOG_URL));
                KLog.e("最终文件名字：" + lastFileName);
                log2File2("E", paths, msg, headString, lastFileName);
                Log.e(tag, msg);
                break;
            case A:
//                log2File2("A", tag, msg, getLogFileName(new Date()));
                Log.wtf(tag, msg);
                break;
            default:
        }
    }

    private static void printJson(String tag, String msg, String headString) {
        String message;
        try {
            final int jsonIndent = 4;
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(jsonIndent);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(jsonIndent);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }
        Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }
        Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    private static void printXml(String tag, String xml, String headString) {
        if (xml != null) {
            xml = formatXml(xml);
            xml = headString + "\n" + xml;
        } else {
            xml = headString + NULL_TIPS;
        }
        Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        String[] lines = xml.split(LINE_SEPARATOR);
        for (String line : lines) {
            if (!(TextUtils.isEmpty(line) || "\n".equals(line) || "\t".equals(line) || TextUtils.isEmpty(line.trim()))) {
                Log.d(tag, "║ " + line);
            }
        }
        Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    private static String formatXml(String inputXml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(inputXml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (Exception e) {
            e.printStackTrace();
            return inputXml;
        }
    }

    private static String getLogFileName(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date) + ".txt";
    }

    private static String getLogFileName2(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private static String getLogFileName3(Date date, int number) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date) + "_" + number + ".txt";
    }

    private static synchronized void log2File2(String level, String path, String msg, String headString, String fileName) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String filePath = path + "/"+((fileName == null) ? getLogFileName(now) : fileName);
        KLog.e("文件完整路径：", filePath);
        File file;
        OutputStream outputStream = null;
        try {
            file = new File(filePath);
//            if (file.exists()) {
//                long fileSize = file.length() / 1024 / 1024;
////                KLog.e("文件大小：" + file.length() + "; " + fileSize);
//                if (fileSize >= CONSTANT_TEN) {
////                    int number = Integer.parseInt(filePath.substring(filePath.lastIndexOf("-") + 1));
////                    String filePath2 = path + getLogFileName(now) + number + 1;
////                    file = new File(filePath2);
//                    Plog.e("新文件路径：" + fileName);
//                    log2File3(level, path, msg, headString, fileName);
//                }
//            }
            outputStream = new FileOutputStream(file, true);

            StringWriter sw = new StringWriter();
            String sb = level +
                    " " +
                    sdf.format(now) +
                    " " +
                    headString +
                    " " +
                    msg +
                    "\n" +
                    sw.toString() +
                    "\n";
            byte[] data = sb.getBytes();
            for (byte datum : data) {
                outputStream.write(datum);
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Plog.e("添加日志异常", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Plog.e("关闭日志文件异常", e);
                }
            }
        }
    }

    private static synchronized void log2File3(String level, String path, String msg, String headString, String fileName) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String filePath = path + ((fileName == null) ? getLogFileName(now) : fileName);
        KLog.e("文件完整路径：", filePath);
        File file;
        OutputStream outputStream = null;
        try {
            file = new File(filePath);
            outputStream = new FileOutputStream(file, true);

            StringWriter sw = new StringWriter();
            String sb = level +
                    " " +
                    sdf.format(now) +
                    " " +
                    headString +
                    " " +
                    msg +
                    "\n" +
                    sw.toString() +
                    "\n";
            byte[] data = sb.getBytes();
            for (byte datum : data) {
                outputStream.write(datum);
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Plog.e(TAG, "添加日志异常", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Plog.e(TAG, "关闭日志文件异常", e);
                }
            }
        }
    }

    /**
     * 检查是否有需要清理文件
     */
    private static void deleteExpiredLogs(final int day) {
        ThreadManager.getInstance().doExecute(new Runnable() {
            @Override
            public void run() {
                String path = MY_LOG_URL;
                File dir = new File(path);
                File[] subFiles = dir.listFiles();
                if (subFiles != null) {
                    int logFileCnt = 0;
                    int expiredLogFileCnt = 0;
                    //one day
                    final int dayMilliseconds = 24 * 60 * 60 * 1000;
                    //day * dayMilliseconds 表示7天的毫秒值
                    long expiredTimeMillis = System.currentTimeMillis() - (day * dayMilliseconds);
                    for (File file : subFiles) {
                        ++logFileCnt;
                        KLog.e("文件最后修改时间", file.lastModified());
                        KLog.e("比较时间", expiredTimeMillis);
                        if (file.lastModified() < expiredTimeMillis) {
                            ++expiredLogFileCnt;
                            boolean deleteResult = deleteDirectory(path);
                            if (deleteResult) {
                                e(TAG, "Delete expired log files successfully:" + file.getName());
                                Log.e(TAG, "删除过期日志:文件总数=" + (subFiles.length) + ", 日志文件数=" + logFileCnt + ", " +
                                        "过期日志文件数=" + expiredLogFileCnt);
                            } else {
                                e(TAG, "Delete expired log files failure:" + file.getName());
                            }
                        } else {
                            KLog.e("当前log文件没有过期", file.getName());
                        }
                    }
                }
            }
        });
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private static boolean deleteDirectory(String filePath) {
        boolean flag;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (File file : files) {
            if (file.isFile()) {
                //删除子文件
                flag = deleteFile(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                //删除子目录
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    private static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取最新修改的文件
     */
    public static String orderByDate(String filePath) {
        String lastFileName = null;
        try {
            ArrayList<String> fileNameList = new ArrayList<>();
            File file = new File(filePath);
            File[] files = file.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0) {
                        return 1;
                    } else if (diff == 0) {
                        return 0;
                    } else {
                        return -1;// 如果 if 中修改为 返回-1 同时此处修改为返回 1 排序就会是递减
                    }
                }

                @Override
                public boolean equals(Object obj) {
                    return true;
                }

            });

            for (File file1 : files) {
                if (file1.isDirectory()) {
                    String fileName = file1.getName();
                    KLog.e("获取的文件名：" + fileName);
                    fileNameList.add(fileName);
                }
            }
            int listSize = fileNameList.size();
            KLog.e("文件大小：" + listSize);
            if (listSize > 0) {
                lastFileName = fileNameList.get(listSize - 1);
            }
        } catch (Exception e) {
            KLog.e("获取文件异常原因：" + e.toString());
        }

        return lastFileName;
    }

    /**
     * 获取目录下所有文件(按时间排序)
     */
    private static String listFileSortByModifyTime(String path) {
        String fileName;
        KLog.e("比较路径：" + path);
        List<File> list = getFiles(path, new ArrayList<File>());
        if (list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return -1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
        int size = list.size();
        if (size > 0) {
            fileName = list.get(list.size() - 1).getName();
            KLog.e("集合最后一个文件名：" + fileName);
        } else {
            fileName = getLogFileName3(new Date(), 0);
        }
        return fileName;
    }

    /**
     * 获取目录下所有文件
     */
    private static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

//    private static synchronized void log2File(String level, String tag, String msg) {
//        Date now = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//        String fileName = getLogFileName(now);
//        FileOutputStream fos = null;
//        try {
//            fos = getAppContext().openFileOutput(fileName, Context.MODE_PRIVATE | Context.MODE_APPEND);
//            StringBuilder sb = new StringBuilder();
//            sb.append(level);
//            sb.append(" ");
//            sb.append(sdf.format(now));
//            sb.append(" ");
//            sb.append(tag);
//            sb.append(" ");
//            sb.append(msg);
////            if (tr != null) {
//            sb.append("\n");
//            StringWriter sw = new StringWriter();
//            Throwable throwable = new Throwable();
//            throwable.printStackTrace(new PrintWriter(sw, true));
//            sb.append(sw.toString());
////            }
//            sb.append("\n");
//            fos.write(sb.toString().getBytes());
//            fos.flush();
//        } catch (Exception e) {
//            Log.e(TAG, "添加日志异常", e);
//        } finally {
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "关闭日志文件异常", e);
//                }
//            }
//        }
//    }

//    /**
//     * 获取指定日期日志
//     */
//    public static synchronized String getLogText(Date date) {
//        String fileName = getLogFileName(date);
//        FileInputStream fis = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            fis = getAppContext().openFileInput(fileName);
//            byte[] buffer = new byte[10240];
//            while (true) {
//                int len = fis.read(buffer);
//                if (len <= 0) {
//                    break;
//                }
//                sb.append(new String(buffer, 0, len));
//            }
//            fis.close();
//        } catch (FileNotFoundException e) {
//            return "未找到对应的日志文件";
//        } catch (Exception e) {
//            Log.e(TAG, "获取日志文本异常", e);
//            StringWriter sw = new StringWriter();
//            e.printStackTrace(new PrintWriter(sw, true));
//            return sw.toString();
//        } finally {
//            if (fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    Log.e(TAG, "获取日志文本异常", e);
//                }
//            }
//        }
//        return sb.toString();
//    }

}
