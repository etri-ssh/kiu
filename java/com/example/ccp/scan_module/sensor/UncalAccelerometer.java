package com.example.sensorlog.scanner.sensor;

import static com.example.sensorlog.MainActivity.PDR_ON_OFF;             // PDR 버튼 활성화 시점
import static com.example.sensorlog.MainActivity.stay_t;                 // 60초 대기시간
import static com.example.sensorlog.MainActivity.time_s;                 // 60초 대기시간 후의 시점
import static com.example.sensorlog.scanner.location.GpsLScanner.G_IO;   //In Out 구분
import static com.example.sensorlog.scanner.location.GpsLScanner.G_IO2;  // 초기위치 In Out 구분
import static com.example.sensorlog.scanner.location.GpsLScanner.G_VDOP; // 구분을 위한 GPS의 VDOP 값
import static com.example.sensorlog.scanner.sensor.EKF.r;    //가변형 r

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.sensorlog.Fingerprinting.Positioning;
import com.example.sensorlog.MainActivity;
import com.example.sensorlog.common.Common;
import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.location.GpsLScanner;
import com.example.sensorlog.scanner.signal.WifiScanner;
//import com.example.sensorlog.scanner.logger.SensorLogger;

import java.util.ArrayList;

public class UncalAccelerometer extends BaseScanner implements SensorEventListener {
    private final SensorManager sensorManager;

    ////////////////////////경일대 추가 부분 시작 (23.08.28)////////////////////////////

    private double ABlpf = 0;  //ButterWorth LPF(Low Pass Filter) 가속도 결과
    // ButterWorth LPF 를 위한 이전 가속도값과 ButterWorth LPF 결과값 저장
    private double A_s = 0;
    private double A_1 = 0;
    private double A_2 = 0;
    private double B_2 = 0;
    private double B_s = 0;
    //
    private double data_p = 0;     // 걸음 검출 지점
    private double d_p=0;          // Peak 지점
    private double step_p=0; // 걸음 검출 지점
    //private double step1=0;
    //private double step2=0;
    private double state_c=1; //걸음 상태 (걸음 시작, 걸음 중 , 걸음 종료&다음 걸음 시작)
    private int cnt=0;  //걷는 도중의 데이터수
    private double S_freq=0; // 걸음 주파수
    private double S_var=0; //걸음간 가속도 분산값
    private double S_dis=0; // 걸음 Stride
    private double SS_dis=0; // 걸음 Stride 합계
    private double sta=0;    // Measure 간의 걸음 Stride 합계

    private double step1=0;  // 걸음 수 합계
    private double S_disW=0; // Measure 들어왔을 때의 Stride
    private double mAx=0,mAy=0,mAz=0;
    private double heading=0; //Gyro Heading
    private double headingM=0; //Magnet Heading

    private double dx=0,dy=0,dxw=0,dyw=0,dx1=0,dy1=0;  // Position x,y축 이동거리 dx=X(i)-X(i-1),dy=Y(i)-Y(i-1)
    private static double[] PDR_p = new double[2];     // Local PDR Position
    private static double[] PDR_W = new double[2];     // Measure 지점의 Local PDR Position
    private static double[] PDR_pM = new double[2];    //Measure Position(WiFi/GPS)
    private static double[] PDR_pG = new double[2];   // Global PDR Postion
    private static double[] PDR_wG = new double[2];   // Measure 지점의 Global PDR Position

    //LPF를 위한 3축 가속도 평균 저장
    private ArrayList<Double> list = new ArrayList<>();
    private ArrayList<Double> Accx = new ArrayList<>();
    private ArrayList<Double> Accy = new ArrayList<>();
    private ArrayList<Double> Accz = new ArrayList<>();
    private float state_t=0f;

    ////////////////////////경일대 추가 부분 종료 (23.08.28)////////////////////////////

    ////////////////////////경일대 추가 부분 종료 (23.09.11)////////////////////////////

    // 방위각 계산을 위한 3축 가속도 평균 저장
    private static double mdx=0.0,mdy=0.0,mdz=0.0;
    private ArrayList<Double> Accx2 = new ArrayList<>();
    private ArrayList<Double> Accy2 = new ArrayList<>();
    private ArrayList<Double> Accz2 = new ArrayList<>();
    private static double a,b,c;             // Measure 지점
    ////////////////////////경일대 추가 부분 종료 (23.09.11)////////////////////////////

    public UncalAccelerometer(Context context, String header) {
        super(header);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        fileStream = new FileStream("UncalAccelerometer");
    }

    @Override
    public void start() {
        super.start();
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
            sensorManager.registerListener(this, sensor, Common.SENSOR_DELAY);
    }

