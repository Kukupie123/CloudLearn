package kuku.OS;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.Models.ResponseModel;
import kuku.OS.Models.entity.UserEntity;
import kuku.OS.Models.exceptions.generic.ExceptionEmptyBody;
import kuku.OS.service.db.DBService;

import java.io.FileNotFoundException;
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

            Map bodyMap = gson.fromJson(body, Map.class);
            String action = (String) bodyMap.get("action");
            DBService service = DBService.instance();

            switch (action) {
                case "user.getUserByIdAndPassword":
                    String userID = (String) bodyMap.get("userId");
                    String password = (String) bodyMap.get("password");
                    UserEntity user = service.getUser(userID, password);
                    if (user == null) {
                        throw new FileNotFoundException("User with ID " + userID + " and Password : " + password + " not found.");
                    }
                    return sendResponse(new ResponseModel<>(null, user), 200);
            }


            UserEntity user = service.getUser("test");
            return new APIGatewayProxyResponseEvent().withBody(gson.toJson(user));

        } catch (NullPointerException | ExceptionEmptyBody e) {
            return sendResponse(new ResponseModel<>(e.getMessage(), null), 400);
        } catch (FileNotFoundException e) {
            return sendResponse(new ResponseModel<>(e.getMessage(), null), 404);
        }

    }

    private APIGatewayProxyResponseEvent sendResponse(ResponseModel resp, int statusCode) {
        return new APIGatewayProxyResponseEvent().withBody(gson.toJson(resp)).withStatusCode(statusCode);
    }
}