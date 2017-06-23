package info.pascalkrause.vertx.mongodata.datasource;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface MongoDataSource {

    /**
     * Count matching documents in a collection.
     *
     * @param collection the collection
     * @param query query used to match documents
     * @param resultHandler will be provided with the number of matching documents
     */
    MongoDataSource count(String collection, JsonObject query, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Find matching documents in the specified collection
     *
     * @param collection the collection
     * @param query query used to match documents
     * @param resultHandler will be provided with list of documents
     */
    MongoDataSource find(String collection, JsonObject query, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    /**
     * 
     * Insert or update a document into the collection.
     * 
     * @param collection the collection
     * @param document The document to insert or update.
     * @param resultHandler A handler to handle the AsnycResult which contains a String with the new id (insert), or
     * null (update).
     * 
     */
    MongoDataSource upsert(String collection, JsonObject document, Handler<AsyncResult<String>> resultHandler);

    /**
     * Insert many documents into the collection.
     * 
     * @param collection the collection
     * @param documents A collection of resources to insert into the collection.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of inserted documents.
     * @return The actual MongoCollection.
     */
    MongoDataSource bulkInsert(String collection, List<JsonObject> documents, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Remove matching documents in a collection.
     * 
     * @param collection the collection
     * @param query A filter for the remove request.
     * @param resultHandler A handler to handle the AsnycResult which contains the number of deleted entries.
     */
    MongoDataSource removeDocuments(String collection, JsonObject query, Handler<AsyncResult<Long>> resultHandler);

    /**
     * Drops the collection.
     * 
     * @param resultHandler A handler which will be called when complete.
     * @param resultHandler will be called when complete
     */
    MongoDataSource dropCollection(String collection, Handler<AsyncResult<Void>> resultHandler);
}
