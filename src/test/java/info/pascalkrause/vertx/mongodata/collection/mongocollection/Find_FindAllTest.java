package info.pascalkrause.vertx.mongodata.collection.mongocollection;

import java.util.List;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestSuite;

public class Find_FindAllTest extends AbstractMongoCollectionTest {

    private static final String COLLECTION_NAME = "findFindAll";

    public Find_FindAllTest(String suitePrefix, MongoDataSource mds, MongoClient mc) {
        super(suitePrefix + "test_method_find_findAll", COLLECTION_NAME, mds, mc);
    }

    private static List<JsonObject> inserts = ImmutableList.<JsonObject>builder()
            .add(new JsonObject("{\"Some_key\" : \"SomeValue1\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();

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
        suite.test("Find Test", c -> {
            List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                    .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
                    .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}")).build();

            Async testComplete = c.async();
            testClass.find(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"), res -> {
                TestUtils.containsExactlyIgnoreId(expected, res.result(), c);
                testComplete.complete();
            });
        });
        suite.test("FindAll Test", c -> {
            Async testComplete = c.async();
            testClass.findAll(res -> {
                TestUtils.containsExactlyIgnoreId(inserts, res.result(), c);
                testComplete.complete();
            });
        });
    }
}
