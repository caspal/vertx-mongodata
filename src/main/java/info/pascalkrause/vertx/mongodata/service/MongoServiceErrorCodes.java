package info.pascalkrause.vertx.mongodata.service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Splitter;

public enum MongoServiceErrorCodes {
    ACTION_NOT_FOUND(404, "Action Not Found"),
    UNEXPECTED_ERROR(500, "Unexpected Error");

    private final int statuscode;
    private final String id;
    private final String description;

    private MongoServiceErrorCodes(int statuscode, String description) {
        this.statuscode = statuscode;
        this.description = description;

        Stream<String> descParts = Splitter.on(" ").splitToList(description).stream();
        // Transform "Resource Already Exist" to "REALEX"
        String desc = descParts.map(s -> s.substring(0, 2).toUpperCase()).collect(Collectors.joining(""));
        id = new StringBuilder("[").append(statuscode).append("-").append(desc).append("]").toString();
    }

    public int getStatusCode() {
        return statuscode;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
