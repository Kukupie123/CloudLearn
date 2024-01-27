package kuku.OS.service;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.util.Map;

public class APICallService {

    private static APICallService instance;

    private APICallService() {
    }

    public static APICallService getInstance() {
        if (instance == null) {
            instance = new APICallService();
        }
        return instance;
    }

    /**
     * Invoke an AWS Endpoint with AWS4 Signature. The Credential provider will assume the role attached to it using "DefaultCredentialProvider.create()"
     */
    public HttpResponse invokeAWSEndpoint(String endpoint, String serviceName, Region region, String body, Map<String, String> headers) throws IOException {
        HttpRequestInterceptor interceptor = new AwsRequestSigningApacheInterceptor(serviceName, Aws4Signer.create(), DefaultCredentialsProvider.create(), region);
        HttpPost httpPost = new HttpPost(endpoint);

        // Set body
        StringEntity bodyEntity = new StringEntity(body, ContentType.APPLICATION_JSON);
        httpPost.setEntity(bodyEntity);

        if (headers != null)
            // Set headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }

        try (CloseableHttpClient httpClient = HttpClients.custom().addInterceptorLast(interceptor).build()) {
            return httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
