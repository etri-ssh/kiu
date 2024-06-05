package com.example.ccp.scan_module.pdr;

import static com.example.ccp.module.pos.AndGnssLModule.G_IO;
import static com.example.ccp.module.pos.AndGnssLModule.G_IO2;
import static com.example.ccp.scan_module.pdr.EKF.r;
import static com.example.ccp.module.pos.AndGnssLModule.G_VDOP;

import android.content.Context;

import com.example.ccp.common.Common;
import com.example.ccp.common.FileStream;
import com.example.ccp.module.pos.AndGnssLModule;
import com.example.ccp.scan_module.fingerprinting.Positioning;
import com.example.ccp.scan_module.scanner.WIFIScanner;
import com.example.ccp.scan_module.pdr.dnn.Model;

import java.util.ArrayList;
import java.util.Collections;

public class PDR {

    /** [ETRI] 테스트시간단축을 위해 3000을 1000으로 임시로 줄임 (24.01.22) **/
    //    public static int stay_t = 3000; // Delay count (10ms period). so 3000 means 30000 ms (30s)
    public static int stay_t = 1000;
    public static int time_s = 0;              // 0: before, 1: after ( imu alignment )
    public static int PDR_ON_OFF;              //PDR BUTTON  1 : ON , 0 : OFF
    public static int goTest = 0;       // PDR 시작 여부에 대한 Flag
    public static int DNNA;
    double d2r = Math.PI / 180.0;
    double r2d = 180 / Math.PI;


    public static double[] pos = new double[2];
    public static double[] posM = new double[2];
    public static double[] posW = new double[2];

    private double dx = 0,dy = 0;   // Position x,y축 Local distance, dx=X(i)-X(i-1),dy=Y(i)-Y(i-1)
    private double dxMeas = 0,dyMeas = 0;   // Wi-Fi or GNSS가 measure된 순간까지의 x,y축 Local distance
    private double[] posPdrPre = new double[2];  // 이전 순간의 Local PDR Position

    private static double[] posPdr = new double[2];  // Local PDR Position
    private static double[] posPdrMeas = new double[2];  // GPS or Wi-Fi가 measure된 지점의 Local PDR Position
    private static double[] posMeas = new double[2];     // Measured Position(Wi-Fi or GNSS)
    private static double[] posPdrGlob = new double[2];  // Global PDR Position
    private static double[] posPdrMeasGlob = new double[2];  // posPdrMeas의 Global 좌표

    private int rowCountAcc = 0;
    private int rowCountGyro = 0;
    private int rowCountMag = 0;


    /** [ETRI] PDR 알고리즘 부분을 UncalAcc 에서 PDR.java 파일로 이전작업 (24.01.30) **/
    private FileStream fileStream1;
    private FileStream fileStream2;
    private FileStream fileStream3;


    private double accPBWLPF = 0;  //ButterWorth LPF(Low Pass Filter) 가속도 결과
    // ButterWorth LPF 를 위한 이전 가속도값과 ButterWorth LPF 결과값 저장
    private double A_s = 0;
    private double A_1 = 0;
    private double A_2 = 0;
    private double B_2 = 0;
    private double B_s = 0;
    //
    private double peakCheck = 0;     // peak 검출을 위해 이전 filter acc power와 현재 power를 비교
    private double peakCheckPre = 0;   // 이전 순간의 peakCheck
    private double stepDetect = 0; // 걸음 검출 Flag

    private double state_c=1; //걸음 상태 (걸음 시작, 걸음 중 , 걸음 종료&다음 걸음 시작)
    private int cnt=0;  // 걷는 도중의 데이터 수
    private double stepFreq = 0; // 걸음 주파수
    private double stepVar = 0; //걸음간 가속도 분산값
    private double stepStride=0; // 걸음 Stride
    private double stepStrideTotal = 0; // 걸음 Stride 총합
    private double stepStrideMeasured=0; // Measurement 획득 순간의 Stride
    private double stepStrideMeasuredTotal=0;    // Measurement 획득 순간의 Stride 총합
    private double stepNum = 0;  // 걸음 수 합계

    private static double cntWifiMeasured, cntGnssMeasured;

    /* measurement 획득 시의 cnt와 검출된 걸음걸이의 총 cnt를 비율로
        measurement 획득 순간에 대한 step length의 길이를 계산하기 위한 scale factor */
    private static double sfCntMeasured;


    private double heading=0; //Gyro Heading
    private double headingM=0; //Magnet Heading



    //3축 가속도 배열 및 평균 저장 (LPF, 초기 정렬용)
    private ArrayList<Double> list = new ArrayList<>();
    private ArrayList<Double> accXArrAlign = new ArrayList<>();
    private ArrayList<Double> accYArrAlign = new ArrayList<>();
    private ArrayList<Double> accZArrAlign = new ArrayList<>();
    private double accXMAlign = 0, accYMAlign = 0, accZMAlign=0;     // 평균 x, y, z 가속도


