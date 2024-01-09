package com.example.sensorlog.scanner.signal;

import static com.example.sensorlog.MainActivity.PDR_ON_OFF;
import static com.example.sensorlog.MainActivity.time_s;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.sensorlog.Fingerprinting.Positioning;
import com.example.sensorlog.MainActivity;
import com.example.sensorlog.common.Common;
import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.sensor.Lowpassfilter;
//import com.example.sensorlog.scanner.logger.WifiLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("MissingPermission")
public class WifiScanner extends BaseScanner {
    private final WifiManager wifiManager;
    private final Timer timer = new Timer();
    //
    private boolean dbKey = true;
    private String BSSID_mac;
    private int level_rssi;
    private final ArrayList<String> mac = new ArrayList<>();
    private final ArrayList<Integer> rssi = new ArrayList<>();
    private double preTime = 0;
    private double curTime = 0;
    private double[] Pos = {0, 0, 0};

    public static int F_P = 0;
    //
    public static double[] Init_P = new double[2];
    private ArrayList<Double> P_lat = new ArrayList<>();
    private ArrayList<Double> P_lon = new ArrayList<>();

    //

    public WifiScanner(Context context, String header) {
        super(header);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        fileStream = new FileStream("Wifi");
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
                Common.log("success : " + success);
                List<ScanResult> scanList =  wifiManager.getScanResults();
                scanList.forEach(item -> {
                    String data = "\n" + (++rowCount) + "";
                    data += "," + System.currentTimeMillis();
                    data += "," + item.timestamp;
                    data += "," + item.BSSID;
                    data += "," + item.SSID;
                    data += "," + item.capabilities;
                    data += "," + item.centerFreq0;
                    data += "," + item.centerFreq1;
                    data += "," + item.channelWidth;
                    data += "," + item.frequency;
                    data += "," + item.level;
                    data += "," + item.operatorFriendlyName; // 31부터 Deprecated
                    data += "," + item.venueName; // 31부터 Deprecated

                    //
                    dbKey = MainActivity.dbKey;


                //    data += "," + F_P;
                    if(!dbKey) {
                        curTime = System.currentTimeMillis();

                        if (rowCount > 1) {
                            if (curTime - preTime > 1000) {
                                Pos = Positioning.kNN(mac, rssi);

                                data += "," + Pos[0];
                                data += "," + Pos[1];
                                data += "," + Pos[2];


                                F_P=1;
                                mac.clear();
                                rssi.clear();
                                if(time_s==0){
                                    P_lat.add(Pos[0]);
                                    P_lon.add(Pos[1]);
                                }else {
                                    Init_P[0]= Lowpassfilter.mean_D(P_lat);
                                    Init_P[1]=Lowpassfilter.mean_D(P_lon);

                               //     data += "," + time_s;
                               //     data += "," + Init_P[0];
                               //     data += "," + Init_P[1];
                                }

                            }
                        }
                        BSSID_mac = item.BSSID;
                        level_rssi = item.level;
                        mac.add(BSSID_mac.replaceAll(":", ""));
                        rssi.add(level_rssi);
                        preTime = curTime;
                    }
                    //
                    fileStream.fileWrite(data);
                });
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
    }

    @Override
    public void start() {
            super.start();
            timer.schedule(new TimerTask() {
                @Override
                public void run() { wifiManager.startScan(); }
            }, 0, 1000);
    }

    @Override
    public void stop() {
        super.stop();
        timer.cancel();
    }
}
