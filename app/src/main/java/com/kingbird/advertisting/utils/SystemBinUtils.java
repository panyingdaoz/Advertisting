package com.kingbird.advertisting.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Pan Yingdao
 */
public class SystemBinUtils {

    public static String run(String command) {
        Scanner input = null;
        String result = "";
        Process process = null;
        List<String> list = new ArrayList<>();
        try {
            list.add("sh");
            list.add("-c");
            list.add(command);
            process = Runtime.getRuntime().exec(list.toArray(new String[0]));
            try {
                //等待命令执行完成，10, TimeUnit.SECONDS
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            InputStream is = process.getInputStream();
            input = new Scanner(is);
            while (input.hasNextLine()) {
                result += input.nextLine() + "\n";
            }
            //加上命令本身，打印出来
            result = command + "\n" + result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    public static String run(String[] command) throws IOException {
        Scanner input = null;
        StringBuilder result = new StringBuilder();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            try {
                //等待命令执行完成，10, TimeUnit.SECONDS
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            InputStream is = process.getInputStream();
            input = new Scanner(is);
            while (input.hasNextLine()) {
                result.append(input.nextLine()).append("\n");
            }
            //加上命令本身，打印出来
            result.insert(0, command + "\n");
        } finally {
            if (input != null) {
                input.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result.toString();
    }

}
