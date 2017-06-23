package info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import info.pascalkrause.vertx.mongodata.JsonCollection;
import info.pascalkrause.vertx.mongodata.MongoTest;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class UpsertTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection("JSON_COLLECTION_UPSERT", getClient());
    }

    @Test
    public void testUpsertWithoutId(TestContext context) {
        JsonObject expected = new JsonObject("{\"Somekey\" : \"SomeValue\"}");

        Async testComplete = context.async();
        testClass.upsert(expected, res -> {
            expected.put("_id", res.result());
            testClass.find(new JsonObject("{\"_id\" : \"" + res.result() + "\"}"), res2 -> {
                assertThat(res2.result()).containsExactly(expected);
                testComplete.complete();
            });
        });
    }

    @Test
    public void testUpsertWithId(TestContext context) {
        JsonObject expected = new JsonObject("{\"_id\" : \"someId\", \"Somekey\" : \"SomeValue\"}");

        Async testComplete = context.async();
        testClass.upsert(expected, res -> {
            assertThat(res.result()).isNull();
            testClass.find(new JsonObject("{\"_id\" : \"someId\"}"), res2 -> {
                assertThat(res2.result()).containsExactly(expected);
                testComplete.complete();
            });
        });
    }

    @Test
    public void testInsertAndUpdate(TestContext context) {
        JsonObject initial = new JsonObject("{\"Somekey\" : \"SomeValue\"}");
        JsonObject expected = new JsonObject("{\"OtherKey\" : \"otherValue\"}");

        Async testComplete = context.async();
        testClass.upsert(initial, res -> {
            expected.put("_id", res.result());
            testClass.upsert(expected, res2 -> {
                testClass.find(new JsonObject("{\"_id\" : \"" + res.result() + "\"}"), res3 -> {
                    assertThat(res3.result()).containsExactly(expected);
                    testComplete.complete();
                });
            });
        });
    }
}
