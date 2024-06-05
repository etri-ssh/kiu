
let map1;
let etri_geoserver_url = "http://heliosen.iptime.org:18082/geoserver/etri";

//map
let vectorLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});

//building
let baseLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let pathLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let poiLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let wallLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});

//locate
let oLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let sLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let tLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let gLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});
let rLayer = new ol.layer.Vector({
    source: new ol.source.Vector({}),
});

//let fileList = [];
//let fileString = 'ETRI13';
//let zipStr = fileString+'.zip';
//let url = 'http://222.99.56.3:9999/';
//let zipUrl = url + zipStr;


//국토지리원 생성
function emap(){
    let center = [988280.7637576415, 1820680.7916311917];
    let opt = {
        zoom : 1,
        center : center,
        minZoom:1,
        maxZoom:20,
        //extent:[958551.04896058,1917735.5150606,962551.04896058,1921735.5150606], //이동제한
        extent:[664244.8118956586, 1413126.4615682214, 1466405.4518956586, 2109794.6215682216],
        mapMode:0, //0:일반, 1:색각, 2:큰글자, 3:English, 4:백지도, 5:中國語, 6:にほんご, 7:교육용백지도, 8:mobile, 9:항공영상, 10:야간지도
    }

    //extent:[MINX,MINY,MAXX,MAXY], //이동제한

    map1 = new ngii_wmts.map("map", opt);

    map1._getMap().addLayer(vectorLayer);

    map1._getMap().addLayer(baseLayer);
    map1._getMap().addLayer(pathLayer);
    map1._getMap().addLayer(poiLayer);
    map1._getMap().addLayer(wallLayer);

    map1._getMap().addLayer(oLayer);
    map1._getMap().addLayer(sLayer);
    map1._getMap().addLayer(tLayer);
    map1._getMap().addLayer(gLayer);
    map1._getMap().addLayer(rLayer);
}

//레이어 스타일 설정
function setLayerStyle(){
    //BuildingLayer
    baseLayer.setStyle(new ol.style.Style({
        stroke: new ol.style.Stroke({color: '#000000', width: 0.1}),
        fill: new ol.style.Fill({color: '#fcf3ae'})
    }));
    pathLayer.setStyle(new ol.style.Style({stroke: new ol.style.Stroke({color: '#80f571'})}));
    poiLayer.setStyle((feature) => getTextStyle(feature));
    wallLayer.setStyle(new ol.style.Style({stroke: new ol.style.Stroke({color: '#000000'})}));

    // 마커 레이커 스타일 설정
    oLayer.setStyle(getRotateStyle('rgba(255,0,0,0.5)')); // 반 투명 빨강
    sLayer.setStyle(getRotateStyle('rgba(0,153,0,0.5)')); // 반 투명 초록
    tLayer.setStyle(getRotateStyle('rgba(0,0,255,0.5)')); // 반 투명 파랑
    gLayer.setStyle(getRotateStyle('rgba(128,128,128,0.5)')); // 반 투명 회색
    rLayer.setStyle(getRotateStyle('rgba(127,0,255,0.5)')); // 불 투명 보라색
}

//위치표시 레이어 기본 스타일 객체 생성 //fillColor : 채우기 색상
function getRotateStyle(fillColor){
    return new ol.style.Style({
        image: new ol.style.Circle({
            radius : 4,
            fill: new ol.style.Fill({
                color: fillColor
            }),
        }),
    })
}

//건물레이어 텍스트 기본 스타일 객체 생성 //text : 건물명
function getTextStyle(feature){
    let text = feature.get("TYPE");
    if(!text) text = feature.get("Type");
    return new ol.style.Style({
        text: new ol.style.Text({
            text : text,
            fill : new ol.style.Fill({
                color: '#000000'
            }),
            font: '5px sans-serif',
        })
    })
}

//일반지도, 항공지도 모드 변경 //modeNum : 0 - 일반, 9 - 항공, ishybrid : true,false - 항공지도+지하철 건물명 등 표시 여부
function changeMapMode(modNum, el){
    document.querySelectorAll(".map_mod").forEach(function(classEl){
        classEl.className = 'map_mod';
    })
    el.className = 'map_mod on';

    map1._setMapMode(modNum);

    baseLayer.setVisible(true);
    pathLayer.setVisible(true);
    poiLayer.setVisible(true);
    wallLayer.setVisible(true);

    oLayer.setVisible(true);
    sLayer.setVisible(true);
    tLayer.setVisible(true);
    gLayer.setVisible(true);
    rLayer.setVisible(true);
}

//지도 확대
function zoomIn(){ map1.zoomIn(); }

//지도 축소
function zoomOut(){ map1.zoomOut(); }

