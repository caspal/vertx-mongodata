package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

public class DeleteIndexTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "deleteIndex";

    public DeleteIndexTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_delete_index", COLLECTION_NAME, mds, mc);
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
        suite.test("Delete", testDeleteIndex(testClass, mc));
    }

    private Handler<TestContext> testDeleteIndex(MongoCollection<JsonObject> testClass, MongoClient mc) {
        return c -> {
            Async testComplete = c.async();
            mc.createIndexWithOptions(COLLECTION_NAME, new JsonObject("{ \"name\" : 1 }"),
                    new IndexOptions().name("TestIndex"), res -> {
                        testClass.deleteIndex("TestIndex", res2 -> {
                            mc.listIndexes(COLLECTION_NAME, res3 -> {
                                JsonObject expected = new JsonObject();
                                expected.put("v", 2);
                                expected.put("key", new JsonObject("{ \"_id\" : 1 }"));
                                expected.put("name", "_id_");
                                expected.put("ns", "test.deleteIndex");
                                TestUtils.runTruthTests(c, v -> {
                                    assertThat(res3.result()).containsExactly(expected);
                                    testComplete.complete();
                                });
                            });
                        });
                    });
        };
    }
}
