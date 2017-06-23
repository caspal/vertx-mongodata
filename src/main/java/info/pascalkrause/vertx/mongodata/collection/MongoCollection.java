package info.pascalkrause.vertx.mongodata.collection;

import java.util.Collection;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface MongoCollection<T> {

    /**
     *
     * @return The name of the collection.
     */
    public String getCollectionName();

    /**
     * Count matching entries in a collection.
     * 
     * @param query A filter for the count request. For counting all entries in a collection use
     * {@link #countAll(Handler)}.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of matching entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> count(JsonObject query, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Count all entries in a collection.
     * 
     * @param resultHandler A handler to handle the AsnycResult which contains the number of all entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> countAll(Handler<AsyncResult<Long>> resultHandler);

    /**
     * Finds matching entries in a collection.
     * 
     * @param query A filter for the find request. For finding all entries in a collection use
     * {@link #findAll(Handler)}.
     * @param resultHandler A handler to handle the AsnycResult which contains a List with all matching entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> find(JsonObject query, Handler<AsyncResult<List<T>>> resultHandler);

    /**
     * Finds all entries in a collection.
     * 
     * @param resultHandler A handler to handle the AsnycResult which contains a List with all entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> findAll(Handler<AsyncResult<List<T>>> resultHandler);

    /**
     * Insert or update a resource into the collection.
     * 
     * @param resource The resource to insert or update.
     * @param resultHandler A handler to handle the AsnycResult which contains a String with the new id (insert), or
     * null (update).
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> upsert(T resource, Handler<AsyncResult<String>> resultHandler);

    /**
     * Inserts many resources into the collection.
     * 
     * @param resources A collection of resources to insert into the collection.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of inserted resources.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> bulkInsert(Collection<T> resources, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Removes the resource with the passed ID from the collection.
     * 
     * @param id The ID of the resource to delete.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of deleted entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> remove(String id, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Remove matching entries in a collection.
     * 
     * @param query A filter for the remove request.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of deleted entries.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> remove(JsonObject query, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Drop the collection.
     * 
     * @param resultHandler A handler which will be called when complete.
     * @return The actual MongoCollection.
     */
    public MongoCollection<T> drop(Handler<AsyncResult<Void>> resultHandler);
}
