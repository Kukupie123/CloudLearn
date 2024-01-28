package kuku.OS.service.db;

import kuku.OS.Models.entity.UserEntity;
import kuku.OS.Models.exceptions.user.UserAlreadySignedUpException;
import org.javatuples.Pair;

public interface IDBService {


    UserEntity getUser(String id);

    UserEntity getUser(String id, String pass);

    Pair<String, Boolean> createUser(String id, String pass) throws UserAlreadySignedUpException;

}
