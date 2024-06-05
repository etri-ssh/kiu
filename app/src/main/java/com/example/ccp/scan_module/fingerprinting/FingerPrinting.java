package com.example.ccp.scan_module.fingerprinting;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/** FingerPrinting 알고리즘
 *
 * 1. 수집된 Wifi list로 구성된 FDB를 init
 * 2. wifi scanner를 통해 검색되는 wifi를 검사
 * 3. k-최근접 이웃 알고리즘을 통해 위치 특정
 *
 * [수정 & 문의]
 * 1. 헬리오센 신호기반을 통해서 위치를 특정하기에 현재 V2앱에선 굳이 필요없는 기능
 *    FDB통해서 결과값을 특정하기에 헬리오센 서버에서 돌아야하는 구조
 *
 * 2. FDB를 가져다 쓰면서 기존 Positioning에서 [100] -> [21]와 같이 잘라서 씀 이유가 있나?
 * 3. WIFI scanner에서 scanList.foreach안에 굳이 썻는데 정작 mac과 rssi는 넘겨져있지않아서
 *    초기값은 의미가 없어 보이는데 특정이유가 있나?
 */


public class FingerPrinting {

    public static final ArrayList<Double> lat = new ArrayList<>();
    public static final ArrayList<Double> lon = new ArrayList<>();
    public static final ArrayList<Integer> floor = new ArrayList<>();
    public static final ArrayList<Integer> num = new ArrayList<>();

    /**************** ETRI DB *******************/
    // public static final ArrayList<String>[] mac = new ArrayList[3402];  // DataBase 크기에 따라 배열크기 변경 필요
    // public static final ArrayList<Double>[] rssi = new ArrayList[3402];

    /**************** KIU_3F DB *******************/
    //public static final ArrayList<String>[] mac = new ArrayList[46];  // DataBase 크기에 따라 배열크기 변경 필요
    //public static final ArrayList<Double>[] rssi = new ArrayList[46];

    /**************** KIU_All DB *******************/
    public static final ArrayList<String>[] mac = new ArrayList[100];  // DataBase 크기에 따라 배열크기 변경 필요
    public static final ArrayList<Double>[] rssi = new ArrayList[100];

    public static void setFDB(Context context) { // Database의 jsonParsing, initArray 포함
        String json;


        for (int i = 0; i < mac.length; i++) { //2차원 ArrayList
            mac[i] = new ArrayList<>();
            rssi[i] = new ArrayList<>();
        }

        try {
            //  InputStream is = context.getAssets().open("FDB.json");            // ETRI DB
            //   InputStream is = context.getAssets().open("FDB_46.json");         // KIU_3F DB
            InputStream is = context.getAssets().open("FDB_100.json");  // KIU_All DB
            int fileSize = is.available();
            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray Array1 = new JSONArray(json);
            for (int ia=0; ia < Array1.length(); ia++) {
                JSONObject Ref = Array1.getJSONObject(ia);
                lat.add(Ref.getDouble("latitude"));
                lon.add(Ref.getDouble("longitude"));
                floor.add(Ref.getInt("floor"));
                num.add(Ref.getInt("num"));

                JSONArray Array2 = Ref.getJSONArray("wifi");
                for(int ib=0; ib < Array2.length(); ib++) {
                    JSONObject WiFi = Array2.getJSONObject(ib);
                    mac[ia].add(WiFi.getString("mac"));
                    rssi[ia].add(WiFi.getDouble("rssi"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    /** Positioning 부분
     *
     * ?? mac과 rssi를 굳이 잘라서 써야할 이유가 있을까
     *
     * */
    private static ArrayList<String>[] DB_mac = new ArrayList[21];
    private static ArrayList<Double>[] DB_rssi = new ArrayList[21];

    public static double[] Pos_F = new double[3];

    public static void referencePoint(){
        DB_mac = mac;
        DB_rssi = rssi;
    }

    public static double[] kNN(ArrayList<String> mac, ArrayList<Integer> rssi) {
        double[] Pos = {0, 0, 0};
        referencePoint();

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

        for(int ia = 0; ia < DB_mac.length; ia++) {
            double rssiDiff = 1;
            double rssiDist;
            double normDiff;
            int numMatchedAP = 0;

            for (int ib = 0; ib < num.get(ia); ib++) {
                RP_mac = DB_mac[ia].get(ib);
                RP_rssi = DB_rssi[ia].get(ib);

                for (int ic = 0; ic < mac.size(); ic++) {
                    MS_mac = mac.get(ic);
                    MS_rssi = rssi.get(ic);
                    if (RP_mac.equals(MS_mac)) {
                        numMatchedAP = numMatchedAP + 1;
                        rssiDist = Math.pow((Math.abs(RP_rssi - MS_rssi)), 2);
                        rssiDiff = rssiDiff + rssiDist;
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
                Pos_x = lat.get(aNN);
                Pos_y = lon.get(aNN);

                weight = Power.get(kb)/weightSum;
                Pos[0] = Pos[0] + Pos_x * weight;
                Pos[1] = Pos[1] + Pos_y * weight;
            }

            Pos_z = floor.get(idxList.get(0));
            Pos[2] = Pos_z;
        }
        Pos_F=Pos;
        return Pos;
    }
}
