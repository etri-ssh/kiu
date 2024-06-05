proj4.defs('EPSG:5179', '+proj=tmerc +lat_0=38 +lon_0=127.5 +k=0.9996 +x_0=1000000 +y_0=2000000 +ellps=GRS80 +units=m +no_defs');
proj4.defs('EPSG:5186', '+proj=tmerc +lat_0=38 +lon_0=127 +k=1 +x_0=200000 +y_0=600000 +ellps=GRS80 +units=m +no_defs');
ol.proj.proj4.register(proj4);
var epsg_5179 = ol.proj.get('EPSG:5179');
epsg_5179.setExtent([-200000.0, -28024123.62, 31824123.62, 4000000.0]);

var ngii_wmts = {};
ngii_wmts.version = "1.0";
ngii_wmts.util = {};
ngii_wmts.air_year=[];
var map_layers = {};
var mapArray=[];
var draw;
var vector;
var map;
var vSource = new ol.source.Vector();
var measureTooltipElement;
var helpTooltipElement;
var measureTooltip;
var helpTooltip;
var drawType;
var listener;
var mapMode;
var mapDivArray = [];
var cnLayers;
var mapIndex;
var mapModeArray=[];
var fmMapObj;
var marker;
var marker_vector;
var popup;
var wms;
var attribution_html =  '';//'<img style="width:96px; height:16px; bottom:2px; right:2px; position:absolute;" src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">';

ngii_wmts.mapIndex = 0;
ngii_wmts.mapObjects = [];
ngii_wmts.findMapObject = function(index){
	for(var i=0;i<ngii_wmts.mapObjects.length;i++){
		if(ngii_wmts.mapObjects[i].key==index)return ngii_wmts.mapObjects[i].val;
	}
};
ngii_wmts.findMapObjectKey = function(mapObject){
	for(var i=0;i<ngii_wmts.mapObjects.length;i++){
		if(ngii_wmts.mapObjects[i].val==mapObject)return ngii_wmts.mapObjects[i].key;
	}
};
ngii_wmts.util.objMap = function(key, val){
	this.key = key;
	this.val = val;
};
ngii_wmts.apikey = "04trYP9_xwLAfALjwZ-B8g";
//ngii_wmts.apikey = "02DA33D4FF5944F2D46B084A04F49096";

//옵션
ngii_wmts.properties = {	
	tileNames_en : [
	           "korean_map",
	           "color_map",
	           "lowV_map",
	           "english_map",
	           "white_map",
	           "chinese_map",
	           "japanese_map",
	           "white_edu_map",
	           "base_hd",
			   "AIRPHOTO",
			   "night_map"
	],
	tileNames_ko : [
	           "일반",
	           "색각",
	           "큰글씨",
	           "영문",
	           "백지도",
	           "중문",
	           "일문",
	           "교육용백지도",
	           "일반HD",
			   "영상지도",
			   "야간지도"
	],
	airWMTSLayerOptions : {
		url: "http://210.117.198.120:8081/o2map/services?apikey="+ngii_wmts.apikey
		,matrixSet : "NGIS_AIR"
		,format: "image/jpg"
		,projection : epsg_5179
		,tileGrid : new ol.tilegrid.WMTS({
				origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
				resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
				matrixIds : ["5","6","7","8","9","10","11","12","13","14","15","16","17","18"]
		})
		,style : '_null'
		,wrapX : true
		,attributions:[
			//'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
		]
		,crossOrigin: 'anonymous'
		,tileLoadFunction: function(imageTile, src) {
			imageTile.getImage().src = "http://map.ngii.go.kr/openapi/proxy/proxyTile.jsp?apikey="+ngii_wmts.apikey+ "&URL=" + encodeURIComponent(src);
		}
	},
	wmtEmapOption:{
        url : "http://map.ngii.go.kr/openapi/Gettile.do?apikey="+ngii_wmts.apikey
		,matrixSet : "EPSG:5179"
		,format : "image/png"
		,projection : epsg_5179
		,tileGrid : new ol.tilegrid.WMTS({
					origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
					resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
					matrixIds : ["L05","L06","L07","L08","L09","L10","L11","L12","L13","L14","L15","L16","L17","L18"]
		})
		,style : 'korean'
		,wrapX : true
		,attributions:[
			//'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
		]
		,crossOrigin : 'anonymous'
	},
	wmtHdEmapOption:{
        url : "http://map.ngii.go.kr/openapi/Gettile.do?apikey="+ngii_wmts.apikey
		,matrixSet : "EPSG:5179"
		,format : "image/png"
		,projection : epsg_5179
		,tilePixelRatio: 2
		,tileGrid : new ol.tilegrid.WMTS({
					origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
					resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
					matrixIds : ["L05","L06","L07","L08","L09","L10","L11","L12","L13","L14","L15","L16","L17","L18"]
		})
		,style : 'korean'
		,wrapX : true
		,attributions:[
			//'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
		]
		,crossOrigin : 'anonymous'
	},

	wmtEmapNorthOption:{
        url : "//map.ngii.go.kr/openapi/NorthGettile.do?apikey="+ngii_wmts.apikey
		,matrixSet : "korean"
		,format : "image/png"
		,projection : epsg_5179
		,tileGrid : new ol.tilegrid.WMTS({
					origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
					resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
					matrixIds : ["L05","L06","L07","L08","L09","L10","L11","L12","L13","L14","L15","L16","L17","L18"]
		})
		,style : 'north'
		,wrapX : true
		,attributions:[
			//'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
		]
		,crossOrigin : 'anonymous'
	},
	IndoorOptions:{
		url : "http://map.ngii.go.kr/openapi/Gettile.do?apikey="+ngii_wmts.apikey
		,name : "Indoor Base Layer"
		,layer : 'indoor_map'
		,matrixSet : "korean"
		,format : "image/png"
		,projection : epsg_5179
		,tileGrid : new ol.tilegrid.WMTS({
			origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
			resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28,32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255, 0.1275, 0.06375],
			matrixIds : ["L05","L06","L07","L08","L09","L10","L11","L12","L13","L14","L15","L16","L17","L18","L19","L20"]
		})
		,style : 'korean'
		,wrapX : true
		//,minZoom : 5
		,attributions:[
			//'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
		]
		,crossOrigin : 'anonymous'
	},
};

