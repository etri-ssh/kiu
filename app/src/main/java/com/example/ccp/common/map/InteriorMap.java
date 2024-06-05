package com.example.ccp.common.map;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.BuildingMapResponse;
import com.example.ccp.map_module.model.JLine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class InteriorMap {
    public final int buildingId; // 현재 지도의 id
    private final JSONArray floorMapList = new JSONArray(); // 지도에 뿌릴 데이터
    public double currentFloor; // 현재 보여지고 있는 층(기본 1층)
    public JSONArray basePaths;

    public InteriorMap(int buildingId, List<BuildingMapResponse> data) {
        this.buildingId = buildingId;
        TreeMap<Double, JSONObject> treeMap = new TreeMap<>();
        data.forEach(buildingMapResponse -> { // 실내 지도 정보들(모든 층)
            try {
                // 현재 지도 및 경로 데이터가 없더라도 층 view에서 활용 중
                // 만약 데이터가 없어 활용 하기 싫다면 if 걸어서 지도 및 경로 확인 필요
                JSONObject map = new JSONObject();
                map.put("floor", buildingMapResponse.floorNumber); // 층 숫자
                map.put("floorName", buildingMapResponse.floorName); // 층 이름
                map.put("polygon", buildingMapResponse.getBackgroundMapGeometry()); // 지도
                map.put("isPolygon", buildingMapResponse.isBackgroundMapGeometry()); // 지도 확인(있다면 true)
                map.put("path", buildingMapResponse.getPathGeometry()); // 경로
                map.put("isPath", buildingMapResponse.isPathGeometry()); // 경로 확인(있다면 true)
                treeMap.put(buildingMapResponse.floorNumber, map);
            } catch(Exception e) { Common.logW("InteriorMap data.forEach Error : " + e); }
        });
        Common.log("treeMap : " + treeMap);

        // key 정렬과 동시에 javascript 로 보내기 위한 객체 설정
        Iterator<Double> iterator = treeMap.navigableKeySet().descendingIterator(); // 정렬 keySet
        while(iterator.hasNext()) {
            try { floorMapList.put(treeMap.get(iterator.next())); }
            catch (Exception e) { Common.logW("treeMap keySet Error : " + e); }
        }
        Common.log("floorMapList : " + floorMapList);

        // 1층 존재 확인 후 기본 층 설정
        if(treeMap.containsKey(1.0)) currentFloor = 1; // 1층
        else currentFloor = Collections.min(treeMap.keySet()); // 가장 낮은 층
        Common.log("currentFloor : " + currentFloor);

        // 기본 층 경로 설정
        basePaths = findFloorPaths(currentFloor);
    }

    public JSONArray getFloorMapList() { return floorMapList; }

    // 특정 층의 경로 데이터 찾기
    public JSONArray findFloorPaths(double floor) {
        Common.log("경로를 찾을 층 : " + floor);
        JSONArray result = new JSONArray(); // 특정 층 라인 모두 넣은 곳 기대값 구조 : [[[위,경],[위,경]],[[위,경],[위,경]]]
        currentFloor = floor;
        try {
            for(int i = 0; i < floorMapList.length(); i++) {
                JSONObject map = floorMapList.getJSONObject(i);
                if(map.getInt("floor") == floor) { // 찾을 층 check
                    Common.log("map : " + map);
                    if(!map.getBoolean("isPath")) Common.log(floor + "층에 경로가 없습니다");
                    else { // 해당 층에 경로 데이터가 존재할 경우
                        JSONObject geometryAll = map.getJSONObject("path");
                        result = findCoordinates(geometryAll);
                    }
                }
            }
        } catch(Exception e) { Common.logW("findFloorPaths Error : " + e); }
        return result;
    }

    // geometry 에서 경로만 모두 추출하기(기대값 구조 : [[[위,경],[위,경]],[[위,경],[위,경]]])
    private JSONArray findCoordinates(JSONObject geometryAll) {
        JSONArray result = new JSONArray();
        try {
            JSONArray features = geometryAll.getJSONArray("features");
            for(int i = 0; i < features.length(); i++) {
                JSONObject multiLineJson = features.getJSONObject(i).getJSONObject("geometry");
                String type = multiLineJson.getString("type");
                JSONArray coordinate;
                if(type.equals("MultiLineString")) {
                    JSONArray coordinates = multiLineJson.getJSONArray("coordinates"); // [[[x,y,z],[x,y,z],[x,y,z]]]
                    coordinate = coordinates.getJSONArray(0);
                } else {
                    coordinate = multiLineJson.getJSONArray("coordinates"); // [[x,y,z],[x,y,z],[x,y,z]]
                }
                for(int i2 = 0; i2 < coordinate.length() - 1; i2++) {
                    JSONArray totalPoint = new JSONArray();
                    JSONArray startPoint = coordinate.getJSONArray(i2);
                    JSONArray endPoint = coordinate.getJSONArray(i2 + 1);
                    totalPoint.put(startPoint);
                    totalPoint.put(endPoint);
                    result.put(totalPoint);
                }
                Common.log("result : " + result);
            }
        } catch(Exception e) { Common.logW("findCoordinates Error : " + e); e.printStackTrace(); }
        return result;
    }

    public List<JLine> getStringToLines(String data) {
        List<JLine> result = new ArrayList<>();
        try {
            JSONArray jsonArr = new JSONArray(data);
            for(int i = 0; i < jsonArr.length(); i++) {
                JSONArray startPoint = jsonArr.getJSONArray(i).getJSONArray(0);
                JSONArray endPoint = jsonArr.getJSONArray(i).getJSONArray(1);
                result.add(new JLine( // x,y 좌표 반대로
                        startPoint.getDouble(0),startPoint.getDouble(1),
                    endPoint.getDouble(0), endPoint.getDouble(1)
                ));
            }
            Common.log("List<JLine> : " + result);
        } catch(Exception e) { Common.logW("getStringToLines Error : " + e); }
        return result;
    }
}
