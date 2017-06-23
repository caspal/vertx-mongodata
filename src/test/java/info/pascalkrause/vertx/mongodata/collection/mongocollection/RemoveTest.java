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

public class RemoveTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "remove";

    public RemoveTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_remove", COLLECTION_NAME, mds, mc);
    }

    private static JsonObject objectWithId = new JsonObject("{\"_id\" : \"someId\", \"Somekey\" : \"SomeValue\"}");
    private static List<JsonObject> objectsWithoutId = ImmutableList.<JsonObject>builder()
            .add(new JsonObject("{\"Some_key\" : \"SomeValue1\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();
    private static List<JsonObject> inserts = ImmutableList.<JsonObject>builder().addAll(objectsWithoutId)
            .add(objectWithId).build();

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.beforeEach(c -> {
            Async beforeEachComplete = c.async();
            mc.dropCollection(COLLECTION_NAME, res -> {
                mc.bulkWrite(COLLECTION_NAME, TestUtils.transformToBulkOps(inserts), res2 -> {
                    beforeEachComplete.complete();
                });
            });
        });

        suite.test("Remove by ID", testRemoveById(testClass, mc));
        suite.test("Remove by query", testRemoveQuery(testClass, mc));
    }

    private Handler<TestContext> testRemoveById(MongoCollection<JsonObject> testClass, MongoClient mc) {
        JsonObject toBeRemoved = objectWithId;
        return c -> {
            Async testComplete = c.async();
            testClass.remove(toBeRemoved.getString("_id"), res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isEqualTo(1));
                mc.find(COLLECTION_NAME, new JsonObject(), res2 -> {
                    TestUtils.containsExactlyIgnoreId(objectsWithoutId, res2.result(), c);
                    testComplete.complete();
                });
            });
        };
    }

    private Handler<TestContext> testRemoveQuery(MongoCollection<JsonObject> testClass, MongoClient mc) {
        String idDoNotRemove = objectWithId.getString("_id");
        JsonObject query = new JsonObject("{ \"_id\" : {\"$ne\" : \"" + idDoNotRemove + "\"} }");
        return c -> {
            Async testComplete = c.async();
            testClass.remove(query, res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isEqualTo(objectsWithoutId.size()));
                mc.find(COLLECTION_NAME, new JsonObject(), res2 -> {
                    TestUtils.runTruthTests(c, v -> assertThat(res2.result()).containsExactly(objectWithId));
                    testComplete.complete();
                });
            });
        };
    }
}
