package info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.vertx.mongodata.JsonCollection;
import info.pascalkrause.vertx.mongodata.MongoTest;
import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class Find_FindAllTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection("JSON_COLLECTION_FIND_ALL", getClient());
    }

    @Before
    public void cleanUpDB(TestContext context) {
        testClass.drop(context.asyncAssertSuccess());
    }

    private static List<JsonObject> INSERTS = ImmutableList.<JsonObject>builder()
            .add(new JsonObject("{\"Some_key\" : \"SomeValue1\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();

    @Test
    public void findTest(TestContext context) {
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}")).build();

        Async testComplete = context.async();
        testClass.bulkInsert(INSERTS, res -> {
            testClass.find(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"), res2 -> {
                TestUtils.containsExactlyIgnoreId(expected, res2.result());
                testComplete.complete();
            });
        });
    }

    @Test
    public void findAllTest(TestContext context) {
        Async testComplete = context.async();
        testClass.bulkInsert(INSERTS, res -> {
            testClass.findAll(res2 -> {
                TestUtils.containsExactlyIgnoreId(INSERTS, res2.result());
                testComplete.complete();
            });
        });
    }
}