    // 3축 가속도 배열 및 평균 저장 (방위각 계산용)
    private ArrayList<Double> accXArr = new ArrayList<>();
    private ArrayList<Double> accYArr = new ArrayList<>();
    private ArrayList<Double> accZArr = new ArrayList<>();
    private double accXM = 0.0, accYM = 0.0, accZM = 0.0; // acceleration x,y,z mean


    // 가속도 파워 및 3축 가속도 배열 및 평균 저장 (DNN Feature 생성을 위한 가속도 평균 생성용)
    /** @ETRI DNN용 변수 추가 **/
    private ArrayList<Double> accPArrDnn = new ArrayList<>();    // acceleration power array
    private ArrayList<Double> accXArrDnn = new ArrayList<>();    // x-axis acceleration array
    private ArrayList<Double> accYArrDnn = new ArrayList<>();    // y-axis acceleration array
    private ArrayList<Double> accZArrDnn = new ArrayList<>();    // z-axis acceleration array
    private double accPMDnn = 0.0, accXMDnn = 0.0, accYMDnn = 0.0, accZMDnn = 0.0;  // acceleration power, x, y, z mean for DNN

    // 가속도 파워 및 3축 가속도 배열 (DNN Feature 생성을 위한 가속도-평균값(deviation) 생성용 )
    private ArrayList<Double> accPArrDnnDev = new ArrayList<>();    // acceleration power array
    private ArrayList<Double> accXArrDnnDev = new ArrayList<>();    // x-axis acceleration array
    private ArrayList<Double> accYArrDnnDev = new ArrayList<>();    // y-axis acceleration array
    private ArrayList<Double> accZArrDnnDev = new ArrayList<>();    // z-axis acceleration array

    // 가속도 파워 및 3축 가속도 개별 값 및 배열 (DNN Feature 생성을 위한 (가속도-평균)값을 입력으로한 LPF 출력 값)
    private double accPDnnLPF = 0, accXDnnLPF = 0, accYDnnLPF = 0, accZDnnLPF = 0;
    private ArrayList<Double> accPArrDnnLPF = new ArrayList<>();    // acceleration power array
    private ArrayList<Double> accXArrDnnLPF = new ArrayList<>();    // x-axis acceleration array
    private ArrayList<Double> accYArrDnnLPF = new ArrayList<>();    // y-axis acceleration array
    private ArrayList<Double> accZArrDnnLPF = new ArrayList<>();    // z-axis acceleration array

    // 가속도 파워 및 3축 가속도 배열 (걸음에 대한 DNN Feature 생성 부분  )
    private ArrayList<Double> accPArrDnnF = new ArrayList<>();    // acceleration power array
    private ArrayList<Double> accXArrDnnF = new ArrayList<>();    // x-axis acceleration array
    private ArrayList<Double> accYArrDnnF = new ArrayList<>();    // y-axis acceleration array
    private ArrayList<Double> accZArrDnnF = new ArrayList<>();    // z-axis acceleration array

    // EKF 관련 변수
    private double dH = 0.0f;       // EKF에서 계산된 헤딩 오차
    private double dS = 0.0f;       // ??



    ////////////////////// gyro 관련 변수 ///////////////////////
    public  double yaw_C=0;
    public static double[] gyro= new double[3];     //Gyro Signal
    public static double[] mAccel= new double[3];   //3-Axis Accel Mean
    public static double[] Pos_s= new double[3];    //Initial Position
    public static double[] mGyro= new double[3];    //3-Axis Gyro Mean
    private ArrayList<Double> Gyrox = new ArrayList<>();    //x-Axis Gyro
    private ArrayList<Double> Gyroy = new ArrayList<>();    //y-Axis Gyro
    private ArrayList<Double> Gyroz = new ArrayList<>();    //z-Axis Gyro
    private double Yaw=0.0f;                        //Initial Yaw (rad)
    private double Yaw2= 0.0f;                      //Initial Yaw (deg)

    private double d_time1= 0.0f;
    private double dt1= 0.0f;

    private int flagLpfiltered= 1;
    /////////////////////////////////////////////////////////////////


    ////////////////////// mag 관련 변수 ///////////////////////
    private double Ar,Ap;
    private static double[] mag = new double[3];
    private static double[] magM = new double[3];
    private double Roll= 0.0f;                    //Initial Yaw (deg)
    private double Pitch= 0.0f;                    //Initial Yaw (deg)
    private static double bias_M=0.0f;

    private double mMx=0,mMy=0,mMz=0;
    private ArrayList<Double> Magx = new ArrayList<>();
    private ArrayList<Double> Magy = new ArrayList<>();
    private ArrayList<Double> Magz = new ArrayList<>();
    private ArrayList<Double> Mag_H = new ArrayList<>();
    /////////////////////////////////////////////////////////////////


