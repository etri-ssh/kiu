package com.example.ccp.common.retrofit.model;

import androidx.annotation.NonNull;

public class CameraResponse {
    /**
     result : 측위 성공 여부
     mode : 모드 ( 1 : 랜드마크 인식까지 수행, 2 : 카메라 위치 추정까지 수행 )
     floor : 층 정보
     number : 랜드마크 번호 (면적이 가장 크게 검출된 랜드마크 선택)
     type : 랜드마크 종류 ( atm, company_signboard, frame, letter, section_number, signboard, vending_machine )
     info : 랜드마크 정보 ( 랜드마크 내 인식된 글자, 디버깅 용도 )
     box_xmin : 랜드마크 box top_left_x ([0,1] range, 이미지 좌표)
     box_ymin : 랜드마크 box top_left_y ([0,1] range, 이미지 좌표)
     box_xmax : 랜드마크 box bottom_right_x ([0,1] range, 이미지 좌표)
     box_ymax : 랜드마크 box bottom_right_y ([0,1] range, 이미지 좌표)
     landmark_x : 랜드마크 중심 x (coordinate=2인 경우 경도)
     landmark_y : 랜드마크 중심 y (coordinate=2인 경우 위도)
     landmark_z : 랜드마크 중심 z (coordinate=2인 경우 고도)
     camera_x : 카메라 위치 x (coordinate=2인 경우 경도)
     camera_y : 카메라 위치 y (coordinate=2인 경우 위도)
     camera_z : 카메라 위치 z (coordinate=2인 경우 고도)
     message : 디버깅용 메시지
     coordinate : 좌표계 정의 ( 1 : EPSG:5186 좌표계, 2 : WGS84 위경도 좌표계 )
     */
    public boolean result;
    public int mode;
    public String floor;
    public int number;
    public String type;
    public String info;
    public float box_xmin;
    public float box_ymin;
    public float box_xmax;
    public float box_ymax;
    public float landmark_x;
    public float landmark_y;
    public float landmark_z;
    public float camera_x;
    public float camera_y;
    public float camera_z;
    public String message;
    public int coordinate;

    @NonNull
    @Override
    public String toString() {
        return "CameraResponse : {result : " + result + ", " +
            "mode : " + mode + ", " +
            "floor : " + floor + ", " +
            "number : " + number + ", " +
            "type : " + type + ", " +
            "info : " + info + ", " +
            "box_xmin : " + box_xmin + ", " +
            "box_ymin : " + box_ymin + ", " +
            "box_xmax : " + box_xmax + ", " +
            "box_ymax : " + box_ymax + ", " +
            "landmark_x : " + landmark_x + ", " +
            "landmark_y : " + landmark_y + ", " +
            "landmark_z : " + landmark_z + ", " +
            "camera_x : " + camera_x + ", " +
            "camera_y : " + camera_y + ", " +
            "camera_z : " + camera_z + ", " +
            "message : " + message + ", " +
            "coordinate : " + coordinate + "}";
    }
}
