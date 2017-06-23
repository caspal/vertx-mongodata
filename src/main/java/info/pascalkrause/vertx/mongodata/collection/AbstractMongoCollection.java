package info.pascalkrause.vertx.mongodata.collection;

import java.util.List;
import java.util.stream.Collectors;

import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public abstract class AbstractMongoCollection<T> implements MongoCollection<T> {

    private final MongoClient mc;

    public AbstractMongoCollection(MongoClient client) {
        mc = client;
    }

    @Override
    public abstract String getCollectionName();

    @Override
    public AbstractMongoCollection<T> count(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mc.count(getCollectionName(), query, resultHandler);
        return this;
    }

    @Override
    public AbstractMongoCollection<T> upsert(T resource, Handler<AsyncResult<String>> resultHandler) {
        mc.save(getCollectionName(), encode(resource), resultHandler);
        return this;
    }

    @Override
    public AbstractMongoCollection<T> find(JsonObject query, Handler<AsyncResult<List<T>>> resultHandler) {
        mc.find(getCollectionName(), query, dbResponse -> {
            if (dbResponse.failed()) {
                resultHandler.handle(new SimpleAsyncResult<List<T>>(dbResponse.cause()));
                return;
            }
            List<T> results = dbResponse.result().parallelStream().map(json -> decode(json))
                    .collect(Collectors.toList());
            resultHandler.handle(new SimpleAsyncResult<List<T>>(results));
        });
        return this;
    }

    @Override
    public AbstractMongoCollection<T> removeDocument(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mc.removeDocument(getCollectionName(), query, deleteResult -> {
            if (deleteResult.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.result().getRemovedCount()));
        });
        return this;
    }

    protected abstract JsonObject encode(T resource);

    protected abstract T decode(JsonObject dbResult);

}
