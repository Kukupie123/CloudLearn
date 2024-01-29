package kuku.OS.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.exception.InvalidEnvironmentVariable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Operations that need to be performed based on actions in the payload are handled by this class.
 */
public class PayloadActionService {
    private PayloadActionService() {
    }

    private static PayloadActionService instance;

    public static PayloadActionService getInstance() {
        if (instance == null) {
            instance = new PayloadActionService();
        }
        return instance;
    }

    /**
     * Expected Request Payload
     * {
     * "action":"generateToken",
     * "claims" :"{"claim1":"value1","claim2":"value2"}" (Key is String NOT a Map/Dictionary. Make sure to Convert the key to String)
     * }
     * Response Payload
     * {
     * "msg" :"Some msg",
     * "data":"generatedJWTToken"
     * }
     */
    public APIGatewayProxyResponseEvent GenerateTokenAction(String body) {
        try {
            //Get claims from payload
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
            Map<String, String> bodyMap = new Gson().fromJson(body, type); //Convert String Body to map
            String claimsString = bodyMap.get("data"); //Get data String. It is claims
            Map<String, String> claims = new Gson().fromJson(claimsString, type);  //Claims is a Map in form of string, so we can get its map representation
            String token = JWTService.getInstance().createToken(claims);
            return new APIGatewayProxyResponseEvent().withBody(ResponseModel.jsonResponseModel("Generated JWT Token", token)).withStatusCode(200);
        } catch (InvalidEnvironmentVariable e) {
            return new APIGatewayProxyResponseEvent().withBody(ResponseModel.jsonResponseModel(e.getMessage(), null)).withStatusCode(403);
        }

    }

    /**
     * Expected Request payload
     * {
     * "action":"validateToken",
     * "token":"token"
     * }
     * Response Payload
     * {
     * "msg":"someMsg",
     * "data":"{"claim1":"value1","claim2":"value2"}"
     * }
     */
    public APIGatewayProxyResponseEvent ValidateToken(String body) {
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
        Map<String, String> bodyMap = new Gson().fromJson(body, type);
        String token = bodyMap.get("token");
        if (token == null || token.isBlank() || token.isEmpty()) {
            return new APIGatewayProxyResponseEvent().withStatusCode(403).withBody(ResponseModel.jsonResponseModel("Token is empty or invalid", null));
        }
        DecodedJWT decodedJWT = JWTService.getInstance().verifyToken(token);
        Map<String, Claim> claimsMap = decodedJWT.getClaims();
        Map<String, String> claimsMapString = new HashMap<>();
        for (String k : claimsMap.keySet()) {
            claimsMapString.put(k, claimsMap.get(k).asString());
        }
        String claimsString = new Gson().toJson(claimsMapString);
        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(ResponseModel.jsonResponseModel(null, claimsString));
    }
}
