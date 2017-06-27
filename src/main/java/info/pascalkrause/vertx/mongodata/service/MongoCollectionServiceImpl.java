package info.pascalkrause.vertx.mongodata.service;

import info.pascalkrause.vertx.mongodata.SimpleAsyncResult;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import info.pascalkrause.vertx.mongodata.datasource.MongoServiceDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

public class MongoCollectionServiceImpl implements MongoService {

    private final EventBus eb;
    private final String serviceAddress;
    private final MongoDataSource mds;

    public MongoCollectionServiceImpl(EventBus eb, String serviceAddress) {
        this.eb = eb;
        this.serviceAddress = serviceAddress;
        mds = new MongoServiceDataSource(eb, serviceAddress);
    }

    @Override
    public void checkAvailability(Handler<AsyncResult<Void>> resultHandler) {
        eb.send(serviceAddress, null, new DeliveryOptions().addHeader(ACTION, CHECK_AVAILABILITY), response -> {
            if (response.failed()) {
                resultHandler.handle(new SimpleAsyncResult<Void>(response.cause()));
                return;
            }
            resultHandler.handle(new SimpleAsyncResult<Void>((Void) null));
        });
    }

    @Override
    public MongoDataSource getDataSource() {
        return mds;
    }
}
