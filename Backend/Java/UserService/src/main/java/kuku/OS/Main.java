package kuku.OS;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;

public class Main implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        /**
         * Important Notes
         * We can use STS to get session credentials
         * DefaultCredentialsProvider will assume the role attached to it. so make sure your attached role has the right permissions set.
         */
        String endpoint = "https://7fkgoc5qr4.execute-api.ap-south-1.amazonaws.com/DEV/private/db-service/users";


        HttpRequestInterceptor interceptor = new AwsRequestSigningApacheInterceptor("execute-api", Aws4Signer.create(), DefaultCredentialsProvider.create(), Region.AP_SOUTH_1);
        HttpPost httpPost = new HttpPost(endpoint);
        try (CloseableHttpClient closeableHttpClient = HttpClients.custom().addInterceptorLast(interceptor).build()) {
            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpPost);
            String responseString = EntityUtils.toString(httpResponse.getEntity());
            return new APIGatewayProxyResponseEvent().withBody(responseString);
        } catch (IOException e) {
            return new APIGatewayProxyResponseEvent().withBody(e.getMessage()).withStatusCode(500);
        }

    }
}
