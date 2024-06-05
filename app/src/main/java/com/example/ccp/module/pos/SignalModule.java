package com.example.ccp.module.pos;

import static com.example.ccp.common.MyApplication.shared;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.API;
import com.example.ccp.common.retrofit.RetrofitClient;
import com.example.ccp.common.retrofit.model.PayloadRequest;
import com.example.ccp.common.retrofit.model.SessionLogRequest;
import com.example.ccp.common.retrofit.model.SignalRequest;
import com.example.ccp.common.retrofit.model.SignalResponse;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.SignalPositioning;
import com.example.ccp.scan_module.scanner.BLEScanner;
import com.example.ccp.scan_module.scanner.LTEScanner;
import com.example.ccp.scan_module.scanner.WIFIScanner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignalModule {
    private Timer timer = null;

    public void start(long ms) {
        if(timer != null) return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            try {
                if(Common.H_SIGNAL_API == null) return;
                Common.H_SIGNAL_API.getSignal(getSignalScanData(ms)).enqueue(new Callback<SignalResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SignalResponse> call, @NonNull Response<SignalResponse> response) {
                        Common.log("getSignal onResponse");
                        if(response.isSuccessful()) {
                            SignalResponse data = response.body();
                            if(shared.getSignalState()) {
                                if(data == null || !data.indoor) DataCollection.instance.signalFailPut("신호 측위 데이터를 사용할 수 없습니다");
                                else DataCollection.instance.signalPut(new SignalPositioning(data, Common.getSignalPseudo_code2(
                                    data.building.floor.floorNumber, true)));
                            } else Common.log("[signal] Signal 측위가 현재 미사용 중에 있습니다");
                        } else DataCollection.instance.signalFailPut("서버 통신에 실패하였습니다 : isSuccessful fail");
                    }
                    @Override
                    public void onFailure(@NonNull Call<SignalResponse> call, @NonNull Throwable t) {
                        DataCollection.instance.signalFailPut("통신 Failure : " + t);
                    }
                });
            } catch(Exception e) { Common.logW("[signal] scan data error"); e.printStackTrace(); }
            }
        }, 0, ms);
    }

    public void stop() {
        try { timer.cancel(); }
        catch(Exception e) {
            Common.logW("[signal] start timer error");
            e.printStackTrace();
        }
    }

    // api request 세팅
    private SignalRequest getSignalScanData(long ms) {
        SignalRequest request = new SignalRequest();
        try {
            request.sessionLog = getSessionLog(ms); // session log request
            request.payload = new ArrayList<PayloadRequest>() {{
                add(new PayloadRequest(
                    BLEScanner.getLastRequest(), // ble list request
                    LTEScanner.getLastRequest(), // lte list request
                    WIFIScanner.getLastRequest() // wifi list request
                ));
            }};
        } catch(Exception e) { Common.logW("getSignalScanData Error : " + e); e.printStackTrace(); }
        return request;
    }

    private SessionLogRequest getSessionLog(long ms) {
        long sysTime = System.currentTimeMillis();
        return new SessionLogRequest(sysTime - ms, sysTime);
    }
}
