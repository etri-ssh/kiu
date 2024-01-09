package com.example.sensorlog.scanner.sensor;

public class PDR {
    public static double[] pos = new double[2];
    public static double[] posM = new double[2];
    public static double[] posW = new double[2];

    //PDR 계산 (Gyro Heading)
    public static double[] PDR_pos(double Stride, double Heading,double[] dpos) {
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

    public static double[] PDR_posW(double StrideW, double HeadingW,double[] dpos) {
        posW[1] =dpos[1]+StrideW*Math.cos(HeadingW);  //Px_i = Px_(i-1) + l * cos(heading)
        posW[0] =dpos[0]+StrideW*Math.sin(HeadingW);  //Py_i = Py_(i-1) + l * sin(heading)

        return posW;
    }
//    public static double[] PDR_Out(){ return pos; }
//    public static double[] PDR_M_Out(){ return posM; }

}
