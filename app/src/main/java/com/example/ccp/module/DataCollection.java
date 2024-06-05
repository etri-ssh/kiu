package com.example.ccp.module;

import com.example.ccp.common.Common;
import com.example.ccp.common.FileStream;
import com.example.ccp.module.model.BasePositioning;
import com.example.ccp.module.model.GnssPositioning;
import com.example.ccp.module.model.ImagePositioning;
import com.example.ccp.module.model.SignalPositioning;
import com.example.ccp.module.pos.FusedModule;
import com.example.ccp.views.MainActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/** [ETRI] MainActivity에서 활용되는 변수, 함수들을 정의 (24.01.29) **/

public class DataCollection {
    private DataCollection() {}
    public static final DataCollection instance = new DataCollection();

    public MainActivity.MarkerAction markerAction = null;

    // Success 시 데이터 적재 하는 list
    private final List<BasePositioning> fused = new ArrayList<>(); // 최종 좌표 에서 사용한 fused
    private final List<GnssPositioning> gnss = new ArrayList<>(); // PPS GNSS
    private final List<SignalPositioning> signal = new ArrayList<>(); // signal
    private final List<ImagePositioning> image = new ArrayList<>(); // image
    private final List<BasePositioning> r1 = new ArrayList<>(); // 보정 되지 않는 최종 좌표?

    /** [ETRI] Android GNSS, KI Wi-Fi 측위 결과 Module화 필요 (24.01.24) **/
    private final List<BasePositioning> andGnssL = new ArrayList<>();    // Android 제공 GNSS position
    private final List<BasePositioning> andNlpL = new ArrayList<>();    // Android 제공 NLP position




    // 사용한 last 좌표인지 체크
    private int gnssUseIndex = -1;
    private int signalUseIndex = -1;

    // Fail 시 데이터 적재 하는 list
    private final Queue<SignalPositioning> signalQueue = new LinkedList<>(); // signal fail queue
    private final Queue<GnssPositioning> gnssQueue = new LinkedList<>(); // gnss fail queue
    private final Queue<ImagePositioning> imageQueue = new LinkedList<>(); // image fail queue

    // 상세 정보에서 활용 중인 success, fail count
    public int gnssSuccessCount = 0;
    public int gnssFailCount = 0;
    public int signalSuccessCount = 0;
    public int signalFailCount = 0;
    public int imageSuccessCount = 0;
    public int imageFailCount = 0;

    // 데이터 존재 시 해당 데이터 적재
    public void fusedPut(BasePositioning bp) { fused.add(bp); markerAction(bp); }
    public void gnssPut(GnssPositioning gp) { gnss.add(gp); gnssSuccessCount++; gnssQueue.clear(); markerAction(gp); }
    public void signalPut(SignalPositioning sp) { signal.add(sp); signalSuccessCount++; signalQueue.clear(); markerAction(sp); }
    public void imagePut(ImagePositioning ip) { image.add(ip); imageSuccessCount++; imageQueue.clear(); markerAction(ip); }
    public void r1Put(BasePositioning bp) { r1.add(bp); markerAction(bp); }
    public void andGnssLPut(BasePositioning bp) {andGnssL.add(bp); markerAction(bp);}
    public void andNlpLPut(BasePositioning bp) {andNlpL.add(bp); markerAction(bp);}


    // fail count update
    public void gnssFailPut(String msg) { gnssQueue.offer(new GnssPositioning(msg)); gnssFailCount++; }
    public void signalFailPut(String msg) { signalQueue.offer(new SignalPositioning(msg)); signalFailCount++; }
    public void imageFailPut(String msg) { imageQueue.offer(new ImagePositioning(msg)); imageFailCount++; }

    // 마지막 데이터 가져 오기
    public BasePositioning fusedLast() { return fused.get(fused.size() - 1); }
    public GnssPositioning gnssLast() { try { return gnss.get(gnss.size() - 1); } catch(Exception e) { return null; } }
    public SignalPositioning signalLast() { try { return signal.get(signal.size() - 1); } catch(Exception e) { return null; } }
    public ImagePositioning imageLast() { try { return image.get(image.size() - 1); } catch(Exception e) { return null; } }
    public BasePositioning r1Last() { return r1.get(r1.size() - 1); }