//타일 호출
ngii_wmts.map = function(objId, options){
	var initLayers = [];
	if(!objId||typeof objId!="string")return;
	mapDivArray.push(objId);
	var mapObj = document.getElementById(objId);
	
	var properties = ngii_wmts.properties;
	
	var initCenter = [960551.04896058,1919735.5150606];
	var initZoom = 1;
	var initExtent = epsg_5179.getExtent();
	var initMinZoom = 5;
	var initMaxZoom = 18;

	mapIndex = ngii_wmts.mapIndex;

	if(options){
		if(options.mapMode){
			mapMode = options.mapMode;
			mapModeArray[mapIndex] = mapMode;
			delete options.mapMode;
		}
		if(options.center){
			initCenter = options.center;
			delete options.center;
		}
		if(options.zoom){
			initZoom = options.zoom//+4;
			delete options.zoom;
		}		
		if(options.minZoom){
			initMinZoom = options.minZoom+4;
			delete options.minZoom;
		}
		if(options.maxZoom){
			initMaxZoom = options.maxZoom+4;
			delete options.maxZoom;
		}
		if(options.extent){
			initExtent = options.extent			
			delete options.extent;
		}
	}

	var tileNames_en = properties.tileNames_en;

	for (var i =0; i<tileNames_en.length;i++){
		if(tileNames_en[i] == 'AIRPHOTO'){
			map_layers["map"+mapIndex+"_"+tileNames_en[i]] = new ol.layer.Tile({source:new ol.source.WMTS($.extend({},properties.airWMTSLayerOptions,{name:tileNames_en[i],layer:tileNames_en[i]})),visible:false});
		} else {
			map_layers["map"+mapIndex+"_"+tileNames_en[i]] = new ol.layer.Tile({source:new ol.source.WMTS($.extend({},properties.wmtEmapOption,{name:tileNames_en[i],layer:tileNames_en[i]})),visible:false});
		}
		initLayers.push(map_layers["map"+mapIndex+"_"+tileNames_en[i]]);
	};

	if (mapMode > -1 && Object.keys(map_layers).length > mapMode) {
		map_layers["map"+mapIndex+"_"+tileNames_en[mapMode]].setVisible(true);
		mapModeArray[mapIndex] = mapMode;
	} else {
		map_layers["map"+mapIndex+"_"+"korean_map"].setVisible(true);
		mapModeArray[mapIndex] = "0";
	}

	var airMapLogCk = true;

	if (mapMode == 9) {//항공사진 호출시
		airMapApiUseLog();
	}

	map = new ol.Map({		
		layers: initLayers,
		target: mapObj,		
		view: new ol.View({
			projection: 'EPSG:5179',
			center: initCenter, 
			zoom: initZoom,
			//maxZoom:initMaxZoom,
			minZoom:initMinZoom,
			extent:initExtent,
			//maxResolution: 2088.96,
			//minResolution: 0.255,
			constrainResolution: true

		}),
		interactions: ol.interaction.defaults.defaults({mouseWheelZoom: false}).extend([
		      new ol.interaction.MouseWheelZoom({
		        constrainResolution: true
		      })
		]),
		controls: ol.control.defaults.defaults({
	          attributionOptions: {	        	  
	        	  collapsible: false
	          }
	    }),
	    logo:false		
	});
		
	mapArray.push(map);
	ngii_wmts.mapObjects.push(new ngii_wmts.util.objMap(mapIndex, this));
	
	//기본 줌인줌아웃 박스 제거
	$(".ol-overlaycontainer-stopevent .ol-zoom").remove();
	$(".ol-rotate").remove();
	//$(".ol-overlaycontainer-stopevent .ol-attribution").remove();
	
	//현재 맵 확인
	this._getMap = function(){
	var thisMap = this;
	var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		return mapArray[fmIndex];
	};
		
	//지도 변환버튼 생성_sample02	
	this._addDefaultMapModeBox = function(options){		
		mapObj.style.position = "relative";
		var boxId = "ngiimap_mapmodebox_"+mapIndex;
		var boxClass = "ngiimap_mapmodebox";
		var boxObj= document.getElementById(boxId);

		if(!boxObj){
			var tileNames_ko = ngii_wmts.properties.tileNames_ko;
			var tileNames_en = ngii_wmts.properties.tileNames_en;
			boxObj = document.createElement("div");
			boxObj.id=boxId;
			boxObj.className = boxClass;
			boxObj.style.width="230px";
			boxObj.style.height="105px";
			boxObj.style.position="absolute";
			boxObj.style.top="35px";
			boxObj.style.right="15px";
			boxObj.style.zIndex="1000000";
			var html="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('0');\" title=\""+tileNames_ko[0]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[0]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_chkd_off_01.png\" alt=\""+tileNames_ko[0]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('1');\" title=\""+tileNames_ko[1]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[1]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_02.png\" alt=\""+tileNames_ko[1]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('2');\" title=\""+tileNames_ko[2]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[2]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_03.png\" alt=\""+tileNames_ko[2]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('4');\" title=\""+tileNames_ko[4]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[4]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_05.png\" alt=\""+tileNames_ko[4]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('3');\" title=\""+tileNames_ko[3]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[3]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_04.png\" alt=\""+tileNames_ko[3]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('5');\" title=\""+tileNames_ko[5]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[5]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_06.png\" alt=\""+tileNames_ko[5]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('6');\" title=\""+tileNames_ko[6]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[6]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_07.png\" alt=\""+tileNames_ko[6]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			html+="<div style=\"width:56px;height:52px;float:left;\">";
			html+="<a href=\"javascript:ngii_wmts.findMapObject("+mapIndex+")._setMapMode('7');\" title=\""+tileNames_ko[7]+"\">";
			html+="<img id=\""+"map"+mapIndex+"_"+tileNames_en[7]+"\" src=\"http://map.ngii.go.kr/images/design/btCircle_off_08.png\" alt=\""+tileNames_ko[7]+"\" onmouseover=\"this.src=this.src.replace('_off','_on')\" onmouseout=\"this.src=this.src.replace('_on','_off')\" border=\"0\"/></a>";
			html+="</div>";
			boxObj.innerHTML = html;
			mapObj.appendChild(boxObj);
		}
		if(options){
			if(options.top){
				boxObj.style.top=options.top;boxObj.style.bottom="";
			}
			if(options.bottom){
				boxObj.style.bottom=options.bottom;boxObj.style.top="";
			}
			if(options.left){
				boxObj.style.left=options.left;boxObj.style.right="";
			}
			if(options.right){
				boxObj.style.right=options.right;boxObj.style.left="";
			}
		}

		if(mapModeArray[mapIndex]!=0) {
			this._setMapMode(mapModeArray[mapIndex]);
		}
	};

	//지도 컨트롤버튼 생성
	this._addDefaultMeasureControl = function(){
		var thisMap = this;
		mapObj.style.position = "relative";
		var boxId = "ngiimap_measurecontrolbox_"+mapIndex;
		var boxObj= document.getElementById(boxId);
		thisMap._gfn_add_vector();

		if(!boxObj){
			var tileNames_ko = ngii_wmts.properties.tileNames_ko;
			var tileNames_en = ngii_wmts.properties.tileNames_en;
			boxObj = document.createElement("div");
			boxObj.id=boxId;
			boxObj.style.width="54px";
			boxObj.style.height="200px";
			boxObj.style.position="absolute";
			boxObj.style.top="120px";
			boxObj.style.right="20px";
			boxObj.style.zIndex="1000000";
			var html = '<div id="toolbar" style="width:39px;margin-top:10px;margin-left:10px;">';
			html += '<ul style="padding:0 0 0 0;margin:0 0 0 0;list-style:none outside none;">';
			html += '<li style="background:url(/emap/images/tool_off_01.png); background-repeat:repeat-y; height:30px;padding:0 0 0 0;margin:0 0 0 0;"></li>';
			html += '<li style="background:url(/emap/images/BgTool.png); background-repeat:repeat-y;padding:0 0 0 0;margin:0 0 0 0;">';
			html += '<ul id="tool_swich" style="padding:0 0 0 0;margin:0 0 0 0;list-style:none outside none;">';
			html += '<li name="zoomIn" style="padding:0 0 0 0;margin:0 0 0 0;" title="확대"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+').zoomIn();" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_02.png" border="0"/></a></li>';
			html += '<li name="zoomOut" style="padding:0 0 0 0;margin:0 0 0 0;" title="축소"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+').zoomOut();" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_03.png" border="0"/></a></li>';
			html += '<li name="measure_circle" style="padding:0 0 0 0;margin:0 0 0 0;" title="반경"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+')._mapControl(\'measure_circle\');" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_on_11.png" border="0"/></a></li>';
			html += '<li name="measure_distance" style="padding:0 0 0 0;margin:0 0 0 0;" title="거리"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+')._mapControl(\'measure_distance\');" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_05.png" border="0"/></a></li>';
			html += '<li name="measure_area" style="padding:0 0 0 0;margin:0 0 0 0;" title="면적"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+')._mapControl(\'measure_area\');" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_06.png" border="0"/></a></li>';
			html += '<li name="deleteLayer" style="padding:0 0 0 0;margin:0 0 0 0;" title="초기화"><a href="javascript:ngii_wmts.findMapObject('+mapIndex+')._mapControl();" style="text-decoration:none;padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_04.png" border="0"/></a></li>';
			html += '</ul>';
			html += '</li>';
			html += '<li style="padding:0 0 0 0;margin:0 0 0 0;"><img src="/emap/images/tool_off_09.png" width="39" height="24"  alt=""/></li>';
			html += '</ul>';
			html += '</div>';
			boxObj.innerHTML = html;
			mapObj.appendChild(boxObj);
		}
	};

	//배경지도 전환
	this._setMapMode = function(value) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);

		if(airMapLogCk && value == 9){
			airMapApiUseLog();
		}

		for (var j = 0; j < mapArray[fmIndex].getLayers().getArray().length; j++){
			mapArray[fmIndex].getLayers().getArray()[j].setVisible(false);
		};

		var lv_map_layer_list= ngii_wmts.properties.tileNames_en;
		for(var j = 0; j < lv_map_layer_list.length-1; j++) {
			if($("#map"+fmIndex+"_"+lv_map_layer_list[j]).attr("src")){
				var changeSrc = $("#map"+fmIndex+"_"+lv_map_layer_list[j]).attr("src").replace("btCircle_chkd_", "btCircle_");
				$("#map"+fmIndex+"_"+lv_map_layer_list[j]).attr("src",changeSrc);
			}
		}
		if($("#map"+fmIndex+"_"+lv_map_layer_list[value]).attr("src")){
			var selectedImg = $("#map"+fmIndex+"_"+lv_map_layer_list[value]).attr("src").replace("btCircle_","btCircle_chkd_");
			$("#map"+fmIndex+"_"+lv_map_layer_list[value]).attr("src",selectedImg);
		}

		if(lv_map_layer_list[value]){
			map_layers["map"+fmIndex+"_"+lv_map_layer_list[value]].setVisible(true);
			mapModeArray[mapIndex] = value;
		} else {
			map_layers["map"+fmIndex+"_korean_map"].setVisible(true);
			mapModeArray[mapIndex] = 0;
		}
	};
	
	//현재 배경지도 확인
	this._getMapMode = function() {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		return mapModeArray[fmIndex];
	};

	/** 현재맵의 설정을 대상맵에 일치시킨다. */
	this._synchronize = function(toMaps){
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var zoom = mapArray[fmIndex].getView().getZoom();
		var center = mapArray[fmIndex].getView().getCenter();
		for(var i=0;i<mapArray.length;i++){
			mapArray[i].getView().setCenter(center);
			mapArray[i].getView().setZoom(zoom);
		}
	};

	/** 대상맵의 설정을 현재맴에 일치시킨다. */
	this._synchronizeWith = function(toMap){
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var tmIndex = ngii_wmts.findMapObjectKey(toMap);
		mapArray[fmIndex].getView().setCenter(mapArray[tmIndex].getView().getCenter());
		mapArray[fmIndex].getView().setZoom(mapArray[tmIndex].getView().getZoom());
	};

	// vectorlayer생성
	this._gfn_add_vector=function() {
		vector = new ol.layer.Vector({
			source: vSource,
			style: new ol.style.Style({
			  fill: new ol.style.Fill({
				color: 'rgba(39, 118, 220, 0.5)'
			  }),
			  stroke: new ol.style.Stroke({
				color: 'rgba(248, 81, 81, 0.5)',
				width: 4
			  }),
			  image: new ol.style.Circle({
				radius: 7,
				fill: new ol.style.Fill({
				  color: 'rgba(39, 118, 220, 0.5)'
				}),
				stroke: new ol.style.Stroke({
				  color: 'rgba(67, 57, 210, 0.5)',
				  width:4
				})
			  })
			})
		  });

		vector.setZIndex(1000);
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		mapArray[fmIndex].addLayer(vector);
	}

	//wms layer 생성
	this._gfn_add_wms=function(index) {
		var indexUrl;
		if (index>0) {
			indexUrl = "http://210.117.198.32:6080/arcgis/rest/services/NGII_INDEXMAP/MapServer/export/";
		} else {
			indexUrl = "http://210.117.198.32:6080/arcgis/services/NGII_INDEXMAP_ORDER_SDE/MapServer/WmsServer";
		}
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		wms = new ol.layer.Tile({
				source: new ol.source.TileWMS({
					url:"http://210.117.198.32:6080/arcgis/rest/services/NGII_INDEXMAP/MapServer/export/",
					params:{
						FORMAT:'png32',
						LAYERS:'show:'+index,
						F:'image',
						BBOXSR:'5179',
						IMAGESR:'5179',
						SIZE:'400,400'
					}
				})
		});
		wms.setZIndex(100);
		wms.setVisible(true);
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		mapArray[fmIndex].getView().setZoom(13);
		mapArray[fmIndex].addLayer(wms);
	};
	
	//hybrid layer 생성, 삭제
	this._setHybridMap=function(boolVisible) {
		var fmIndex = ngii_wmts.findMapObjectKey(this);
		
		// 초기화
		mapArray[fmIndex].getLayers().forEach(function(layer, i){
		    if(layer.get('name') && layer.get('name') == 'hybridlayer') {
	        	mapArray[fmIndex].removeLayer(layer);
			}
		});
		    
		if(boolVisible == true){
			var hybridLayer = new ol.layer.Tile({
				source : new ol.source.WMTS(
						$.extend(
							{},
							ngii_wmts.properties.wmtEmapOption,
							{
								name: 'hybridlayer',
								layer: 'hybrid_map',
							}
						)
					),
					zIndex : 1,
					name : 'hybridlayer',
					crossOrigin : 'anonymous'
			});
			
			hybridLayer.setZIndex(200);
			hybridLayer.setVisible(true);
			mapArray[fmIndex].addLayer(hybridLayer);
		}
	};

	/** 대상 컨트롤을 활성화한다. */
	this._mapControl = function(value) {
		var fmIndex = ngii_wmts.findMapObjectKey(this);
		for(var i = 0; i<mapArray.length;i++){
				vector.getSource().clear(measureTooltipElement);
				mapArray[i].removeLayer(vector);
				mapArray[i].removeInteraction(draw);
				mapArray[i].removeOverlay(measureTooltip+"_"+i);
				$('.tooltip-static'+i).remove();
				mapArray[i].removeInteraction(draw);
		};
		//vector.setSource(vSource);
		mapArray[fmIndex].addLayer(vector);
		if (value=="measure_distance") {
			value= "LineString";
		} else if(value=="measure_area") {
			value="Polygon";
		} else if(value=="measure_circle") {
			value="Circle";
		} else {
			this._clearPopup();
			return false;
		}

		drawType = value;
		addInteraction(fmIndex);
	};

	this._clearPopup = function (){
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		vector.getSource().clear(measureTooltipElement);
		mapArray[fmIndex].removeLayer(vector);
		mapArray[fmIndex].removeInteraction(draw);
		mapArray[fmIndex].removeOverlay(measureTooltip);
		mapArray[fmIndex].removeOverlay(helpTooltip);
		$('.tooltip-static'+fmIndex).remove();
	}

	//항공사진 레이어 온오프
	this._setAirMapYear = function(value) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var lv_map_air_layer_list = ngii_wmts.air_year;
		var lv_map_layer_list = ngii_wmts.properties.tileNames_en;

		mapModeArray[mapIndex] = 9

		if(airMapLogCk){
			airMapApiUseLog();
		}

		for (var j = 0; j < mapArray[fmIndex].getLayers().getArray().length; j++){
			mapArray[fmIndex].getLayers().getArray()[j].setVisible(false);
		};

		if(value=='base') {
			map_layers["map"+fmIndex+"_"+lv_map_layer_list[9]].setVisible(true);
		} else {

			map_layers["airmap"+fmIndex+"_"+value].setVisible(true);
		}
	};

	//해당 맵의 줌인 줌아웃
	this.zoomIn = function () {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var zoom = mapArray[fmIndex].getView().getZoom();

		//mapArray[fmIndex].getView().animate({zoom:zoom+1, duration:500});
		mapArray[fmIndex].getView().setZoom(zoom+1);
		/*
		if(zoom<13) {
			mapArray[fmIndex].getView().animate({zoom:zoom+1, duration:500});
		}
		*/
	};

	this.zoomOut = function () {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var zoom = mapArray[fmIndex].getView().getZoom();

		//mapArray[fmIndex].getView().animate({zoom:zoom-1, duration:500});
		mapArray[fmIndex].getView().setZoom(zoom-1);
		/*
		if(zoom>3) {
			mapArray[fmIndex].getView().animate({zoom:zoom-1, duration:500});
		}
		*/
	};

	this._getCenter = function () {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		return mapArray[fmIndex].getView().getCenter();

	};

	this._setCenter = function (pointX,pointY) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		mapArray[fmIndex].getView().setCenter([pointX,pointY]);
	};

	this._getExtent = function () {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		var Size = mapArray[fmIndex].getSize();
		return mapArray[fmIndex].getView().calculateExtent(Size);

	};

	this._getZoom = function () {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		return mapArray[fmIndex].getView().getZoom();

	};

	this._setZoom = function (zoom) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		mapArray[fmIndex].getView().setZoom(zoom);
	};

	//해당 위치의 포인트로 이동.
	this._showpoint = function(lon, lat, zoom) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		mapArray[fmIndex].getView().setZoom(Number(zoom) + 5);
		mapArray[fmIndex].getView().setCenter([lon,lat]);
	};

	this._setMarker = function (markerOpion) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);

		var zoom =16;
		/* 마커 생성 */
		if(markerOpion){
			if(markerOpion.pointX){
				var pointX = markerOpion.pointX;
				delete markerOpion.pointX;
			}
			if(markerOpion.pointY){
				var pointY = markerOpion.pointY;
				delete markerOpion.pointY;
			}
			if(markerOpion.imgUrl){
				var imgUrl = markerOpion.imgUrl;
				delete markerOpion.imgUrl;
			}
			if(markerOpion.zoom){
				zoom = markerOpion.zoom+4;
				delete markerOpion.zoom;
			}
		}

		marker = new ol.Feature({
			geometry: new ol.geom.Point([pointX,pointY])
		});

		marker.setStyle(
			new ol.style.Style({
				image: new ol.style.Icon({
					src: imgUrl
				})
			})
		);

		marker_vector = new ol.layer.Vector({
			source: new ol.source.Vector({
				features:[marker]
			})
		});
		marker_vector.setZIndex(10);

		mapArray[fmIndex].addLayer(marker_vector);

		/* popup 생성 */
		var container = document.getElementById('popup');
		var content = document.getElementById('popup-content');
		var closer = document.getElementById('popup-closer');

		popup = new ol.Overlay({
			position: [pointX,pointY],
			element: container,
			autoPan: true,
			autoPanAnimation: {
				duration: 250,
			}
		});

		mapArray[fmIndex].addOverlay(popup);

		mapArray[fmIndex].getView().setCenter([pointX,pointY]);
		mapArray[fmIndex].getView().setZoom(zoom);

		closer.onclick = function () {
			popup.setPosition(undefined);
			closer.blur();
			return false;
		};

		mapArray[fmIndex].on('singleclick', function(evt) {
			var feature = mapArray[fmIndex].forEachFeatureAtPixel(evt.pixel,
				function(feature) {
				  return feature;
				});
			if (feature) {
			  var coordinates = feature.getGeometry().getCoordinates();
			  popup.setPosition(coordinates);

			  $(container).addClass('_popupOn');
			  $(container).css('display','block');
			} else {
			  $(container).removeClass('_popupOn');
			}

		});

		mapArray[fmIndex].on('pointermove', function(e) {
			var pixel = mapArray[fmIndex].getEventPixel(e.originalEvent);
			var hit = mapArray[fmIndex].hasFeatureAtPixel(pixel);
			map.getTarget().style.cursor = hit ? 'pointer' : '';
		});
	};

	this._showcircle_len = function (pointX, pointY, radius, zoom){
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);
		

			if(pointX){
				var pointX = pointX;
				delete pointX;
			}
			if(pointY){
				var pointY = pointY;
				delete pointY;
			}
			if(radius){
				var circleRadius = radius;
				delete radius;
			}
			if(zoom){
				zoom = zoom+4;
				delete zoom;
			}

		circleCenter = new ol.Feature({
			geometry: new ol.geom.Point([pointX,pointY])
		});

		circleCenter.setStyle(
			new ol.style.Style({
				image: new ol.style.Circle({
					radius: circleRadius,
					fill: new ol.style.Fill({
						color: 'rgba(255, 255, 255, 0.9)'
					}),
					stroke: new ol.style.Stroke({
						color: '#ffcc33',
						width: 2
					})
				})
			})
		);

		vector = new ol.layer.Vector({
			source: new ol.source.Vector({
				features:[circleCenter]
			})
		});

		
		vector.setZIndex(10);

		mapArray[fmIndex].getView().setCenter([pointX,pointY]);
		mapArray[fmIndex].getView().setZoom(zoom);
		mapArray[fmIndex].addLayer(vector);
	};
	
	this._getWKT = function (type, feature) {
		if(type=="map") {
			var bound = this._getExtent();
			var bottomLeft = ol.extent.getBottomLeft(bound);
			var topLeft = ol.extent.getTopLeft(bound);
			var topRight = ol.extent.getTopRight(bound);
			var bottomRight = ol.extent.getBottomRight(bound);
			var ring = [ 
	        	[bottomLeft[0], bottomLeft[1]], 
	        	[topLeft[0], topLeft[1]] , 
	        	[topRight[0], topRight[1]],
	        	[bottomRight[0], bottomRight[1]],
	        	[bottomLeft[0], bottomLeft[1]]
	    	];
			var poly = new ol.geom.Polygon([ring]);
			var feature = new ol.Feature(poly);
	    }
		var format = new ol.format.WKT;
		var featureWkt = format.writeFeature(feature);
		
		return featureWkt;
	};

	// 항공사진 목록 가져오기
	this._getAirMapList = function() {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);

		var airMapList =[];
		var param = {
				TYPE: "STORE",
				API: "WMTS",
				REQUEST: "LIST"
			};
	
		$.ajax({
			type : "POST",
			url : "/ms/map/callLayer.do?url=http://210.117.198.120:8081/o2map/admin",
			data : param,
			async: false,
			dataType : "json"
		}).done(function(result) {
			if(result.SUCCESS == true){				
				engineResult = result.RESULT;
				for(var i=0 ;engineResult.length > i ;i++ ){
					if(engineResult[i].ACTIVATE && engineResult[i].NAME != 'AIRPHOTO'){					
						airMapList.push(engineResult[i].NAME);
						ngii_wmts.air_year.push(engineResult[i].NAME);
						map_layers["airmap"+mapIndex+"_"+engineResult[i].NAME] = new ol.layer.Tile({source:new ol.source.WMTS($.extend({},properties.airWMTSLayerOptions,{name:engineResult[i].NAME,layer:engineResult[i].NAME})),visible:false});
						mapArray[fmIndex].addLayer(map_layers["airmap"+mapIndex+"_"+engineResult[i].NAME]);
					}
				}
			}
			
		}.bind(this));
		
		return airMapList;
	}

	//수치지도 도엽인덱스
	this._gfn_add_arcgis = function(mapObj) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);

		var tileGrid = new ol.tilegrid.TileGrid({
			extent : [705680.0000, 1349270.0000, 1388291.0000, 2581448.0000],
			resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
			tileSize : [(mapArray[fmIndex].getSize()[0]*2),(mapArray[fmIndex].getSize()[1]*2)],
		});

		var boundaryLayer = new ol.layer.Tile({
			source : new ol.source.TileArcGISRest({
				params: {
					LAYERS : "show:" + mapObj,
					FORMAT : "PNG32",
					TRANSPARENT : true
					},
				url: 'http://210.117.198.32:6080/arcgis/rest/services/NGII_INDEXMAP_ORDER_SDE/MapServer/export/',
				//크로스오류처리
				crossOrigin: 'anonymous',
				tileGrid : tileGrid
			}),
			extent : [705680.0000, 1349270.0000, 1388291.0000, 2581448.0000],
			projection : "EPSG:5179",
			extractAttributes : false,
			opacity :0.5,
			name : 'boundaryLayer'
		});

		mapArray[fmIndex].addLayer(boundaryLayer);
	};

	//표준도엽인덱스
	this._gfn_add_indexmap=function(mapObj) {
		var thisMap = this;
		var fmIndex = ngii_wmts.findMapObjectKey(thisMap);

		var tileGrid = new ol.tilegrid.TileGrid({
			extent : [705680.0000, 1349270.0000, 1388291.0000, 2581448.0000],
			resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
			tileSize : [(mapArray[fmIndex].getSize()[0]*2),(mapArray[fmIndex].getSize()[1]*2)],
		});

		var indexLayer = new ol.layer.Tile({
			source : new ol.source.TileArcGISRest({
				params: {
					LAYERS : "show:" + mapObj,
					FORMAT : "PNG32",
					TRANSPARENT : true
				},
				url: 'http://210.117.198.32:6080/arcgis/rest/services/NGII_INDEXMAP/MapServer/export/',
				//크로스오류처리
				crossOrigin: 'anonymous',
				tileGrid : tileGrid
			}),
			extent : [705680.0000, 1349270.0000, 1388291.0000, 2581448.0000],
			projection : "EPSG:5179",
			extractAttributes : false,
			opacity :0.5,
			name : 'indexLayer'
		});

		mapArray[fmIndex].addLayer(indexLayer);
	};

	ngii_wmts.mapIndex++;
};

