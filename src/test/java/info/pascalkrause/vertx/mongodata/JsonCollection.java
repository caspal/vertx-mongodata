package info.pascalkrause.vertx.mongodata;

import info.pascalkrause.vertx.mongodata.collection.AbstractMongoCollection;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class JsonCollection extends AbstractMongoCollection<JsonObject> {

    private final String name;

    public JsonCollection(String name, MongoClient client) {
        super(client);
        this.name = name;
    }

    @Override
    public String getCollectionName() {
        return name;
    }

    @Override
    protected JsonObject encode(JsonObject resource) {
        return resource;
    }

    @Override
    protected JsonObject decode(JsonObject dbResult) {
        return dbResult;
    }
}
