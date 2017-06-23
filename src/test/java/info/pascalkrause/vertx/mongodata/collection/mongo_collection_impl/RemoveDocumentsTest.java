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

public class RemoveDocumentsTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection("JSON_COLLECTION_REMOVE", getClient());
    }

    @Before
    public void cleanUpDB(TestContext context) {
        testClass.drop(context.asyncAssertSuccess());
    }

    private static List<JsonObject> INSERTS = ImmutableList.<JsonObject>builder()
            .add(new JsonObject("{\"_id\" : \"someid\", \"Some_key\" : \"SomeValue1\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
            .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();

    @Test
    public void testRemoveDocument(TestContext context) {
        JsonObject toRemove = new JsonObject("{\"_id\" : \"someid\", \"Some_key\" : \"SomeValue1\"}");
        List<JsonObject> expected = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"))
                .add(new JsonObject("{\"Some_key\" : \"SomeValue3\"}")).build();

        Async testComplete = context.async();
        testClass.bulkInsert(INSERTS, res -> {
            testClass.removeDocument(toRemove, res2 -> {
                assertThat(res2.result()).isEqualTo(1);
                testClass.findAll(res3 -> {
                    TestUtils.containsExactlyIgnoreId(expected, res3.result());
                    testComplete.complete();
                });
            });
        });
    }

    @Test
    public void testRemoveDocuments(TestContext context) {
        Async testComplete = context.async();
        testClass.bulkInsert(INSERTS, res -> {
            testClass.removeDocuments(new JsonObject("{\"Some_key\" : \"SomeValue2\"}"), res2 -> {
                assertThat(res2.result()).isEqualTo(2);
                testClass.countAll(res3 -> {
                    assertThat(res3.result()).isEqualTo(2);
                    testComplete.complete();
                });
            });
        });
    }
}
