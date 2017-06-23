package info.pascalkrause.vertx.mongodata;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoTestVerticle extends AbstractVerticle {

    private MongoClient mc;
    private JsonObject mongoConfig;

    public MongoClient getMongoClient() {
        return mc;
    }

    public MongoTestVerticle(JsonObject mongoConfig) {
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        mc = MongoClient.createShared(vertx, mongoConfig);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mc.close();
    }
}