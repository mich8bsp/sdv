/**
 * Created by mich8bsp on 16-Jun-16.
 */

var app = angular.module('sdv', ['ngMaterial']);

app.controller('OptionsController', function ($scope, $timeout, $mdSidenav, $log) {
    $scope.toggleRightFilters = buildToggler('rightFilters');
    $scope.toggleRightCorrelations = buildToggler('rightCorrelations')
    $scope.isOpenRight = function(){
      return $mdSidenav('rightFilters').isOpen() || $mdSidenav('rightCorrelations').isOpen();
    };
    /**
     * Supplies a function that will continue to operate until the
     * time is up.
     */
    function debounce(func, wait, context) {
      var timer;
      return function debounced() {
        var context = $scope,
            args = Array.prototype.slice.call(arguments);
        $timeout.cancel(timer);
        timer = $timeout(function() {
          timer = undefined;
          func.apply(context, args);
        }, wait || 10);
      };
    }
    /**
     * Build handler to open/close a SideNav; when animation finishes
     * report completion in console
     */
    function buildDelayedToggler(navID) {
      return debounce(function() {
        // Component lookup should always be available since we are not using `ng-if`
        $mdSidenav(navID)
          .toggle()
          .then(function () {
            $log.debug("toggle " + navID + " is done");
          });
      }, 200);
    }
    function buildToggler(navID) {
      return function() {
        // Component lookup should always be available since we are not using `ng-if`
        $mdSidenav(navID)
          .toggle()
          .then(function () {
            $log.debug("toggle " + navID + " is done");
          });
      }
    }
  })
  .controller('RightCtrl', function ($scope, $timeout, $mdSidenav, $log) {
    $scope.close = function () {
      // Component lookup should always be available since we are not using `ng-if`
      $mdSidenav('rightFilters').close()
        .then(function () {
          $log.debug("close RIGHT is done");
        });
      $mdSidenav('rightCorrelations').close()
                .then(function () {
                  $log.debug("close RIGHT is done");
      });
    };
  });


