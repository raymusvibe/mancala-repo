package com.bol.games.mancala.data;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MancalaRepositoryIntegrationTests {
    //test against embedded instance
    private static final String DATABASE_NAME = "embedded";

    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;
    private MongoClient mongo;

    @BeforeEach
    public void beforeEach() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        String bindIp = "localhost";
        int port = 12_345;
        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                .build();
        this.mongodExecutable = starter.prepare(mongodConfig);
        this.mongod = mongodExecutable.start();
        this.mongo = MongoClients.create(String.format("mongodb://%s:%d", bindIp, port));
    }

    @AfterEach
    public void afterEach() {
        if (this.mongod != null) {
            this.mongod.stop();
            this.mongodExecutable.stop();
        }
    }

    @Test
    void shouldCreateNewObjectInEmbeddedMongoDb() {
        MongoDatabase mongoDatabase = mongo.getDatabase(DATABASE_NAME);
        mongoDatabase.createCollection("testCollection");
        MongoCollection<BasicDBObject> col = mongoDatabase.getCollection("testCollection", BasicDBObject.class);
        col.insertOne(new BasicDBObject("testDoc", new Date()));
        assertEquals(1L, col.countDocuments(), "There should only be one document");
    }
}
