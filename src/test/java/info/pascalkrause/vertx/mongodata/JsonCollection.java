package info.pascalkrause.vertx.mongodata;

import info.pascalkrause.vertx.mongodata.collection.MongoCollectionImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class JsonCollection extends MongoCollectionImpl<JsonObject> {

    public JsonCollection(String collectionName, MongoClient client) {
        super(collectionName, j -> j.copy() , j -> j.copy(), client);
    }
}