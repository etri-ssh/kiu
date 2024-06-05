// center 이동 활성화 이벤트
function location_icon_click() {
    let location_el = document.querySelector(".location");
//    if(!location_el.classList.contains('map-on')) {
//        location_el.classList.add('map-on');
//        firstCenter = true;
//    } else {
//        location_el.classList.remove('map-on');
//        firstCenter = false;
//    }
    var pos = Android.currentPosition();
    var arr = pos.split(',');
    var lon = arr[0];
    var lat = arr[1];
    let rotate = [lon, lat];
    firstCenter = true;
    centerMove(rotate, 'EPSG:4326', null);
}

// location 아이콘 off
function location_icon_off() {
    let location_el = document.querySelector(".location");
    location_el.classList.remove('map-on');
}

// 카메라 아이콘 이벤트
function capture_icon_click(){
    let capture_el = document.querySelector(".capture");
    if(capture_el.classList.contains('on')) {
        capture_el.classList.remove('on');
        Android.cameraAction(8);
    } else {
        capture_el.classList.add('on');
        Android.cameraAction(0);
    }
}

// 측위 토글 on/off
function switchPosToggle(type, flag) {
    var targetLayer = getTypeCheckObject(type).layer;
    targetLayer.setVisible(flag);
}

// 안드로이드 네이티브로 층이 변경됨을 알림
function floorChangeToast() { Android.floorChangeAction(currentFloor); }