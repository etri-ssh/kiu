package com.example.ccp.scan_module;

import static com.example.ccp.common.MyApplication.shared;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.example.ccp.scan_module.scanner.BLEScanner;
import com.example.ccp.scan_module.scanner.GnssStatusScanner;
import com.example.ccp.scan_module.scanner.IMUScanner;
import com.example.ccp.scan_module.scanner.LTEScanner;
import com.example.ccp.scan_module.scanner.RawGnssScanner;
import com.example.ccp.scan_module.scanner.WIFIScanner;

public class ScanModule {

    public static MutableLiveData<String> acc = new MutableLiveData<>();//가속도 문자열
    public static MutableLiveData<float[]> accValue = new MutableLiveData<>();//가속도 값
    public static MutableLiveData<String> gyro = new MutableLiveData<>();//자이로 문자열
    public static MutableLiveData<float[]> gyroValue = new MutableLiveData<>();//자이로 값
    public static MutableLiveData<String> mag = new MutableLiveData<>();//지자기 문자열
    public static MutableLiveData<float[]> magValue = new MutableLiveData<>();//지자기 값
    public static MutableLiveData<String> ori = new MutableLiveData<>();   //orientation 문자열
    public static MutableLiveData<float[]> oriValue = new MutableLiveData<>();//orientation 값

    public static MutableLiveData<String> ble = new MutableLiveData<>();//블루투스
    public static MutableLiveData<String> lte = new MutableLiveData<>();//이동통신망
    public static MutableLiveData<String> wifi = new MutableLiveData<>();//와이파이

    public static MutableLiveData<String> rawGnss = new MutableLiveData<>();    //raw GNSS
    public static MutableLiveData<String> gnssStatus = new MutableLiveData<>();     // GNSS SV status

    private final BLEScanner bleScanner;
    private final LTEScanner lteScanner;
    private final WIFIScanner wifiScanner;
    private final IMUScanner imuScanner;

    private final RawGnssScanner rawGnssScanner;
    private final GnssStatusScanner gnssStatusScanner;

    public ScanModule(Context context) {
        bleScanner = new BLEScanner(context);
        lteScanner = new LTEScanner(context);
        wifiScanner = new WIFIScanner(context);
        imuScanner = new IMUScanner(context);
        rawGnssScanner = new RawGnssScanner(context);
        gnssStatusScanner = new GnssStatusScanner(context);
    }

    // 스캔 시작
    public void start() {
        /*
         * TODO: 특정 주기에 따른 데이터 적재
         */
        bleScanner.start(shared.getBleTimer());
        lteScanner.start(shared.getLteTimer());
        wifiScanner.start(shared.getWifiTimer());
        imuScanner.start(shared.getImuTimer());

        rawGnssScanner.start(1000); // get timeer 는 추후에 생성
        //gnssStatusScanner.start(1000); // get timeer 는 추후에 생성
    }

    // 스캔 정지
    public void stop() {
        bleScanner.stop();
        lteScanner.stop();
        wifiScanner.stop();
        imuScanner.stop();

        rawGnssScanner.stop();
        //gnssStatusScanner.stop();
    }
}
