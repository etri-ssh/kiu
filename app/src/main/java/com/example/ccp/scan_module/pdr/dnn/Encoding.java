package com.example.ccp.scan_module.pdr.dnn;

import java.util.Arrays;

public class Encoding {

    public static double[] OneHot(double[] input) {
        double[] output = new double[input.length];
        double max = input[0];

        for(int ia = 0; ia < input.length; ia++) {
            if (input[ia] > max )
                max = input[ia];
        }

        for(int ib = 0; ib < output.length; ib++) {
            if (input[ib] == max)
                output[ib] = 1;
            else
                output[ib] = 0;
        }

        return output;
    }
}
