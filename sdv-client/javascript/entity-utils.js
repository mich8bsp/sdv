/**
 * Created by mich8bsp on 17-Jun-16.
 */
colorMapping = {};

function getReadingEntityId(reading){
    return "Reading " + reading["id"].toString() + "-" + reading["sensorId"].toString();
}

function getTrackEntityId(track) {
    return "Track " + track["id"].toString() + "-" + track["sensorId"].toString();
}

function getSensorId(entity){
    return entity["sensorId"];
}

function getGenericEntityId(entity){
    return entity["id"];
}

function getPosition(entity){
    return Cesium.Cartesian3.fromDegrees(entity["lon"], entity["lat"], entity["alt"]);
}

function addPoint(cesiumEntity, originalEntity){
    cesiumEntity["point"] = {
        pixelSize : 5,
            color : getEntityColor(originalEntity),
            outlineColor : Cesium.Color.WHITE,
            outlineWidth : 2
    };
    return cesiumEntity;
}

function addBox(cesiumEntity, originalEntity){
    cesiumEntity["box"] = {
        dimensions : new Cesium.Cartesian3(300.0, 300.0, 300.0),
        fill: false,
        outline: true,
        outlineColor : getEntityColor(originalEntity),
        outlineWidth : 2
    };
    return cesiumEntity;
}

function getEntityColor(entity) {
    var chosenColor = colorMapping[entity["sensorId"]];
    if(!chosenColor){
        chosenColor = new Cesium.Color(Math.random(), Math.random(), Math.random(), 1.0);
        colorMapping[entity["sensorId"]] = chosenColor;
    }
    return chosenColor;
}