function airMapApiUseLog(){
	$.ajax({
		type : "GET",
		url : "http://map.ngii.go.kr/openapi/airMapApiUseLog.do",
		data : {apikey : ngii_wmts.apikey},
		dataType : "json",
		success: function(data){
			airMapLogCk = false;
		}
	});
}

//면적, 거리 측정
 function addInteraction (index){
	var pointerMoveHandler = function(evt) {
		if (evt.dragging) {
			return;
		}
	};
	
	mapArray[index].on('pointermove', pointerMoveHandler);
	
	draw = new ol.interaction.Draw({
		source: vSource,
		type: drawType,
		style: new ol.style.Style({
    		fill: new ol.style.Fill({
      			color: 'rgba(255, 255, 255, 0.2)'
    		}),
    		stroke: new ol.style.Stroke({
	      		color: 'rgba(0, 0, 0, 0.5)',
      			lineDash: [10, 10],
      			width: 2
    		}),
    		image: new ol.style.Circle({
				radius: 5,
				stroke: new ol.style.Stroke({
					color: 'rgba(0, 0, 0, 0.7)'
				}),
				fill: new ol.style.Fill({
					color: 'rgba(255, 255, 255, 0.2)'
				})
			})
		})
	});
	mapArray[index].addInteraction(draw);

	createMeasureTooltip(index);	
	createHelpTooltip(index);

	draw.on('drawstart',
        function(evt) {          
			sketch = evt.feature;
			var tooltipCoord = evt.coordinate;
			listener = sketch.getGeometry().on('change', function(evt) {
				var geom = evt.target;
				var output;
				var helpMsg;
				if (geom instanceof ol.geom.Polygon) {
					output = formatArea(geom);
					tooltipCoord = geom.getInteriorPoint().getCoordinates();
					helpMsg = "계속 진행하시려면 클릭하세요";
				} else if (geom instanceof ol.geom.LineString) {
					output = formatLength(geom);
					tooltipCoord = geom.getLastCoordinate();
					helpMsg = "계속 진행하시려면 클릭하세요";
				} else if (geom instanceof ol.geom.Circle) {
					output = formatCircle(geom);
					tooltipCoord = geom.getFirstCoordinate();
					helpMsg = "클릭하면 종료됩니다";
				}

				helpTooltipElement.innerHTML=helpMsg;
				helpTooltip.setPosition(tooltipCoord);
				measureTooltipElement.innerHTML = output;
				measureTooltip.setPosition(tooltipCoord);
			});
        },
	this);

    draw.on('drawend',
        function() {
			measureTooltipElement.className = 'tooltip tooltip-static'+index;
			measureTooltip.setOffset([0, -7]);
			//sketch = null;
			measureTooltipElement = null;
			helpTooltipElement.innerHTML="";
			createMeasureTooltip(index);
			ol.Observable.unByKey(listener);
        },
	this);
};	

