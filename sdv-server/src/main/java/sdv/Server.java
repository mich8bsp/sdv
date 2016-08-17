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
import sdv.datastructures.DataId;
import sdv.datastructures.FusedTrack;
import sdv.datastructures.SensorReading;
import sdv.datastructures.TrackCorrelation;
import sdv.parsing.FusedTrackInputParser;
import sdv.parsing.SensorReadingInputParser;
import sdv.parsing.TrackCorrelationInputParser;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

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
        //   router.route("/tracks/:time").handler(routingContext -> handleRequest(routingContext, this::getTracksJson));
        //  router.route("/track/:sensorId/:trackId/:time").handler(this::handleTrackRequest);
        router.route("/readings/:start/:end").handler(routingContext -> handleRequest(routingContext, this::getReadingsJson));
        router.post("/newreadings/").handler(this::addMapping);
        //   router.route("/correlations/:time").handler(routingContext -> handleRequest(routingContext, this::getCorrelationsJson));
        router.route("/timeframe").handler(this::getTimeframe);

        router.route("/static/*").handler(StaticHandler.create("sdv-client").setCachingEnabled(false));
        router.get("/sdv").handler(context -> context.reroute("/static/index.html"));
        router.route("/favicon.ico").handler(StaticHandler.create("sdv-client").setCachingEnabled(false));
        router.route("/logo.png").handler(StaticHandler.create("sdv-client").setCachingEnabled(false));

        server.requestHandler(router::accept).listen(8080);
    }

    private void addMapping(RoutingContext routingContext) {
        System.out.println("Got back json " + routingContext.getBodyAsString());
        JsonArray allNewMappings = routingContext.getBodyAsJsonArray();
        allNewMappings.stream().forEach(mapping -> {
            JsonObject newMapping = (JsonObject) mapping;
            DataId id = new DataId(newMapping.getInteger("id"), newMapping.getInteger("sensorId"));
            store.updateReadingWithCesiumId(id, newMapping.getString("cesiumId"));
        });

        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
    }


    private void initData() {
        new SensorReadingInputParser().addToStore(store, "readings.csv");
        new FusedTrackInputParser().addToStore(store, "tracks.csv");
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

    private void handleTrackRequest(RoutingContext context) {
        String requestedTime = context.request().getParam("time");
        String requestedTrackId = context.request().getParam("trackId");
        String requestedSensorId = context.request().getParam("sensorId");
        if (requestedTime != null) {
            long time = Long.parseLong(requestedTime);
            int trackId = Integer.parseInt(requestedTrackId);
            int sensorId = Integer.parseInt(requestedSensorId);
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.setChunked(true);
            JsonArray resultJson = getTrackUpdates(trackId, sensorId, time);
            // Write to the response and end it
            response.write(resultJson.toString());
            response.end();
        }
    }

    private JsonArray getTrackUpdates(int trackId, int sensorId, long time) {
        Collection<FusedTrack> updates = store.getTrackUpdatesAtTime(time, new DataId(trackId, sensorId));

        JsonArray res = new JsonArray();
        for (FusedTrack track : updates) {
            res.add(track.toJson());
        }
        return res;
    }


    private JsonArray getTracksJson(long time) {
        Collection<Optional<FusedTrack>> updates = store.getTrackAtTime(time);
        JsonArray res = new JsonArray();
        updates.stream().forEach(t -> t.ifPresent(tr -> res.add(tr.toJson())));
        return res;
    }

    private JsonArray getReadingsJson(long startTime, long endTime) {
        System.out.println("Got request for readings " + startTime + " till " + endTime);
        Collection<SensorReading> readings = store.getReadingsAtTime(startTime, endTime);
        JsonArray res = new JsonArray();
        for (SensorReading reading : readings) {
            res.add(reading.toJson());
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
}
