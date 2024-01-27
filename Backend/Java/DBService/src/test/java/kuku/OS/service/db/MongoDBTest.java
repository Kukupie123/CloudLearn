package kuku.OS.service.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBTest {

    MongoClient client;
    MongoDatabase db;

    @Before
    public void setup() {
        String password = "kuku";
        String user = "kuku";
        String url = "mongodb+srv://" + user + ":" + password + "@cluster0.l7o1grf.mongodb.net/?retryWrites=true&w=majority";
        client = MongoClients.create(url);
        db = client.getDatabase("OS_Learner_GLOBAL");
    }

    @After
    public void cleanup() {
        if (client != null) client.close();
    }

    @Test
    @Ignore
    public void getUserWithoutPOJOTest() {
        var coll = db.getCollection("users");
        var doc = coll.find(eq("_id", "test")).first();
        Assert.assertEquals("test", doc.get("password"));
    }


}