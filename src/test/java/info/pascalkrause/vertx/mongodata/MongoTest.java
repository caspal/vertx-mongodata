package info.pascalkrause.vertx.mongodata;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MongoTest {

    private static Vertx vertx;
    private static MongoTestVerticle mongoVerticle;
    private static MongodExecutable mongodExecutable = null;

    protected static MongoClient getClient() {
        return mongoVerticle.getMongoClient();
    }

    public static MongodExecutable prepareMongo(JsonObject mongoConfig) throws UnknownHostException, IOException {
        Logger logger = LoggerFactory.getLogger(MongoTest.class);
        String bindIp = mongoConfig.getString("host");
        int port = mongoConfig.getInteger("port");
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, false)).build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaultsWithLogger(Command.MongoD, logger)
                .processOutput(ProcessOutput.getDefaultInstanceSilent()).build();

        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        return starter.prepare(mongodConfig);
    }

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("host", "127.0.0.1");
        mongoConfig.put("port", Network.getFreeServerPort());
        mongoConfig.put("db_name", "test");

        mongodExecutable = prepareMongo(mongoConfig);
        mongodExecutable.start();

        System.out.println(new StringBuilder("Embedded MongoDB Started with:\n")
                .append(Json.encodePrettily(mongoConfig)).toString());

        vertx = Vertx.vertx();
        mongoVerticle = new MongoTestVerticle(mongoConfig);
        vertx.deployVerticle(mongoVerticle, context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        mongodExecutable.stop();
        vertx.close(context.asyncAssertSuccess());
    }
}