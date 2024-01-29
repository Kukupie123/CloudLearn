package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import kuku.OS.enums.ConnectionType;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.UserEntity;
import kuku.OS.model.customExceptions.InvalidAuthorizatonHeaderException;
import kuku.OS.service.APICallService;
import kuku.OS.util.EnvironmentVariablesUtil;
import kuku.OS.util.PayloadHelper;
import org.apache.http.HttpResponse;
import software.amazon.awssdk.regions.Region;

import java.util.Map;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
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
                    System.out.println("POST REQUEST : LOGIN.");
                    log.log("POST REQUEST : LOGIN");
                    //CASE LOGIN
                    UserEntity user = PayloadHelper.parseUserFromPayload(body);
                    String payload = PayloadHelper.createUserPayload(user, "user.getUserByIdAndPassword");
                    HttpResponse response = callService.invokeAWSEndpoint(ConnectionType.POST, dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null); //Send Post request to database service
                    if (response.getStatusLine().getStatusCode() != 200) {
                        String responseModelString = PayloadHelper.parseHttpPayloadToResponseModelString(response);
                        return sendResponse(response.getStatusLine().getStatusCode(), "FAILED DBSERVICE " + responseModelString);
                    }
                    payload = PayloadHelper.createGenerateTokenPayload(Map.of("userId", user.getUserId()));
                    response = callService.invokeAWSEndpoint(ConnectionType.POST, authEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        String responseModelString = PayloadHelper.parseHttpPayloadToResponseModelString(response);
                        return sendResponse(response.getStatusLine().getStatusCode(), "FAILED AUTH SERVICE CALL " + responseModelString);
                    }
                    ResponseModel responseModel = PayloadHelper.parseHttpPayloadToResponseModelObj(response);
                    String token = (String) responseModel.getData();
                    return sendResponse(200, ResponseModel.jsonResponseModel("Successfully Generated JWT Token", token));
                }
                case "PUT" -> {
                    //CASE SIGN UP
                    UserEntity user = PayloadHelper.parseUserFromPayload(body);
                    String payload = PayloadHelper.createUserPayload(user, "user.createUser");
                    HttpResponse response = callService.invokeAWSEndpoint(ConnectionType.POST, dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
                    String responseModelString = PayloadHelper.parseHttpPayloadToResponseModelString(response);
                    return sendResponse(response.getStatusLine().getStatusCode(), responseModelString);
                }
                case "GET" -> {
                    //CASE GET USER USING JWT TOKEN
                    /*
                    1. Get authorization header token
                    2. Send it to AuthService to get claims
                    3. Get userId From claim and send it to DBService to validate
                    4. Return the user from payload
                     */
                }
            }
            return sendResponse(260, ResponseModel.jsonResponseModel("WTF", null));

        } catch (InvalidAuthorizatonHeaderException e) {
            return sendResponse(401, ResponseModel.jsonResponseModel(e.getMessage(), null));
        } catch (Exception e) {
            return sendResponse(500, ResponseModel.jsonResponseModel("USER SERVICE : " + e.getMessage(), null));
        }
    }


    private APIGatewayProxyResponseEvent sendResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body);
    }

}
