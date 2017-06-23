package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

public class BulkInsertTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "bulkInsert";

    public BulkInsertTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_bulkInsert", COLLECTION_NAME, mds, mc);
    }

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(COLLECTION_NAME, res -> {
                beforeEachComplete.complete();
            });
        });
        suite.test("Bulk insert with Id", testBulkdInsertWithId(testClass, mc));
        suite.test("Bulk insert without Id", testBulkdInsertWithoutId(testClass, mc));
    }

    private Handler<TestContext> testBulkdInsertWithoutId(MongoCollection<JsonObject> testClass, MongoClient mc) {
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"Somekey\" : \"SomeValue\"}"))
                .add(new JsonObject("{\"Somekey2\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"Somekey3\" : \"SomeValue3\"}")).build();
        return c -> {
            Async testComplete = c.async();
            testClass.bulkInsert(expected, res -> {
                assertThat(res.result()).isEqualTo(expected.size());
                mc.find(COLLECTION_NAME, new JsonObject(), res2 -> {
                    TestUtils.containsExactlyIgnoreId(expected, res2.result(), c);
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testBulkdInsertWithId(MongoCollection<JsonObject> testClass, MongoClient mc) {
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"_id\" : \"someId1\", \"Somekey2\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"_id\" : \"someId2\", \"Somekey3\" : \"SomeValue3\"}"))
                .add(new JsonObject("{\"_id\" : \"someId3\", \"Somekey4\" : \"SomeValue4\"}")).build();
        return c -> {
            Async testComplete = c.async();
            testClass.bulkInsert(expected, res -> {
                assertThat(res.result()).isEqualTo(expected.size());
                mc.find(COLLECTION_NAME, new JsonObject(), res2 -> {
                    TestUtils.runTruthTests(c, v -> assertThat(res2.result()).containsExactlyElementsIn(expected));
                    testComplete.complete();
                });
            });
        };
    }
}
