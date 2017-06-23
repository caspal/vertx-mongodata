package info.pascalkrause.vertx.mongodata.collection;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.MongoClient;

public class MongoCollectionImpl<T> implements MongoCollection<T> {

    private final MongoClient mc;
    private final Function<T, JsonObject> encode;
    private final Function<JsonObject, T> decode;
    private final String collectionName;

    /**
     * <b>Important:</b> When implementing the transformations, check that you use the key "_id" for the id. For binary
     * types you should use a new JsonObject which contains a key with the name "$binary".
     * <p>
     * Example:<br>
     * 
     * <pre>
     * {
     *     JsonObject encoded = new JsonObject();
     *     encoded.put("_id", resource.getId());
     *     encoded.put("image", new JsonObject().put("$binary", resource.getImageBytes()));
     * }
     * </pre>
     * 
     * @param collectionName
     *            The name of the collection in the database.
     * @param encode
     *            A method to transform the resource object into a JsonObject which can be handled by the database.
     * @param decode
     *            A method to transform the JsonObject return from the database into a resource object.
     * @param client
     *            An instance of MongoClient which is used to talk with the Mongo database.
     */
    public MongoCollectionImpl(String collectionName, Function<T, JsonObject> encode, Function<JsonObject, T> decode,
            MongoClient client) {
        this.collectionName = collectionName;
        this.encode = encode;
        this.decode = decode;
        mc = client;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public MongoCollection<T> countAll(Handler<AsyncResult<Long>> resultHandler) {
        return count(new JsonObject(), resultHandler);
    }

    @Override
    public MongoCollectionImpl<T> count(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mc.count(getCollectionName(), query, resultHandler);
        return this;
    }

    @Override
    public MongoCollection<T> bulkInsert(Collection<T> resources, Handler<AsyncResult<Long>> resultHandler) {
        List<BulkOperation> operations = resources.parallelStream()
                .map(resource -> BulkOperation.createInsert(encode.apply(resource))).collect(Collectors.toList());
        mc.bulkWrite(getCollectionName(), operations, dbResponse -> {
            if (dbResponse.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(dbResponse.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Long>(dbResponse.result().getInsertedCount()));
        });
        return this;
    }

    @Override
    public MongoCollectionImpl<T> upsert(T resource, Handler<AsyncResult<String>> resultHandler) {
        mc.save(getCollectionName(), encode.apply(resource), resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> findAll(Handler<AsyncResult<List<T>>> resultHandler) {
        find(new JsonObject(), resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> find(JsonObject query, Handler<AsyncResult<List<T>>> resultHandler) {
        mc.find(getCollectionName(), query, dbResponse -> {
            if (dbResponse.failed()) {
                resultHandler.handle(new SimpleAsyncResult<List<T>>(dbResponse.cause()));
                return;
            }
            List<T> results = dbResponse.result().parallelStream().map(json -> decode.apply(json))
                    .collect(Collectors.toList());
            resultHandler.handle(new SimpleAsyncResult<List<T>>(results));
        });
        return this;
    }

    @Override
    public MongoCollectionImpl<T> removeDocuments(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mc.removeDocuments(getCollectionName(), query, deleteResult -> {
            if (deleteResult.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Long>(deleteResult.result().getRemovedCount()));
        });
        return this;
    }

    @Override
    public MongoCollection<T> removeDocument(T resource, Handler<AsyncResult<Long>> resultHandler) {
        removeDocuments(encode.apply(resource), resultHandler);
        return this;
    }

    @Override
    public MongoCollection<T> drop(Handler<AsyncResult<Void>> resultHandler) {
        mc.dropCollection(getCollectionName(), resultHandler);
        return this;
    }
}