    @Override
    public void stop() {
        super.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        // double[] Pos = {0, 0};

        String data = "\n" + (++rowCount) + "";
        data += "," + System.currentTimeMillis();
        data += "," + sensorEvent.timestamp;
        data += "," + sensorEvent.values[0];
        data += "," + sensorEvent.values[1];
        data += "," + -sensorEvent.values[2];
        data += "," + sensorEvent.values[3];
        data += "," + sensorEvent.values[4];
        data += "," + sensorEvent.values[5];

        ////////////////////////경일대 추가 부분 시작 (23.09.04)////////////////////////////

        // Power 값 생성
        double ax, ay, az, Apower, Apow, cntw = 0;
        // double step;
        double[][] Fa;

        ax = sensorEvent.values[1];
        ay = sensorEvent.values[0];
        az = -sensorEvent.values[2];

        Apow = Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2);
        Apower = Math.sqrt(Apow) - 9.81;  //센서 데이터 => Power-(중력가속도)

        // 이전 Power 값 저장
        Mag_Heading.Accel_Attit(mdx, mdy, mdz);

        double d2r = Math.PI / 180.0;
        double r2d = 180 / Math.PI;

        if (rowCount == 1) {
            A_s = Apower;
            A_1 = A_s;
            A_2 = A_s;
        //    EKF.header_EKF(36*d2r);
        } else if (rowCount == 2) {
            A_1 = A_s;
            A_2 = A_s;
            A_s = Apower;
            B_2 = 0;
            B_s = ABlpf;
        } else if (rowCount >= 3) {
            A_2 = A_1;
            A_1 = A_s;
            A_s = Apower;
            B_2 = B_s;
            B_s = ABlpf;
        }


        //Butterworth LPF

        ABlpf = Lowpassfilter.BWlowPass(Apower, ABlpf, A_1, A_2, B_2); //2nd order ButterWorth LPF(필터)


        // Alpf = Lowpassfilter.lowPass(Alpf, Alpf);  //LPF(필터)

        data += "," + Apower;
        data += "," + ABlpf;


        data += "," + (step1); //걸음 수!!

        if(PDR_ON_OFF==1){
            if (G_IO2 < 0.6) {
                if (Glob_Pos.GPS_P[0] != 0) {
                    if (PDR_p[0] == 0) {
                        PDR_pG[0] = Glob_Pos.GPS_P[0] * d2r;  //초기 GPS 위치
                        PDR_pG[1] = Glob_Pos.GPS_P[1] * d2r;
                        Glob_Pos.radicurv(PDR_pG[0]);
                        EKF.header_EKF(PDR_pG[0]);
                    }
                }
        }
        else {
            if (Positioning.Pos_F[0] != 0) {
                if (PDR_p[0] == 0) {
                    if (time_s == 1) {
                        PDR_pG[0] = WifiScanner.Init_P[0] * d2r;  //초기 WiFi 위치
                        PDR_pG[1] = WifiScanner.Init_P[1] * d2r;
                        Glob_Pos.radicurv(PDR_pG[0]);
                        EKF.header_EKF(PDR_pG[0]);
                    }
                }
            }
        }
    }
        if (rowCount<stay_t)
        {
            Accx.add(ax);
            Accy.add(ay);
            Accz.add(az);
        }
        if(rowCount==stay_t){
            mAx=Lowpassfilter.mean_D(Accx);
            mAy=Lowpassfilter.mean_D(Accy);
            mAz=Lowpassfilter.mean_D(Accz);
            Lowpassfilter.mean_A(mAx,mAy,mAz);
        }

        if(PDR_ON_OFF==1){        //PDR On : Initial Alignment 후 버튼을 누르면 PDR 걸음 검출
            d_p=data_p;
            data_p=DetectPeck.peck_P(ABlpf,B_s,data_p);
            step_p = (int) DetectPeck.DetectStep(data_p,d_p,ABlpf, cnt);

            step1=DetectPeck.StepCount();
       //     step2 = DetectPeck.step_c; // Before : etectPeck.Stepout();

               }else{
            cnt=0;
        }

        data += "," + step_p; // 걸음 지점!

