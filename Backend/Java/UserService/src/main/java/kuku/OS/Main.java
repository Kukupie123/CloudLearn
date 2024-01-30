package kuku.OS;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import kuku.OS.model.ResponseModel;
import kuku.OS.model.customExceptions.InvalidAuthorizatonHeaderException;
import kuku.OS.service.APICallService;
import kuku.OS.service.RequestHandlerService;
import kuku.OS.util.EnvironmentVariablesUtil;
import kuku.OS.util.ErrorMsgGenerator;

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
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(body);
    }

}
