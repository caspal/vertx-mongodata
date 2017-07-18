package info.pascalkrause.vertx.mongodata.service;

import java.util.Objects;
import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.mongo.impl.MongoServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;;

public class MongoServiceVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(MongoServiceVerticle.class);
    private final String serviceAddress;
    private final JsonObject mongoConfig;

    private MongoService service;

    public MongoServiceVerticle(String serviceAddress, JsonObject mongoConfig) {
        this.serviceAddress = (Objects.isNull(serviceAddress) || serviceAddress.isEmpty())
                ? UUID.randomUUID().toString() : serviceAddress;
        this.mongoConfig = mongoConfig;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        service = new MongoServiceImpl(MongoClient.createNonShared(vertx, mongoConfig));
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        MessageConsumer<JsonObject> consumer = ProxyHelper.registerService(MongoService.class, vertx, service,
                serviceAddress);
        consumer.completionHandler(res -> {
            if (res.succeeded()) {
                String message = "MongoCollectionService is now available under: " + serviceAddress;
                logger.info(message);
                startFuture.complete();
            } else {
                String message = "Failed to initialize MongoCollectionService";
                logger.error(message, res.cause());
                logger.info(message);
                startFuture.fail(res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        service.close();
    }
}
