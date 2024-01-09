package com.example.sensorlog.scanner.sensor;

public class Heading {

    public static double roll=0;
    public static double pitch=0;
    public static double yaw=0,yaw1=0;
    public static double d2r=180.0/Math.PI;
    public static double r2d=Math.PI/180.0;
    public static double[][] Cbn= new double[3][3];
    public static double[] Qtn= new double[4];
    public static double[] gyroB_hat=new double[3];
    public static double Lat=0,Lon=0,Hgt=0;
    public static double vN=0,vE=0,vD=0;

    public static double yawC;
    public static void Mag_H(double mag_Y) {
        yawC=mag_Y;
    }
    // 초기정렬
    public static void Init_Align(double mA[], double mP[], double mM[],double[] gyro ,double yaw_c) {
        double mfx,mfy,mfz;
        double nom,den;
        double[] WnbB = new double[3];

        mfx = mA[0];
        mfy = mA[1];
        mfz = mA[2];

        //Eular
        roll = Math.atan(mfy/mfz);
        pitch = Math.atan(mfx/Math.sqrt(mfy*mfy+mfz*mfz));



        // WnbB : Bias  제거
        WnbB[0] = gyro[0] - gyroB_hat[0];
        WnbB[1] = gyro[1] - gyroB_hat[1];
        WnbB[2] = gyro[2] - gyroB_hat[2];

      // nom = WnbB[2]*Math.sin(roll)  - WnbB[1]*Math.cos(roll);
      // den = WnbB[0]*Math.cos(pitch) + WnbB[1]*Math.sin(pitch)*Math.sin(roll) + WnbB[2]*Math.sin(pitch)*Math.cos(pitch);
      // yaw = Math.atan2(nom, den);

     // yaw = -95 * d2r;
      yaw=yaw_c;




        Eular2Cbn();
        Eular2Qtn();

        Lat =mP[0];
        Lon =mP[1];
        Hgt =mP[2];

        vN = 0.0;
        vE = 0.0;
        vD = 0.0;

        gyroB_hat[0] = mM[0];
        gyroB_hat[1] = mM[1];
        gyroB_hat[2] = mM[2];

    }

    //오일러 각 Cbn 변환
    public static void Eular2Cbn(){

        double sR,cR, sP,cP,sY,cY;

        sR=Math.sin(roll);
        cR=Math.sin(roll);
        sP=Math.sin(pitch);
        cP=Math.sin(pitch);
        sY=Math.sin(yaw);
        cY=Math.sin(yaw);

        Cbn[0][0] = cR*cY;
        Cbn[0][1] = sR*sP*cY-cR*sY;
        Cbn[0][2] = cR*sP*cY+sR*sY;

        Cbn[1][0] = cP*sY;
        Cbn[1][1] = sR*sP*sY+cR*cY;
        Cbn[1][2] = cR*sP*sY-sR*cY;

        Cbn[2][0] = 0.0-sP;
        Cbn[2][1] = sR*cP;
        Cbn[2][2] = cR*cP;
    }
    //오일러각 쿼터니언 변환
    public static void Eular2Qtn(){
        double sR2, cR2, sP2, cP2, sY2, cY2;

        sR2 = Math.sin(roll  / 2.0);
        cR2 = Math.cos(roll  / 2.0);
        sP2 = Math.sin(pitch / 2.0);
        cP2 = Math.cos(pitch / 2.0);
        sY2 = Math.sin(yaw   / 2.0);
        cY2 = Math.cos(yaw   / 2.0);

        Qtn[0] = cR2*cP2*cY2 + sR2*sP2*sY2;
        Qtn[1] = sR2*cP2*cY2 - cR2*sP2*sY2;
        Qtn[2] = cR2*sP2*cY2 + sR2*cP2*sY2;
        Qtn[3] = cR2*cP2*sY2 - sR2*sP2*cY2;

    }

