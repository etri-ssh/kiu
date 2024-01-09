package com.example.sensorlog.Fingerprinting;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DataBase {

    public static final ArrayList<Double> Lat = new ArrayList<>();
    public static final ArrayList<Double> Lon = new ArrayList<>();
    public static final ArrayList<Integer> Floor = new ArrayList<>();
    public static final ArrayList<Integer> Num = new ArrayList<>();

    /**************** ETRI DB *******************/
   // public static final ArrayList<String>[] MAC = new ArrayList[3402];  // DataBase 크기에 따라 배열크기 변경 필요
   // public static final ArrayList<Double>[] RSSI = new ArrayList[3402];

    /**************** KIU_3F DB *******************/
    //public static final ArrayList<String>[] MAC = new ArrayList[46];  // DataBase 크기에 따라 배열크기 변경 필요
    //public static final ArrayList<Double>[] RSSI = new ArrayList[46];

    /**************** KIU_All DB *******************/
    public static final ArrayList<String>[] MAC = new ArrayList[104];  // DataBase 크기에 따라 배열크기 변경 필요
    public static final ArrayList<Double>[] RSSI = new ArrayList[104];
    public static void initArray() {
        for (int i = 0; i < MAC.length; i++) {
            MAC[i] = new ArrayList<>();
            RSSI[i] = new ArrayList<>();
        }
    }

    public static void jsonParsing(Context context) {
        String json;

        try {
         //  InputStream is = context.getAssets().open("FDB.json");                 //ETRI DB
         //  InputStream is = context.getAssets().open("FDB_46.json");              //KIU 3F
         //   InputStream is = context.getAssets().open("FDB_104.json");      //KIU 건물
            InputStream is = context.getAssets().open("FDB_104_2.json");      //KIU 건물_new
            int fileSize = is.available();
            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray Array1 = new JSONArray(json);
            for (int ia=0; ia < Array1.length(); ia++) {
                JSONObject Ref = Array1.getJSONObject(ia);
                Lat.add(Ref.getDouble("latitude"));
                Lon.add(Ref.getDouble("longitude"));
                Floor.add(Ref.getInt("floor"));
                Num.add(Ref.getInt("num"));

                JSONArray Array2 = Ref.getJSONArray("wifi");
                for(int ib=0; ib < Array2.length(); ib++) {
                    JSONObject WiFi = Array2.getJSONObject(ib);
                    MAC[ia].add(WiFi.getString("mac"));
                    RSSI[ia].add(WiFi.getDouble("rssi"));
                }
            }

//            System.out.println(Lat);
//            System.out.println(Lon);
//            System.out.println(Floor);
//            System.out.println(Num);
//            System.out.println(MAC[1]);
//            System.out.println(RSSI[1]);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ArrayList<Double> x(){ return Lat; }

    public static ArrayList<Double> y(){
        return Lon;
    }

    public static ArrayList<Integer> z(){
        return Floor;
    }

    public static ArrayList<Integer> num(){
        return Num;
    }

    public static ArrayList<String>[] mac(){
        return MAC;
    }

    public static ArrayList<Double>[] rssi(){
        return RSSI;
    }
}
