package kuku.OS;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.service.APICallService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
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
        APICallService callService = APICallService.getInstance();

        String method = requestEvent.getHttpMethod().toUpperCase();

        String body = requestEvent.getBody();
        Map bodyMap = gson.fromJson(body, Map.class);

        switch (method) {
            case "POST": {
                String userId = (String) bodyMap.get("userId");
                String password = (String) bodyMap.get("password");
                try {
                    Map<String, String> payloadMap = new HashMap<>();
                    payloadMap.put("action", "user.getUserByIdAndPassword");
                    payloadMap.put("userId", userId);
                    payloadMap.put("password", password);
                    String payload = gson.toJson(payloadMap);
                    HttpResponse response = callService.invokeAWSEndpoint(dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
                    String responseString = EntityUtils.toString(response.getEntity());
                    Map respponseMap = gson.fromJson(responseString, Map.class);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        return sendResponse(response.getStatusLine().getStatusCode(), gson.toJson(new ResponseModel<>((String) respponseMap.get("msg"), null)));
                    }
                    //TODO: Return JWT TOKEN
                } catch (IOException e) {
                    return sendResponse(500, gson.toJson(new ResponseModel<>(e.getMessage() + "\n" + e.getStackTrace(), null)));
                }
            }
        }
        return sendResponse(404, gson.toJson(new ResponseModel<>("WTF HAPPENED", null)));
    }

    private APIGatewayProxyResponseEvent sendResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body);
    }
}
