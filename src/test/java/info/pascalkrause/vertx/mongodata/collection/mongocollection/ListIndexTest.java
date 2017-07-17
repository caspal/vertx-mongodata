package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.Index;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

public class ListIndexTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "listIndex";

    public ListIndexTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_list_index", COLLECTION_NAME, mds, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(COLLECTION_NAME, res -> {
                beforeEachComplete.complete();
            });
        });
        // There is always the default index on "_id"
        suite.test("Non-empty list", testListIndex(testClass, mc));
    }

    private Handler<TestContext> testListIndex(MongoCollection<JsonObject> testClass, MongoClient mc) {
        return c -> {
            Async testComplete = c.async();
            mc.createIndexWithOptions(COLLECTION_NAME, new JsonObject("{ \"name\" : 1 }"),
                    new IndexOptions().name("TestIndex"), res -> {
                        testClass.listIndexes(res2 -> {
                            TestUtils.runTruthTests(c, v -> {
                                assertThat(res2.result()).contains(new Index("TestIndex", "name"));
                                testComplete.complete();
                            });
                        });
                    });
        };
    }
}
