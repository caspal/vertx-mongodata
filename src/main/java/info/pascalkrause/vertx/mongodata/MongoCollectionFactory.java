package info.pascalkrause.vertx.mongodata;

import java.util.function.Function;

import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.collection.MongoCollectionImpl;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import info.pascalkrause.vertx.mongodata.datasource.MongoServiceDataSource;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoCollectionFactory {

    private final MongoDataSource mds;

    private MongoCollectionFactory(MongoDataSource mds) {
        this.mds = mds;
    }

    public static MongoCollectionFactory using(MongoClientDataSource mds) {
        return new MongoCollectionFactory(mds);
    }

    public static MongoCollectionFactory using(MongoClient client) {
        return new MongoCollectionFactory(new MongoClientDataSource(client));
    }

    public static MongoCollectionFactory using(EventBus eb, String serviceAddress) {
        return new MongoCollectionFactory(new MongoServiceDataSource(eb, serviceAddress));
    }

    public <T> MongoCollection<T> build(String collectionName, Function<T, JsonObject> encode,
            Function<JsonObject, T> decode) {
        return new MongoCollectionImpl<T>(collectionName, encode, decode, mds);
    }
}
