package kuku.OS.Models.exceptions.user;

public class UserAlreadySignedUpException extends  Exception{

    public UserAlreadySignedUpException(String message) {
        super(message);
    }
}
