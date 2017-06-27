package info.pascalkrause.vertx.mongodata.service;

import static info.pascalkrause.vertx.mongodata.service.MongoService.ACTION;
import static info.pascalkrause.vertx.mongodata.service.MongoService.BULK_INSERT;
import static info.pascalkrause.vertx.mongodata.service.MongoService.CHECK_AVAILABILITY;
import static info.pascalkrause.vertx.mongodata.service.MongoService.COLLECTION;
import static info.pascalkrause.vertx.mongodata.service.MongoService.COUNT;
import static info.pascalkrause.vertx.mongodata.service.MongoService.DROP;
import static info.pascalkrause.vertx.mongodata.service.MongoService.FIND;
import static info.pascalkrause.vertx.mongodata.service.MongoService.REMOVE;
import static info.pascalkrause.vertx.mongodata.service.MongoService.UPSERT;
import static info.pascalkrause.vertx.mongodata.service.MongoServiceErrorCodes.ACTION_NOT_FOUND;
import static info.pascalkrause.vertx.mongodata.service.MongoServiceErrorCodes.UNEXPECTED_ERROR;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.MongoClient;;

public class MongoCollectionServiceVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(MongoCollectionServiceVerticle.class);
    private final String serviceAddress;

    private MongoClient mc;
    private JsonObject mongoConfig;
    private EventBus eb;

    public MongoCollectionServiceVerticle(String serviceAddress, JsonObject mongoConfig) {
        this.serviceAddress = serviceAddress;
        this.mongoConfig = mongoConfig;
    }

    private MessageConsumer<?> initializeEBConsumer() {
        return eb.consumer(serviceAddress, message -> {
            String collectionName = message.headers().get(COLLECTION);
            switch (message.headers().get(ACTION)) {
            case CHECK_AVAILABILITY:
                message.reply(null);
                break;
            case COUNT:
                mc.count(collectionName, (JsonObject) message.body(), res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(res.result());
                });
                break;
            case FIND:
                mc.find(collectionName, (JsonObject) message.body(), res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(new JsonArray(res.result()));
                });
                break;
            case UPSERT:
                mc.save(collectionName, (JsonObject) message.body(), res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(res.result());
                });
                break;
            case REMOVE:
                mc.removeDocuments(collectionName, (JsonObject) message.body(), res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(res.result().getRemovedCount());
                });
                break;
            case BULK_INSERT:
                JsonArray arr = (JsonArray) message.body();
                List<BulkOperation> operations = arr.stream()
                        .map(document -> BulkOperation.createInsert(((JsonObject) document).copy()))
                        .collect(Collectors.toList());
                mc.bulkWrite(collectionName, operations, res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(res.result().getInsertedCount());
                });
                break;
            case DROP:
                mc.dropCollection(collectionName, res -> {
                    if (res.failed()) {
                        message.fail(UNEXPECTED_ERROR.getStatusCode(), UNEXPECTED_ERROR.getDescription());
                        return;
                    }
                    message.reply(null);
                });
                break;
            default:
                message.fail(ACTION_NOT_FOUND.getStatusCode(), ACTION_NOT_FOUND.getDescription());
                break;
            }
        });
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        eb = vertx.eventBus();
        mc = MongoClient.createShared(vertx, mongoConfig);
        initializeEBConsumer();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        MessageConsumer<?> consumer = initializeEBConsumer();
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
        mc.close();
    }
}
