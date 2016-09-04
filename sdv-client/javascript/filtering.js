function buildFilters($http, $scope, viewer){
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
}