//측정 결과 툴팁 생성
 function createMeasureTooltip (index) {	
	if (measureTooltipElement) {
    	measureTooltipElement.parentNode.removeChild(measureTooltipElement);
    }
    measureTooltipElement = document.createElement('div');
    measureTooltipElement.className = 'tooltip tooltip-measure'+index;
    measureTooltip = new ol.Overlay({
		element: measureTooltipElement,
		offset: [0, -15],
		positioning: 'bottom-center'
    });
   	mapArray[index].addOverlay(measureTooltip);
};

 function createMarkerDiv (index) {
    createMarkerDiv = document.createElement('div');
    createMarkerDiv.className = 'marker-popup'+index;
    popup = new ol.Overlay({
		element: createMarkerDiv,
		offset: [0, -15],
		positioning: 'bottom-center'
    });   	
};

 function createHelpTooltip (index) {
	if (helpTooltipElement) {
    	helpTooltipElement.parentNode.removeChild(helpTooltipElement);
    }
	helpTooltipElement = document.createElement('div');
    helpTooltipElement.className = 'tooltip tooltip-help'+index;
	helpTooltip = new ol.Overlay({
		element: helpTooltipElement,
		offset: [15, 0],
		positioning: 'top-left'
    });

   	mapArray[index].addOverlay(helpTooltip);
};

 /** 메져 컨트롤 measure Control 이하 */
