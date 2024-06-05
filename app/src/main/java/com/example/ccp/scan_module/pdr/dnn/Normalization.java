package com.example.ccp.scan_module.pdr.dnn;

import java.util.ArrayList;

public class Normalization {

    private static final double[] min = {
            0.037004482583066,-3.464306161800495,2.393347150933209,
            0.029592888493836,-3.072582118102884,2.005337314783223,
            0.043579831163675,-1.979871034642323,2.345615623956957,
            0.075102971427840,-4.108335038988525,3.975417909526195};

    private static final double[] max = {
            2.561644954175958, 0.39920641954510, 134.6751654191883,
            2.087319441691319, 2.416755224235279, 1123.319632005154,
            1.036972093070778, 0.533933937826158, 278.8352474739081,
            2.697551842424922, 0.659380482201061, 683.7941980445278};

    public static double[] MinMax(double[] Features) {
        double[] normFeatures = new double[Features.length];

        for(int ia = 0; ia < Features.length; ia++) {
            normFeatures[ia] = (Features[ia] - min[ia]) / (max[ia] - min[ia]);
        }

        return normFeatures;
    }
}
