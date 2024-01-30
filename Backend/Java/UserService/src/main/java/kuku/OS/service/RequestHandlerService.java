package kuku.OS.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import kuku.OS.enums.ConnectionType;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.customExceptions.InvalidResponseModelPayload;
import kuku.OS.util.ErrorMsgGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.regions.Region;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class RequestHandlerService {

    private final String dbEndpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/db-service";
    private final String authEndpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/authService";
    private static RequestHandlerService instance;
    private final Gson gson;

    public RequestHandlerService() {
        gson = new Gson();
    }

    public static RequestHandlerService getInstance() {
        if (instance == null) {
            instance = new RequestHandlerService();
        }
        return instance;
    }

    public APIGatewayProxyResponseEvent LoginHandler(String body) {
        int errCode;
        String errMsg;
        try {
            //1. Extract User from body
            Map<String, String> bodyMap = gson.fromJson(body, RequestHandlerHelper.getMapType(String.class, String.class));
            String userId = bodyMap.get("userId");
            String password = bodyMap.get("password");
            //2. Call DB Service and try to get user by Id and Password
            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("action", "user.getUserByIdAndPassword");
            payloadMap.put("userId", userId);
            payloadMap.put("password", password);
            String payload = gson.toJson(payloadMap);
            HttpResponse response = APICallService.getInstance().invokeAWSEndpoint(ConnectionType.POST, dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
            if (response.getStatusLine().getStatusCode() != 200) {
                return new APIGatewayProxyResponseEvent().withStatusCode(response.getStatusLine().getStatusCode()).withBody(ResponseModel.jsonResponseModel("Failed DB Call For Login. DB's Response = " + EntityUtils.toString(response.getEntity()), null));
            }
            //3. Since user was found in record we can make Auth Service generate JWT Token
            Map<String, String> jwtClaims = new HashMap<>(); //Create claim to store in Jwt token
            jwtClaims.put("userId", userId);
            String jwtClaimsStr = gson.toJson(jwtClaims);
            payloadMap.clear();
            payloadMap.put("action", "generateToken");
            payloadMap.put("claims", jwtClaimsStr);
            payload = gson.toJson(payloadMap);
            response = APICallService.getInstance().invokeAWSEndpoint(ConnectionType.POST, authEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
            if (response.getStatusLine().getStatusCode() != 200) {
                return new APIGatewayProxyResponseEvent().withStatusCode(response.getStatusLine().getStatusCode()).withBody(ResponseModel.jsonResponseModel("Failed Auth Call For Login. Auth's Response = " + EntityUtils.toString(response.getEntity()), null));
            }
            String respPayload = EntityUtils.toString(response.getEntity());
            Map<String, String> respPayloadMap = gson.fromJson(respPayload, RequestHandlerHelper.getMapType(String.class, String.class));
            String token = respPayloadMap.get("data");
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(ResponseModel.jsonResponseModel("Generated JWT Token with userId as Claim", token));
        } catch (Exception e) {
            errCode = 500;
            errMsg = ErrorMsgGenerator.generateErrorString(e);
        }
        return new APIGatewayProxyResponseEvent().withStatusCode(errCode).withBody(ResponseModel.jsonResponseModel(errMsg, null));

    }

    public APIGatewayProxyResponseEvent SignUpHandler(String body) {
        int errCode;
        String errMsg;
        try {

            //1. Parse Payload to get user data
            Map<String, String> bodyMap = gson.fromJson(body, RequestHandlerHelper.getMapType(String.class, String.class));
            String userId = bodyMap.get("userId");
            String password = bodyMap.get("password");
            if (userId == null) {
                throw new InvalidResponseModelPayload("UserId is missing from payload");
            }
            if (password == null) {
                throw new InvalidResponseModelPayload("Password is missing from payload");
            }
            //2. Create Payload and request Auth Service to create user
            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("action", "user.createUser");
            payloadMap.put("userId", userId);
            payloadMap.put("password", password);
            String payload = gson.toJson(payloadMap);
            HttpResponse response = APICallService.getInstance().invokeAWSEndpoint(ConnectionType.POST, dbEndpoint, "execute-api", Region.AP_SOUTH_1, payload, null);
            if (response.getStatusLine().getStatusCode() != 200) {
                return new APIGatewayProxyResponseEvent().withStatusCode(response.getStatusLine().getStatusCode()).withBody(ResponseModel.jsonResponseModel("Failed Db Call For Signup. Db's Response = " + EntityUtils.toString(response.getEntity()), null));
            }
            return new APIGatewayProxyResponseEvent().withBody(ResponseModel.jsonResponseModel("Created user successfully.", null)).withStatusCode(200);

        } catch (Exception e) {
            errCode = 500;
            errMsg = ErrorMsgGenerator.generateErrorString(e);
        }
        return new APIGatewayProxyResponseEvent().withStatusCode(errCode).withBody(ResponseModel.jsonResponseModel(errMsg, null));
    }

}

class RequestHandlerHelper {
    public static Type getMapType(Type t1, Type t2) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{t1, t2};
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
    }

}
