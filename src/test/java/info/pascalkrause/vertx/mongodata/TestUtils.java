package info.pascalkrause.vertx.mongodata;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestOptions;
import io.vertx.ext.unit.report.ReportOptions;

public class TestUtils {

    public static void runTruthTests(TestContext context, Handler<Void> testCode) {
        try {
            testCode.handle(null);
        } catch (Throwable t) {
            context.fail(t);
        }
    }

    public static void containsExactlyIgnoreId(Collection<JsonObject> expected, Collection<JsonObject> verfiy,
            TestContext context) {
        runTruthTests(context, v -> {
            Collection<JsonObject> transformed = verfiy.parallelStream().map(j -> {
                JsonObject copy = j.copy();
                copy.remove("_id");
                return copy;
            }).collect(Collectors.toList());
            assertThat(transformed).containsExactlyElementsIn(expected);
        });
    }

    public static List<BulkOperation> transformToBulkOps(List<JsonObject> documents) {
        List<BulkOperation> operations = documents.parallelStream()
                .map(document -> BulkOperation.createInsert(document.copy())).collect(Collectors.toList());
        return operations;
    }

    public static MongodExecutable prepareMongo(JsonObject mongoConfig) throws UnknownHostException, IOException {
        Logger logger = LoggerFactory.getLogger(TestUtils.class);
        String bindIp = mongoConfig.getString("host");
        int port = mongoConfig.getInteger("port");
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, false)).build();

        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaultsWithLogger(Command.MongoD, logger)
                .processOutput(ProcessOutput.getDefaultInstanceSilent()).build();

        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        return starter.prepare(mongodConfig);
    }

    public static JsonObject getDefaultMongoConfig() throws IOException {
        JsonObject mongoConfig = new JsonObject();
        mongoConfig.put("host", "127.0.0.1");
        mongoConfig.put("port", Network.getFreeServerPort());
        mongoConfig.put("db_name", "test");
        return mongoConfig;
    }

    public static TestOptions getTestOptions(Vertx vertx) {
        String reportsPath = "./testreports";
        if (!vertx.fileSystem().existsBlocking(reportsPath)) {
            vertx.fileSystem().mkdirBlocking(reportsPath);
        }
        TestOptions opts = new TestOptions().addReporter(new ReportOptions().setTo("console"));
        opts.addReporter(new ReportOptions().setTo("file:./testreports/").setFormat("junit"));
        opts.addReporter(new ReportOptions().setTo("file:./testreports/").setFormat("simple"));
        return opts;
    }
}
