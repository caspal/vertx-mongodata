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

public class UpsertTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "upsert";

    public UpsertTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_upsert", COLLECTION_NAME, mds, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(COLLECTION_NAME, res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Upsert with ID", testInsertWithId(testClass, mc));
        suite.test("Upsert without ID", testInsertWithoutId(testClass, mc));
        suite.test("Upsert updating document", testUpdate(testClass, mc));
    }

    private Handler<TestContext> testInsertWithId(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject expected = new JsonObject("{\"_id\" : \"someId\", \"Somekey\" : \"SomeValue\"}");
        return c -> {
            Async testComplete = c.async();
            testClass.upsert(expected, res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isNull());
                mc.find(COLLECTION_NAME, new JsonObject("{\"_id\" : \"someId\"}"), res2 -> {
                    TestUtils.runTruthTests(c, v -> assertThat(res2.result()).containsExactly(expected));
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testInsertWithoutId(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject expected = new JsonObject("{\"Somekey\" : \"SomeValue\"}");
        return c -> {
            Async testComplete = c.async();
            testClass.upsert(expected, res -> {
                expected.put("_id", res.result());
                mc.find(COLLECTION_NAME, new JsonObject("{\"_id\" : \"" + res.result() + "\"}"), res2 -> {
                    TestUtils.runTruthTests(c, v -> assertThat(res2.result()).containsExactly(expected));
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testUpdate(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject initial = new JsonObject("{\"Somekey\" : \"SomeValue\"}");
        JsonObject expected = new JsonObject("{\"OtherKey\" : \"otherValue\"}");
        return c -> {
            Async testComplete = c.async();
            mc.insert(COLLECTION_NAME, initial, res -> {
                expected.put("_id", res.result());
                testClass.upsert(expected, res2 -> {
                    mc.find(COLLECTION_NAME, new JsonObject("{\"_id\" : \"" + res.result() + "\"}"), res3 -> {
                        TestUtils.runTruthTests(c, v -> assertThat(res3.result()).containsExactly(expected));
                        testComplete.complete();
                    });
                });
            });
        };
    }
}
