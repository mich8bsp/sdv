/**
 * Created by mich8bsp on 16-Jun-16.
 */

var app = angular.module('sdv', []);

app.controller('MainController', function($scope, $http){
    $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
    $http.get("/timeframe").then(function(response){
        timeOfFirst = response.data["start"];
        timeOfLast = response.data["end"];
        var viewer = new Cesium.Viewer('cesiumContainer', {
                clock: new Cesium.Clock({
                    startTime: Cesium.JulianDate.fromDate(new Date(timeOfFirst)),
                    currentTime: Cesium.JulianDate.fromDate(new Date(timeOfFirst)),
                    stopTime: Cesium.JulianDate.fromDate(new Date(timeOfLast)),
                    clockRange: Cesium.ClockRange.CLAMPED,
                    clockStep: Cesium.ClockStep.TICK_DEPENDENT,
                    multiplier: 1,
                    canAnimate: true,
                    shouldAnimate: true
                })
            });
            viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(34.807491, 31.974653, 1000000.0),
                duration: 3.0
            });
            var lastTime = Cesium.JulianDate.fromDate(new Date(0));

            viewer.clock.onTick.addEventListener(function (clock) {
                if (Cesium.JulianDate.compare(clock.currentTime, lastTime) != 0) {
                    var currentTimeMS = Cesium.JulianDate.toDate(clock.currentTime).getTime();
                    var lastTimeMS = Cesium.JulianDate.toDate(lastTime).getTime();
                    //minimum time difference to request data from server [ 50ms ]
                    if(Math.abs(currentTimeMS-lastTimeMS)>50){
                        if(currentTimeMS > lastTimeMS){
                            $http.get("/readings/" + lastTimeMS + "/"+ currentTimeMS).then(function(response){
                                if(response.data.length > 0){
                                    var added = renderReadings(viewer, response.data);
                                    $http.post("/newreadings/", added).success(function(data, status, headers, config) {
                                                                       			console.log("post was success");
                                                                       		}).error(function(data,status,headers,config){
                                                                       		    console.log("post was unsuccessful");
                                                                       		});
                                }
                            });
                        }else{
                            $http.get("/readings/" + currentTimeMS + "/" + lastTimeMS).then(function(response){
                               if(response.data.length > 0){
                                    clearReadings(viewer, response.data);
                                }
                            });
                        }
                    }
                    Cesium.JulianDate.clone(clock.currentTime, lastTime);
                }

            });
    });

    function clearReadings(viewer, toClear){
        for (var i = 0; i < toClear.length; i++) {
            var reading = toClear[i];
            var cesiumId = reading["cesiumId"];
            clearEntity(viewer, cesiumId);
        }
        return cesiumIdMappings;
    }

    function renderReadings(viewer, toRender){
            cesiumIdMappings = []
            for (var i = 0; i < toRender.length; i++) {
                var reading = toRender[i];
                var cesiumId = renderEntity(viewer, reading, "reading", getReadingEntityId, addPoint);
                cesiumIdMappings.push({
                    "id": reading["id"],
                    "sensorId": reading["sensorId"],
                    "cesiumId": cesiumId
                });
            }
            return cesiumIdMappings;
    }

    function renderEntity(viewer, entity, type, getEntityId, addShape){
        var pos = getPosition(entity);
        var entityId = getEntityId(entity);
        var entityToAdd = addShape({
                entityType: type,
                name: entityId,
                position: pos,
                description: '<div><p>' + JSON.stringify(entity) + '</p></div>'
        }, entity);
        var added = viewer.entities.add(entityToAdd);
        return added.id;
     }

    function clearEntity(viewer, id){
        viewer.entities.removeById(id);
    }
});