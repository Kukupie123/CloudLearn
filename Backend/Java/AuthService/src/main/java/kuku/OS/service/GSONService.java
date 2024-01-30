package kuku.OS.service;

import com.google.gson.Gson;

public class GSONService {

    private static GSONService instance;
    public final Gson gson;

    public GSONService() {
        this.gson = new Gson();
    }

    public static GSONService getInstance() {
        if (instance == null) {
            instance = new GSONService();
        }
        return instance;
    }
}
