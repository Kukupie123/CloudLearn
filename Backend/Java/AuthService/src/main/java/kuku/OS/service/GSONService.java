package kuku.OS.service;

import com.google.gson.Gson;
import kuku.OS.model.exception.InvalidActionInPayloadException;
import kuku.OS.model.request.BaseRequestModel;
import kuku.OS.model.request.GenerateTokenRequestModel;
import kuku.OS.model.request.ValidateTokenRequestModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class GSONService {
    private static GSONService instance;

    private final Gson gson;

    private GSONService() {
        gson = new Gson();
    }

    public static GSONService getInstance() {
        if (instance == null) {
            instance = new GSONService();
        }
        return instance;
    }

    public BaseRequestModel getBodyMap(String body) throws InvalidActionInPayloadException {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class, String.class};
            }

            @Override
            public Type getRawType() {
                return Map.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        Map<String, String> map = gson.fromJson(body, type);

        String action = map.get("action");

        if (action == null) {
            throw new InvalidActionInPayloadException("Action not found in Payload");
        }

        switch (action) {
            case "generateToken" -> {
                /*
                Payload will look like this
                {
                "action" = "generateToken"
                claims = {
                    "userId" : "user1"
                    }
                }
                 */
                String claims = map.get("claims");
                Map<String, String> claimsMap = gson.fromJson(claims, type);
                return new GenerateTokenRequestModel(action, claimsMap);
            }
            case "validateToken" -> {
                String token = map.get("token");
                return new ValidateTokenRequestModel(action, token);
            }
        }
        throw new InvalidActionInPayloadException("Invalid Action in Payload");
    }
}
