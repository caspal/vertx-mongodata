package info.pascalkrause.vertx.mongodata.collection;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import info.pascalkrause.vertx.mongodata.JsonCollection;
import info.pascalkrause.vertx.mongodata.MongoTest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public class MongoCollectionTest extends MongoTest {

    private static MongoCollection<JsonObject> testClass;

    @BeforeClass
    public static void initTestClass() {
        testClass = new JsonCollection("JSON_COLLECTION", getClient());
    }

    @Test
    public void testCount(TestContext context) {
        JsonObject resource = new JsonObject();
        resource.put("SomeKey", "SomeValue");
        Async testCount = context.async();
        testClass.count(new JsonObject(), res -> {
            assertThat(res.result()).isEqualTo(0);
            testClass.upsert(resource, res1 -> {
                assertThat(res1.succeeded()).isTrue();
                testClass.count(new JsonObject(), res2 -> {
                    assertThat(res2.result()).isEqualTo(1);
                    testCount.complete();
                });
            });
        });
    }
}
