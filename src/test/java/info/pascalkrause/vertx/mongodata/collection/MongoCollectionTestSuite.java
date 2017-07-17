package info.pascalkrause.vertx.mongodata.collection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

import de.flapdoodle.embed.mongo.MongodExecutable;
import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.AbstractMongoCollectionTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.BulkInsertTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.Count_CountAllTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.CreateIndexTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.DeleteIndexTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.DropTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.Find_FindAllTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.ListIndexTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.RemoveTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.UpsertTest;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import info.pascalkrause.vertx.mongodata.service.MongoServiceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MongoCollectionTestSuite {
    private static final String serviceAddress = "MongoCollectionServiceTest";
    private static final String SUITE_NAME_CLIENT = "MongoCollection_Test_Suite_Client__";
    private static final String SUITE_NAME_SERVICE = "MongoCollection_Test_Suite_Service__";
    private static Vertx vertx;

    private static JsonObject mongoConfig;
    private static MongodExecutable mongodExecutable;
    private static MongoClient mc;

    private void startMongoDB() throws IOException {
        mongodExecutable = TestUtils.prepareMongo(mongoConfig);
        mongodExecutable.start();
        System.out.println(new StringBuilder("Embedded MongoDB Started with:\n")
                .append(Json.encodePrettily(mongoConfig)).toString());
    }

    @Before
    public void tearUp() throws IOException {
        mongoConfig = TestUtils.getDefaultMongoConfig();
        startMongoDB();
        vertx = Vertx.vertx();
        mc = MongoClient.createShared(vertx, mongoConfig);
    }

    @After
    public void tearDown(TestContext context) {
        mongodExecutable.stop();
        vertx.close(context.asyncAssertSuccess());
    }

    private List<AbstractMongoCollectionTest> initializeSuites(MongoDataSource mds, String suiteName) {
        List<AbstractMongoCollectionTest> suites = ImmutableList.<AbstractMongoCollectionTest>builder()
                .add(new Find_FindAllTest(suiteName, mds, mc)).add(new UpsertTest(suiteName, mds, mc))
                .add(new RemoveTest(suiteName, mds, mc)).add(new BulkInsertTest(suiteName, mds, mc))
                .add(new CreateIndexTest(suiteName, mds, mc)).add(new ListIndexTest(suiteName, mds, mc))
                .add(new DeleteIndexTest(suiteName, mds, mc)).add(new DropTest(suiteName, mds, mc))
                .add(new Count_CountAllTest(suiteName, mds, mc)).build();
        return suites;
    }

    @Test
    public void testClientDatasource(TestContext testContext) throws IOException {
        Async testComplete = testContext.async();
        // Run Client Tests
        initializeSuites(new MongoClientDataSource(mc), SUITE_NAME_CLIENT).forEach(suite -> {
            suite.getSuite().run(vertx, TestUtils.getTestOptions(vertx)).awaitSuccess(TimeUnit.SECONDS.toMillis(2));
        });
        testComplete.complete();
    }

    @Test
    public void testServiceDatasource(TestContext testContext) throws IOException {
        Async testComplete = testContext.async();
        vertx.deployVerticle(new MongoServiceVerticle(serviceAddress, mongoConfig), deployResponse -> {
            if (deployResponse.failed()) {
                testContext.fail(deployResponse.cause());
                return;
            }
            MongoService ms = MongoService.createEventBusProxy(vertx, serviceAddress);
            List<AbstractMongoCollectionTest> suites = initializeSuites(new MongoClientDataSource(ms),
                    SUITE_NAME_SERVICE);
            vertx.executeBlocking(fut -> {
                suites.forEach(suite -> {
                    try {
                        suite.getSuite().run(vertx, TestUtils.getTestOptions(vertx))
                                .awaitSuccess(TimeUnit.SECONDS.toMillis(2));
                    } catch (Throwable t) {
                        testContext.fail(t);
                        testComplete.complete();
                    }
                });
                fut.complete();
            }, res -> {
                testComplete.complete();
            });
        });
    }
}
