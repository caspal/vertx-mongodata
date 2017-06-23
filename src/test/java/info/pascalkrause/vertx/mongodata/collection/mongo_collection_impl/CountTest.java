package info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import info.pascalkrause.vertx.mongodata.JsonCollection;
import info.pascalkrause.vertx.mongodata.MongoTest;
import info.pascalkrause.vertx.mongodata.collection.MongoCollection;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class CountTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection("JSON_COLLECTION_COUNT", getClient());
    }

    @Before
    public void cleanUpDB(TestContext context) {
        testClass.drop(context.asyncAssertSuccess());
    }

    @Test
    public void testCountAll(TestContext context) {
        JsonObject resource = new JsonObject();
        resource.put("SomeKey", "SomeValue");

        Async testComplete = context.async();
        Future<Void> completeFuture = Future.future(v -> testComplete.complete());

        Future<Long> startFuture = Future.future();
        testClass.countAll(startFuture.completer());

        startFuture.compose(res -> {
            assertThat(res).isEqualTo(0);
            Future<String> nextFuture = Future.future();
            testClass.upsert(resource, nextFuture.completer());
            return nextFuture;
        }).compose(res -> {
            Future<Long> nextFuture = Future.future();
            testClass.countAll(nextFuture.completer());
            return nextFuture;
        }).compose(res -> {
            assertThat(res).isEqualTo(1);
        }, completeFuture);
    }

    @Test
    public void testCountQuery(TestContext context) {
        List<JsonObject> inserts = ImmutableList.<JsonObject>builder()
                .add(new JsonObject("{\"SomeKey\" : \"SomeValue\"}"))
                .add(new JsonObject("{\"SomeKey\" : \"SomeValue\"}"))
                .add(new JsonObject("{\"SomeKey3\" : \"SomeValue3\"}")).build();

        Async testComplete = context.async();
        testClass.bulkInsert(inserts, res -> {
            testClass.count(new JsonObject("{\"SomeKey\": \"SomeValue\"}"), res2 -> {
                assertThat(res2.result()).isEqualTo(2);
                testComplete.complete();
            });
        });
    }
}
