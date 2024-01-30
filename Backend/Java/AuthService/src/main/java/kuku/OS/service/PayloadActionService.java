package kuku.OS.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.exception.InvalidEnvironmentVariable;
import kuku.OS.model.exception.NoTokenInPayloadException;

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
     * <h2>Expected Request Payload</h2>
     * {
     * "action":"generateToken",
     * "claims" :"{"claim1":"value1","claim2":"value2"}" (Key is String NOT a Map/Dictionary. Make sure to Convert the key to String)
     * }
     * <h2>Response Payload</h2>
     * {
     * "msg" :"Some msg",
     * "data":"generatedJWTToken"
     * }
     */
    public APIGatewayProxyResponseEvent GenerateTokenAction(String body) {
        int errorStatusCode;
        String errorMsg;

        try {
            //Get claims from payload
            Map<String, String> bodyMap = new Gson().fromJson(body, PayloadActionHelper.getDynamicMapType(String.class, String.class)); //Convert String Body to map
            String claimsString = bodyMap.get("claims"); //Get claims
            Map<String, String> claims = new Gson().fromJson(claimsString, PayloadActionHelper.getDynamicMapType(String.class, String.class));  //Claims is a Map in form of string, so we can get its map representation
            String token = JWTService.getInstance().createToken(claims);
            return new APIGatewayProxyResponseEvent().withBody(ResponseModel.jsonResponseModel("Generated JWT Token", token)).withStatusCode(200);
        } catch (InvalidEnvironmentVariable e) {
            errorStatusCode = 403;
            errorMsg = e.getMessage();
        } catch (Exception e) {
            errorStatusCode = 500;
            errorMsg = e.getMessage();
        }
        return new APIGatewayProxyResponseEvent().withBody(ResponseModel.jsonResponseModel(errorMsg, null)).withStatusCode(errorStatusCode);

    }

    /**
     * <h2>Expected Request payload</h2>
     * <br/>
     * {
     * "action":"validateToken",
     * "token":"token"
     * } <br>
     * <h2>Response Payload</h2>
     * <br>
     * {
     * "msg":"someMsg",
     * "data":"{"claim1":"value1","claim2":"value2"}"
     * }
     */
    public APIGatewayProxyResponseEvent ValidateToken(String body) {
        int errorStatusCode;
        String errorMsg;
        try {
            Map<String, String> bodyMap = new Gson().fromJson(body, PayloadActionHelper.getDynamicMapType(String.class, String.class));
            String token = bodyMap.get("token");
            if (token == null || token.isBlank() || token.isEmpty()) {
                throw new NoTokenInPayloadException("Token not found in payload");
            }
            DecodedJWT decodedJWT = JWTService.getInstance().verifyToken(token);
            Map<String, Claim> claimsMap = decodedJWT.getClaims();
            Map<String, String> claimsMapString = new HashMap<>();
            for (String k : claimsMap.keySet()) {
                claimsMapString.put(k, claimsMap.get(k).asString());
            }
            String claimsString = new Gson().toJson(claimsMapString);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(ResponseModel.jsonResponseModel(null, claimsString));
        } catch (NoTokenInPayloadException e) {
            errorStatusCode = 401;
            errorMsg = e.getMessage();
        } catch (Exception e) {
            errorStatusCode = 500;
            errorMsg = e.getMessage();
        }
        return new APIGatewayProxyResponseEvent().withStatusCode(errorStatusCode).withBody(ResponseModel.jsonResponseModel(errorMsg, null));

    }
}

class PayloadActionHelper {
    public static Type getDynamicMapType(Type t1, Type t2) {
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