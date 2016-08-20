package sdv;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import sdv.datastore.DataStore;
import sdv.datastructures.*;
import sdv.parsing.FusedTrackInputParser;
import sdv.parsing.SensorReadingInputParser;
import sdv.parsing.TrackCorrelationInputParser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class Server {
    private DataStore store = new DataStore();
    private Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {

        new Server().run();
    }

    private void run() {
        initData();
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/tracks/:start/:end").handler(routingContext -> handleRequest(routingContext, this::getTracksJson));
        router.route("/readings/:start/:end").handler(routingContext -> handleRequest(routingContext, this::getReadingsJson));
        router.post("/newreadings-mapping/").handler(context -> addMapping(context, store::updateReadingWithCesiumId));
        //   router.route("/correlations/:time").handler(routingContext -> handleRequest(routingContext, this::getCorrelationsJson));
        router.route("/timeframe").handler(this::getTimeframe);

        router.route("/static/*").handler(StaticHandler.create("sdv-client").setCachingEnabled(false));
        router.get("/sdv").handler(context -> context.reroute("/static/index.html"));

        server.requestHandler(router::accept).listen(8080);
    }



    private void addMapping(RoutingContext routingContext, BiConsumer<UpdateKey, String> updateFunction) {
        JsonArray allNewMappings = routingContext.getBodyAsJsonArray();
        allNewMappings.stream().forEach(mapping -> {
            JsonObject newMapping = (JsonObject) mapping;
            DataId id = new DataId(newMapping.getInteger("id"), newMapping.getInteger("sensorId"));
            UpdateKey key = new UpdateKey(id, newMapping.getLong("time"));
            updateFunction.accept(key, newMapping.getString("cesiumId"));
        });

        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
    }




    private void initData() {
        new SensorReadingInputParser().addToStore(store, "readings.csv");
        new FusedTrackInputParser().addToStore(store, "tracks.2.csv");
        new TrackCorrelationInputParser().addToStore(store, "correlations.csv");
    }


    private void getTimeframe(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "application/json");
        JsonObject resultJson = new JsonObject();
        resultJson.put("start", store.getStartTime());
        resultJson.put("end", store.getEndTime());
        // Write to the response and end it
        response.setChunked(true);
        System.out.println("Got timeframe request, returning " + resultJson.toString());
        response.write(resultJson.toString());
        response.end();
    }

    private void handleRequest(RoutingContext context, BiFunction<Long, Long, JsonArray> jsonParser) {
        String startTimeStr = context.request().getParam("start");
        String endTimeStr = context.request().getParam("end");
        if (startTimeStr != null && endTimeStr != null) {
            long startTime = Long.parseLong(startTimeStr);
            long endTime = Long.parseLong(endTimeStr);
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.setChunked(true);
            JsonArray resultJson = jsonParser.apply(startTime, endTime);
            // Write to the response and end it
            response.write(resultJson.toString());
            response.end();
        }
    }

//    private void handleTrackRequest(RoutingContext context) {
//        String requestedTime = context.request().getParam("time");
//        String requestedTrackId = context.request().getParam("trackId");
//        String requestedSensorId = context.request().getParam("sensorId");
//        if (requestedTime != null) {
//            long time = Long.parseLong(requestedTime);
//            int trackId = Integer.parseInt(requestedTrackId);
//            int sensorId = Integer.parseInt(requestedSensorId);
//            HttpServerResponse response = context.response();
//            response.putHeader("content-type", "application/json");
//            response.setChunked(true);
//            JsonArray resultJson = getTrackUpdates(trackId, sensorId, time);
//            // Write to the response and end it
//            response.write(resultJson.toString());
//            response.end();
//        }
//    }
//
//    private JsonArray getTrackUpdates(int trackId, int sensorId, long time) {
//        Collection<FusedTrack> updates = store.getTrackUpdatesAtTime(time, new DataId(trackId, sensorId));
//
//        JsonArray res = new JsonArray();
//        for (FusedTrack track : updates) {
//            res.add(track.toJson());
//        }
//        return res;
//    }


    private JsonArray getTracksJson(long startTime, long endTime) {
        Map<DataId, Collection<FusedTrack>> updatesPerTrack = store.getTrackUpdatesAtTime(startTime, endTime);

        JsonArray res = new JsonArray();
        for(Collection<FusedTrack> trackUpdates : updatesPerTrack.values()){
            JsonArray t = new JsonArray();
            trackUpdates.stream().sorted((u1,u2)->Long.compare(u1.getData().getTime(), u2.getData().getTime())).forEachOrdered(upd -> t.add(upd.toJson()));
            res.add(t);
        }

        return res;
    }

    private JsonArray getReadingsJson(long startTime, long endTime) {
        Collection<SensorReading> readings = store.getReadingsAtTime(startTime, endTime);
        JsonArray res = new JsonArray();
        for (SensorReading reading : readings) {
            res.add(reading.toJson());
        }
        return res;
    }

    private JsonArray getTrackStatesJson(long lastTime, long currentTime) {
        Collection<FusedTrackState> states = store.getTrackStatesAtTime(currentTime);
        JsonArray res = new JsonArray();
        for(FusedTrackState state : states){
            res.add(state.toJson());
        }
        return res;
    }

    private JsonArray getCorrelationsJson(long time) {
        Collection<TrackCorrelation> correlations = store.getCorrelationsAtTime(time);
        JsonArray res = new JsonArray();
        for (TrackCorrelation correlation : correlations) {
            res.add(correlation.toJson());
        }
        return res;
    }

    public class UpdateKey {
        private DataId id;

        public UpdateKey(DataId id, long time) {
            this.id = id;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public DataId getId() {
            return id;
        }

        public void setId(DataId id) {
            this.id = id;
        }

        private long time;
    }
}
