package kuku.OS;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.Models.ResponseModel;
import kuku.OS.Models.entity.UserEntity;
import kuku.OS.Models.exceptions.generic.ExceptionEmptyBody;
import kuku.OS.Models.exceptions.generic.ExceptionNoActionInPayload;
import kuku.OS.service.db.DBService;
import org.javatuples.Pair;

import java.io.FileNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;


public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private final Gson gson = new Gson();

    //Entry Point for Lambda function

    /**
     * Entry point for Lambda function.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            String body = input.getBody();

            //Validate body
            if (body == null) {
                throw new NullPointerException("Payload is null");
            }
            if (body.isEmpty() || body.isBlank()) {
                throw new ExceptionEmptyBody("Body is empty");
            }

            //NOTE : Since gson.fromJason's second parameter requires a class type we can't pass Map<String,String>.class, so we have to create a ParameterizedType to create a dynamic type using java's reflection power
            Type type = new ParameterizedType() {
                ///Returns the actual generic types. In our case it's a map of <String,String>
                @Override
                public Type[] getActualTypeArguments() {
                    Type[] type = new Type[2];
                    type[0] = String.class;
                    type[1] = String.class;
                    return type;
                }

                ///Returns the raw class that is specifying the generic types
                @Override
                public Type getRawType() {
                    return Map.class;
                }

                ///Returns Super class type.
                @Override
                public Type getOwnerType() {
                    return Object.class;
                }
            };
            Map<String, String> bodyMap = gson.fromJson(body, type);
            String action = bodyMap.get("action");

            if (action == null) {
                throw new ExceptionNoActionInPayload("Payload Has no action");
            }
            DBService service = DBService.instance();

            switch (action) {
                case "user.getUserByIdAndPassword" -> {
                    String userID = bodyMap.get("userId");
                    String password = bodyMap.get("password");
                    UserEntity user = service.getUser(userID, password);
                    if (user == null) {
                        throw new FileNotFoundException("User with ID " + userID + " and Password : " + password + " not found.");
                    }
                    return sendResponse(new ResponseModel<>(null, user), 200);
                }
                case "user.createUser" -> {
                    String userID = bodyMap.get("userId");
                    String password = bodyMap.get("password");
                    Pair<String, Boolean> result = service.createUser(userID, password);
                    if (result.getValue1()) {
                        return sendResponse(new ResponseModel<>("User Created Successfully", null), 200);
                    }
                    return sendResponse(new ResponseModel<>("DB SERVICE : Something went wrong. Failed to create new Use Record", null), 500);
                }
            }

            UserEntity user = service.getUser("test");
            return new APIGatewayProxyResponseEvent().withBody(gson.toJson(user));
        } catch (Exception e) {
            return sendResponse(new ResponseModel<>("DB SERVICE EXCEPTION " + e.getMessage(), null), 500);
        }

    }

    /**
     * Sends a response with the given response model and status code.
     *
     * @param resp       the response model to be sent
     * @param statusCode the status code of the response
     * @return the APIGatewayProxyResponseEvent containing the response and status code
     */
    ///NOTE : private <T> is used to specify the type of T for ResponseModel as it is a class with Generics
    private <T> APIGatewayProxyResponseEvent sendResponse(ResponseModel<T> resp, int statusCode) {
        return new APIGatewayProxyResponseEvent().withBody(gson.toJson(resp)).withStatusCode(statusCode);
    }
}