//GeoJSON 데이터에 따른 레이어 그리기 , floor : 층수 (추후변경)
function drawGeoJSON(floor){
    console.log('fileList : ', fileList);
    let fileArr = [];
    let preFileName = '';
    let files = [];

    fileList.forEach(fileName => {
        if(preFileName != '' && fileName.split('_')[0] != preFileName) {
            files.push(fileArr);
            preFileName = fileName.split('_')[0];
            fileArr = [];
            fileArr.push(fileName);
        }else {
            fileArr.push(fileName);
        }
        if(preFileName == '') preFileName = fileName.split('_')[0];
        if(fileList[fileList.length-1] == fileName) files.push(fileArr);

    })

    if(floor >= files.length) return false;

    removeBulding();
    files[floor].forEach(fileName => {
        //shp('http://127.0.0.1:5500/app/src/main/assets/areaShp/ETRI1/AF01_Base').then(function(geojson) {
        shp( url +fileString+'/'+fileName).then(function(geojson) {

        let feature = new ol.format.GeoJSON({featureProjection: 'EPSG:5179'}).readFeatures(geojson);
        let lyrName = fileName.split("_")[1];

        changeFloor(lyrName, feature);
      })
    })
}

//지도 북쪽 정렬
function rotateNorth(){
    let _map = map1._getMap();
    let view = _map.getView();
    view.setRotation(0);
}

//단일 위치 표시 //layerType : o(1타입), s(2타입), t(3타입), coord(coordinate array) : [long, lat]
function drawSingleMarker(layerType, coord){
    let feature = new ol.Feature({
        geometry : new ol.geom.Point(coord)
    })
    addFeature(layerType, feature);
}

//0~4개 타입과 같이 전달하면 좌표 찍기
//dodo리스트에 id값 넣기
//단일 좌표 넣으면 리스트에 id 추가
//단일 좌표 넣으면 리스트 맨앞 꺼 id가져와서 removeFeature 그리고 리스트 재정렬

// 각 layer 마커들
let markerIdCabinet = { 'o':[], 's':[], 't':[], 'g':[], 'r':[] };
let markerIdCount = 1;

function doSingleMarker(type, lon, lat){
    let rotate = [lon, lat];
    let typeObject = getTypeCheckObject(type);
    let markerIds = typeObject.markerIds;

    markerIds.push(markerIdCount);
    let feature = new ol.Feature({
       geometry : new ol.geom.Point(rotate)
    })
    feature.setId(markerIdCount);
    addFeature(typeObject.type, feature);

    if(markerIds.length > 4){ // 4개 초과했을 경우 오래된 마커 지우기
        let removeId = markerIds[0];
        removeMarker(typeObject.layer, removeId);
        markerIds.shift();
    }

    if(firstCenter) { // 첫번째 이동 체크 후 센터 이동
        firstCenter = false;
        centerMove5186(rotate, 'EPSG:4326', null);
        location_icon_off();
    }
    markerIdCount++;
}

function removeMarker(layer, id){
    console.log('[cubicinc] remove id : ',id);
    layer.getSource().removeFeature(layer.getSource().getFeatureById(id));
}


//위치레이어 feature 값 추가 //type : 위치조회타입, feature : 지리적 데이터를 가진 객체
function addFeature(type, feature){
    feature = trans4326to5179(feature);
    feature = [feature];
    switch(type){
        case 'o': oLayer.getSource().addFeatures(feature); break;
        case 's': sLayer.getSource().addFeatures(feature); break;
        case 't': tLayer.getSource().addFeatures(feature); break;
        case 'g': gLayer.getSource().addFeatures(feature); break;
        case 'r': rLayer.getSource().addFeatures(feature); break;
    }
}

// 4326 -> 5179 feature 변환
function trans4326to5179(feature){
    let geom5179 = feature.getGeometry().transform('EPSG:4326', 'EPSG:5179');
    feature.setGeometry(geom5179);
    return feature;
}

//5186 -> 5179 feature 모두 변환
function trans5186to5179(features){
    features.forEach(function(feature){
        feature.getGeometry().transform('EPSG:5186', 'EPSG:5179');
    })
    return features;
}

// 모든 레이어 제거
function removeLayer(){
    vectorLayer.getSource().clear();

    baseLayer.getSource().clear();
    pathLayer.getSource().clear();
    poiLayer.getSource().clear();
    wallLayer.getSource().clear();

    oLayer.getSource().clear();
    sLayer.getSource().clear();
    tLayer.getSource().clear();
    gLayer.getSource().clear();
    rLayer.getSource().clear();
}

//건물 레이어 제거
function removeBulding(){
    baseLayer.getSource().clear();
    pathLayer.getSource().clear();
    poiLayer.getSource().clear();
    wallLayer.getSource().clear();
}

