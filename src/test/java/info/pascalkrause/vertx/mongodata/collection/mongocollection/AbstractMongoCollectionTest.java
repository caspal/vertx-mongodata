package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.collection.MongoCollectionImpl;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public abstract class AbstractMongoCollectionTest {
    
    private final TestSuite suite;

    public AbstractMongoCollectionTest(String suiteName, String collectionName, MongoDataSource mds, MongoClient mc) {
        suite = TestSuite.create(suiteName);
        suite.before(c -> {
            Async a = c.async();
            mc.dropCollection(collectionName, res -> {
                if (res.failed()) {
                    c.fail(res.cause());
                    return;
                }
                a.complete();
            });
        });
        suite.after(c -> {
            Async a = c.async();
            mc.dropCollection(collectionName, res -> {
                if (res.failed()) {
                    c.fail(res.cause());
                    return;
                }
                a.complete();
            });
        });

        MongoCollection<JsonObject> testClass = new MongoCollectionImpl<>(collectionName, res -> res.copy(),
                res -> res.copy(), mds);
        addTestCases(suite, testClass, mc);
    }

    public abstract void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc);

    public TestSuite getSuite() {
        return suite;
    }
}
