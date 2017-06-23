package info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl;

import static com.google.common.truth.Truth.assertThat;

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

public class BulkInsertTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    private static final String COLLECTION_NAME = "JSON_COLLECTION_BULK_INSERT";

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection(COLLECTION_NAME, getClient());
    }

    @Before
    public void cleanUpDB(TestContext context) {
        testClass.drop(context.asyncAssertSuccess());
    }

    @Test
    public void testBulkInsertWithoutId(TestContext context) {
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"Somekey\" : \"SomeValue\"}"))
                .add(new JsonObject("{\"Somekey2\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"Somekey3\" : \"SomeValue3\"}")).build();

        Async testComplete = context.async();
        testClass.bulkInsert(expected, res -> {
            assertThat(res.result()).isEqualTo(expected.size());
            testClass.findAll(res2 -> {
                TestUtils.containsExactlyIgnoreId(expected, res2.result());
                testComplete.complete();
            });
        });
    }

    @Test
    public void testBulkInsertWithId(TestContext context) {
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"_id\" : \"someId1\", \"Somekey2\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"_id\" : \"someId2\", \"Somekey3\" : \"SomeValue3\"}"))
                .add(new JsonObject("{\"_id\" : \"someId3\", \"Somekey4\" : \"SomeValue4\"}")).build();

        Async testComplete = context.async();
        testClass.bulkInsert(expected, res -> {
            assertThat(res.result()).isEqualTo(expected.size());
            testClass.findAll(res2 -> {
                assertThat(res2.result()).containsExactlyElementsIn(expected);
                testComplete.complete();
            });
        });
    }
}
