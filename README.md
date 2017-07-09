# vertx-mongodata
An async access framework for mongodb

## Requirements
To use this library you have to provide the following libraries:

* io.vertx:vertx-core:3.4.2 or higher
* io.vertx:vertx-mongo-client:3.4.2 or higher

```
// Gradle dependencies

compile group: 'io.vertx', name: 'vertx-core', version: 3.4.2+
compile group: 'io.vertx', name: 'vertx-mongo-client', version: 3.4.2+
```

## MongoCollection
MongoCollection is a tool to store objects of any type in a [MongoDB](https://www.mongodb.com/). It's the nature
of MongoDB, that it can only handle json objects. To store objects of another type, it's necessary to
provide encode and decode functions. MongoCollection helps to bundle all necessary logic at one point.

MongoCollection provides actions to access and manipulate data in a MongoDB. A list of supported actions can
be found [here](https://caspal.github.io/vertx-mongodata/info/pascalkrause/vertx/mongodata/collection/MongoCollection.html).

## MongoCollectionFactory
MongoCollectionFactory is a factory to build MongoCollection instances. An instance of MongoCollectionFactory can be
created by calling the *using* method with an instance of [MongoClient](http://vertx.io/docs/vertx-mongo-client/java/)
or MongoService.

```
    public static MongoCollectionFactory using(MongoClient client);
    public static MongoCollectionFactory using(MongoService ms);
```

The MongoCollectionFactory itsef can create MongoCollection instances by calling the method *build*. The *build*
method needs a collection name and functions to decode and encode the resource type.

```
public <T> MongoCollection<T> build(String collectionName, Function<T, JsonObject> encode, Function<JsonObject, T> decode);
```

### Resource Transformation
If attributes of a resource don't fit to the JSON data types, have a look at [MongoDB extend JSON support](http://vertx.io/docs/vertx-mongo-client/java/#_mongodb_extended_json_support).
This could be very helpful especially in the case that an attribute is a binary.

## MongoService
With MongoService you can access a MongoDB via the Vertx EventBus with all advantages of MongoCollection. Just deploy a
MongoServiceVerticle somewhere in the Vertx cluster and create a MongoService instance.

```
// Create MongoServiceVerticle with serviceAddress and MongoDB configurations
vertx.deployVerticle(new MongoServiceVerticle(serviceAddress, mongoConfig)

// Create a MongoService instance
MongoService ms = MongoSevice.createInstance(eventBus, serviceAddress);
```

## Contribute
We are using Gerrit, so PRs in Github will be ignored. Please use [GerritHub.io](https://review.gerrithub.io)
to contribute changes. The project name is *caspal/vertx-mongodata*

### Code Style
1. Encoding must be in UTF-8.
2. Change must have a commit message.
3. The line endings must be LF (linux).
4. The maximum length of a line should be between 80 and 120 characters.
5. Use spaces instead of tabs.
6. Use 4 spaces for indentation
  * If the project already uses 2, adapt the project rules
8. No trailing whitespaces.
9. Avoid unnecessary empty lines.
10. Adapt your code to the surroundings.
11. Follow the default language style guide.
  * [Java](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf)
  * [JavaScript](http://javascript.crockford.com/code.html)