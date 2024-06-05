package com.example.ccp.scan_module.pdr;

import java.util.List;
import java.util.ArrayList;

public class Stride {

    private static double sum = 0;
    private static  double sum2 = 0;
    private static double ave = 0F;
    private static  double var = 0F;
    public static  double stride = 0F;
    private static  double stride_A = 0F;


    // Stride 계산용 a(aH),b(bH),r(cH)
    private static  double aH = 0.0285;
    private static  double bH = -0.0797;
    private static  double cH = 0.6898;
    public static double Str_freq(float freq ) {

        freq = (float) (freq * 0.01);

        return (float)freq;

    }

    //Accel Power Variance(Var)
    public static double Str_var( List<Double> marks ) {
        sum  = 0;
        sum2 = 0;
        //Accel Power mean

        for (Double mark : marks){
            sum += mark;
        }
        ave=sum/marks.size();

        for (Double mark : marks){
            sum2 += (mark-ave)*(mark-ave);
        }
        var=sum2/(marks.size()-1);

        return var;
    }

    //Stride Distance
    public static double Str_Distance(double Sfreq, double Svar ) {
        stride=aH*Svar+bH*Sfreq+cH;
        return stride;
    }

//    public static double Stride_out() { return stride; }

    public static double Stride_Dist(){
        stride_A+=stride;
        return stride_A;
    }

    public static double Stride_reset(){
        return stride_A=0;
    }

}
