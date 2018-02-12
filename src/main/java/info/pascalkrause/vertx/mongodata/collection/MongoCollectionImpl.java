package info.pascalkrause.vertx.mongodata.collection;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class MongoCollectionImpl<T> implements MongoCollection<T> {

    private final MongoDataSource mds;
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
     * @param collectionName The name of the collection in the database.
     * @param encode A method to transform the resource object into a JsonObject which can be handled by the database.
     * @param decode A method to transform the JsonObject return from the database into a resource object.
     * @param mds A MongoDataSource which is used to talk with the Mongo database.
     */
    public MongoCollectionImpl(String collectionName, Function<T, JsonObject> encode, Function<JsonObject, T> decode,
            MongoDataSource mds) {
        this.collectionName = collectionName;
        this.encode = encode;
        this.decode = decode;
        this.mds = mds;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public MongoCollectionImpl<T> countAll(Handler<AsyncResult<Long>> resultHandler) {
        return count(new JsonObject(), resultHandler);
    }

    @Override
    public MongoCollectionImpl<T> count(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mds.count(collectionName, query, resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> bulkInsert(Collection<T> resources, Handler<AsyncResult<Long>> resultHandler) {
        final List<JsonObject> documents = resources.parallelStream().map(resource -> encode.apply(resource))
                .collect(Collectors.toList());
        mds.bulkInsert(collectionName, documents, resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> upsert(T resource, Handler<AsyncResult<String>> resultHandler) {
        mds.upsert(collectionName, encode.apply(resource), resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> findAll(Handler<AsyncResult<List<T>>> resultHandler) {
        find(new JsonObject(), resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> find(JsonObject query, Handler<AsyncResult<List<T>>> resultHandler) {
        mds.find(collectionName, query, dbResponse -> {
            if (dbResponse.failed()) {
                resultHandler.handle(Future.failedFuture(dbResponse.cause()));
                return;
            }
            final List<T> results = dbResponse.result().parallelStream().map(json -> decode.apply(json))
                    .collect(Collectors.toList());
            resultHandler.handle(Future.succeededFuture(results));
        });
        return this;
    }

    @Override
    public MongoCollectionImpl<T> remove(JsonObject query, Handler<AsyncResult<Long>> resultHandler) {
        mds.removeDocuments(collectionName, query, resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> remove(String id, Handler<AsyncResult<Long>> resultHandler) {
        final JsonObject removeQuery = new JsonObject();
        removeQuery.put("_id", id);
        remove(removeQuery, resultHandler);
        return this;
    }

    @Override
    public MongoCollectionImpl<T> drop(Handler<AsyncResult<Void>> resultHandler) {
        mds.dropCollection(collectionName, resultHandler);
        return this;
    }

    @Override
    public MongoCollection<T> listIndexes(Handler<AsyncResult<List<Index>>> resultHandler) {
        mds.listIndexes(collectionName, res -> {
            if (res.failed()) {
                resultHandler.handle(Future.failedFuture(res.cause()));
                return;
            }
            final List<Index> indexes = res.result().stream().map(o -> ((JsonObject) o)).map(indexObject -> {
                final String name = indexObject.getString("name");
                final boolean unique = indexObject.getBoolean("unique", false);

                final JsonObject key = indexObject.getJsonObject("key");
                final String column = key.fieldNames().stream().findFirst().get();
                final boolean ascending = key.getInteger(column) == 1 ? true : false;

                return new Index(name, column, unique, ascending);
            }).collect(Collectors.toList());
            resultHandler.handle(Future.succeededFuture(indexes));
        });
        return this;
    }

    @Override
    public MongoCollection<T> createIndex(Index index, Handler<AsyncResult<Void>> resultHandler) {
        mds.createIndex(collectionName, index, resultHandler);
        return this;
    }

    @Override
    public MongoCollection<T> deleteIndex(String name, Handler<AsyncResult<Void>> resultHandler) {
        mds.deleteIndex(collectionName, name, resultHandler);
        return this;
    }
}