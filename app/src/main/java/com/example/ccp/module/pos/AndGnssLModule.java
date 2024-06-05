package com.example.ccp.module.pos;

import static com.example.ccp.scan_module.pdr.PDR.time_s;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.ccp.common.FileStream;
import com.example.ccp.common.Common;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.BasePositioning;
import com.example.ccp.scan_module.pdr.Glob_Pos;
import com.example.ccp.scan_module.pdr.Lowpassfilter;


import java.util.ArrayList;

/** [ETRI] Fused 모듈 참고해서 데이터 저장, 불러오는 방식 변경 필요 (2023.01.23)
 * 6월초 수정 예정
**/

@SuppressLint({"MissingPermission", "InlinedApi"})
public class AndGnssLModule implements LocationListener {

    private final LocationManager locationManager;
    private int rowCount = 0;
    private ArrayList<Integer> IO = new ArrayList<>();
    public static int G_P = 0;  // Measure 동기화
    public static double G_VDOP;  //VDOP 값
    public static int G_IO = 1 ;  //초기 실내/외 확인
    public static double G_IO2 = 1 ; //정렬 이후의 필터링된 I/O값을 이용한 실내/외 확인

    public static Location location;

    private FileStream fileStream;


    public AndGnssLModule(Context context) {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        fileStream = new FileStream("andGnssPos");
        fileStream.fileCreate(Common.LOC_TITLE); // 파일 로그 기능은 우선 삭제
    }

    public void start() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public void stop() {
        locationManager.removeUpdates(this);
        fileStream.fileClose();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double Lon, Lat;
        
        /** [ETRI] DataCollection에 위치 결과만을를 추가 (24.01.29) **/
        Common.log("[AndGnssL] location : " + location);
        AndGnssLModule.location = location;
        DataCollection.instance.andGnssLPut(new BasePositioning(Common.T_ANDGNSSL, location));

        // file write 부분
        String data = "\n" + (++rowCount) + "";
        data += "," + System.currentTimeMillis();
        data += "," + location.getElapsedRealtimeNanos();
        data += "," + location.getTime();
        data += "," + location.getProvider();
        data += "," + location.getLatitude();
        data += "," + location.getLongitude();
        data += "," + location.getAltitude();
        data += "," + location.getSpeed();
        data += "," + location.getAccuracy();
        data += "," + location.getBearing();
        data += "," + location.getSpeedAccuracyMetersPerSecond();
        data += "," + location.getBearingAccuracyDegrees();
        data += "," + location.getVerticalAccuracyMeters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) data += "," + location.isMock();

        Lat = location.getLatitude();
        Lon = location.getLongitude();
        Glob_Pos.Get_GPS(Lat, Lon);

        G_P = 1;
        G_VDOP = location.getVerticalAccuracyMeters();
        if (location.getVerticalAccuracyMeters() < 2.55) {
            G_IO = 0;
        } else {
            G_IO = 1;
        }
        if(rowCount>10)
        {
            if (time_s == 0) {
                IO.add(G_IO);
            } else {
                G_IO2 = Lowpassfilter.mean_I(IO);
            }
        }
        data += "," + G_IO;

        fileStream.fileWrite(data);

    }

}