    // 생성자
    public PDR(Context context){

        fileStream1 = new FileStream("PDR");
        fileStream1.fileCreate(Common.PDR_TITLE);

        fileStream2 = new FileStream("gyro");
        fileStream2.fileCreate(Common.GYRO_TITLE);

        fileStream3 = new FileStream("mag");
        fileStream3.fileCreate(Common.MAG_TITLE);

    }// public PDR()


    //PDRactivity에서만 PDR, IMU 로그파일이 저장되도록 우선 설정(PDR 테스트시에는 PDRactivity만 사용하고 있음)
    public void close(){
        fileStream1.fileClose();
        fileStream2.fileClose();
        fileStream3.fileClose();
    }



    //PDR 계산 (Gyro Heading)
    public double[] PDR_pos(double Stride, double Heading,double[] dpos) {

        pos[1] =dpos[1]+Stride*Math.cos(Heading);  //Px_i = Px_(i-1) + l * cos(heading)
        pos[0] =dpos[0]+Stride*Math.sin(Heading);  //Py_i = Py_(i-1) + l * sin(heading)

        return pos;
    }

    //PDR 계산 (Magnet Heading)
    public static double[] PDR_posM(double StrideM, double HeadingM,double[] dposm) {
        posM[0] =dposm[0]+StrideM*Math.cos(HeadingM);
        posM[1] =dposm[1]+StrideM*Math.sin(HeadingM);

        return posM;
    }


