package com.example.sensorlog.scanner.sensor;

public class DetectPeck{

    private static double d_d=0;
    private static double ad_d=0;
    private static double p=0;
    private static int step=0;

    public static int step_c=0;

    public static double peck_P(double d1, double d2, double peak){     //Peck 검출
        d_d = d1-d2;
        ad_d=Math.sqrt(d_d*d_d);
        if(ad_d>=0.01){
            if(d_d>0){
                peak = 1;
                p=1;
            }else{
                peak = -1;
            }
        }else {
            peak = 0;
            if(p==1){
                peak=peak;
            }
        }
        return peak;
    }

    //Peck 지점에서 걸음 지점 검출
    public static int DetectStep(double peak, double dpeak,double Power,int cnt) {
        if(dpeak==1){
            if(peak != 1) {
                if(Power>0.25){
                    if(cnt>25 ) {    //한 걸음이 0.25초에서 2초 사이에 존재하며 그 외의 신호는 걸음이 아니라 간주한다.(2023.09.07)
                        step = 1;
                    }
                }
            }
        }
        else {  step=0; }
        return step;
    }

    //Step 지점을 모두 더한 총 걸음수
    public static int StepCount(){
        if (step == 1){
           ++step_c;
        }else {
            step_c = step_c;
        }
        return step_c;
    }

//  public static int Stepout() { return step_c; }
    public static int Stepreset() {
        return step_c=0;
    }
}
