class Map {
    constructor({
        id = null,
        center = [0, 0],
        zoom,
        projection = 'EPSG:3857',
        extent = extent
    } = {}){
        if (!id) {
            alert('no exist "id"')
            return false;
        }

        this.backgroundLayer = new ol.layer.Vector({
            source: new ol.source.Vector({})
        });

        this.mapLayer = new ol.layer.Tile({
            source : new ol.source.OSM()
        });

        if (extent == null) {
            this.map = new ol.Map({
                target: id,
                view: new ol.View({
                    center: center,
                    zoom: zoom,
                    maxZoom: 17.5,
                    projection: projection
                }),
                layers: [
                    /*
                    new ol.layer.Tile({
                      source: new ol.source.OSM(),                      
                    }),
                    */
                    this.mapLayer,
                    this.backgroundLayer,
                    
                  ],
            });
        } else {
            this.map = new ol.Map({
                target: id,
                view: new ol.View({
                    center: center,
                    zoom: zoom,
                    maxZoom: 17.5,
                    projection: projection,
                    extent: extent
                }),
                layers: [
                    this.backgroundLayer,
                ]
            });
        }

        this.map.on("click", function(evt){
            console.log(evt.coordinate);
        })
    }

    moveToCoordinate({
        coordinate = [],
        style = {}
    } = {}) {
        let _map = this.map;
        try {
            _map.getView().setCenter(coordinate);
            _map.getView().setZoom(16);

        } catch (e) {
            alert(e);
        }
    }

    drawGeoJSON(geojsonObject){
        try {
            let _map = this.map;
            
            this.backgroundLayer.getSource().addFeatures(
                //new ol.format.GeoJSON().readFeatures(geojsonObject)
                new ol.format.GeoJSON({featureProjection: 'EPSG:5179'}).readFeatures(geojsonObject)
            );
        } catch (e) {
            alert(e);
        }
    }

    clearGeoJSON(layer) {
        layer.getSource().clear();
    }
}