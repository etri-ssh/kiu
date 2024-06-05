package com.example.ccp.scan_module.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.BLERequest;
import com.example.ccp.scan_module.ScanModule;

import java.util.ArrayList;
import java.util.List;


/**
 * @name 블루투스 스캐너
 * 설정된 m/s 시간마다 스캔된 BLE model을 리턴해주는 블루투스 스캔 모듈
 */
@SuppressLint("MissingPermission")
public class BLEScanner extends BaseScanner {
    private final BluetoothLeScanner bluetoothLeScanner;
    private final List<ScanResult> scanList = new ArrayList<>();
    public static final List<List<BLERequest>> signalList = new ArrayList<>();

    private final ScanCallback scanCallback = new ScanCallback() {//BLE 스캔 결과(scanList 에 데이터 삽입)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        scanList.add(result);
        }
    };

    public BLEScanner(Context context) {
        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    void action() {
        if(loopFlag) {
            loopFlag = false;
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            signalList.add(getScanRequestList());
            scanList.clear();
        }
    }

    @Override
    void close() {
        try {
            bluetoothLeScanner.stopScan(scanCallback);
        } catch(Exception e) { Common.logW("[ble] close error"); e.printStackTrace(); }
    }

    public List<BLERequest> getScanRequestList() {
        List<BLERequest> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        try {
            List<ScanResult> target = new ArrayList<>(scanList);
            for(ScanResult sr : target) {
                result.add(new BLERequest(
                    sr.getRssi(),
                    sr.getDevice().getAddress().replace(":", "")
                ));
                sb.append("[Rssi] : "+sr.getRssi()+" [Addr] : "+ sr.getDevice().getAddress()+"\n");
            }
            ScanModule.ble.postValue(""+sb);
//            Common.log("[ble] size : " + result.size() + ", data : " + result);
        } catch(Exception e) { Common.logW("[ble] getScanJsonData error : " + e); e.printStackTrace(); }
        return result;
    }

    public static List<BLERequest> getLastRequest() {
        int lastIndex = signalList.size() - 1;
        if(lastIndex == -1) return new ArrayList<>();
        else return signalList.get(signalList.size() - 1);
    }
}
