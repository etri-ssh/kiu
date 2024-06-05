package com.example.ccp.scan_module.scanner;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.WIFIRequest;
import com.example.ccp.scan_module.ScanModule;
import com.example.ccp.scan_module.fingerprinting.DataBase;
import com.example.ccp.scan_module.fingerprinting.FingerPrinting;
import com.example.ccp.views.PDRActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.ccp.scan_module.pdr.PDR.time_s;


import java.util.Timer;
import java.util.TimerTask;
import com.example.ccp.scan_module.pdr.Lowpassfilter;
import com.example.ccp.scan_module.fingerprinting.Positioning;
import com.example.ccp.common.FileStream;

/**
 * @name 와이파이 스캐너
 * 설정된 m/s 시간마다 스캔된 WIFI list 를 리턴해주는 와이파이 스캔 모듈
 */
@SuppressLint("MissingPermission")
public class WIFIScanner extends BaseScanner {
    private final WifiManager wifiManager;

    private final Timer timer = new Timer();
    private List<ScanResult> scanList = new ArrayList<>();      // 매 순간 획득한 신호의 List
    public static final List<List<WIFIRequest>> signalList = new ArrayList<>();     // 누적해서 획득한 신호의 List

    /** FingerPrinting 변수*/
    private boolean dbKey = true;
    private String BSSID_mac;
    private int level_rssi;
    private final ArrayList<String> mac = new ArrayList<>();
    private final ArrayList<Integer> rssi = new ArrayList<>();
    private double preTime = 0;
    private double curTime = 0;
    private double[] Pos = {0, 0, 0};
    public static int F_P = 0;

    public static double[] Init_P = new double[2];

    private ArrayList<Double> P_lat = new ArrayList<>();
    private ArrayList<Double> P_lon = new ArrayList<>();

    private  int rowCount = 0;

//    private FileStream fileStream;

    public WIFIScanner(Context context) {

        fileStream = new FileStream("Wifi");
        fileStream.fileCreate(Common.WIFI_TITLE);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            scanList = wifiManager.getScanResults();

            /** FingerPrinting start*/
            for (ScanResult scanResult : scanList){     // list에 반복 접근

                dbKey = DataBase.dbKey;

                if(!dbKey){
                    curTime = System.currentTimeMillis();       // ms
                    if(rowCount > 1){
                        if(curTime - preTime > 1000){           // ms


                            /** @ETRI 헬리오센 알고리즘 **/
                            //ETRI-heliosen Fingerprinting algorithm
                            //Pos = FingerPrinting.kNN(mac,rssi);

                            /** @ETRI **/
                            Pos = Positioning.kNN(mac, rssi);   // 추후 Module로 변경하여 구성 필요
  /*                          data += "," + Pos[0];
                            data += "," + Pos[1];
                            data += "," + Pos[2];*/

                            if(time_s==0){
                                P_lat.add(Pos[0]);
                                P_lon.add(Pos[1]);
                            }else {
                                Init_P[0] = Lowpassfilter.mean_D(P_lat);
                                Init_P[1] = Lowpassfilter.mean_D(P_lon);

                            }
                                //////////////////////////

                            F_P =1;
                            mac.clear();
                            rssi.clear();

                        }
                    }
                    BSSID_mac = scanResult.BSSID;
                    level_rssi = scanResult.level;
                    mac.add(BSSID_mac.replaceAll(":",""));
                    rssi.add(level_rssi);
                    preTime = curTime;
                }

                //fileStream.fileWrite(data);
            }
            /** FingerPrinting end  */


            if(loopFlag) {
                loopFlag = false;
                signalList.add(getScanRequestList());
                scanList.clear();
            }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    void action() {
        wifiManager.startScan();
        if(!loopFlag) {
            signalList.add(getScanRequestList());
            scanList.clear();
        }
    }

    @Override
    void close() {
        fileStream.fileClose();
    }

    /**
     * WIFI 쓰로틀링 체크 메서드
     * 안드로이드 11버전 이후로 쓰로틀링 제한이 만들어짐 그에 맞는 설정 요구
     */
    public boolean checkThrottle(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return wifiManager.isScanThrottleEnabled();
        } else return false;
    }

    public List<WIFIRequest> getScanRequestList() {
        List<WIFIRequest> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            List<ScanResult> target = new ArrayList<>(scanList);
            for(ScanResult sr : target) {
                result.add(new WIFIRequest(sr.level, sr.SSID, sr.BSSID.replace(":", "")));
                sb.append("[Level]:"+ sr.level+" [Ssid]:"+sr.SSID+"\n");

                /** [ETRI] wifi log test (22.01.22) **/
                String data = "\n" + (++rowCount) + "";
                data += "," + System.currentTimeMillis();
                data += "," + sr.timestamp;
                data += "," + sr.BSSID;
                data += "," + sr.SSID;
                data += "," + sr.capabilities;
                data += "," + sr.centerFreq0;
                data += "," + sr.centerFreq1;
                data += "," + sr.channelWidth;
                data += "," + sr.frequency;
                data += "," + sr.level;


                fileStream.fileWrite(data);

            }
            ScanModule.wifi.postValue(""+sb);

        } catch(Exception e) { Common.logW("[wifi] getScanJsonData error : " + e); e.printStackTrace(); }
        return result;
    }

    public static List<WIFIRequest> getLastRequest() {
        int lastIndex = signalList.size() - 1;
        if(lastIndex == -1) return new ArrayList<>();
        else return signalList.get(signalList.size() - 1);
    }
}
