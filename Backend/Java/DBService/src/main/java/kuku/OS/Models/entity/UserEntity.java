package kuku.OS.Models.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.Document;

@AllArgsConstructor
@ToString
@Getter
public class UserEntity {
    private String userID;
    private String password;

    public static UserEntity ParseDocForUserEntity(Document doc) {
        if (doc != null) {
            String userID = (String) doc.get("_id");
            String password = (String) doc.get("password");
            return new UserEntity(userID, password);
        }
        return null;
    }

}
