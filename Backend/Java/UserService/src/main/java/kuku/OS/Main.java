package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.enums.ConnectionType;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.customExceptions.InvalidAuthorizatonHeaderException;
import kuku.OS.service.APICallService;
import kuku.OS.service.RequestHandlerService;
import kuku.OS.util.EnvironmentVariablesUtil;
import kuku.OS.util.ErrorMsgGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        int errCode = 469;
        String errMsg = "Uninitialised Error Message";
        String dbEndpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/db-service";
        String authEndpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/authService";
        var log = context.getLogger();

        APICallService callService = APICallService.getInstance();
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
                    //2. Send it to AuthService to get claims
                    Map<String, String> payloadMap = new HashMap<>();
                    payloadMap.put("action", "validateToken");
                    payloadMap.put("token", token);
                    String payload = new Gson().toJson(payloadMap);
                    HttpResponse response = callService.invokeAWSEndpoint(ConnectionType.POST, authEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new Exception("AuthService did not return 200 status code. It's payload is " + response.getEntity().toString());
                    }
                    //3. Get userId From claim
                    String responsePayload = EntityUtils.toString(response.getEntity());
                    Map responsePayloadMap = new Gson().fromJson(responsePayload, Map.class);
                    String claimsString = (String) responsePayloadMap.get("data");
                    Map claims = new Gson().fromJson(claimsString, Map.class);
                    String userId = (String) claims.get("userId");
                    //4. Send userId to DBService to validate
                    payloadMap.clear();
                    payloadMap.put("action", "user.getUserById");
                    payloadMap.put("userId", userId);
                    response = callService.invokeAWSEndpoint(ConnectionType.POST, dbEndpoint, "execute-api", Region.AP_SOUTH_1, new Gson().toJson(payloadMap), null);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new Exception("DBService did not return 200 status code. It's payload is " + response.getEntity().toString());
                    }
                    //5. Return the user from payload
                    responsePayload = EntityUtils.toString(response.getEntity());
                    responsePayloadMap = new Gson().fromJson(responsePayload, Map.class);
                    Map userJson = (Map) responsePayloadMap.get("data");
                    return sendResponse(200, ResponseModel.jsonResponseModel("Successfully fetched user", userJson));
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
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body);
    }

}