    public void PdrAccOperation(float[] accVal){

        // 값이 없는 경우 예외처리
        if (accVal == null){
            return;
        }

        double[] Fe = new double[12];       // ??
        double[] FeN = new double[12];      // ??
        double dnnOutput = 0;
        // double[] Pos = {0, 0};

        //일단 부터 맞추기
        String data = "\n" + (++rowCountAcc) + "";
        data += "," + accVal[0];
        data += "," + accVal[1];
        data += "," + accVal[2];
        data += "," + accVal[3];

        // Power 값 생성
        double accX, accY, accZ, accP, Apow, cntw = 0;
        // double step;
        double[][] Fa;

        double dz=0, da, db, dmt = 0;
        double[] dm = new double[2];
        double[] dx_ = new double[4];

        // NED frame
        accX = accVal[2];
        accY = accVal[1];
        accZ = -accVal[3];

        Apow = Math.pow(accX, 2) + Math.pow(accY, 2) + Math.pow(accZ, 2);
        accP = Math.sqrt(Apow) - 9.81;  //센서 데이터 => Power-(중력가속도)

        // 이전 Power 값 저장
        Mag_Heading.Accel_Attit(accXM, accYM, accZM);

        if (rowCountAcc == 1) {
            A_s = accP;
            A_1 = A_s;
            A_2 = A_s;
            //    EKF.header_EKF(36*d2r);
        } else if (rowCountAcc == 2) {
            A_1 = A_s;
            A_2 = A_s;
            A_s = accP;
            B_2 = 0;
            B_s = accPBWLPF;
        }
        else if(rowCountAcc>=3) {
            A_2 = A_1;
            A_1 = A_s;
            A_s = accP;
            B_2 = B_s;
            B_s = accPBWLPF;
        }

        //Butterworth LPF
        accPBWLPF = Lowpassfilter.BWlowPass(accP, accPBWLPF, A_1, A_2, B_2); //2nd order ButterWorth LPF(필터)

        // Alpf = Lowpassfilter.lowPass(Alpf, Alpf);  //LPF(필터)

        data += "," + accP;
        data += "," + accPBWLPF;
        data += "," + (stepNum); //걸음 수!!

        if(PDR_ON_OFF==1){
            if (G_IO2 < 0.6) {
                if (Glob_Pos.GPS_P[0] != 0) {
                    if (posPdr[0] == 0) {
                        posPdrGlob[0] = Glob_Pos.GPS_P[0] * d2r;    //초기 GPS 위치 Lat
                        posPdrGlob[1] = Glob_Pos.GPS_P[1] * d2r;    //초기 GPS 위치 Lon
                        Glob_Pos.radicurv(posPdrGlob[0]);
                        EKF.header_EKF(posPdrGlob[0]);
                    }
                }
            }
            else {
                if (Positioning.Pos_F[0] != 0) {
                    if (posPdr[0] == 0) {
                        if (time_s == 1) {
                            posPdrGlob[0] = WIFIScanner.Init_P[0] * d2r;    //초기 Wi-Fi 위치 Lat
                            posPdrGlob[1] = WIFIScanner.Init_P[1] * d2r;    //초기 Wi-Fi 위치 Lon
                            Glob_Pos.radicurv(posPdrGlob[0]);
                            EKF.header_EKF(posPdrGlob[0]);
                        }
                    }
                }
            }// else
        }// if(PDR_ON_OFF==1)


        // 초기 정렬시의  평균 가속도 생성
        if (rowCountAcc < stay_t)
        {
            accXArrAlign.add(accX);
            accYArrAlign.add(accY);
            accZArrAlign.add(accZ);
        } else if (rowCountAcc == stay_t) {
            accXMAlign=Lowpassfilter.mean_D(accXArrAlign);
            accYMAlign=Lowpassfilter.mean_D(accYArrAlign);
            accZMAlign=Lowpassfilter.mean_D(accZArrAlign);
            Lowpassfilter.mean_A(accXMAlign, accYMAlign, accZMAlign);
        }


        // 걸음 검출을 위한 peak, step 검출
        if(PDR_ON_OFF==1){        //PDR On : Initial Alignment 후 버튼을 누르면 PDR 걸음 검출
            if( goTest == 1){
                peakCheckPre=peakCheck;
                peakCheck= DetectPeak.peak_P(accPBWLPF,B_s,peakCheck); /** [ETRI] 함수 검토 필요 **/
                stepDetect = (int) DetectPeak.DetectStep(peakCheck,peakCheckPre,accPBWLPF, cnt);
                stepNum = DetectPeak.StepCount();
            }   // if( goTest == 1)
        } else {
            cnt=0;
        }
        data += "," + stepDetect; // 걸음 지점!


        // 자세 계산을 위한 가속도값 처리
        accXArr.add(accX);
        accYArr.add(accY);
        accZArr.add(accZ);

        /** [ETRI] 아래의 내용 정리 필요 (24.01.22) **/
        if (rowCountAcc > 99) {
            // SWA (sliding window average)
            accXM = Lowpassfilter.mean_D(accXArr);
            accYM = Lowpassfilter.mean_D(accYArr);
            accZM = Lowpassfilter.mean_D(accZArr);
            accXArr.remove(accXArr.size() - 100);
            accYArr.remove(accYArr.size() - 100);
            accZArr.remove(accZArr.size() - 100);
        }

        //PDR 시작
        if(PDR_ON_OFF==1) {

            if( goTest == 0){
                //DNN Feature를 위한 가속도 raw 값 저장
                accPArrDnn.add(accP);
                accXArrDnn.add(accX);
                accYArrDnn.add(accY);
                accZArrDnn.add(accZ);

            } else if ( goTest == 1 ){

                //DNN Feature를 위한 가속도 평균 저장
                accPMDnn = Lowpassfilter.mean_D(accPArrDnn);
                accXMDnn = Lowpassfilter.mean_D(accXArrDnn);
                accYMDnn = Lowpassfilter.mean_D(accYArrDnn);
                accZMDnn = Lowpassfilter.mean_D(accZArrDnn);

                double temp_accPArrDnnDev = accP - accPMDnn;
                double temp_accXArrDnnDev = accX - accXMDnn;
                double temp_accYArrDnnDev = accY - accYMDnn;
                double temp_accZArrDnnDev = accZ - accZMDnn;

                accPArrDnnDev.add(temp_accPArrDnnDev);
                accXArrDnnDev.add(temp_accXArrDnnDev);
                accYArrDnnDev.add(temp_accYArrDnnDev);
                accZArrDnnDev.add(temp_accZArrDnnDev);

                if( accPArrDnnDev.size() > 2 ){

                    //DNN Feature을 위한 가속도 BW LPF값 저장
                    accPDnnLPF = Lowpassfilter.BWlowPass2(temp_accPArrDnnDev, accPDnnLPF, accPArrDnnDev.get(accPArrDnnDev.size() - 2),
                            accPArrDnnDev.get(accPArrDnnDev.size() - 3),accPArrDnnLPF.get(accPArrDnnLPF.size() - 2)); //2nd order ButterWorth LPF(필터)
                    accXDnnLPF = Lowpassfilter.BWlowPass2(temp_accXArrDnnDev, accXDnnLPF, accXArrDnnDev.get(accXArrDnnDev.size() - 2),
                            accXArrDnnDev.get(accXArrDnnDev.size() - 3),accXArrDnnLPF.get(accXArrDnnLPF.size() - 2)); //2nd order ButterWorth LPF(필터)
                    accYDnnLPF = Lowpassfilter.BWlowPass2(temp_accYArrDnnDev, accYDnnLPF, accYArrDnnDev.get(accYArrDnnDev.size() - 2),
                            accYArrDnnDev.get(accYArrDnnDev.size() - 3),accYArrDnnLPF.get(accYArrDnnLPF.size() - 2)); //2nd order ButterWorth LPF(필터)
                    accZDnnLPF = Lowpassfilter.BWlowPass2(temp_accZArrDnnDev, accZDnnLPF, accZArrDnnDev.get(accZArrDnnDev.size() - 2),
                            accZArrDnnDev.get(accZArrDnnDev.size() - 3),accZArrDnnLPF.get(accZArrDnnLPF.size() - 2)); //2nd order ButterWorth LPF(필터)
                } else {
                    accPDnnLPF = temp_accPArrDnnDev;
                    accXDnnLPF = temp_accXArrDnnDev;
                    accYDnnLPF = temp_accYArrDnnDev;
                    accZDnnLPF = temp_accZArrDnnDev;
                }
                accPArrDnnLPF.add(accPDnnLPF);
                accXArrDnnLPF.add(accXDnnLPF);
                accYArrDnnLPF.add(accYDnnLPF);
                accZArrDnnLPF.add(accZDnnLPF);


                //걸음 감지 부분 시작
                if (stepDetect == 1) { //걸음 감지!
                    if (state_c == 1) {  //걸음 시작
                        ++cnt;
                        list.add(accP);

                        accPArrDnnF.add(accPDnnLPF);
                        accXArrDnnF.add(accXDnnLPF);
                        accYArrDnnF.add(accYDnnLPF);
                        accZArrDnnF.add(accZDnnLPF);

                        state_c = 0;
                        stepFreq = 0;
                        stepVar = 0;
                        stepStride = 0;
                        stepStrideTotal = 0;
                    } else {//걸음 끝
                        cntw = cnt;

                        if (G_IO == 0) {  //GPS
                            sfCntMeasured = cntGnssMeasured / cnt;
                        }
                        else{             //WiFi
                            sfCntMeasured = cntWifiMeasured / cnt;
                        }

                        cnt = 0;
                        ++cnt;
                        list.add(accP);

                        accPArrDnnF.add(accPDnnLPF);
                        accXArrDnnF.add(accXDnnLPF);
                        accYArrDnnF.add(accYDnnLPF);
                        accZArrDnnF.add(accZDnnLPF);

                        stepFreq = list.size() * 0.01;
                        stepVar = Stride.Str_var(list);
                        stepStride = Stride.Str_Distance(stepFreq, stepVar);
                        stepStrideTotal = Stride.Stride_Dist();
                        stepStrideMeasuredTotal += stepStride;
                        
                        //DNN Feature 생성
                        Fe[0] = Math.sqrt(Stride.Str_var(accPArrDnnF));  //걸음 구간 가속도 power 표준편차
                        Fe[2] = Lowpassfilter.sum_D(accPArrDnnF);        //걸음 구간 가속도 power 합
                        Collections.sort(accPArrDnnF);
                        Fe[1] = accPArrDnnF.get(0);                      //걸음 구간 가속도 power 최소값

                        Fe[3] = Math.sqrt(Stride.Str_var(accXArrDnnF));   //걸음 구간 가속도 x 표준편차
                        Fe[5] = Lowpassfilter.mean_D(accXArrDnnF);        //걸음 구간 가속도 x 합
                        Collections.sort(accXArrDnnF);
                        Fe[4] = accXArrDnnF.get(0);                       //걸음 구간 가속도 x 최소값

                        Fe[6] = Math.sqrt(Stride.Str_var(accYArrDnnF));   //걸음 구간 가속도 y 표준편차
                        Fe[8] = Lowpassfilter.mean_D(accYArrDnnF);        //걸음 구간 가속도 y 합
                        Collections.sort(accYArrDnnF);
                        Fe[7] = accYArrDnnF.get(0);                       //걸음 구간 가속도 y 최소값

                        Fe[9] = Math.sqrt(Stride.Str_var(accZArrDnnF));   //걸음 구간 가속도 z 표준편차
                        Fe[11] = Lowpassfilter.mean_D(accZArrDnnF);       //걸음 구간 가속도 z 합
                        Collections.sort(accZArrDnnF);
                        Fe[10] = accZArrDnnF.get(0);                          //걸음 구간 가속도 z 최소값

                        dnnOutput = Model.DNN(Fe);                  //DNN Feature 입력
                        DNNA = (int) dnnOutput;                      //DNN 결과값 출력

                        // DNN 결과에 의해 비보행 신호 필터링
                        if (dnnOutput == 0){
                            stepStride=0.00001f;
                        }

                        accPArrDnnF.clear();
                        accXArrDnnF.clear();
                        accYArrDnnF.clear();
                        accZArrDnnF.clear();

                        list.clear();
                        list.add(accP);
                        state_c = 1;

                    }   // if (state_c == 1) {  //걸음 시작
                } else {        // 결음이 감지 되기 전
                    ++cnt;
                    list.add(accP);

                    accPArrDnnF.add(accPDnnLPF);
                    accXArrDnnF.add(accXDnnLPF);
                    accYArrDnnF.add(accYDnnLPF);
                    accZArrDnnF.add(accZDnnLPF);

                    stepFreq = 0;
                    stepVar = 0;
                    stepStride = 0;
                    state_c = 0;
                    stepStrideTotal = 0;
                } // if (stepDetect == 1) { //걸음 감지!


                if (state_c == 1) {
                    if (stepStride < 0) stepStride = 0.000001;
                    //Gyro Heading PDR
                    heading = Heading.yaw - dH; // Before : Heading.Heading_Dist(); (dH : EKF로 계산된 Heading 오차)

                    //Magnet Heading PDR
                    headingM = Mag_Heading.Yaw_m; // Before : Mag_Heading.Heading_M_Dist();

                    //PDR Positioning
                    posPdrPre[0] = posPdr[0];
                    posPdrPre[1] = posPdr[1];

                    // PDR 위치 갱신
                    posPdr = PDR_pos(stepStride, heading, posPdr);

                    cntWifiMeasured = 0;
                    cntGnssMeasured = 0;

                    dx = posPdr[0] - posPdrPre[0];
                    dy = posPdr[1] - posPdrPre[1];

                    posPdrMeasGlob = Glob_Pos.Glob_P(posPdrGlob, dx, dy);


                    if (sfCntMeasured != 0) {
                        /** [ETRI] Step이 감지되는 동안에 획득된 measurement의 획득지점까지의
                         *  스텝길이를 계산해서 측정치가 측정된 순간의 위치를 계산하기 위함
                         *  이를 위해 1개의 step 길이인 S_dis에 scale factor b를 곱함
                         */
                        stepStrideMeasured = stepStride * sfCntMeasured;
                        EKF.P_pri_Update(stepStride, heading);
                        posPdrMeas = PDR_pos(stepStrideMeasured, heading, posPdr);

                        dxMeas = posPdrMeas[0] - posPdrPre[0];
                        dyMeas = posPdrMeas[1] - posPdrPre[1];

                        posPdrMeasGlob = Glob_Pos.Glob_P2(posPdrGlob, dxMeas, dyMeas);  //Fingerprint updata 시점에서의 PDR 위치 정보  WGS84로 변환

                        EKF.pos_pP[0] = posPdrMeasGlob[0];   //Lat   rad  PDR
                        EKF.pos_pP[1] = posPdrMeasGlob[1];   //lon   rad  PDR
                        // measure 선택
                        if (G_IO == 0) {  //GPS
                            EKF.pos_wP[0] = Glob_Pos.GPS_P[0] * d2r; //lat  rad   GPS
                            EKF.pos_wP[1] = Glob_Pos.GPS_P[1] * d2r; //lon  rad   GPS
                            da = (posPdrMeasGlob[0] - Glob_Pos.GPS_P[0] * d2r) * Glob_Pos.Rm;                           //rad
                            db = (posPdrMeasGlob[1] - Glob_Pos.GPS_P[1] * d2r) * Glob_Pos.Rt * Math.cos(EKF.pos_wP[0]);   //rad
                            r = 0.1 / 6385493.0;  // (GPS 위치 정보 오차 표준 편차,m)/Rt
                        }
                        else {  //WiFi
                            EKF.pos_wP[0] = Positioning.Pos_F[0] * d2r; //lat  rad   WiFi
                            EKF.pos_wP[1] = Positioning.Pos_F[1] * d2r; //lon  rad   WiFi
                            da = (posPdrMeasGlob[0] - Positioning.Pos_F[0] * d2r) * Glob_Pos.Rm;                        //rad
                            db = (posPdrMeasGlob[1] - Positioning.Pos_F[1] * d2r) * Glob_Pos.Rt * Math.cos(EKF.pos_wP[0]);//rad
                            r = 0.2 / 6385493.0;  // (WiFi Fingerprinting 오차 표준 편차,m)/Rt
                        }


                        dz = ((da * da) + (db * db));  //residual

                        if (dz > 100 && G_IO == 1) {
                            r = 10.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                        }
                        //       if(dz>7) {
                        //        //   r = 10.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                        //       }
                        dm[0] = (posMeas[0] - EKF.pos_wP[0]) * Glob_Pos.Rm;
                        dm[1] = (posMeas[1] - EKF.pos_wP[1]) * Glob_Pos.Rt * Math.cos(EKF.pos_wP[0]);

                        dmt = Math.sqrt((dm[0] * dm[0]) + (dm[1] * dm[1]));
                        dmt = Math.abs((dmt - stepStrideMeasuredTotal));
                        if (dmt > 8 && dmt < 10000) {
                            r = 30.0 / 6385493.0;  // (예외 처리 오차 표준 편차)/Rt
                        }
                        posMeas[0] = EKF.pos_wP[0];  //Measurement Lat
                        posMeas[1] = EKF.pos_wP[1];  //Measurement Lon

                        stepStrideMeasuredTotal = 0;
                        //      EKF.P_pri_Update(S_disW, heading);
                        dx_ = EKF.EKF_Update(stepStrideMeasured, heading);  //EKF 계산된 오차 정보

                        dS = dx_[2];
                        dH = dx_[3];


                        posPdrMeasGlob[0] = EKF.pos_pP[0];   //EKF Updata Lat   rad
                        posPdrMeasGlob[1] = EKF.pos_pP[1];   //EKF Updata lon   rad

                        posPdrGlob = Glob_Pos.Glob_P(posPdrMeasGlob, (posPdr[0] - posPdrMeas[0]), (posPdr[1] - posPdrMeas[1]));  //EKF에 의해 보정된 PDR 위치(Global)

                    } else {

                        EKF.P_pri_Update(stepStride, heading);
                        EKF.P_post = EKF.P_pri;

                    }

                }

                if(stepDetect!=0) {

                    data += "," + stepStride; //보폭!!
                    data += "," + heading; //보폭!!

                    data += "," + posPdr[0]; //x
                    data += "," + posPdr[1]; //y

                    data += "," + posPdrGlob[0] * r2d; //x    Glob
                    data += "," + posPdrGlob[1] * r2d; //y    Glob

                    data += "," + dnnOutput; //DNN 결과


                } else {
                    data += ",,,,,,,,,";
                }

                if (time_s==1) {

                    if (G_IO == 0) {  //GPS
                        data += "," + Glob_Pos.GPS_P[0];//Lon
                        data += "," + Glob_Pos.GPS_P[1];//Lat
                        data += "," + G_IO;
                        data += "," + sfCntMeasured;
                        //  data += ",GPS";
                    } else {             //WiFi
                        // data += ", ,";
                        data += "," + Positioning.Pos_F[0];//Lon
                        data += "," + Positioning.Pos_F[1];//Lat
                        data += "," + G_IO;
                        data += "," + sfCntMeasured;
                        //    data += ",WiFi";
                    }
                }

                // WiFi 시간 동기화
                if (WIFIScanner.F_P == 1) {
                    WIFIScanner.F_P = 0;
                    cntWifiMeasured = cnt;
                }
                // GPS 시간 동기
                if (AndGnssLModule.G_P == 1) {
                    AndGnssLModule.G_P = 0;
                    cntGnssMeasured = cnt;
                }


                if (state_c == 1 && sfCntMeasured != 0) {
                    data += "," + 1; //EKF 사용 여부 확인 (사용 o)
                    //        data += "," + S_disW; //x    Stride
                    //        data += "," + heading; //y    Heading
                    //       data += "," + dH; //y    Heading

                    data += "," + System.currentTimeMillis(); //Timestamp

                    data += "," + posPdrMeasGlob[0] * r2d; //measurement가 들어온 시점에서의 PDR 위치 (Global) Lat
                    data += "," + posPdrMeasGlob[1] * r2d; //measurement가 들어온 시점에서의 PDR 위치 (Global) Lon

                    data += "," + Positioning.Pos_F[0];//WiFi Lat
                    data += "," + Positioning.Pos_F[1];//WiFi Lon

                    data += "," + Glob_Pos.GPS_P[0];//GPS Lat
                    data += "," + Glob_Pos.GPS_P[1];//GPS Lon

                    // 선택된 Measurement
                    if (G_IO == 0) {  //GPS
                        data += "," + Glob_Pos.GPS_P[0];//Lat
                        data += "," + Glob_Pos.GPS_P[1];//Lon
                        data += "," + G_IO;
                        data += "," + G_VDOP;
                        //     data += ",GPS";
                    }
                    else {             //WiFi
                        data += "," + Positioning.Pos_F[0];//Lat
                        data += "," + Positioning.Pos_F[1];//Lon
                        data += "," + G_IO;
                        data += "," + G_VDOP;
                        //     data += ",WiFi";
                    }

                    data += "," + dmt;       //동일한 Measure filtering
                    data += "," + dz;          // residual 확인
                    data += "," + r * 6385493;   // R값 확인

                    //P값 출력
                    data += "," + EKF.P_post[0][0];//Glob_Pos.Rm;
                    data += "," + EKF.P_post[1][1];//Glob_Pos.Rt*Math.cos(PDR_pG[0]);
                    data += "," + EKF.P_post[2][2];
                    data += "," + EKF.P_post[3][3];//r2d;

                    // EKF 보정된 PDR 위치 결과
                    data += "," + posPdrGlob[0] * r2d; //lat    Glob
                    data += "," + posPdrGlob[1] * r2d; //lon    Glob

                    // EKF 보정된 measurement가 측정된 지점에서의 PDR 위치 결과
                    data += "," + EKF.pos_wP[0] * r2d; //lat
                    data += "," + EKF.pos_wP[1] * r2d; //lon

                    // EKF로 계산된 오찰 정보
                    data += "," + dx_[0]; //Lat
                    data += "," + dx_[1]; //Lon
                    data += "," + dx_[2]; //Heading
                    data += "," + dx_[3]; //Stride

                }
                //EKF 사용 x
                else {
                    //       EKF.P_post = EKF.P_pri;
                    data += "," + 0; //EKF 사용 여부 확인 (사용 x)
                    data += ",,,,,,,,,,,,,,";
                    //P값 출력
                    data += "," + EKF.P_post[0][0];//*Glob_Pos.Rm;
                    data += "," + EKF.P_post[1][1];//*Glob_Pos.Rt*Math.cos(PDR_pG[0]);
                    data += "," + EKF.P_post[2][2];// Stride(m)
                    data += "," + EKF.P_post[3][3];//*r2d;

                    data += "," + posPdrGlob[0] * r2d; //lat    Glob
                    data += "," + posPdrGlob[1] * r2d; //lon    Glob
                }
                sfCntMeasured = 0;
                stepStrideMeasured = 0;
            }
        }

        fileStream1.fileWrite(data);

    }//public void PdrAccOperation()



