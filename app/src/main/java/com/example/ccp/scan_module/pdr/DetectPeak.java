package com.example.ccp.scan_module.pdr;

public class DetectPeak {

    private static double d_d=0;
    private static double ad_d=0;
    private static double p=0;
    private static int step=0;

    public static int step_c=0;

    //Detective Peck
    public static double peak_P(double d1, double d2, double peak){
        d_d = d1-d2;
        ad_d=Math.sqrt(d_d*d_d);
        if(ad_d >= 0.01){
            if(d_d > 0){
                peak = 1;
                p=1;            // [ETRI] 불필요 할 듯?
            }else{
                peak = -1;
            }
        }else {
            peak = 0;
            if(p==1){
                peak = peak;
            }
        }
        return peak;

    }

    //Peck 지점에서 걸음 지점 검출
    public static int DetectStep(double peak, double dpeak, double Power, int cnt) {

        // peak이고 기울기가 바뀌는 경우
        if(dpeak == 1){
            if(peak != 1) {
                if(Power > 0.25){
                    if(cnt>25){//한 걸음이 0.25초에서 2초 사이에 존재하며 그 외의 신호는 걸음이 아니라 간주한다.(2023.09.07)

                        step = 1;
                    }
                }
            }
        }

        else { step=0;}
        return step;
    }
    public static int StepCount(){// Step 지점을 모두 더한 총 걸음수
        if (step==1){ ++step_c;}
        else { step_c = step_c; }
        return step_c;
    }
    

    public static int Stepreset() {
        return step_c=0;
    }
}