app.controller('MainController', function($scope, $http){
    $http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";
    $http.get("/timeframe").then(function(response){
        timeOfFirst = response.data["start"];
        timeOfLast = response.data["end"];
        var start = Cesium.JulianDate.fromDate(new Date(timeOfFirst));
        var end = Cesium.JulianDate.fromDate(new Date(timeOfLast));
        var viewer = new Cesium.Viewer('cesiumContainer', {
                clock: new Cesium.Clock({
                    startTime: start,
                    currentTime: start,
                    stopTime: end,
                    clockRange: Cesium.ClockRange.CLAMPED,
                    clockStep: Cesium.ClockStep.TICK_DEPENDENT,
                    multiplier: 0,
                    canAnimate: true,
                    shouldAnimate: true
                })
        });



        viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(34.807491, 31.974653, 1000000.0),
                duration: 3.0
        });

        $http.get("/filters/").then(function(response){
            $scope.dataFilters = response.data;
            $scope.lastFilters = JSON.parse(JSON.stringify(response.data));
            $scope.filtersChanged = function(){
                    var dataFilters = $scope.dataFilters;
                    var lastFilters = $scope.lastFilters;
                    for(var i=0;i<dataFilters.length;i++){
                        var currentFilter = dataFilters[i];
                        var lastFilter = lastFilters[i];
                        if(currentFilter["isShown"]!=lastFilter["isShown"]){
                            sensorShownChanged(currentFilter["sensorId"], currentFilter["isShown"]);
                        }else if(currentFilter["readingsShown"]!=lastFilter["readingsShown"]){
                            sensorReadingsChanged(currentFilter["sensorId"], currentFilter["readingsShown"]);
                        }else{
                            for(var j=0;j<currentFilter["tracksShown"].length;j++){
                                var currentTrackShown = currentFilter["tracksShown"][j];
                                var lastTrackShown = lastFilter["tracksShown"][j];
                                if(currentTrackShown["isTrackShown"]!=lastTrackShown["isTrackShown"]){
                                    sensorTrackChanged(currentFilter["sensorId"], currentTrackShown["trackId"], currentTrackShown["isTrackShown"]);
                                }
                            }
                        }
                    }
                    $scope.lastFilters = JSON.parse(JSON.stringify(dataFilters));
                }

                function sensorShownChanged(sensorId, isShown){
                    var collection = viewer.entities.values;
                        for(var i=0;i<collection.length;i++){
                            if(collection[i]["sensorId"]==sensorId){
                                   collection[i].show = isShown;
                             }
                        }
                    console.log("sensor changed " + sensorId + " to " + isShown);
                }

                function sensorReadingsChanged(sensorId, isShown){
                    var collection = viewer.entities.values;
                    for(var i=0;i<collection.length;i++){
                        if(collection[i]["entityType"]=="reading" && collection[i]["sensorId"]==sensorId){
                            collection[i].show = isShown;
                        }
                    }
                    console.log("sensor readings changed " + sensorId + " to " + isShown);

                }

                function sensorTrackChanged(sensorId, trackId, isShown){
                  var collection = viewer.entities.values;
                         for(var i=0;i<collection.length;i++){
                             if(collection[i]["entityType"]=="track" && collection[i]["sensorId"]==sensorId && collection[i]["entityId"]==trackId){
                                     collection[i].show = isShown;
                             }
                         }
                    console.log("sensor track changed " + sensorId + " trackId " + trackId + " to " + isShown);
                }
        });

        var lastTime = Cesium.JulianDate.fromDate(new Date(0));
        $http.get("/tracks/" + timeOfFirst + "/" + timeOfLast).then(function(response){
            for(var i=0;i<response.data.length;i++){
                buildTrackPath(viewer, response.data[i],start, end);
            }
        });
        viewer.clock.onTick.addEventListener(function (clock) {
            if (Cesium.JulianDate.compare(clock.currentTime, lastTime) != 0) {
                var currentTimeMS = Cesium.JulianDate.toDate(clock.currentTime).getTime();
                var lastTimeMS = Cesium.JulianDate.toDate(lastTime).getTime();
                //minimum time difference to request data from server [ 50ms ]
                if(Math.abs(currentTimeMS-lastTimeMS)>50){
                    if(currentTimeMS > lastTimeMS){
                        $http.get("/readings/" + lastTimeMS + "/"+ currentTimeMS).then(function(response){
                            if(response.data.length > 0){
                                    renderReadings(viewer, response.data);
                          }
                        });
                    }else{
                            $http.get("/readings/" + currentTimeMS + "/" + lastTimeMS).then(function(response){
                               if(response.data.length > 0){
                                    clearEntities(viewer, response.data);
                                }
                            });
                    }
                    $http.get("/track-correlation/"+timeOfFirst +"/" + currentTimeMS).then(function(response){
                        $scope.trackCorrelations = response.data;
                    });

                    Cesium.JulianDate.clone(clock.currentTime, lastTime);
                }
            }
        });
    });

    function buildTrackPath(viewer, updates, start, end){
        var position = computePath(viewer, updates);

//        var start = updates[0]["time"];
//        var end = updates[updates.length - 1]["time"];

        position.setInterpolationOptions({
            interpolationDegree : 5,
            interpolationAlgorithm : Cesium.LagrangePolynomialApproximation
        });
                var entity = viewer.entities.add({

                        //Set the entity availability to the same interval as the simulation time.
                        availability : new Cesium.TimeIntervalCollection([new Cesium.TimeInterval({
                            start : start,
                            stop : end
                        })]),

                        //Use our computed positions
                        position : position,

                        //Automatically compute orientation based on position movement.
                        orientation : new Cesium.VelocityOrientationProperty(position),

                        //Load the Cesium plane model to represent the entity
                        model : {
                            uri : '/static/Duck.glb',
                            minimumPixelSize : 64
                        },

                        path : {
                            resolution : 1,
                            material : new Cesium.PolylineGlowMaterialProperty({
                                glowPower : 0.1,
                                color : getEntityColor(updates[0])
                            }),
                            width : 10
                        }
                 });
       }

    function computePath(viewer, updates){
        var property = new Cesium.SampledPositionProperty();
        for(var j=0;j<updates.length;j++){
           var trackUpdate = updates[j];
           var updateTime = Cesium.JulianDate.fromDate(new Date(trackUpdate["time"]));
           property.addSample(updateTime, getPosition(trackUpdate));
           renderEntity(viewer, trackUpdate, "track", getTrackEntityId, addBox);
         }
         return property;
    }

    function clearEntities(viewer, toClear){
        for (var i = 0; i < toClear.length; i++) {
            var entity = toClear[i];
            var cesiumId = entity["cesiumId"];
            viewer.entities.removeById(cesiumId);
        }
    }

    function renderReadings(viewer, toRender){
        cesiumIdMappings = []
        for (var i = 0; i < toRender.length; i++) {
            var reading = toRender[i];
            var cesiumId = renderEntity(viewer, reading, "reading", getReadingEntityId, addPoint);
            cesiumIdMappings.push({
                    "id": reading["id"],
                    "sensorId": reading["sensorId"],
                    "time": reading["time"],
                    "cesiumId": cesiumId
                });
            }

             $http.post("/newreadings-mapping/", cesiumIdMappings).success(function(data, status, headers, config) {
            	}).error(function(data,status,headers,config){
          		    console.log("post was unsuccessful");
              });
    }

    function renderEntity(viewer, entity, type, getEntityId, addShape){

        var toparse = [entity];
        var jsonEntity = ConvertJsonToTable(toparse);
        var pos = getPosition(entity);
        var entityId = getEntityId(entity);
        var entityToAdd = addShape({
                entityType: type,
                entityId: getGenericEntityId(entity),
                sensorId: getSensorId(entity),
                name: entityId,
                position: pos,
                description: '<p><br>' + jsonEntity +'<br></p>'
        }, entity);
        var added = viewer.entities.add(entityToAdd);

        return added.id;
     }


});