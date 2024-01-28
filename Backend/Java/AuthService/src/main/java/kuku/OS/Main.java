package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.exception.EmptyPayloadException;
import kuku.OS.model.exception.EnvironmentVariableNotFoundException;
import kuku.OS.model.exception.InvalidActionInPayloadException;
import kuku.OS.model.request.BaseRequestModel;
import kuku.OS.model.request.GenerateTokenRequestModel;
import kuku.OS.service.GSONService;
import kuku.OS.service.JWTService;
import kuku.OS.utils.EnvironmentVariablesUtil;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            GSONService gson = GSONService.getInstance();
            JWTService jwtService = JWTService.getInstance();
            EnvironmentVariablesUtil.VALIDATE_ENV_VARIABLES();
            String body = input.getBody();
            if (body == null || body.isBlank() || body.isEmpty()) {
                throw new EmptyPayloadException("Empty Payload");
            }
            BaseRequestModel requestModel = gson.getBodyMap(body);

            switch (requestModel.getAction()) {
                case "generateToken" -> {
                    String token = jwtService.createToken(((GenerateTokenRequestModel) requestModel).getClaims());
                    return sendResponse(ResponseModel.jsonResponseModel(null, token), 200);
                }
            }
            return sendResponse(ResponseModel.jsonResponseModel("GGEZ", "GGEZ"), 404);

        } catch (EmptyPayloadException | EnvironmentVariableNotFoundException | InvalidActionInPayloadException e) {
            return sendResponse(ResponseModel.jsonResponseModel(e.getMessage(), null), 403);
        } catch (Exception e) {
            return sendResponse(ResponseModel.jsonResponseModel(e.getMessage(), null), 500);
        }
    }

    private APIGatewayProxyResponseEvent sendResponse(String body, int statusCode) {
        return new APIGatewayProxyResponseEvent().withBody(body).withStatusCode(statusCode);
    }
}