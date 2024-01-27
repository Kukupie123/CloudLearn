package kuku.OS;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.UserEntity;
import kuku.OS.service.APICallService;
import kuku.OS.service.JWTService;
import kuku.OS.util.EnvironmentVariablesUtil;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.Map;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        /**
         * Important Notes
         * We can use STS to get session credentials
         * DefaultCredentialsProvider will assume the role attached to it. so make sure your attached role has the right permissions set.
         */

        String dbEndpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/db-service/users";
        var log = context.getLogger();

        APICallService callService = APICallService.getInstance();
        String method = requestEvent.getHttpMethod().toUpperCase();
        //Global Exception Catcher
        try {
            //Validate if the required environment variables are set
            EnvironmentVariablesUtil.VALIDATE_ENV_VARIABLES();
            String body = requestEvent.getBody();

            switch (method) {
                case "POST": {
                    log.log("POST REQUEST : LOGIN");
                    //CASE LOGIN
                    UserEntity user = gson.fromJson(body, UserEntity.class);
                    log.log("USER : " + user.getUserId() + " " + user.getPassword());
                    Map<String, String> payloadMap = new HashMap<>(); //Create payload to send post request to database service
                    payloadMap.put("action", "user.getUserByIdAndPassword");
                    payloadMap.put("userId", user.getUserId());
                    payloadMap.put("password", user.getPassword());
                    String payload = gson.toJson(payloadMap);
                    HttpResponse response = callService.invokeAWSEndpoint(dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null); //Send Post request to database service
                    String responseString = EntityUtils.toString(response.getEntity());
                    Map respponseMap = gson.fromJson(responseString, Map.class);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        return sendResponse(response.getStatusLine().getStatusCode(), ResponseModel.jsonResponseModel((String) respponseMap.get("msg"), null));
                    }
                    JWTService jwtService = JWTService.getInstance();
                    String token = jwtService.createToken(user.getUserId());
                    return sendResponse(200, ResponseModel.jsonResponseModel("Successfully Generated JWT Token", token));

                }
                case "PUT": {
                    //CASE SIGN UP
                }
            }
            return sendResponse(260, ResponseModel.jsonResponseModel("WTF", null));
        } catch (Exception e) {
            return sendResponse(500, ResponseModel.jsonResponseModel(e.getMessage(), null));

        }
    }

    private APIGatewayProxyResponseEvent sendResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body);
    }

}