        ///////////////////////////경일대 추가 부분 시작 (23.09.11)///////////////////////
        Accx2.add(ax);
        Accy2.add(ay);
        Accz2.add(az);
        if (rowCount > 99) {
            mdx = Lowpassfilter.mean_D(Accx2);
            mdy = Lowpassfilter.mean_D(Accy2);
            mdz = Lowpassfilter.mean_D(Accz2);
            Accx2.remove(Accx2.size() - 100);
            Accy2.remove(Accy2.size() - 100);
            Accz2.remove(Accz2.size() - 100);
        }
        if(PDR_ON_OFF==1) {
            ///////////////////////////경일대 추가 부분 시작 (23.09.11)///////////////////////
            if (step_p == 1) { //걸음 감지!
                if (state_c == 1) {  //걸음 시작
                    ++cnt;
                    list.add(Apower);

                    state_c = 0;
                    S_freq = 0;
                    S_var = 0;
                    S_dis = 0;
                    SS_dis = 0;
                } else {//걸음 끝
                    cntw = cnt;
                    if (G_IO == 0) {  //GPS
                        b = c / cnt;
                    }
                    else{             //WiFi
                        b = a / cnt;
                    }
                    cnt = 0;
                    ++cnt;
                    list.add(Apower);

                    //  S_freq=Stride.Str_freq(state_t);
                    S_freq = list.size() * 0.01;
                    S_var = Stride.Str_var(list);
                    S_dis = Stride.Str_Distance(S_freq, S_var);
                    SS_dis = Stride.Stride_Dist();
                    sta +=S_dis;
                    list.clear();
                    list.add(Apower);
                    state_c = 1;
                }
            } else {
                ++cnt;
                list.add(Apower);

                state_t = 0;
                S_freq = 0;
                S_var = 0;
                S_dis = 0;
                state_c = 0;
                SS_dis = 0;
            }


            if (state_c == 1) {
                if (S_dis < 0) S_dis = 0.0;
                //Gyro Heading PDR
                heading = Heading.yaw; // Before : Heading.Heading_Dist();

                //Magnet Heading PDR
                headingM = Mag_Heading.Yaw_m; // Before : Mag_Heading.Heading_M_Dist();

                //PDR Positioning
                dx1 = PDR_p[0];
                dy1 = PDR_p[1];
                EKF.P_pri_Update(S_dis,heading);
                if (b != 0) {
                    S_disW = S_dis * b;
                    PDR_W = PDR.PDR_posW(S_disW, heading, PDR_p);

                    dxw = -dx1 + PDR_W[0];
                    dyw = -dy1 + PDR_W[1];

                    PDR_wG = Glob_Pos.Glob_P2(PDR_pG, dxw, dyw);

                }
                PDR_p = PDR.PDR_pos(S_dis, heading, PDR_p);

                a = 0;
                c = 0;

                dx = -dx1 + PDR_p[0];
                dy = -dy1 + PDR_p[1];


                PDR_pG = Glob_Pos.Glob_P(PDR_pG, dx, dy);


            }
            if(S_dis!=0) {


                data += "," + PDR_p[0]; //x
                data += "," + PDR_p[1]; //y

                data += "," + PDR_pG[0] * r2d; //x    Glob
                data += "," + PDR_pG[1] * r2d; //y    Glob

                data += "," + S_dis; //보폭!!
                data += "," + heading; //방위각!!

                EKF.pos_pP[0]=PDR_pG[0];   //Lat
                EKF.pos_pP[1]=PDR_pG[1];   //lon

            }
            else {
                data += ", , , , , ,"  ;
            }
            if (time_s==1) {

                if (G_IO == 0) {  //GPS
                    data += "," + Glob_Pos.GPS_P[0];//Lon
                    data += "," + Glob_Pos.GPS_P[1];//Lat
                    data += "," + G_IO;
                    data += "," + b;
                  //  data += ",GPS";
                }
                else{             //WiFi
                   // data += ", ,";
                    data += "," + Positioning.Pos_F[0];//Lon
                    data += "," + Positioning.Pos_F[1];//Lat
                    data += "," + G_IO;
                    data += "," + b;
                //    data += ",WiFi";
                }
            }

            // WiFi 시간 동기화
            if (WifiScanner.F_P == 1) {
                WifiScanner.F_P = 0;
                a = cnt;
            }
            // GPS 시간 동기
            if (GpsLScanner.G_P == 1) {
                GpsLScanner.G_P = 0;
                c = cnt;
            }


            if (state_c == 1 && b != 0) {
                data += "," + 1; //x    Stride
             //   data += "," + S_disW; //x    Stride
             //   data += "," + heading; //y    Heading

             //   data += "," + System.currentTimeMillis();


             //   data += "," + PDR_wG[0]* r2d; //x   Fingerpoint/PDR Point Glob Lon
             //   data += "," + PDR_wG[1]* r2d; //y   Fingerpoint/PDR Point Glob Lat

             //   data += "," + Positioning.Pos_F[0];//Lon
             //   data += "," + Positioning.Pos_F[1];//Lat

             //   data += "," + Glob_Pos.GPS_P[0];//Lon
             //   data += "," + Glob_Pos.GPS_P[1];//Lat
                if (G_IO == 0) {  //GPS
             //       data += "," + Glob_Pos.GPS_P[0];//Lon
             //       data += "," + Glob_Pos.GPS_P[1];//Lat
             //       data += "," +G_IO;
                //    data += "," +G_VDOP;
                }
                else{             //WiFi
             //       data += "," + Positioning.Pos_F[0];//Lon
             //       data += "," + Positioning.Pos_F[1];//Lat
             //       data += "," +G_IO;
                //    data += "," +G_VDOP;
                }

                EKF.pos_pP[0]=PDR_wG[0];   //Lat   rad  PDR
                EKF.pos_pP[1]=PDR_wG[1];   //lon   rad  PDR

                // measure
                double dz,da,db, dmt ;
                double[] dm=new double[2];
                if (G_IO == 0) {  //GPS
                    EKF.pos_wP[0]=Glob_Pos.GPS_P[0]*d2r; //lat  rad   GPS
                    EKF.pos_wP[1]=Glob_Pos.GPS_P[1]*d2r; //lon  rad   GPS
                    da=(PDR_wG[0]-Glob_Pos.GPS_P[0]*d2r)*Glob_Pos.Rm;                           //rad
                    db=(PDR_wG[1]-Glob_Pos.GPS_P[1]*d2r)*Glob_Pos.Rt*Math.cos(EKF.pos_wP[0]);   //rad
                         r = 0.1 / 6385493.0;  // (GPS 위치 정보 오차 표준 편차,m)/Rt
                }
                else {  //WiFi
                    EKF.pos_wP[0] = Positioning.Pos_F[0] * d2r; //lat  rad   WiFi
                    EKF.pos_wP[1] = Positioning.Pos_F[1] * d2r; //lon  rad   WiFi
                    da=(PDR_wG[0]-Positioning.Pos_F[0]*d2r)*Glob_Pos.Rm;                        //rad
                    db=(PDR_wG[1]-Positioning.Pos_F[1]*d2r)*Glob_Pos.Rt*Math.cos(EKF.pos_wP[0]);//rad
                       r = 0.2 / 6385493.0;  // (WiFi Fingerprinting 오차 표준 편차,m)/Rt
                }
                double[] dx_= new double[4];


                    dz=Math.sqrt((da*da)+(db*db));  //residual

                if(dz>6 && G_IO==1) {
                    r = 10.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                }
                if(dz>10) {
                    r = 10.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                }
                dm[0]=(PDR_pM[0]-EKF.pos_wP[0])*Glob_Pos.Rm;
                dm[1]=(PDR_pM[1]-EKF.pos_wP[1])*Glob_Pos.Rt*Math.cos(EKF.pos_wP[0]);
                dmt=Math.sqrt((dm[0]*dm[0])+(dm[1]*dm[1]));
                dmt=Math.abs((dmt-sta));

                data += "," + dmt;       //동일한 Measure filtering

                if(dmt>5&&dmt<10000) {
                    r = 30.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                }
                PDR_pM[0]=EKF.pos_wP[0];
                PDR_pM[1]=EKF.pos_wP[1];

                sta=0;
                    EKF.P_pri_Update(S_disW,heading);
                    dx_ = EKF.EKF_Update(S_disW, heading);




                PDR_wG[0] = EKF.pos_pP[0];   //Lat   rad
                PDR_wG[1] = EKF.pos_pP[1];   //lon



                   PDR_pG = Glob_Pos.Glob_P(PDR_wG, (-PDR_W[0]+PDR_p[0]), (-PDR_W[1]+PDR_p[1]));

                data += "," + dz;          // residual 확인
                data += "," + r*6385493;   // R값 확인

                //P값 출력
            //    data += "," + EKF.P_post[0][0];//Glob_Pos.Rm;
            //    data += "," + EKF.P_post[1][1];//Glob_Pos.Rt*Math.cos(PDR_pG[0]);
            //    data += "," + EKF.P_post[2][2];
            //    data += "," + EKF.P_post[3][3];//r2d;

            }
            else {
                EKF.P_post=EKF.P_pri;
                data += "," + 0; //x    Stride
            //    data += ",,,,,,,,,,,,,,,,";
                //P값 출력
            //    data += "," + EKF.P_post[0][0];//*Glob_Pos.Rm;
            //    data += "," + EKF.P_post[1][1];//*Glob_Pos.Rt*Math.cos(PDR_pG[0]);
            //    data += "," + EKF.P_post[2][2];
            //    data += "," + EKF.P_post[3][3];//*r2d;
            }
            b = 0;
            S_disW = 0;
        }

        ////////////////////////경일대 추가 부분 종료 (23.09.04)////////////////////////////
        fileStream.fileWrite(data);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

}