    public void pdrGyroOperation(float[] gyroVal){

        // 값이 없는 경우 예외처리
        if (gyroVal == null){
            return;
        }

        double Gpow;

        String data = "\n" + (++rowCountGyro) + "";
        data += "," + gyroVal[0];       // utc
        data += "," + gyroVal[1];
        data += "," + gyroVal[2];
        data += "," + gyroVal[3];


        /**센서 데이터 사이의 정확한 시간 생성*/
        if(rowCountGyro == 1){
            dt1 = Common.SENSOR_DELAY / 1000000;    // us to sec
        }else{
            dt1=( System.currentTimeMillis() - d_time1 )/1000; /** ms => sec   **/
        }
        d_time1 = System.currentTimeMillis();

        // NED frame
        gyro[0]=gyroVal[2];
        gyro[1]=gyroVal[1];
        gyro[2]=-gyroVal[3];

        // Power 값 생성
        Gpow=Math.pow(gyro[0],2) +Math.pow(gyro[1],2) +Math.pow(gyro[2],2);    //Generate Gyro Power

        yaw_C = Heading.yawC;
        data += "," + Gpow;
        // Initial Alignment

        if (rowCountGyro<stay_t) {
            Gyrox.add(gyro[0]);
            Gyroy.add(gyro[1]);
            Gyroz.add(gyro[2]);

            data += ",,";  // Heading (deg) ????
        }else if(rowCountGyro>=stay_t && yaw_C!=0 && flagLpfiltered==1) {
            mGyro[0]=Lowpassfilter.mean_D(Gyrox);
            mGyro[1]=Lowpassfilter.mean_D(Gyroy);
            mGyro[2]=Lowpassfilter.mean_D(Gyroz);

            mAccel = Lowpassfilter.meanA; //Lowpassfilter.out_mA();
            //    yaw_C=-95*(Math.PI/180.0); //직접 초기 방위각 설정
            Heading.Init_Align( mAccel, Pos_s, mGyro,gyro,yaw_C);
            data += ",,";
            flagLpfiltered=0;
        }else {
            Yaw =Heading.Attitude_Update(gyro,dt1);
            Yaw2=Yaw*180.0/Math.PI;
            data += "," +Yaw;   // Heading (rad)
            data += "," +Yaw2;  // Heading (deg)
        }

        data += "," + flagLpfiltered;    // 초기 lowpass filter를 거쳤는지 확인하는 단계

        fileStream2.fileWrite(data);

    } //public void pdrGyroOperation()



