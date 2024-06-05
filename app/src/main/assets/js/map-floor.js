/**
 *  @variable mapArr
 *    전 층 기준의 사이즈로 되어 있는 건물 정보(실내 지도 및 floor 정보 등 포함)
 *    초기화할때 data reverse 함수 호출 필수
 *    ex) 총 3층이면 length = 3
 *  @variable currentIndex
 *    현재 활성화되어 있는 건물 정보의 index
 **/
var mapArr = []; // 전 층 기준의 사이즈 건물 정보
var currentIndex = 0;
var currentFloor = 0;

// 가장 처음 호출되는 곳으로 데이터 설정 및 각 함수 호출
function setMapLayer(data) {
    console.log('[cubicinc] setMapLayer method !!');
    initData(data);
    setFloorView();
    setFloorPosition();
}

function initData(data) {
    console.log('[initData] initData method !!');

    // data
    mapArr = data;
    currentIndex = 0;

    // side bar
    let ui = document.querySelector(".floor ul");
    ui.style.marginTop = '0px';

    // side bar
    slide_max_index = 0;
    slide_min_index = 0;
    slide_li_height = 51;
}

// 층 ui를 그리며 click listener 설정
function setFloorView() {
    console.log('[cubicinc] setFloorView method !!');
    let floorEl = '';
    let index = 0;
    setSideIndex(mapArr.length);
    mapArr.forEach(i => {
        floorEl += '<li><a id="floor_' + index + '" class="num';
        if(i.floor === 1) { currentIndex = index; }
        floorEl += '" onclick="floorChange(this,' + index + ')">' + i.floorName + '</a></li>';
        index++;
    });
    document.querySelector(".floor ul").innerHTML = floorEl;
    document.querySelector(".step").style.display = "block";
    document.querySelector("#floor_" + currentIndex).click();
}

// 1층 기준, 층 view 중앙 설정(1층 없을 시 그대로)
function setFloorPosition() {
    console.log('[cubicinc] setFloorPosition method !!');
    if(mapArr.length > 3) {
        let diff = (mapArr.length - 1) - currentIndex;
        let loopNum = 0;
        if(currentIndex == 0) loopNum = diff - 2;
        else loopNum = diff - 1;
        while(loopNum > 0) {
            upSlider();
            loopNum--;
        }
    }
}

// 지도 ui 설정 부분, currentIndex 에 맞는 층 지도를 설정
function setMapView() {
    console.log('[cubicinc] setMapView method !!');
    removeBulding();
    let data = mapArr[currentIndex];
    let polygonFeatures = data.polygon.features;
    let pathFeatures = data.path.features;

    // 지도 및 경로 찍기
    polygonFeatures.forEach(i => {
        changeFloor("Base", trans5186to5179(new ol.format.GeoJSON().readFeatures(i)));
    });
    pathFeatures.forEach(i => {
        changeFloor("Path", trans5186to5179(new ol.format.GeoJSON().readFeatures(i)));
    });
}

// 현재 표시 되고 있는 층 변경
function floorChange(el, index) {
    console.log('[cubicinc] floorChange method !!');
    document.querySelectorAll(".floor a").forEach(el => el.classList.remove('on'));
    el.classList.add('on');
    currentIndex = index;
    currentFloor = mapArr[currentIndex].floor;
    setMapView();
    removeRotate(); // 층 전환 시 기존 마커 지우기
    floorChangeToast(); // 층 전환 알림
}

// 특정 층 기준, 층 변경
function findFloorChange(floorNumber, lon, lat) {
    console.log('[cubicinc] findFloorChange method !!');
    for(var i = 0; i < mapArr.length; i++) {
        if(floorNumber == mapArr[i].floor) {
            floorChange(document.querySelectorAll(".floor a")[i], i);
            doSingleMarker('blue', lon, lat);
            return;
        }
    }
}