var formatLength = function(line) {
	var length = ol.sphere.getLength(line);
    var output;
    if (length > 100) {
    	output = (Math.round(length / 1000 * 100) / 100) + ' ' + 'km';
    } else {
    	output = (Math.round(length * 100) / 100) + ' ' + 'm';
    }
    return output;
};

var formatArea = function(polygon) {
    var area = ol.sphere.getArea(polygon);
    var output;
    if (area > 10000) {
    	output = (Math.round(area / 1000000 * 100) / 100) + ' ' + 'km<sup>2</sup>';
    } else {
    	output = (Math.round(area * 100) / 100) + ' ' + 'm<sup>2</sup>';
    }
    return output;
};

var formatCircle = function(circle) {
    var radius = circle.getRadius();
    if (radius > 100) {
    	output = (Math.round(radius / 1000 * 100) / 100) +
				' ' + 'km';
    } else {
    	output = (Math.round(radius * 100) / 100) +
				' ' + 'm';
    }
    return output;
};

ngii_wmts.util.fillzero = function(n, digits) {
	var zero = '';
	n = n.toString();
	if (digits > n.length) {
		for (var i = 0; digits - n.length > i; i++) {
			zero += '0';
		}
	}
	return zero + n;
};

ngii_wmts.setBaseLayer = function(map, layer){
	for(var i=0; i<map.getLayers().getArray().length; i++){
		map.getLayers().getArray()[i].setVisible(false);
		if(map.getLayers().getArray()[i] == layer)
			map.getLayers().getArray()[i].setVisible(true);
	}
}

