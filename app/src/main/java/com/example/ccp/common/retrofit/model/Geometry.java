package com.example.ccp.common.retrofit.model;

import androidx.annotation.NonNull;

import com.example.ccp.map_module.model.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geometry {
    /**
     * geometry : jsonString Original 데이터
     * newGeometry : geometry 데이터 기준으로 변환한 JSONObject
     *      ## getNewGeometry 을 이용하여 반드시 사용할 것 ##
     */
    public int id;
    public String note;
    public String geometry;

    private JSONObject newGeometry = null;

    public JSONObject getNewGeometry() {
        if(newGeometry == null) {
            try {
                newGeometry = new JSONObject(this.geometry);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return newGeometry;
    }

    public Point[] getLinePoint() {
        try {
            JSONArray jsonArray = getNewGeometry().getJSONArray("features").getJSONObject(0).getJSONArray("coordinates");
            JSONArray startArray = jsonArray.getJSONArray(0);
            JSONArray endArray = jsonArray.getJSONArray(1);
            return new Point[] {
                new Point(startArray.getDouble(0), startArray.getDouble(1)),
                new Point(endArray.getDouble(0), endArray.getDouble(1)),
            };
        } catch (JSONException e) { return null; }
    }

    @NonNull
    @Override
    public String toString() {
        return "[Geometry] id : " + id + ", "
            + "note : " + note + ", "
            + "geometry : " + geometry;
    }
}
