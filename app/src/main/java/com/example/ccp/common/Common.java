package com.example.ccp.common;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.example.ccp.common.retrofit.API;
import com.example.ccp.common.retrofit.RetrofitClient;
import com.ppsoln.apolo_r_lib.APOLO_R_Client.VO.EstimationResult;
import com.ppsoln.apolo_r_lib.GNSSMeasurementLib.VO.QM2;

import java.util.ArrayList;


//////////kiu(24.01.09)/////////
import android.os.Build;
//public static String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

///////////////////////////////



@SuppressLint("SimpleDateFormat")
public class Common {
    public static String PUBLIC_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "";

    public static void log(String s) { Log.d("[ETRI]", s); }
    public static void logW(String s) { Log.w("[ETRI]", s); }

    public static final String DEFAULT_MAP_URL = "http://heliosen2.duckdns.org:18080/api/v1";
    public static final String DEFAULT_SIGNAL_URL = "http://heliosen2.duckdns.org:18080/api/v1";
    public static final String DEFAULT_IMAGE_URL = "http://13.124.221.244:8081";

    public static API H_MAP_API = null;
    public static String H_GNSS_URL = null;
    public static API H_SIGNAL_API = null;
    public static API E_IMAGE_API = null;

    public static final String T_RESULT = "result";
    public static final String T_FUSED = "gray";
    public static final String T_GNSS = "red";
    public static final String T_SIGNAL = "blue";
    public static final String T_IMAGE = "green";

    /** [ETRI] 내용 추가 (24.01.29) **/
    public static final String T_ANDGNSSL = "yellow";
    public static final String T_ANDNLPL = "cyan";



    // 주기 기본 값
    public static final long MAP = 1000; // 지도, 자동 층전환 감지 주기
    public static final long FINAL = 2000; // r1, r2 계산 주기
    public static final long FUSED = 1000; // fused 갱신 주기
    public static final long GNSS = 2000; // gnss data 적재 주기
    public static final long SIGNAL = 2000; // signal data 적재 주기
    public static final long BLE = 800; // ble scan data 적재 주기
    public static final long LTE = 1000; // lte scan data 적재 주기
    public static final long WIFI = 2000; // wifi scan data 적재 주기
    public static final long IMU = 500; // imu scan data 적재 주기


    /** @ETRI 로그 데이터 관련 변수 정리 및 적용 필요 **/
    public static int SENSOR_DELAY = 10000;
    public static String PDR_TITLE = ",utcTimestamp,accX,accY,accZ,biasX,biasY,biasZ,accP,accPBWLPF,stepNum,stepDetect,stepStride,heading, posPdrX, posPdrY, posPdrGlobX, posPdrGlobY, DNN";
    public static String GYRO_TITLE = ",utcTimestamp,gyroX,gyroY,gyroZ,driftX,driftY,driftZ,Power,Rad,Deg,flagLpfiltered";
    public static String MAG_TITLE = ",utcTimestamp,magX,magY,magZ,Roll,Pitch,Yaw(rad),Yaw(deg)";
    public static String ORI_TITLE = ",utcTimestamp,azimuth,pitch,roll";

