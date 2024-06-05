package com.example.ccp.common.retrofit;

import com.example.ccp.common.retrofit.model.BuildingFloor;
import com.example.ccp.common.retrofit.model.BuildingMapResponse;
import com.example.ccp.common.retrofit.model.BuildingResponse;
import com.example.ccp.common.retrofit.model.CameraRequest;
import com.example.ccp.common.retrofit.model.CameraResponse;
import com.example.ccp.common.retrofit.model.RadiusMapResponse;
import com.example.ccp.common.retrofit.model.SignalRequest;
import com.example.ccp.common.retrofit.model.SignalResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface API {
//    JSON
//    @GET("weather")
//    Call<WeatherResponse> getWeatherLatLon(@Query("lat")String lat,@Query("lon")String lon,@Query("appid")String appid);

//    XML
//    @GET("getCovid19InfStateJson")
//    Call<CovidResponse> getCovid(@Query("serviceKey") String serviceKey);

    // 건물 이름 으로 건물 조회
    @GET("buildings/search")
    Call<List<BuildingResponse>> getBuildings(@Query("key") String key);

    // 단일 건물 내에 모든 정보 가져오기
    @GET("maps/paths/in/building/{buildingId}")
    Call<List<BuildingMapResponse>> getBuildingMap(@Path("buildingId") int id);

    // 반경 건물 검색
    @GET("buildings/search/location")
    Call<List<RadiusMapResponse>> getRadiusMap(@Query("x") double x, @Query("y") double y, @Query("radius") int radius);

    // 영상 지점 측위
    @POST("image")
    Call<CameraResponse> getCameraPoint(@Body CameraRequest cameraRequest);

    // 신호 기반 측위
    @POST("gdbs/positioning")
    Call<SignalResponse> getSignal(@Body SignalRequest request);
}
