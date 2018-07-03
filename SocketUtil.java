package com.suipu.listrowview.robotvoice.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketUtil {
    public static int WIFIPORT = 51000; //我们自己创建一个socket服务 用于初始化设备
    public static int[] INITPORT = new int[]{40003, 40004, 40005};
    public static final int DEVICE_CONNECTED = 123;
    public static final int GET_MSG = 124;
    public static final int SEND_MSG_SUCCSEE = 125;
    public static final int SEND_MSG_ERROR = 126;
    public static final int DEVICE_CONNECTING = 127;
    public static final int APOPEN = 128;
    public static final String SELECTWIRED = "wired"; // 选择有线
    public static final String SELECTWIFI = "wifi"; // 选择无线
    public static final String SELECT4G = "4g"; // 选择4G // 那么手机必须连接发出来的热点 才可以进行操作
    public static final String INITOK = "initok"; // 有线连接成功// 即手机判断处于同一个局域网
    private static final String TAG = SocketUtil.class.getSimpleName();


    public static int getValidPort(int[] ports) {
        for(int i = 0; i < ports.length; ++i) {
            if (verifyPort(ports[i])) {
                return ports[i];
            }

            Log.e("suip", "portUnavailable " + ports[i]);
        }

        return -1;
    }
    private static void bindSocket(String var0, int var1) throws IOException {
        Socket var2 = new Socket();
        var2.bind(new InetSocketAddress(var0, var1));
        var2.close();
    }
    private static boolean verifyPort(int port) {
        new Socket();
        try {
            bindSocket("0.0.0.0", port);
            bindSocket(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    public static boolean ping(String host, int pingCount ) {
        String line = null;
        BufferedReader successReader = null;
        String command = "ping -c " + pingCount + " -W 1 " + host; // 1016 + 8字节的头
        boolean isSuccess = false;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                Log.e("network","ping fail:process is null.");
               // append(stringBuffer, "ping fail:process is null.");
                return false;
            }

            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                Log.e("network",line);
                if (line.contains("Unreachable")) {// 网络连接错误
                    //
                    process.destroy();
                    try {
                        successReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }
            int status = process.waitFor();
            if (status == 0) {
                Log.e("network","exec cmd success:" + command);
                isSuccess = true;
            } else {
                Log.e("network","exec cmd fail.");
                isSuccess = false;
            }
            Log.e("network","exec finished.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Log.e("network","ping exit.");
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

 /**
     * 得到有限网关的IP地址
     *
     * @return
     */
    public static String getLocalIp() {

        try {
            // 获取本地设备的所有网络接口
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                Log.i("tag", "网络名字" + interfaceName);

                // 如果是有限网卡
                if (interfaceName.equals("eth0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();

                    while (enumIpAddr.hasMoreElements()) {
                        // 返回枚举集合中的下一个IP地址信息
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // 不是回环地址，并且是ipv4的地址
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            Log.i("tag", inetAddress.getHostAddress() + "   ");

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";

    }

}
