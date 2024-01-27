package kuku.OS.service.db;

import kuku.OS.Models.entity.UserEntity;

public interface IDBService {


    UserEntity getUser(String id);

    UserEntity getUser(String id, String pass);

}
