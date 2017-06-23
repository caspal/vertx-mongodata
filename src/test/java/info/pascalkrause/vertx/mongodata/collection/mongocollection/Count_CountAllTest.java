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

public class Count_CountAllTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "countCountAll";

    public Count_CountAllTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_count_countAll", COLLECTION_NAME, mds, mc);
    }

    private static List<JsonObject> inserts = ImmutableList.<JsonObject>builder()
            .add(new JsonObject("{\"Some_key\" : \"SomeValue1\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();

    @Override
    public void addTestCases(TestSuite suite, MongoCollection<JsonObject> testClass, MongoClient mc) {
        suite.before(c -> {
            Async beforeComplete = c.async();
            mc.bulkWrite(COLLECTION_NAME, TestUtils.transformToBulkOps(inserts), res -> {
                beforeComplete.complete();
            });
        });
        suite.test("Count Test", testCount(testClass));
        suite.test("CountAll Test", testCountAll(testClass));
    }

    private Handler<TestContext> testCount(MongoCollection<JsonObject> testClass) {
        return c -> {
            Async testComplete = c.async();
            testClass.count(new JsonObject("{\"Some_key\": \"SomeValue2\"}"), res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isEqualTo(2));
                testComplete.complete();
            });
        };
    }

    private Handler<TestContext> testCountAll(MongoCollection<JsonObject> testClass) {
        return c -> {
            Async testComplete = c.async();
            testClass.countAll(res -> {
                TestUtils.runTruthTests(c, v -> assertThat(res.result()).isEqualTo(4));
                testComplete.complete();
            });
        };
    }
}