    public BasePositioning andGnssLLast() {return andGnssL.get(andGnssL.size() - 1);}
    public BasePositioning andNlpLLast() {return  andNlpL.get(andNlpL.size() - 1);}




    // 마지막 데이터 가져 오기(사용한 데이터는 제외)
    public GnssPositioning gnssLast(int index) {
        int lastIndex = gnss.size() - 1;
        if(lastIndex == index) return null;
        else gnssUseIndex = lastIndex; return gnss.get(lastIndex);
    }
    public SignalPositioning signalLast(int index) {
        int lastIndex = signal.size() - 1;
        if(lastIndex == index) return null;
        else signalUseIndex = lastIndex; return signal.get(lastIndex);
    }

    // 실패 데이터 가져 오기(데이터 없을 시 null 가져 오니 호출 시 null check 필수)
    public GnssPositioning gnssFail() { return gnssQueue.peek(); }
    public SignalPositioning signalFail() { return signalQueue.peek(); }
    public ImagePositioning imageFail() { return imageQueue.peek(); }

    // 각 적재 데이터 사이즈 가져 오기
    public int fusedSize() { return fused.size(); }
    public int gnssAllSize() { return gnss.size() + gnssFailCount; }
    public int signalAllSize() { return signal.size() + signalFailCount; }
    public int imageAllSize() { return image.size() + imageFailCount; }
    public int r1Size() { return r1.size(); }

    public int andGnssLSize() { return andGnssL.size(); }
    public int andNlpLSize() { return andNlpL.size(); }



    // marker 표시 함수 호출(MainActivity action 호출)
    private void markerAction(BasePositioning bp) {
        if(markerAction == null) return;
        markerAction.action(bp);
    }

    // 최종 좌표 계산 timer
    private Timer timer = null;

    // 최종 좌표 계산 시작
    public void resultStart(long ms) {
        if(timer != null) return;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            try {
                // r1 최종 좌표 넣기
                BasePositioning bp;
                GnssPositioning gp = gnssLast(gnssUseIndex); // gnss 마지막 좌표(미사용한 좌표)
                SignalPositioning sp = signalLast(signalUseIndex); // signal 마지막 좌표(미사용한 좌표)
                if(!posEmptyChecks()) {
                    int pps = 0; int sig = 0;
                    if(gp != null) pps = gp.flag;
                    if(sp != null) sig = sp.flag;
                    int flag = reliability_comparison(pps, sig);
                    switch(flag) {
                        case 2: { bp = sp; break; } // 최종 signal 사용
                        case 3: { bp = gp; break; } // 최종 gnss 사용
                        default: { bp = new BasePositioning(pps, sig); }
                    }
                    r1Put(new BasePositioning(Common.T_RESULT, bp.lon, bp.lat, flag));
                } else r1Put(new BasePositioning(Common.T_RESULT, FusedModule.location));
            } catch(Exception e) { Common.logW("[d.c] result point error"); e.printStackTrace(); }
            }
        }, Common.FINAL, ms);
    }

    // 최종 좌표 계산 정지
    public void stop() {
        try { if(timer != null) timer.cancel(); }
        catch(Exception e) {
            Common.logW("[d.c] start timer error");
            e.printStackTrace();
        }
    }

    // gnss, signal 데이터 빈 것인지 체크
    private boolean posEmptyChecks() { return gnss.isEmpty() && signal.isEmpty(); }

    // pos flag 에 따른 측위 선택
    // 1. fused     2. signal     3. gnss
    // pdr 인자 값은 받지만 현재는 미적용
    public int reliability_comparison(int pps, int sig) {
        Common.log("[d.c] pps : " + pps + ", sig : " + sig);
        int pr;
        if(pps ==0){
            if(sig ==0) pr = 1;
            else if(sig == 2) pr = 1;
            else pr = 2;
        } else {
            if(sig ==0) pr = 3;
            else if(sig ==1) pr = 2;
            else pr = 3;
        }
        return pr;
    }
}
