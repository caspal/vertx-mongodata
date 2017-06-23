package info.pascalkrause.vertx.mongodata.collection;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl.BulkInsertTest;
import info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl.CountTest;
import info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl.Find_FindAllTest;
import info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl.RemoveDocumentsTest;
import info.pascalkrause.vertx.mongodata.collection.mongo_collection_impl.UpsertTest;

@RunWith(Suite.class)
@SuiteClasses({ BulkInsertTest.class, CountTest.class, Find_FindAllTest.class, RemoveDocumentsTest.class,
        UpsertTest.class })
public class MongoCollectionImpl_Suite {

}
