package kuku.OS.Models.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.Document;

@AllArgsConstructor
@ToString
@Getter
public class UserEntity {
    private String userId;
    private String password;

    public static UserEntity ParseDocForUserEntity(Document doc) {
        if (doc != null) {
            String userID = (String) doc.get("_id");
            String password = (String) doc.get("password");
            return new UserEntity(userID, password);
        }
        return null;
    }

    public static Document parseUserEntityForDoc(UserEntity user) {
        Document doc = new Document();
        doc.append("_id", user.getUserId());
        doc.append("password", user.getPassword());
        return doc;
    }

}