function wmtEmapOption2(layerId, boolVisible){
	return 	{	url : "http://map.ngii.go.kr/openapi/Gettile.do?apikey="+ngii_wmts.apikey
				,matrixSet : "EPSG:5179"
				,format : "image/png"
				,projection : epsg_5179
				,tileGrid : new ol.tilegrid.WMTS({
					origin : ol.extent.getTopLeft(epsg_5179.getExtent()),
					resolutions : [2088.96, 1044.48, 522.24, 261.12, 130.56, 65.28, 32.64, 16.32, 8.16, 4.08, 2.04, 1.02, 0.51, 0.255],
					matrixIds : ["L05","L06","L07","L08","L09","L10","L11","L12","L13","L14","L15","L16","L17","L18"]
				})
				,style : 'korean'
				,wrapX : true
				/*
				,attributions:[
					'<img style="width:96px; height:16px;"src="http://map.ngii.go.kr/img/process/ms/map/common/img_btoLogo3.png" alt="로고">'
				]
				*/
				,crossOrigin : 'anonymous'
				,visible : boolVisible
				,source: new ol.source.XYZ({
							tileUrlFunction: function(coordinate) {
								coordinate[0] = "L" + ngii_wmts.util.fillzero(coordinate[0] + 5, 2);
								url = "http://map.ngii.go.kr/openapi/Gettile.do?apikey="+ngii_wmts.apikey + "&service=" + "WMTS" + "&request=" + "GetTile"
										+ "&version=" + "1.0.0" + "&layer=" + layerId + "&style=korean"
										+ "&format=image/png" + "&tilematrixset=korean"
										+ "&tilematrix=" + coordinate[0] + "&tilerow=" + coordinate[2]
										+ "&tilecol=" + coordinate[1];
								return url;
							},
							type : 'image/png',
							tileSize: new ol.size.toSize([256,256]),
							maxResolution: 2088.96,
							projection: "EPSG:5179"
						})
	}
}