//위치 레이어 제거
function removeRotate(){
    oLayer.getSource().clear();
    sLayer.getSource().clear();
    tLayer.getSource().clear();
    gLayer.getSource().clear();
    rLayer.getSource().clear();
}

//층별 데이터 변경
function changeFloor(floorType, feature){
    switch(floorType) {
        case 'Base' :
            baseLayer.getSource().addFeatures(feature);
            break;
        case 'Path' :
            pathLayer.getSource().addFeatures(feature);
            break;
        case 'Poi' :
            poiLayer.getSource().addFeatures(feature);
            break;
        case 'Wall' :
            wallLayer.getSource().addFeatures(feature);
            break;
    }
}

//센터 이동 coord : [long, lat]
function setCenter(coord){
    map1._getMap().getView().setCenter(new ol.proj.transform(coord, 'EPSG:4326', 'EPSG:5179'));
    map1._getMap().getView().setZoom(18)
}

// 좌표 건물 조회 api 에서 사용하는 용도의 좌표 변환
function transformPoint(lon, lat, type) {
    let arr = new ol.proj.transform([lon, lat], type, 'EPSG:5186');
    return arr.join(',');
}

// 5189 -> 4326 좌표로 모두 변환
function transformPointAll(data) {
    console.log('[cubicinc] transformPointAll method !!');
    let result = [];
    data.forEach(i => {
        let startPoint = new ol.proj.transform(i[0], 'EPSG:5186', 'EPSG:4326');
        let endPoint = new ol.proj.transform(i[1], 'EPSG:5186', 'EPSG:4326');
        let allPoint = [startPoint, endPoint];
        result.push(allPoint);
    });
    return result;
}

// 처음 center 이동 시 확대할 것 이후에는 기존 확대 범위 사용
let firstZoom = true;

// target 5186좌표 센터 이동
function centerMove5186(target, type, zoom) {
    let zoomNumber = Math.floor(map1._getMap().getView().getZoom());
    if(firstZoom) {
        firstZoom = false;
        zoomNumber = 20
    }
    map1._getMap().getView().animate({
        center : new ol.proj.transform(target, type, 'EPSG:5179'),
        zoom: zoomNumber,
        duration:1000
    });
}

// 각 타입에 맞는 object 모두 가져오기
function getTypeCheckObject(type) {
    let result = {};
    if(type == 'red') {
        result.type = 'o';
        result.layer = oLayer;
        result.markerIds = markerIdCabinet.o;
    }
    else if(type == 'green') {
        result.type = 's';
        result.layer = sLayer;
        result.markerIds = markerIdCabinet.s;
    }
    else if(type == 'blue') {
        result.type = 't';
        result.layer = tLayer;
        result.markerIds = markerIdCabinet.t;
    }
    else if(type == 'gray') {
        result.type = 'g';
        result.layer = gLayer;
        result.markerIds = markerIdCabinet.g;
    }
    else if(type == 'result') {
        result.type = 'r';
        result.layer = rLayer;
        result.markerIds = markerIdCabinet.r;
    }
    return result;
}

//지오서버 레이어 요청 START
function get_geojson_features(layerName){
    let features;

    $.ajax({
        type: "GET",
        async : false,
        url : etri_geoserver_url + '/ows',
        data : {
            service: 'WFS',
		    version: '1.0.0',
		    request: 'GetFeature',
		    typeName: 'etri' + ':' + layerName,
		    outputFormat: 'application/json'
        },
        success: function(result){
            features = result;
        }
    });

    return new ol.format.GeoJSON().readFeatures(features);
}

//GML
function get_gml_features(layerName){
    let features;

    $.ajax({
        type: "GET",
        async : false,
        url : etri_geoserver_url + '/ows',
        data : {
            service: 'WFS',
		    version: '1.0.0',
		    request: 'GetFeature',
		    typeName: 'etri' + ':' + layerName,
		    outputFormat: 'text/xml; subtype=gml/2.1.2'
        },
        success: function(result){
            features = result;
        }
    });

    return new ol.format.GML2().readFeatures(features);
}

function get_wms_layer(){
    return new ol.layer.Tile({
        source: new ol.source.TileWMS({
            url: etri_geoserver_url + '/wms',
            params: {
                FORMAT: 'image/png',
                VERSION: '1.1.0',
                TILED: true,
                STYLES: '',
                LAYERS: 'etri' + ':' + 'AF01_Base',
                EXCEPTIONS: 'application/vnd.ogc.se_inimage'
            }
        })
    });
}

function get_querystring(obj){
    return new URLSearchParams(obj).toString();
}

emap();
setLayerStyle();