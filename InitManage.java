package XXXXXXX;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * 专门为有线连接的时候创建的管理器
 */
public class InitManage {
    private static final String TAG = InitManage.class.getSimpleName();
    private Context context;
    private WifiManager wifiManager;
    public InitManage(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }
    /**
     *  打开关闭有线
     * @param enable
     * @throws InvocationTargetException
     * @throws Exception
     */
    private void setEthernetInside(boolean enable) throws InvocationTargetException, Exception {
        String ETHERNET_SERVICE = (String) Context.class.getField("NETWORKMANAGEMENT_SERVICE").get(null);
        Class<?> manager = Class.forName("android.os.ServiceManager");
        Method method1 = manager.getMethod("getService", String.class);
        IBinder b = (IBinder) method1.invoke(manager, ETHERNET_SERVICE);
        Class<?> service = Class.forName("android.os.INetworkManagementService");
        Class<?> stub = Class.forName("android.os.INetworkManagementService$Stub");
        Method method2 = stub.getMethod("asInterface", IBinder.class);
        Object o = method2.invoke(service, b);
        Method[] methods = o.getClass().getDeclaredMethods();
        for (Method ms : methods) {
            if (enable && ms.getName().equals("setInterfaceUp")) {
                ms.invoke(o, "eth0");
            }
            if (!enable && ms.getName().equals("setInterfaceDown")) {
                ms.invoke(o, "eth0");
            }
        }
        Class<?> system = Class.forName("android.os.SystemProperties");
        Method methodSet = system.getMethod("set", String.class, String.class);
        methodSet.invoke(null, "eth.enable", enable ? "1" : "0");
        Settings.System.putString(VoiceApplication.getContext().getContentResolver(), "ETH_ENABLE", enable ? "1" : "0");
    }
    private String pingIddress = "www.baidu.com";

    public static final int PINGRESULT = 90;
    private int tryTime = 7;

