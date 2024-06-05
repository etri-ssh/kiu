package com.example.ccp.scan_module.pdr.dnn;

public class ActivationFunction {

    public static double[] ReLU(double[] input) {
        double[] output = new double[input.length];

        for(int ia = 0; ia < input.length; ia++) {
            if (input[ia] >= 0.0 )
                output[ia] = input[ia];

            else
                output[ia] = 0.0;
        }

        return output;
    }

    public static double[] Softmax(double[] input) {
        double[] output = new double[input.length];
        double denominator = 0.0;

        for(int ib = 0; ib < input.length; ib++) {
            denominator += Math.exp(input[ib]);
        }

        for(int ic = 0; ic < input.length; ic++) {
            output[ic] = Math.exp(input[ic])/denominator;
        }

        return output;
    }
}
