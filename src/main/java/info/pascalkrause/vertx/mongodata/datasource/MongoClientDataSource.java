package info.pascalkrause.vertx.mongodata.datasource;

import java.util.List;
import java.util.stream.Collectors;

import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import info.pascalkrause.vertx.mongodata.collection.Index;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

public class MongoClientDataSource implements MongoDataSource {

    private final MongoClient mc;

    public MongoClientDataSource(MongoClient client) {
        mc = client;
    }

    @Override
    public MongoDataSource count(String collection, JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mc.count(collection, query, resultHandler);
        return this;
    }

    @Override
    public MongoDataSource find(String collection, JsonObject query,
            Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        mc.find(collection, query, resultHandler);
        return this;
    }

    @Override
    public MongoDataSource upsert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler) {
        mc.save(collection, document, resultHandler);
        return this;
    }

    @Override
    public MongoDataSource bulkInsert(String collection, List<JsonObject> documents,
            Handler<AsyncResult<Long>> resultHandler) {
        List<BulkOperation> operations = documents.parallelStream()
                .map(document -> BulkOperation.createInsert(document.copy())).collect(Collectors.toList());

        mc.bulkWrite(collection, operations, dbResponse -> {
            if (dbResponse.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(dbResponse.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Long>(dbResponse.result().getInsertedCount()));
        });
        return this;
    }

    @Override
    public MongoDataSource removeDocuments(String collection, JsonObject query,
            Handler<AsyncResult<Long>> resultHandler) {
        mc.removeDocuments(collection, query, deleteResult -> {
            if (deleteResult.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.result().getRemovedCount()));
        });
        return this;
    }

    @Override
    public MongoDataSource dropCollection(String collection, Handler<AsyncResult<Void>> resultHandler) {
        mc.dropCollection(collection, resultHandler);
        return this;
    }

    @Override
    public MongoDataSource createIndex(String collection, Index index, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject key = new JsonObject();
        key.put(index.getColumn(), index.isAscending() ? 1 : -1);
        mc.createIndexWithOptions(collection, key, new IndexOptions().unique(index.isUnique()).name(index.getName()),
                resultHandler);
        return this;
    }

    @Override
    public MongoDataSource listIndexes(String collection, Handler<AsyncResult<JsonArray>> resultHandler) {
        mc.listIndexes(collection, resultHandler);
        return this;
    }

    @Override
    public MongoDataSource deleteIndex(String collection, String name, Handler<AsyncResult<Void>> resultHandler) {
        mc.dropIndex(collection, name, resultHandler);
        return this;
    }
}