    public void pdrMagOperation(float[] magVal){

        // 값이 없는 경우 예외처리
        if (magVal == null){
            return;
        }

        double mx,my,mz,Yaw_m,Yaw_m1;

        double Yaw_c=0.0;

        String data = "\n" + (++rowCountMag) + "";
        data += "," + magVal[0];        // utc
        data += "," + magVal[1];
        data += "," + magVal[2];
        data += "," + magVal[3];

        // 무슨 용도??
        Ar = Mag_Heading.Out_Aroll();
        Ap = Mag_Heading.Out_Apitch();

        // NED frame
        mx = magVal[2];
        my = magVal[1];
        mz = -magVal[3];

        Mag_Heading.In_mag(mx,my,mz);

        mag=Mag_Heading.Mag_calibration(mx,my,mz);

        bias_M=0;

        Yaw_m=Mag_Heading.Mag_Attitude(mag); //실시간 방위각 계산

        Yaw_m1=Yaw_m*r2d;

        if (rowCountMag < stay_t)
        {
            Magx.add(mag[0]);
            Magy.add(mag[1]);
            Magz.add(mag[2]);
        }
        if(rowCountMag == stay_t){

            magM[0] = Lowpassfilter.mean_D(Magx);
            magM[1] = Lowpassfilter.mean_D(Magy);
            magM[2] = Lowpassfilter.mean_D(Magz);

            Yaw_c=Mag_Heading.Mag_Attitude(magM);
            Heading.Mag_H(Yaw_c);   //지자기 센서를 통한 초기 방위각
        }

        Roll=Mag_Heading.Out_Aroll();
        Pitch=Mag_Heading.Out_Apitch();

        data += "," +Roll;
        data += "," +Pitch;
        data += "," + Yaw_m;            //Mag Heading (Rad)
        data += "," + Yaw_m1;           //Mag Heading (Deg)

        fileStream3.fileWrite(data);

    }   // public void pdrMagOperation()

}
