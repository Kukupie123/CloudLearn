package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.gson.Gson;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.exception.EmptyPayloadException;
import kuku.OS.model.exception.EnvironmentVariableNotFoundException;
import kuku.OS.model.exception.InvalidActionInPayloadException;
import kuku.OS.service.PayloadActionService;
import kuku.OS.utils.EnvironmentVariablesUtil;

import java.util.Map;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        int errorCode = 469;
        String errorMsg = "Uninitialised Error Message";
        try {
            EnvironmentVariablesUtil.VALIDATE_ENV_VARIABLES();
            String body = input.getBody();
            if (body == null || body.isBlank() || body.isEmpty()) {
                throw new EmptyPayloadException("Empty Payload");
            }
            Map bodyMap = new Gson().fromJson(body, Map.class);
            String action = (String) bodyMap.get("action");
            if (action == null || action.isBlank() || action.isEmpty()) {
                throw new InvalidActionInPayloadException("Invalid Action in Payload");
            }

            switch (action) {
                case "generateToken" -> {
                    return PayloadActionService.getInstance().GenerateTokenAction(body);
                }
                case "validateToken" -> {
                    return PayloadActionService.getInstance().ValidateToken(body);
                }
            }

        } catch (JWTVerificationException e) {
            errorMsg = e.getMessage();
            errorCode = 401;
        } catch (EmptyPayloadException | EnvironmentVariableNotFoundException | InvalidActionInPayloadException e) {
            errorMsg = e.getMessage();
            errorCode = 403;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            errorCode = 500;
        }
        return sendResponse(ResponseModel.jsonResponseModel(errorMsg, null), errorCode);

    }

    private APIGatewayProxyResponseEvent sendResponse(String body, int statusCode) {
        return new APIGatewayProxyResponseEvent().withBody(body).withStatusCode(statusCode);
    }
}