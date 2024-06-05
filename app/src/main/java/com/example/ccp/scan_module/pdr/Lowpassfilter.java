package com.example.ccp.scan_module.pdr;

import android.content.Intent;

import java.util.List;

public class Lowpassfilter {
    private static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    // if ALPHA = 1 OR 0, no filter applies.\
    private static final float fc = 2.0f;
    private static final float wc = (float) (2*Math.PI*fc/100); // if ALPHA = 1 OR 0, no filter applies.
    private static final float ALPHA_B = (float) Math.tan((wc/2));
    private static final float fc2 = 3.0f;
    private static final float wc2 = (float) (2*Math.PI*fc2/150); // if ALPHA = 1 OR 0, no filter applies.

    private static final float ALPHA_B2 = (float) Math.tan((wc2/2));
    private static double sum = 0;
    private static double ave = 0;
    public static double[] meanA = new double[3];

    public static double lowPass(double input, double output ) {
        if ( output == 0 ) return input;
        output = output + ALPHA * (input - output);
        return output;
    }
    public static double BWlowPass(double input, double output,double in1, double in2, double out2) {
        double a0,b0,b1,b2,a1,a2;
        //float ALPHA_B = (float) Math.tan((wc/2));

        a0 = (ALPHA_B*ALPHA_B)+Math.sqrt(2)*ALPHA_B + 1;
        b0 = (ALPHA_B*ALPHA_B)/a0;
        b1 = 2*(ALPHA_B*ALPHA_B)/ a0;
        b2 = (ALPHA_B*ALPHA_B)/a0;
        a1 = 2*((ALPHA_B*ALPHA_B) - 1)/a0;
        a2 = ((ALPHA_B*ALPHA_B) - Math.sqrt(2)*ALPHA_B + 1)/a0;
        //if ( output == 0 ) return input;
        output = b0*input + b1*in1 + b2*in2 - a1*output - a2*out2;

        return output;
    }

    public static double BWlowPass2(double input, double output,double in1, double in2, double out2) {
        double a0,b0,b1,b2,a1,a2;
        //float ALPHA_B = (float) Math.tan((wc/2));

        a0 = (ALPHA_B2*ALPHA_B2)+Math.sqrt(2)*ALPHA_B2 + 1;
        b0 = (ALPHA_B2*ALPHA_B2)/a0;
        b1 = 2*(ALPHA_B2*ALPHA_B2)/ a0;
        b2 = (ALPHA_B2*ALPHA_B2)/a0;
        a1 = 2*((ALPHA_B2*ALPHA_B2) - 1)/a0;
        a2 = ((ALPHA_B2*ALPHA_B2) - Math.sqrt(2)*ALPHA_B2 + 1)/a0;
        //if ( output == 0 ) return input;
        output = b0*input + b1*in1 + b2*in2 - a1*output - a2*out2;

        return output;
    }



    public static double BWlowPass1(double input, double output,double in1){
        float a0,b0,b1,a1;
        a0 = ALPHA_B+1;
        b0 = ALPHA_B/a0;
        b1 = b0;
        a1 = (ALPHA_B - 1)/a0;

        output = b0*input + b1*in1 - a1*output;
        return output;
    }
    public static double mean_D( List<Double> marks ) {
        sum = 0;
        ave = 0;
        for (Double mark : marks) {
            sum += mark;
        }
         ave=sum/marks.size();
        return ave;
    }

    public static double sum_D( List<Double> marks ) {
        sum = 0;
        //    for (Double mark : marks) {
        //        sum += Math.abs(mark);
        //    }
        for (int i=0;i< marks.size(); i++) {
            sum += Math.abs(marks.get(i));
        }
        return sum;
    }

    public static double mean_I( List<Integer> marks ) {
        sum = 0;
        ave = 0;
        for (Integer mark : marks) {
            sum += mark;
        }
        ave=sum/marks.size();
        return ave;
    }
    public static void mean_A(double mAx,double mAy,double mAz) {
        meanA[0] =mAx;
        meanA[1] =mAy;
        meanA[2] =mAz;
    }
}
