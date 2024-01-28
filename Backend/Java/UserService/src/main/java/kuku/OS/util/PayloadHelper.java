package kuku.OS.util;

import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.UserEntity;
import kuku.OS.model.customExceptions.InvalidResponseModelPayload;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PayloadHelper {
    private static final Gson gson = new Gson();

    private PayloadHelper() {
    }

    public static UserEntity parseUserFromPayload(String body) {
        return gson.fromJson(body, UserEntity.class);
    }

    public static String createUserPayload(UserEntity user, String action) {

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("action", action);
        payloadMap.put("userId", user.getUserId());
        payloadMap.put("password", user.getPassword());
        return gson.toJson(payloadMap);
    }

    /**
     * Parses Http response whose payload is like Response Model into a Response Model String.
     * Make sure the Payload is structured like Response Model class
     *
     * @return JSON String Payload  of response model
     */
    public static String parseHttpPayloadToResponseModelString(HttpResponse response) throws IOException, InvalidResponseModelPayload {
        String bodyString = EntityUtils.toString(response.getEntity());
        Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> respMap = gson.fromJson(bodyString, type);
        if (respMap.get("data") == null && respMap.get("msg") == null) {
            throw new InvalidResponseModelPayload("The Http response Payload is not structured like Response Model. Its content is " + bodyString);
        }
        return bodyString; //Don't really need to parse we just needed to validate
    }

    /**
     * Parses Http response whose payload is like Response Model into a Response Model Object.
     * Make sure the Payload is structured like Response Model class
     *
     * @return Response Model Object  of Response's Payload
     */
    public static ResponseModel<Object> parseHttpPayloadToResponseModelObj(HttpResponse response) throws InvalidResponseModelPayload, IOException {
        String body = parseHttpPayloadToResponseModelString(response);
        Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> respMap = gson.fromJson(body, type);
        return new ResponseModel<>((String) respMap.get("msg"), respMap.get("data"));
    }
}