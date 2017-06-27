package info.pascalkrause.vertx.mongodata.service;

import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;

public interface MongoService {

    public static final String ACTION = "action";
    public static final String COLLECTION = "collectionName";

    public static final String CHECK_AVAILABILITY = "checkAvailablity";
    public static final String COUNT = "count";
    public static final String FIND = "find";
    public static final String UPSERT = "upsert";
    public static final String REMOVE = "remove";
    public static final String BULK_INSERT = "bulkInsert";
    public static final String DROP = "drop";

    public static MongoService createInstance(EventBus eb, String serviceAddress) {
        return new MongoServiceImpl(eb, serviceAddress);
    }
    
    public void checkAvailability(Handler<AsyncResult<Void>> resultHandler);

    public MongoDataSource getDataSource();
}
