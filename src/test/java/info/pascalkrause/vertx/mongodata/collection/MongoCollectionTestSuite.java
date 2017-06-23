package info.pascalkrause.vertx.mongodata.collection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;

import de.flapdoodle.embed.mongo.MongodExecutable;
import info.pascalkrause.vertx.mongodata.TestUtils;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.AbstractMongoCollectionTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.BulkInsertTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.Count_CountAllTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.DropTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.Find_FindAllTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.RemoveTest;
import info.pascalkrause.vertx.mongodata.collection.mongocollection.UpsertTest;
import info.pascalkrause.vertx.mongodata.datasource.MongoClientDataSource;
import info.pascalkrause.vertx.mongodata.datasource.MongoDataSource;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@RunWith(JUnit4.class)
public class MongoCollectionTestSuite {
    private static final String SUITE_NAME = "MongoCollection_Test_Suite__";
    private static Vertx vertx;

    private static JsonObject mongoConfig;
    private static MongodExecutable mongodExecutable;
    private static MongoClient mc;

    public static void main(String[] args) {
        MongoCollectionTestSuite suite = new MongoCollectionTestSuite();
        try {
            suite.run();
        } catch (IOException e) {
        } finally {
            suite.tearDown();
        }
    }

    private void startMongoDB() throws IOException {
        mongoConfig = TestUtils.getDefaultMongoConfig();
        mongodExecutable = TestUtils.prepareMongo(mongoConfig);
        mongodExecutable.start();
        System.out.println(new StringBuilder("Embedded MongoDB Started with:\n")
                .append(Json.encodePrettily(mongoConfig)).toString());
    }

    private void tearUp() throws IOException {
        startMongoDB();
        vertx = Vertx.vertx();
        mc = MongoClient.createShared(vertx, mongoConfig);
    }

    private void tearDown() {
        mongodExecutable.stop();
        vertx.close();
    }

    private List<AbstractMongoCollectionTest> initializeTestSuites(MongoDataSource mds) {
        List<AbstractMongoCollectionTest> suites = ImmutableList.<AbstractMongoCollectionTest>builder()
                .add(new Find_FindAllTest(SUITE_NAME, mds, mc))
                .add(new UpsertTest(SUITE_NAME, mds, mc))
                .add(new RemoveTest(SUITE_NAME, mds, mc))
                .add(new BulkInsertTest(SUITE_NAME, mds, mc))
                .add(new DropTest(SUITE_NAME, mds, mc))
                .add(new Count_CountAllTest(SUITE_NAME, mds, mc)).build();
        return suites;
    }

    @Test
    public void run() throws IOException {
        tearUp();
        MongoDataSource mds = new MongoClientDataSource(mc);
        List<AbstractMongoCollectionTest> clientSuites = initializeTestSuites(mds);

        clientSuites.forEach(suite -> {
            suite.getSuite().run(vertx, TestUtils.getTestOptions(vertx)).awaitSuccess(TimeUnit.SECONDS.toMillis(10));
        });
        // Run after all Suits are completed
        tearDown();
    }
}
