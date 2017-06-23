package info.pascalkrause.vertx.mongodata.collection;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface MongoCollection<T> {

    public String getCollectionName();

    public MongoCollection<T> count(JsonObject query, Handler<AsyncResult<Long>> resultHandler);

    public MongoCollection<T> upsert(T resource, Handler<AsyncResult<String>> resultHandler);

    public MongoCollection<T> find(JsonObject query, Handler<AsyncResult<List<T>>> resultHandler);

    public MongoCollection<T> removeDocument(JsonObject query, Handler<AsyncResult<Long>> resultHandler);
}
