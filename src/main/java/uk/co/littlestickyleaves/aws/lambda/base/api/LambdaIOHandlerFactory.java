package uk.co.littlestickyleaves.aws.lambda.base.api;

import uk.co.littlestickyleaves.aws.lambda.base.error.ErrorJsonProvider;

import java.net.http.HttpClient;

/**
 * A simple factory to provide a LambdaIOHandler.
 */
public class LambdaIOHandlerFactory {

    public static LambdaIOHandler simple() {
        String runtimeApiEndpoint = System.getenv("AWS_LAMBDA_RUNTIME_API");
        HttpClient httpClient = HttpClient.newHttpClient();
        return new LambdaIOHandlerSimple(httpClient, runtimeApiEndpoint, new ErrorJsonProvider());
    }
}
