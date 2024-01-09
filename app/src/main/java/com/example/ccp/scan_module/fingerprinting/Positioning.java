package com.example.sensorlog.Fingerprinting;

import com.example.sensorlog.scanner.sensor.Lowpassfilter;

import java.util.ArrayList;
import java.util.Collections;

public class Positioning {
    private static ArrayList<Double> DB_lat = new ArrayList<>();
    private static ArrayList<Double> DB_lon = new ArrayList<>();
    private static ArrayList<Integer> DB_floor = new ArrayList<>();
    private static ArrayList<Integer> DB_num = new ArrayList<>();
    private static ArrayList<String>[] DB_mac = new ArrayList[21];
    private static ArrayList<Double>[] DB_rssi = new ArrayList[21];

    public static double[] Pos_F = new double[3];
    //
    public static double wifi_match,wifi_match1,Rs_min,Rs_max,Rs_mean;
    public static void ReferencePoint(){
        DB_lat = DataBase.Lat;
        DB_lon = DataBase.Lon;
        DB_floor = DataBase.Floor;
        DB_num = DataBase.Num;
        DB_mac = DataBase.MAC;
        DB_rssi = DataBase.RSSI;
    }

    public static double[] kNN(ArrayList<String> mac, ArrayList<Integer> rssi) {
        double[] Pos = {0, 0, 0};
        ReferencePoint();

        final int n = 3;
        final int k = 1;
        int aNN;
        String RP_mac;
        double RP_rssi;
        String MS_mac;
        int MS_rssi;
        ArrayList<Double> Power = new ArrayList<>();
        ArrayList<Integer> idxList = new ArrayList<>();
        int nZmA = 0;
        double tempP;
        int tempI;
        double weightSum = 0;
        double weight;
        double Pos_x;
        double Pos_y;
        double Pos_z;
        //
        ArrayList<String> matchedMSList = new ArrayList<>();
        ArrayList<Integer> matchedRSList = new ArrayList<>();

        int matchedAP = 0;

        for(int ia = 0; ia < DB_mac.length; ia++) {
            double rssiDiff = 1;
            double rssiDist;
            double normDiff;
            int numMatchedAP = 0;

            int RSmin,RSmax,RSmean;

            for (int ib = 0; ib < DB_num.get(ia); ib++) {
                RP_mac = DB_mac[ia].get(ib);
                RP_rssi = DB_rssi[ia].get(ib);

                for (int ic = 0; ic < mac.size(); ic++) {
                    MS_mac = mac.get(ic);
                    MS_rssi = rssi.get(ic);
                    if (RP_mac.equals(MS_mac)) {
                        numMatchedAP = numMatchedAP + 1;
                        rssiDist = Math.pow((Math.abs(RP_rssi - MS_rssi)), 2);
                        rssiDiff = rssiDiff + rssiDist;
                        boolean isContainsMAC = matchedMSList.contains(MS_mac);
                        if (!isContainsMAC) {
                            matchedAP = matchedAP + 1;
                            matchedMSList.add(MS_mac);
                            matchedRSList.add(MS_rssi);
                            Collections.sort(matchedRSList);
                            Rs_min = matchedRSList.get(0);
                            Rs_max = matchedRSList.get(matchedRSList.size()-1);
                            Rs_mean = Lowpassfilter.mean_I(matchedRSList);

                        }
                    }
                }
            }
            rssiDiff = Math.sqrt(rssiDiff);
            normDiff = Math.pow(numMatchedAP, n) / rssiDiff;
            Power.add(normDiff);
            idxList.add(ia);
            if (numMatchedAP != 0) {
                nZmA = nZmA + 1;
            }
            //
            wifi_match=matchedAP;
            wifi_match1=rssiDiff/matchedAP;
        }

        for(int ja = 0; ja < DB_mac.length; ja++) {
            for(int jb = (ja + 1); jb < DB_mac.length; jb++) {
                if(Power.get(ja) < Power.get(jb)){
                    tempP = Power.get(ja);
                    Power.set(ja, Power.get(jb));
                    Power.set(jb, tempP);

                    tempI = idxList.get(ja);
                    idxList.set(ja, idxList.get(jb));
                    idxList.set(jb, tempI);
                }
            }
        }

        if(nZmA > 0) {
            for(int ka = 0; ka < k; ka++) {
                weightSum = weightSum + Power.get(ka);
            }

            for(int kb = 0; kb < k; kb++) {
                aNN = idxList.get(kb);
                Pos_x = DB_lat.get(aNN);
                Pos_y = DB_lon.get(aNN);

                weight = Power.get(kb)/weightSum;
                Pos[0] = Pos[0] + Pos_x * weight;
                Pos[1] = Pos[1] + Pos_y * weight;
            }

            Pos_z = DB_floor.get(idxList.get(0));
            Pos[2] = Pos_z;
        }
        Pos_F=Pos;
        return Pos;
    }
}
