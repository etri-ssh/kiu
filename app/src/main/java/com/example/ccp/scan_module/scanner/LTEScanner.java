package com.example.ccp.scan_module.scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.BLERequest;
import com.example.ccp.common.retrofit.model.LTERequest;
import com.example.ccp.scan_module.ScanModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @name LTE 스캐너
 * 설정된 m/s 시간마다 스캔된 이동통신망을 리턴해주는 와이파이 스캔 모듈
 */
@SuppressLint({"MissingPermission", "NewApi"})
public class LTEScanner extends BaseScanner {
    private final TelephonyManager telephonyManager;
    private List<CellInfo> scanList = new ArrayList<>();
    public static final List<List<LTERequest>> signalList = new ArrayList<>();

    public LTEScanner(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    void action() {
        scanList = telephonyManager.getAllCellInfo();
        signalList.add(getScanRequestList());
    }

    @Override
    void close() {}

    public List<LTERequest> getScanRequestList() {
        List<LTERequest> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            List<CellInfo> target = new ArrayList<>(scanList);
            for(CellInfo ci : target) {
                CellInfoLte lte = (CellInfoLte) ci;
                CellIdentityLte identityLte = lte.getCellIdentity();
                result.add(new LTERequest(
                    identityLte.getMncString(),
                    ((CellInfoLte)ci).getCellSignalStrength().getRsrq(),
                    identityLte.getPci(),
                    ((CellInfoLte)ci).getCellSignalStrength().getRsrp(),
                    identityLte.getBandwidth(),
                    identityLte.getMccString(),
                    identityLte.getTac(),
                    identityLte.getCi()
                ));
                sb.append("[Rsrq] : "+((CellInfoLte)ci).getCellSignalStrength().getRsrq()+" [Mnc] : "+identityLte.getMncString()+"\n");
            }
            ScanModule.lte.postValue(""+sb);
//            Common.log("[lte] size : " + result.size() + ", data : " + result);
        } catch(Exception e) { Common.logW("[lte] getScanJsonData error : " + e); e.printStackTrace(); }
        return result;
    }

    public static List<LTERequest> getLastRequest() {
        int lastIndex = signalList.size() - 1;
        if(lastIndex == -1) return new ArrayList<>();
        else return signalList.get(signalList.size() - 1);
    }
}
