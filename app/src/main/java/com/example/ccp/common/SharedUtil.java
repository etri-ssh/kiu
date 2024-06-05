package com.example.ccp.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.ccp.common.retrofit.API;
import com.example.ccp.common.retrofit.RetrofitClient;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressLint({"HardwareIds", "CommitPrefEdits", "ApplySharedPref"})
public class SharedUtil {
    // api url key 값
    private final String SH_PR_URL_MAP = "api.url.map";
    private final String SH_PR_URL_GNSS = "api.url.gnss";
    private final String SH_PR_URL_SIGNAL = "api.url.signal";
    private final String SH_PR_URL_IMAGE = "api.url.image";

    // 기타 설정 key 값
    private final String SH_PR_MAP_RADIUS = "auto.map.radius";

    // gnss 설정 key 값
    private final String SH_PR_GNSS_STATE = "gnss.state";
    private final String SH_PR_GNSS_MOUNT = "gnss.mount";

    // signal 설정 key 값
    private final String SH_PR_SIGNAL_STATE = "signal.state";
    private final String SH_PR_SIGNAL_TIMER = "signal.timer";

    // scanner 설정 key 값
    private final String SH_PR_BLE_TIMER = "ble.timer";
    private final String SH_PR_LTE_TIMER = "lte.timer";
    private final String SH_PR_WIFI_TIMER = "wifi.timer";
    private final String SH_PR_IMU_TIMER = "imu.timer";

    // 영상 설정 key 값
    private final String SH_PR_IMAGE_STATE = "image.state";

    // 측위 연계 key 값
    private final String SH_PR_FINAL_TIMER = "final.timer";


    private final SharedPreferences sp;

    public SharedUtil(Context context) {

        /**
         * sharedPrefrence는 간단한 데이터를 key-value 쌍으로 저장하는데 사용되는 Android의 내장클래스
         */
        sp = context.getSharedPreferences("etri.shared.preferences", Context.MODE_PRIVATE);

        try {
            Common.H_MAP_API = RetrofitClient.getRetrofit(getUrlMap(), 3).create(API.class);
            Common.H_GNSS_URL = getUrlGnss();
            Common.H_SIGNAL_API = RetrofitClient.getRetrofit(getUrlSignal(), 3).create(API.class);
            Common.E_IMAGE_API = RetrofitClient.getRetrofit(getUrlImage(), 10).create(API.class);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            BottomToast.createToast(context, "사용할 수 없는 URL 이 있습니다\nError : " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
            BottomToast.createToast(context, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // api url 설정
    public String getUrlMap() { return sp.getString(SH_PR_URL_MAP, Common.DEFAULT_MAP_URL) + "/"; }
    public void setUrlMap(String url) { sp.edit().putString(SH_PR_URL_MAP, url).commit(); }
    public String getUrlGnss() { return sp.getString(SH_PR_URL_GNSS, "https://estimationrm.dev.gnsson.com/"); }
    public void setUrlGnss(String url) { sp.edit().putString(SH_PR_URL_GNSS, url).commit(); }
    public String getUrlSignal() { return sp.getString(SH_PR_URL_SIGNAL, Common.DEFAULT_SIGNAL_URL) + "/"; }
    public void setUrlSignal(String url) { sp.edit().putString(SH_PR_URL_SIGNAL, url).commit(); }
    public String getUrlImage() { return sp.getString(SH_PR_URL_IMAGE, Common.DEFAULT_IMAGE_URL) + "/"; }
    public void setUrlImage(String url) { sp.edit().putString(SH_PR_URL_IMAGE, url).commit(); }

    // 반경 지도 범위 설정
    public int getMapRadius() { return sp.getInt(SH_PR_MAP_RADIUS, 0); }
    public int getMapRadiusValue(String[] radius) { return Integer.parseInt(radius[getMapRadius()].substring(0, 1)); }
    public void setMapRadius(int index) { sp.edit().putInt(SH_PR_MAP_RADIUS, index).commit(); }

    // gnss on/off 및 보정 좌표 설정
    public boolean getGnssState() { return sp.getBoolean(SH_PR_GNSS_STATE, true); }
    public void setGnssState(boolean flag) { sp.edit().putBoolean(SH_PR_GNSS_STATE, flag).commit(); }
    public int getGnssMount() { return sp.getInt(SH_PR_GNSS_MOUNT, 0); }
    public void setGnssMount(int index) { sp.edit().putInt(SH_PR_GNSS_MOUNT, index).commit(); }

    // signal on/off 및 측위 주기 설정
    public boolean getSignalState() { return sp.getBoolean(SH_PR_SIGNAL_STATE, true); }
    public void setSignalState(boolean flag) { sp.edit().putBoolean(SH_PR_SIGNAL_STATE, flag).commit(); }
    public long getSignalTimer() { return sp.getLong(SH_PR_SIGNAL_TIMER, Common.SIGNAL); }
    public void setSignalTimer(long timer) { sp.edit().putLong(SH_PR_SIGNAL_TIMER, timer).commit(); }

    // scanner 스캔 주기 설정
    public long getBleTimer() { return sp.getLong(SH_PR_BLE_TIMER, Common.BLE); }       // SH_PR_BLE_TIMER (key), Common.BLE(value)
    public void setBleTimer(long timer) { sp.edit().putLong(SH_PR_BLE_TIMER, timer).commit(); }
    public long getLteTimer() { return sp.getLong(SH_PR_LTE_TIMER, Common.LTE); }
    public void setLteTimer(long timer) { sp.edit().putLong(SH_PR_LTE_TIMER, timer).commit(); }
    public long getWifiTimer() { return sp.getLong(SH_PR_WIFI_TIMER, Common.WIFI); }
    public void setWifiTimer(long timer) { sp.edit().putLong(SH_PR_WIFI_TIMER, timer).commit(); }
    public long getImuTimer() { return sp.getLong(SH_PR_IMU_TIMER, Common.IMU); }
    public void setImuTimer(long timer) { sp.edit().putLong(SH_PR_IMU_TIMER, timer).commit(); }

    // 영상 on/off 설정
    public boolean getImageState() { return sp.getBoolean(SH_PR_IMAGE_STATE, true); }
    public void setImageState(boolean flag) { sp.edit().putBoolean(SH_PR_IMAGE_STATE, flag).commit(); }

    // 측위 연계 계산 주기 설정
    public long getFinalTimer() { return sp.getLong(SH_PR_FINAL_TIMER, Common.FINAL); }
    public void setFinalTimer(long timer) { sp.edit().putLong(SH_PR_FINAL_TIMER, timer).commit(); }
}
