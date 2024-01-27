package kuku.OS.service.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import kuku.OS.Models.entity.UserEntity;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class DBService implements IDBService {

    private static DBService instance;
    MongoClient client;
    MongoDatabase database;
    private final String DATABASE = "OS_Learner_GLOBAL";
    private final String USER_COLLECTION = "users";

    private DBService() {
        setupConnection();
    }


    public static DBService instance() {
        if (instance == null) {
            instance = new DBService();
        }
        return instance;
    }

    private void setupConnection() {
        //Create connection
        client = MongoClients.create(getConnectionURL(System.getenv("DB_USER"), System.getenv("DB_PASSWORD")));
        database = client.getDatabase(DATABASE);
    }

    private String getConnectionURL(String user, String password) {
        return "mongodb+srv://" + user + ":" + password + "@cluster0.l7o1grf.mongodb.net/?retryWrites=true&w=majority";
    }

    @Override
    public UserEntity getUser(String id) {
        var coll = database.getCollection(USER_COLLECTION);
        var doc = coll.find(eq("_id", id)).first();


        return UserEntity.ParseDocForUserEntity(doc);

    }
    @Override
    public UserEntity getUser(String id, String pass) {
        var coll = database.getCollection(USER_COLLECTION);
        var doc = coll.find(and(eq("_id", id), eq("password", pass))).first();
        return UserEntity.ParseDocForUserEntity(doc);
    }

    public void setupConnectionTEST(String user, String password) {
        client = MongoClients.create(getConnectionURL(user, password));
        database = client.getDatabase(DATABASE);
    }


}
