package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import static com.google.common.truth.Truth.assertThat;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.Index;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

public class CreateIndexTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "createIndex";

    public CreateIndexTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_create_index", COLLECTION_NAME, mds, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(COLLECTION_NAME, res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Create index", testCreateIndex(testClass, mc));
        suite.test("Create index descending", testCreateIndexDescending(testClass, mc));
        suite.test("Create index unique", testCreateIndexUnique(testClass, mc));
    }

    private Handler<TestContext> testCreateIndex(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject expected = new JsonObject();
        expected.put("v", 2);
        expected.put("key", new JsonObject("{ \"name\" : 1 }"));
        expected.put("name", "TestIndex");
        expected.put("ns", "test.createIndex");
        Index i = new Index("TestIndex", "name");
        return c -> {
            Async testComplete = c.async();
            testClass.createIndex(i, res -> {
                mc.listIndexes(COLLECTION_NAME, res2 -> {
                    JsonArray array = res2.result();
                    TestUtils.runTruthTests(c, v -> assertThat(array).contains(expected));
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testCreateIndexDescending(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject expected = new JsonObject();
        expected.put("v", 2);
        expected.put("key", new JsonObject("{ \"name\" : -1 }"));
        expected.put("name", "TestIndex");
        expected.put("ns", "test.createIndex");
        Index i = new Index("TestIndex", "name", false, false);
        return c -> {
            Async testComplete = c.async();
            testClass.createIndex(i, res -> {
                mc.listIndexes(COLLECTION_NAME, res2 -> {
                    JsonArray array = res2.result();
                    TestUtils.runTruthTests(c, v -> assertThat(array).contains(expected));
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testCreateIndexUnique(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject expected = new JsonObject();
        expected.put("v", 2);
        expected.put("key", new JsonObject("{ \"name\" : 1 }"));
        expected.put("name", "TestIndex");
        expected.put("unique", true);
        expected.put("ns", "test.createIndex");
        Index i = new Index("TestIndex", "name", true);
        return c -> {
            Async testComplete = c.async();
            testClass.createIndex(i, res -> {
                mc.listIndexes(COLLECTION_NAME, res2 -> {
                    JsonArray array = res2.result();
                    TestUtils.runTruthTests(c, v -> assertThat(array).contains(expected));
                    testComplete.complete();
                });
            });
        };
    }
}
