package info.pascalkrause.vertx.mongodata;

import static com.google.common.truth.Truth.assertThat;

import java.util.Collection;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

public class TestUtils {

    public static void containsExactlyIgnoreId(Collection<JsonObject> expected, Collection<JsonObject> verfiy) {
        Collection<JsonObject> transformed = verfiy.parallelStream().map(j -> {
            JsonObject copy = j.copy();
            copy.remove("_id");
            return copy;
        }).collect(Collectors.toList());
        assertThat(transformed).containsExactlyElementsIn(expected);
    }
}
