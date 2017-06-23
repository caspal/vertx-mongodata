package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

public class DropTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "drop";

    public DropTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_drop", COLLECTION_NAME, mds, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.createCollection(COLLECTION_NAME, res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.succeeded()).isTrue());
                beforeEachComplete.complete();
            });
        });
        suite.test("Drop", testDrop(testClass, mc));
    }

    private Handler<TestContext> testDrop(MongoCollection<JsonObject> testClass, MongoClient mc) {
        return c -> {
            Async testComplete = c.async();
            testClass.drop(res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.succeeded()).isTrue());
                mc.getCollections(res2 -> {
                    TestUtils.runTruthTests(c, v -> assertThat(res2.result()).isEmpty());
                    testComplete.complete();
                });
            });
        };
    }
}