    public static String WIFI_TITLE = ",utcTimestamp,microTimestamp,bssid,ssid,capabilities,centerFreq0,centerFreq1,channelWidth,frequency,level,WiFi_Lat,WiFi_Lon";
    public static String LOC_TITLE = ",utcTimestamp,nanoTimestamp,unixTime,provider,lat,lon,altitude,speed,accuracy,bearing,speedAccuracyMeters,bearingAccuracyDegrees,verticalAccuracyMeters,mock,In/Out";
    public static String GNSS_RAW_TITLE = ",utcTimestamp,timeNanos,leapSecond,timeUncertaintyNanos,fullBiasNanos,biasNanos,biasUncertaintyNanos,driftNanosPerSecond,driftUncertaintyNanosPerSecond,hardwareClockDiscontinuityCount,svid,timeOffsetNanos,state,receivedSvTimeNanos,receivedSvTimeUncertaintyNanos,cn0DbHz,pseudorangeRateMetersPerSecond,pseudorangeRateUncertaintyMetersPerSecond,accumulatedDeltaRangeState,accumulatedDeltaRangeMeters,accumulatedDeltaRangeUncertaintyMeters,carrierFrequencyHz,carrierCycles,carrierPhase,carrierPhaseUncertainty,multipathIndicator,snrInDb,constellationType,agcDb,basebandCn0DbHz,fullInterSignalBiasNanos,fullInterSignalBiasUncertaintyNanos,satelliteInterSignalBiasNanos,satelliteInterSignalBiasUncertaintyNanos,codeType,chipsetElapsedRealtimeNanos";


    /**
     * 피피솔, 안드로이드 Positioning 전환 기준 메서드
     * 2023.08.14
     * 초안 전환 기준 메서드
     * @param qm2list : gnssMeasurementToQM2 콜백을 통해서 넘어오는 QM2 리스트
     * @param useGps : EstimationResult에 담긴 사용 기지국 갯수
     * @param dop :  EstimationResult에 담긴 dilution of precision (가중치로 보임 )
     * @return
     *  true : pps result 값 사용
     *  false : 안드로이드 Fused location 값 사용
     */
    public static boolean getGnssPseudo_code2(ArrayList<QM2> qm2list, String useGps, String dop){
        int useGPS = Integer.parseInt(useGps);
        int DOP = (int)Double.parseDouble(dop);
        boolean flag_useGps = false;
        boolean flag_dop = false;
        boolean flag_snr = false;
        boolean flag_ppsPos;
        // 0(false) : Fused location / 1(true) : pps result
        if(useGPS >= 4) flag_useGps = true;

        int qm2_cnt = 0;

        //리턴값은 BigDemical[]인데 0인덱스에만 값이 있어서 사용
        for(QM2 q : qm2list){
            if( q.getSnr()[0] >= -35) qm2_cnt++;
        }
        if(qm2_cnt>=useGPS) flag_snr = true;

        if(DOP < 4) flag_dop = true;

        flag_ppsPos = (flag_useGps && flag_snr && flag_dop);

        return flag_ppsPos;
    }

    public static int getGnssPseudo_code(ArrayList<QM2> qm2list, EstimationResult er) {
        if(qm2list == null || qm2list.isEmpty()) { // 1. QM2list size check
            return 0;
        } else if(er == null) { // 2. EstimationResult data check
            return 0;
        } else if(er.getUsedGPS() == null || Integer.parseInt(er.getUsedGPS()) < 4) { // 3. useGps check
            return 0;
        } else {
            int count = 0;
            for(QM2 qm2 : qm2list) if(qm2.getSnr()[0] >= -35) count++;
            if(count < Integer.parseInt(er.getUsedGPS())) { // 4. qm2 count check
                return 0;
            } else if(er.getDop() == null || Double.parseDouble(er.getDop()) >= 4) { // 5. dop check
                return 0;
            } else return 1;
        }
    }

    public static int getSignalPseudo_code(int floor,double lastErrorRange, boolean indoor){
        if(floor != 0){ // == 1이라는 박사님 제공코드 수정
            if(lastErrorRange < 10.0){
                if(indoor){
                    return 1;
                }else return 2;
            }
        }
        return 0;
    }

    public static int getSignalPseudo_code2(double floor, boolean indoor){//신호기반 간략화
        if(floor > 0 ) {
            if(indoor) return 1;
            else return 2;
        } else return 0;
    }
    public static int getGnssPseudo_code3(ArrayList<QM2> qm2list, EstimationResult er) {//GNSS 간략화
        if(qm2list.size() > 0) {
            if(er != null) {
                return 1;
            } else return 0;
        } return 0;
    }

}
