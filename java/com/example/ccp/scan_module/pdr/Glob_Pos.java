package com.example.sensorlog.scanner.sensor;

public class Glob_Pos {
    public static double[] GPS_P = new double[2];
    public static double Rt,Rm;
    public static double[] P_pos = new double[2];
    public static double[] P_posw = new double[2];

    public static void Get_GPS(double Lat,double Lon){
        GPS_P[0]=Lat;
        GPS_P[1]=Lon;

    }

    public static void radicurv(double Lat) {
        double EQUA_RADIUS = 6378137.0;
        double ECCENTRICITY = 0.0818191908426;

        double slat;
        double e2,den;

        slat = Math.sin(Lat);

        e2=ECCENTRICITY*ECCENTRICITY;
        den=1.0-e2*(slat*slat);

        Rm = (EQUA_RADIUS*(1.0-e2))/Math.pow(den,3.0/2.0);
        Rt = EQUA_RADIUS/(Math.sqrt(den));
    }
    public static double[] Glob_P(double[] pos1,double dx,double dy){

        P_pos[0]=(pos1[0]+dy/(Rm));
        P_pos[1]=(pos1[1]+dx/(Rt*Math.cos(P_pos[0])));
        return P_pos;

    }
    public static double[] Glob_P2(double[] pos1,double dx,double dy){

        P_posw[0]=(pos1[0]+dy/(Rm));
        P_posw[1]=(pos1[1]+dx/(Rt*Math.cos(P_pos[0])));
        return P_posw;

    }

}
