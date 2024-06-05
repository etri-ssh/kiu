package com.example.ccp.module.pos;

import static com.example.ccp.common.MyApplication.shared;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;

import com.example.ccp.R;
import com.example.ccp.common.Common;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.GnssPositioning;
import com.ppsoln.apolo_r_lib.APOLO_R_Client.Service.EstimationAction;
import com.ppsoln.apolo_r_lib.APOLO_R_Client.Service.EstimationResultListener;
import com.ppsoln.apolo_r_lib.APOLO_R_Client.VO.EstimationResult;
import com.ppsoln.apolo_r_lib.GNSSMeasurementLib.GnssMeasurementLib;
import com.ppsoln.apolo_r_lib.GNSSMeasurementLib.VO.QM2;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("MissingPermission")
public class GNSSModule {
    /** GNSS 명세서 확인 **/
    private final GnssMeasurementLib gnssMeasurementLib;
    private final EstimationAction estimationAction;
    private final String importerPosition;
    private final String importerMount;
    private ArrayList<QM2> qm2ArrayList;
    private boolean flagStart = false;

    // 첫 시작 timer 조정 flag
    private boolean firstScan = true;

    public final GnssMeasurementsEvent.Callback gnssMeasurementsEventListener = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
            Common.log("[gnss] onGnssMeasurementsReceived START");
            try {
                // GnssMeasurementsEvent의 Data 필요 (QM2로 변환)
                qm2ArrayList = gnssMeasurementLib.gnssMeasurementToQM2(event);
                // qm2ArrayList는 첫 데이터를 받은 후 GPS L1 Signal이 포함될 때까지 Null을 전송
                if (qm2ArrayList != null && !flagStart) {
                    estimationAction.createEstimationRoom();
                    // event가 발생 할 때마다 createEstimationRoom()이 실행되기 때문에 방지 하기 위해 flag를 변경
                    flagStart = true;
                    // 종료 후 false로 변환필요
                }
                if(qm2ArrayList != null && qm2ArrayList.size() > 0) {
                    int weekNumber = (int) qm2ArrayList.get(0).getWeekNumber();
                    if(weekNumber != 0) { estimationAction.sendPositionforAndroid(importerMount, importerPosition); }
                }
                if(estimationAction.isInit() && qm2ArrayList.size() != 0) {
                    estimationAction.sendEstiamteQM2(qm2ArrayList);
                }
            } catch(Exception e) { Common.logW("onGnssMeasurementsReceived Error\n" + e); e.printStackTrace(); }
        }
        @Override
        public void onStatusChanged(int status) {}
    };

    public final OnNmeaMessageListener onNmeaMessageListener = (msg, timestamp) -> {};
    private final LocationManager locationManager;

    public GNSSModule(Context context) {
        gnssMeasurementLib = new GnssMeasurementLib();
        estimationAction = new EstimationAction(gnssMeasurementLib);

        Resources resources = context.getResources();
        String[] importerPositions = resources.getStringArray(R.array.ImporterPosition);
        String[] importerMounts = resources.getStringArray(R.array.ImporterMount);
        importerPosition = importerPositions[shared.getGnssMount()];
        importerMount = importerMounts[shared.getGnssMount()];

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        long minTime = 1000; // 갱신 최소 시간
        float minDistance = 0;// 갱신 최소 거리
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, location -> {});
    }

    private EstimationResult result = null;
    private Timer timeoutTimer;
    private void setTimeout(long ms) {
        Common.log("[gnss] mudule set timeout");
        try {
            if(timeoutTimer != null) timeoutTimer.cancel();
            timeoutTimer = new Timer();
        } catch(Exception e) { Common.logW("[gnss] time cancel error : " + e); e.printStackTrace(); }
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
            Common.log("[gnss] timer schedule");
            try {
                // TODO: ON, OFF 상태에 따른 flag 값 고정 작업 필요(설정 activity 작성 후 할 것)
                if(shared.getGnssState()) {
                    if(result != null) DataCollection.instance.gnssPut(
                            new GnssPositioning(result, Common.getGnssPseudo_code3(qm2ArrayList, result)));
                    else DataCollection.instance.gnssFailPut("GNSS 측위 결과값이 없습니다");
                } else Common.log("[gnss] GNSS 측위가 현재 미사용 중에 있습니다");
            } catch(Exception e) { Common.logW("[gnss] timer error : " + e); e.printStackTrace(); }
            finally { result = null; }
            }
        }, 0, ms);
    }

    // 스캔 시작
    public void start(long ms) {
        Common.log("[gnss] start");
        try {
            setTimeout(ms);
            locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener);
            locationManager.addNmeaListener(onNmeaMessageListener);
            String apiKey = "mcmt8GpZokJSPndjG2L6GfliLXmdu2PXjI647bf+cjDpdfaoJWearIoGQZsWkLJJTMkpo1iXCBHm/K7KpDVRwg==";
            String secretKey = "f5OmEDZg/NDaKIVRvhl8ZUR7jafDxqiY0nNcSgVy8SfcL0HeFXU0x8pcOfFpqd6JBzDDXHeiAm51XHL+LJpKQA==";
            estimationAction.setkey(apiKey, secretKey);
            EstimationResultListener estimationResultListener = estimationResult -> {
                Common.log("[gnss] estimationResult in data");
                if(firstScan) { firstScan = false; setTimeout(ms); }
                result = estimationResult; };
            estimationAction.setEstimationResultListener(estimationResultListener);
        } catch(Exception e) { e.printStackTrace();}
    }

    // 스캔 정지
    public void stop() {
        try {
            estimationAction.sendEndMessage();
            estimationAction.deleteEstimationRoom();
            flagStart = false;
        } catch(Exception e) { e.printStackTrace();}
        try { if(timeoutTimer != null) timeoutTimer.cancel(); }
        catch(Exception e) { e.printStackTrace();}
    }
}