    // Heading ,Cbn, 쿼터니언 Update
    public static double Attitude_Update(double[] gyro,double dt){

        double[] WnbB = new double[3];
        double WPT, WQT, WRT;
        double TV1, TV2, TV3, TV4;
        double PQT0, PQT1, PQT2, PQT3;
        double qt0, qt1, qt2, qt3, q_normal;
        double U, spsi, cpsi;

        // WnbB : Bias  제거
        WnbB[0] = gyro[0] - gyroB_hat[0];
        WnbB[1] = gyro[1] - gyroB_hat[1];
        WnbB[2] = gyro[2] - gyroB_hat[2];

        // WnbB * dt
        WPT = WnbB[0]*dt;
        WQT = WnbB[1]*dt;
        WRT = WnbB[2]*dt;

        TV1 = Math.sqrt(WPT*WPT + WQT*WQT + WRT*WRT);

        if( TV1 == 0 )  TV3 = 0.5;
        else  TV3 = Math.sin(TV1/2.0)/TV1;

        TV4 = Math.cos(TV1/2.0);

        PQT0 = TV4*Qtn[0] - TV3*(WPT*Qtn[1] + WQT*Qtn[2] + WRT*Qtn[3]);
        PQT1 = TV4*Qtn[1] + TV3*(WPT*Qtn[0] + WRT*Qtn[2] - WQT*Qtn[3]);
        PQT2 = TV4*Qtn[2] + TV3*(WQT*Qtn[0] - WRT*Qtn[1] + WPT*Qtn[3]);
        PQT3 = TV4*Qtn[3] + TV3*(WRT*Qtn[0] + WQT*Qtn[1] - WPT*Qtn[2]);

        TV2 = Math.sqrt(PQT0*PQT0 + PQT1*PQT1 + PQT2*PQT2 + PQT3*PQT3);

        // Quaternion
        qt0 = PQT0 / TV2;
        qt1 = PQT1 / TV2;
        qt2 = PQT2 / TV2;
        qt3 = PQT3 / TV2;

        // Quaternion Normalization
        q_normal = 1.5 - 0.5*(qt0*qt0 + qt1*qt1 + qt2*qt2 + qt3*qt3);

        // New Quaternion
        qt0 *= q_normal;
        qt1 *= q_normal;
        qt2 *= q_normal;
        qt3 *= q_normal;
        Qtn[0] = qt0;
        Qtn[1] = qt1;
        Qtn[2] = qt2;
        Qtn[3] = qt3;

        // Direction Cosine Matrix
        Cbn[0][0] = 1.0 - 2.0*(qt2*qt2 + qt3*qt3);
        Cbn[0][1] = 2.0*(qt1*qt2 - qt0*qt3);
        Cbn[0][2] = 2.0*(qt0*qt2 + qt1*qt3);
        Cbn[1][0] = 2.0*(qt0*qt3 + qt1*qt2);
        Cbn[1][1] = 1.0 - 2.0*(qt1*qt1 + qt3*qt3);
        Cbn[1][2] = 2.0*(qt2*qt3 - qt0*qt1);
        Cbn[2][0] = 2.0*(qt1*qt3 - qt0*qt2);
        Cbn[2][1] = 2.0*(qt0*qt1 + qt2*qt3);
        Cbn[2][2] = 1.0 - 2.0*(qt1*qt1 + qt2*qt2);

        // Euler Angle
        // Roll
        roll = Math.atan2(Cbn[2][1], Cbn[2][2]);
        // Pitch
        U = Math.sqrt(Cbn[1][0]*Cbn[1][0] + Cbn[0][0]*Cbn[0][0]);
        pitch = Math.atan2(0.0 - Cbn[2][0], U);
        // Yaw
        cpsi = Cbn[0][0] / Math.cos(pitch);
        spsi = Cbn[1][0] / Math.cos(pitch);
        yaw = Math.atan2(spsi, cpsi);

        yaw1=yaw*180.0/Math.PI;
        return yaw;
    }

//    Heading 값 출력
//    public static double Heading_Dist(){ return yaw; }



    public static void Qtn2Cbn() {
        double q0s, q1s, q2s, q3s;
        double q01, q02, q03, q12, q13, q23;

        q0s = Qtn[0] * Qtn[0];
        q1s = Qtn[1] * Qtn[1];
        q2s = Qtn[2] * Qtn[2];
        q3s = Qtn[3] * Qtn[3];

        q01 = Qtn[0] * Qtn[1];
        q02 = Qtn[0] * Qtn[2];
        q03 = Qtn[0] * Qtn[3];
        q12 = Qtn[1] * Qtn[2];
        q13 = Qtn[1] * Qtn[3];
        q23 = Qtn[2] * Qtn[3];

        Cbn[0][0] = q0s + q1s - q2s - q3s;
        Cbn[0][1] = 2.0 * (q12 - q03);
        Cbn[0][2] = 2.0 * (q02 + q13);

        Cbn[1][0] = 2.0 * (q03 + q12);
        Cbn[1][1] = q0s - q1s + q2s - q3s;
        Cbn[1][2] = 2.0 * (q23 - q01);

        Cbn[2][0] = 2.0 * (q13 - q02);
        Cbn[2][1] = 2.0 * (q01 + q23);
        Cbn[2][2] = q0s - q1s - q2s + q3s;
    }
}
