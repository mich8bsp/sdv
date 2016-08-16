package sdv;

import sdv.datastore.DataStore;
import sdv.datastructures.DataId;
import sdv.datastructures.FusedTrack;
import sdv.datastructures.SensorReading;
import sdv.datastructures.TrackCorrelation;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import sdv.parsing.FusedTrackInputParser;
import sdv.parsing.SensorReadingInputParser;
import sdv.parsing.TrackCorrelationInputParser;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

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

        router.route("/tracks/:time").handler(routingContext -> handleRequest(routingContext, this::getTracksJson));
        router.route("/track/:sensorId/:trackId/:time").handler(this::handleTrackRequest);
        router.route("/readings/:time").handler(routingContext -> handleRequest(routingContext, this::getReadingsJson));
        router.route("/correlations/:time").handler(routingContext -> handleRequest(routingContext, this::getCorrelationsJson));


        server.requestHandler(router::accept).listen(8080);
    }


    private void initData() {
        new SensorReadingInputParser().addToStore(store, "readings.csv");
        new FusedTrackInputParser().addToStore(store, "tracks.csv");
        new TrackCorrelationInputParser().addToStore(store, "correlations.csv");
    }


    private void handleRequest(RoutingContext context, Function<Long, JsonArray> jsonParser) {
        String requestedTime = context.request().getParam("time");
        if (requestedTime != null) {
            long time = Long.parseLong(requestedTime);
            HttpServerResponse response = context.response();
            response.putHeader("content-type", "application/json");
            response.setChunked(true);
            JsonArray resultJson = jsonParser.apply(time);
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

    private JsonArray getReadingsJson(long time) {
        Collection<SensorReading> readings = store.getReadingsAtTime(time);
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
