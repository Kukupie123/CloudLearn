package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.customExceptions.InvalidAuthorizatonHeaderException;
import kuku.OS.service.RequestHandlerService;
import kuku.OS.util.EnvironmentVariablesUtil;
import kuku.OS.util.ErrorMsgGenerator;

import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        int errCode = 469;
        String errMsg = "Uninitialised Error Message";
        var log = context.getLogger();
        String method = requestEvent.getHttpMethod().toUpperCase();
        //Global Exception Catcher
        try {
            //Validate if the required environment variables are set
            EnvironmentVariablesUtil.VALIDATE_ENV_VARIABLES();
            String body = requestEvent.getBody();

            switch (method) {
                case "POST" -> {
                    return RequestHandlerService.getInstance().LoginHandler(body);
                }
                case "PUT" -> {
                    //CASE SIGN UP
                    return RequestHandlerService.getInstance().SignUpHandler(body);
                }
                case "GET" -> {
                    //CASE GET USER USING JWT TOKEN
                    //1. Get authorization header token
                    String token = requestEvent.getHeaders().get("Authorization");
                    if (token == null || token.isEmpty() || token.isBlank()) {
                        throw new InvalidAuthorizatonHeaderException("Missing Authorization Header");
                    }

                    return RequestHandlerService.getInstance().GetUserDataWithJWTToken(token);
                }
            }
        } catch (InvalidAuthorizatonHeaderException e) {

            errMsg = ErrorMsgGenerator.generateErrorString(e);
            errCode = 401;
        } catch (Exception e) {
            errMsg = ErrorMsgGenerator.generateErrorString(e);
            errCode = 500;
        }
        return sendResponse(errCode, ResponseModel.jsonResponseModel(errMsg, null));

    }


    private APIGatewayProxyResponseEvent sendResponse(int statusCode, String body) {
        // Create a map for headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*"); // Allow requests from any origin
        headers.put("Access-Control-Allow-Methods", "POST,GET,OPTIONS"); // Allow specific methods
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"); // Allow specific headers

        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body).withHeaders(headers);
    }

}