    /**
     * ping 外网 。 注意部分机型不支持ping
     * @param mHandler
     */
    public void haveNet(final Handler mHandler) {
        tryTime = 15;
        //ping 一下外网 即可
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (tryTime > 0) {
                    boolean succ = SocketUtil.ping(pingIddress, 1);
                    Log.e("suipu", "ping =" + succ);
                    if (succ || tryTime == 1) {
                        tryTime = 0;
                        Message message = new Message();
                        message.what = PINGRESULT;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("ping", succ);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                    tryTime--;
                    takeSleep(1000);
                }
            }
        };
       new Thread(runnable).start();
    }

    /**
     * 获取到有线地址
     * @return
     */
    public String getEth0Ip() {
        return SocketUtil.getLocalIp();
    }
    /*
    设置有线网络
     */
    public void setEthernet(final boolean enable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setEthernetInside(enable);
                } catch (InvocationTargetException e) {
                    Log.e("ssss", "此处接收被调用方法内部未被捕获的异常");
                    Throwable t = e.getTargetException();// 获取目标异常
                    Log.e("ssss", t.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ssss", e.getMessage());
                }
            }
        }).start();
    }


    private void takeSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setWifiAP(String ssid, String pwd) {
        if (this.wifiManager.isWifiEnabled()) {
            this.wifiManager.setWifiEnabled(false);
        }

        if (!this.confirmApOpen()) {
            if (TextUtils.isEmpty(pwd)) {
                this.confirmApOpen(ssid);
            } else {
                this.setWifiApInside(ssid, pwd);
            }
        } else {
            Log.d("WifiApAdmin", "ap already enabled ");
        }

    }
    private void confirmApOpen(String ssid) {
        Method var2 = null;

        try {
            var2 = this.wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            WifiConfiguration var3 = new WifiConfiguration();
            var3.SSID = ssid;
            var3.allowedAuthAlgorithms.clear();
            var3.allowedGroupCiphers.clear();
            var3.allowedKeyManagement.clear();
            var3.allowedPairwiseCiphers.clear();
            var3.allowedProtocols.clear();
            var3.allowedAuthAlgorithms.set(0);
            var3.allowedKeyManagement.set(0);
            var3.priority = 100;
            var3.status = 2;
            var2.invoke(this.wifiManager, var3, true);
        } catch (IllegalArgumentException var4) {
            var4.printStackTrace();
        } catch (IllegalAccessException var5) {
            var5.printStackTrace();
        } catch (InvocationTargetException var6) {
            var6.printStackTrace();
        } catch (SecurityException var7) {
            var7.printStackTrace();
        } catch (NoSuchMethodException var8) {
            var8.printStackTrace();
        }

    }
    public boolean confirmApOpen() {
        try {
            Method var1 = this.wifiManager.getClass().getMethod("isWifiApEnabled");
            var1.setAccessible(true);
            return (Boolean)var1.invoke(this.wifiManager);
        } catch (NoSuchMethodException var2) {
            var2.printStackTrace();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return false;
    }
    private void setWifiApInside(String ssid, String pwd) {
        Method var3 = null;
        try {
            var3 = this.wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            WifiConfiguration var4 = new WifiConfiguration();
            var4.SSID = ssid;
            var4.preSharedKey = pwd;
            var4.allowedAuthAlgorithms.set(0);
            var4.allowedProtocols.set(1);
            var4.allowedProtocols.set(0);
            var4.allowedKeyManagement.set(1);
            var4.allowedPairwiseCiphers.set(2);
            var4.allowedPairwiseCiphers.set(1);
            var4.allowedGroupCiphers.set(3);
            var4.allowedGroupCiphers.set(2);
            var3.invoke(this.wifiManager, var4, true);
        } catch (IllegalArgumentException var5) {
            var5.printStackTrace();
        } catch (IllegalAccessException var6) {
            var6.printStackTrace();
        } catch (InvocationTargetException var7) {
            var7.printStackTrace();
        } catch (SecurityException var8) {
            var8.printStackTrace();
        } catch (NoSuchMethodException var9) {
            var9.printStackTrace();
        }

    }
    /**
     * 关闭WiFi热点
     */
    public void closeWifiHotspot() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    private void setMobileDataState(Context cxt, boolean mobileDataEnabled) {
        TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception e) {
            LogUtils.v(TAG, "Error setting" + ((InvocationTargetException)e).getTargetException() + telephonyService);
        }
    }

    public boolean getMobileDataState(Context cxt) {
        TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                return mobileDataEnabled;
            }
        }
        catch (Exception e) {
            LogUtils.v(TAG, "Error getting" + ((InvocationTargetException)e).getTargetException() + telephonyService);
        }

        return false;
    }
    public void open4G(Context context) {
        if (!getMobileDataState(context)) {
            setMobileDataState(context, true);
        }
    }

    public void close4G(Context context) {
        if (getMobileDataState(context)) {
            setMobileDataState(context, false);
        }
    }

    public String compileSSID(String ssid) {
        return "\"" + ssid + "\"";
    }

    public String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    //------------------------成功套路----------------------------
    private WifiConfiguration getOriSsid(String ssid) {
        List var2 = this.wifiManager.getConfiguredNetworks();
        if (var2 == null) {
            return null;
        } else {
            Iterator var3 = var2.iterator();

            WifiConfiguration var4;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                var4 = (WifiConfiguration)var3.next();
            } while(!var4.SSID.equals(ssid));

            return var4;
        }
    }
    private WifiConfiguration getConnectWifi() {
        ConnectivityManager var1 = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (var1 != null) {
            NetworkInfo var2 = var1.getActiveNetworkInfo();
            if (var2 != null && var2.isConnected() && var2.getState() == NetworkInfo.State.CONNECTED) {
                String var3 = this.wifiManager.getConnectionInfo().getSSID();
                WifiConfiguration var4 = this.getOriSsid(var3);
                return var4;
            }
        }

        return null;
    }
    private WifiConfiguration createWifiConfig(String var1, String var2, WifiUtil.WifiCipherType var3) {
        LogUtil.dcf("WifiUtil", "tempssid1 = " + var1);
        WifiConfiguration var4 = new WifiConfiguration();
        var4.SSID = compileSSID(var1);
        var4.status = 2;
        var4.priority = 100;
        if (var3 == WifiUtil.WifiCipherType.WIFICIPHER_NOPASS) {
            var4.allowedKeyManagement.set(0);
            var4.allowedProtocols.set(1);
            var4.allowedProtocols.set(0);
            var4.allowedAuthAlgorithms.clear();
            var4.allowedPairwiseCiphers.set(2);
            var4.allowedPairwiseCiphers.set(1);
            var4.allowedGroupCiphers.set(0);
            var4.allowedGroupCiphers.set(1);
            var4.allowedGroupCiphers.set(3);
            var4.allowedGroupCiphers.set(2);
        } else if (var3 == WifiUtil.WifiCipherType.WIFICIPHER_WPA) {
            var4.allowedProtocols.set(1);
            var4.allowedProtocols.set(0);
            var4.allowedKeyManagement.set(1);
            var4.allowedPairwiseCiphers.set(2);
            var4.allowedPairwiseCiphers.set(1);
            var4.allowedGroupCiphers.set(0);
            var4.allowedGroupCiphers.set(1);
            var4.allowedGroupCiphers.set(3);
            var4.allowedGroupCiphers.set(2);
            var4.preSharedKey = "\"".concat(var2).concat("\"");
        } else if (var3 == WifiUtil.WifiCipherType.WIFICIPHER_WEP) {
            var4.allowedKeyManagement.set(0);
            var4.allowedProtocols.set(1);
            var4.allowedProtocols.set(0);
            var4.allowedAuthAlgorithms.set(0);
            var4.allowedAuthAlgorithms.set(1);
            var4.allowedPairwiseCiphers.set(2);
            var4.allowedPairwiseCiphers.set(1);
            var4.allowedGroupCiphers.set(0);
            var4.allowedGroupCiphers.set(1);
            if (this.parseWifiPwd(var2)) {
                var4.wepKeys[0] = var2;
            } else {
                var4.wepKeys[0] = "\"".concat(var2).concat("\"");
            }

            var4.wepTxKeyIndex = 0;
        }

        return var4;
    }
    private boolean parseWifiPwd(String var1) {
        if (var1 == null) {
            return false;
        } else {
            int var2 = var1.length();
            if (var2 != 10 && var2 != 26 && var2 != 58) {
                return false;
            } else {
                for(int var3 = 0; var3 < var2; ++var3) {
                    char var4 = var1.charAt(var3);
                    if ((var4 < '0' || var4 > '9') && (var4 < 'a' || var4 > 'f') && (var4 < 'A' || var4 > 'F')) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private WifiConfiguration getOriConfigure(String ssid) {
        List var2 = this.wifiManager.getConfiguredNetworks();
        if (var2 == null) {
            return null;
        } else {
            Iterator var3 = var2.iterator();

            WifiConfiguration var4;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                var4 = (WifiConfiguration)var3.next();
            } while(!var4.SSID.equals(ssid));

            return var4;
        }
    }

    /**
     * 连接wifi
     * 1、如果已经连接了wifi, 先断开以前wifi得连接
     * 2、确保wifi打开
     * 3、删除原来得保存记录（如果以前连接过这个wifi）
     * 4、创建新的wificonfiguration
     * 5、进行网络连接
     * @param ssid   wifi名字
     * @param pwd    wifi密码
     * @param wifiType  wifi密码加密方式
     * @param connectTime  设置连接超时(ps.部分设备连接wifi时间比较长)
     * @return
     */
    public boolean connectWifi(String ssid, String pwd, WifiUtil.WifiCipherType wifiType, long connectTime) {
        WifiConfiguration var6 = this.getConnectWifi();
        if (var6 != null) {
            this.wifiManager.disableNetwork(var6.networkId);
            this.wifiManager.disconnect();
        }

        if (!this.wifiManager.isWifiEnabled()) {
            this.wifiManager.setWifiEnabled(true);
        }

        while(this.wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED || this.wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                Thread.currentThread();
                Thread.sleep(500L);
            } catch (InterruptedException var15) {
                ;
            }
        }

        Log.d("WifiUtil", "connectWifi: " + ssid + ", PAWORD=" + pwd + " wifi state = " + this.wifiManager.getWifiState() + " type " + wifiType);
        WifiConfiguration var7 = this.createWifiConfig(ssid, pwd, wifiType);
        String var8 = var7.SSID;
        Log.d("WifiUtil", "tempssid = " + var8);
        if (var7 == null) {
            return false;
        } else {
            WifiConfiguration var9 = this.getOriConfigure(compileSSID(ssid));
            if (var9 != null) {
                this.wifiManager.removeNetwork(var9.networkId);
                LogUtil.dcf("WifiUtil", "remove exsists wificonfig");
            }

            int var10 = this.wifiManager.addNetwork(var7);
            Log.e("WifiUtil", " netID " + var10);
            this.wifiManager.disconnect();
            this.wifiManager.enableNetwork(var10, true);
            this.wifiManager.saveConfiguration();
            this.wifiManager.reconnect();
            long var12 = System.currentTimeMillis();

            while(System.currentTimeMillis() - var12 < connectTime) {
                if (this.checkSame(ssid) && this.wifiManager.getDhcpInfo() != null && this.wifiManager.getDhcpInfo().ipAddress != 0) {
                    String var14 = Formatter.formatIpAddress(this.wifiManager.getDhcpInfo().ipAddress);
                    LogUtil.ecf("WifiUtil", "mWifiManager.getDhcpInfo().ipAddress " + var14);
                    return true;
                }

                takeSleep(100L);
            }

            return false;
        }
    }
    private boolean checkSame(String var1) {
        WifiInfo var2 = this.wifiManager.getConnectionInfo();
        return var2.getSSID().equals(compileSSID(var1));
    }
}
