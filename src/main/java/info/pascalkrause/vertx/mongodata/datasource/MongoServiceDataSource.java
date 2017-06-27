package info.pascalkrause.vertx.mongodata.datasource;

import static info.pascalkrause.vertx.mongodata.service.MongoService.ACTION;
import static info.pascalkrause.vertx.mongodata.service.MongoService.BULK_INSERT;
import static info.pascalkrause.vertx.mongodata.service.MongoService.COLLECTION;
import static info.pascalkrause.vertx.mongodata.service.MongoService.COUNT;
import static info.pascalkrause.vertx.mongodata.service.MongoService.DROP;
import static info.pascalkrause.vertx.mongodata.service.MongoService.FIND;
import static info.pascalkrause.vertx.mongodata.service.MongoService.REMOVE;
import static info.pascalkrause.vertx.mongodata.service.MongoService.UPSERT;

import java.util.List;
import java.util.stream.Collectors;

import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MongoServiceDataSource implements MongoDataSource {

    private final EventBus eb;
    private final String serviceAddress;

    public MongoServiceDataSource(EventBus eb, String serviceAddress) {
        this.eb = eb;
        this.serviceAddress = serviceAddress;
    }

    private DeliveryOptions buildDeliveryOpts(String collection, String action) {
        return new DeliveryOptions().addHeader(ACTION, action).addHeader(COLLECTION, collection);
    }

    @Override
    public MongoDataSource count(String collection, JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        eb.send(serviceAddress, query, buildDeliveryOpts(collection, COUNT), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(response.cause()));
                return;
            }
            Long count = (Long) response.result().body();
            resultHandler.handle(new SimpleAsyncResult<Long>(count));
        });
        return this;
    }

    @Override
    public MongoDataSource find(String collection, JsonObject query,
            Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        eb.send(serviceAddress, query, buildDeliveryOpts(collection, FIND), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<List<JsonObject>>(response.cause()));
                return;
            }
            try {
                JsonArray arr = (JsonArray) response.result().body();
                List<JsonObject> findings = arr.stream().map(o -> ((JsonObject) o)).collect(Collectors.toList());
                resultHandler.handle(new SimpleAsyncResult<List<JsonObject>>(findings));
            } catch (Throwable t) {
                resultHandler.handle(new SimpleAsyncResult<List<JsonObject>>(t));
            }
        });
        return this;
    }

    @Override
    public MongoDataSource upsert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        eb.send(serviceAddress, document, buildDeliveryOpts(collection, UPSERT), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<String>(response.cause()));
                return;
            }
            String id = (String) response.result().body();
            resultHandler.handle(new SimpleAsyncResult<String>(id));
        });
        return this;
    }

    @Override
    public MongoDataSource bulkInsert(String collection, List<JsonObject> documents,
            Handler<AsyncResult<Long>> resultHandler) {
        eb.send(serviceAddress, new JsonArray(documents), buildDeliveryOpts(collection, BULK_INSERT), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(response.cause()));
                return;
            }
            Long inserted = (Long) response.result().body();
            resultHandler.handle(new SimpleAsyncResult<Long>(inserted));
        });
        return this;
    }

    @Override
    public MongoDataSource removeDocuments(String collection, JsonObject query,
            Handler<AsyncResult<Long>> resultHandler) {
        eb.send(serviceAddress, query, buildDeliveryOpts(collection, REMOVE), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(response.cause()));
                return;
            }
            Long deleted = (Long) response.result().body();
            resultHandler.handle(new SimpleAsyncResult<Long>(deleted));
        });
        return this;
    }

    @Override
    public MongoDataSource dropCollection(String collection, Handler<AsyncResult<Void>> resultHandler) {
        eb.send(serviceAddress, null, buildDeliveryOpts(collection, DROP), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Void>(response.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Void>((Void) null));
        });
        return this;
    }
}
