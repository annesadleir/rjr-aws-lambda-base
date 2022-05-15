package uk.co.littlestickyleaves.aws.lambda.base.api;


import uk.co.littlestickyleaves.aws.lambda.base.error.ErrorJsonProvider;
import uk.co.littlestickyleaves.aws.lambda.base.error.LambdaException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

/**
 * An implementation of the LambdaIOHandler interface using Java 11's HttpClient.
 * Q. proper logging sensible or not?
 */
public class LambdaIOHandlerSimple implements LambdaIOHandler {

    private static final String LAMBDA_RUNTIME_AWS_REQUEST_ID = "Lambda-Runtime-Aws-Request-Id";
    private static final String APPLICATION_JSON_UTF8 = "application/json; utf-8";

    private static final String HTTP = "http://";
    private static final String RUNTIME = "/2018-06-01/runtime/";
    private static final String INVOCATION = "invocation/";
    private static final String NEXT = "next";
    private static final String RESPONSE = "/response";
    private static final String INIT = "init";
    private static final String ERROR = "/error";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String LAMBDA_RUNTIME_FUNCTION_ERROR_TYPE = "Lambda-Runtime-Function-Error-Type";
    public static final String UNHANDLED = "Unhandled";

    private final HttpClient httpClient;
    private final String runtimeApiRoot;
    private final ErrorJsonProvider errorJsonProvider;

    public LambdaIOHandlerSimple(HttpClient httpClient,
                                 String runtimeApiEndpoint,
                                 ErrorJsonProvider errorJsonProvider) {
        this.httpClient = httpClient;
        this.runtimeApiRoot = HTTP + runtimeApiEndpoint + RUNTIME;
        this.errorJsonProvider = errorJsonProvider;
    }

    @Override
    public LambdaInputWithId getLambdaInput() throws Exception {
        try {
            URI inputUri = URI.create(runtimeApiRoot + INVOCATION + NEXT);
            System.out.println("Doing GET for next input at " + inputUri.getPath());

            HttpRequest httpRequest = HttpRequest.newBuilder(inputUri).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            if (status > 299) {
                throw new LambdaException("GET for input on " + inputUri + " resulted in status code " + status +
                        " with message: '" + body + "'");
            }

            Optional<String> awsRequestId = response.headers().firstValue(LAMBDA_RUNTIME_AWS_REQUEST_ID);
            if (awsRequestId.isEmpty()) {
                throw new LambdaException("GET for input returned no header value for " + LAMBDA_RUNTIME_AWS_REQUEST_ID);
            }

            return new LambdaInputWithId(awsRequestId.get(), body);

        } catch (IOException exception) {
            throw new LambdaException("GET for input resulted in " + exception.getClass().getSimpleName() +
                    " with message " + exception.getMessage());
        }
    }

    @Override
    public void returnLambdaOutput(String awsRequestId, String result) throws Exception {
        URI outputUri = URI.create(runtimeApiRoot + INVOCATION + awsRequestId + RESPONSE);
        System.out.println("POSTing result for awsRequestId " + awsRequestId + " to " + outputUri.getPath() +
                "\n" + result);

        HttpRequest httpRequest = createPostRequest(outputUri, result);
        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        if (status > 299) {
            String errorMessage = "POSTing processed output for awsRequestId " + awsRequestId +
                    " resulted in status code " + status +
                    " with message: '" + response.body() + "'";
            throw new LambdaException(errorMessage);
        }
    }

    @Override
    public void returnInvocationError(String awsRequestId, Exception exception) throws Exception {
        URI outputUri = URI.create(runtimeApiRoot + INVOCATION + awsRequestId + ERROR);
        System.out.println("POSTing invocation error for awsRequestId " + awsRequestId + " to " + outputUri.getPath() +
                " with message '" + exception.getMessage() + "'");

        String payload = errorJsonProvider.transform(exception);
        HttpRequest httpRequest = createPostRequest(outputUri, payload, true);
        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status > 299) {
            throw new LambdaException("POSTing invocation error for awsRequestId " + awsRequestId +
                    " resulted in status code " + status +
                    " with message: '" + response.body() + "'");
        }

        System.out.println("Posted processing error for " + awsRequestId + ", receiving: " + status);
    }

    @Override
    public void returnInitializationError(Exception exception) throws Exception {
        URI outputUri = URI.create(runtimeApiRoot + INIT + ERROR);
        System.out.println("POSTing initialization error to " + outputUri.getPath() +
                " with message '" + exception.getMessage() + "'");

        String payload = errorJsonProvider.transform(exception);
        HttpRequest httpRequest = createPostRequest(outputUri, payload, true);
        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        throw new LambdaException("Runtime unable to continue.  POSTed initialization error, receiving status: " +
                response.statusCode());
    }

    private HttpRequest createPostRequest(URI uri, String content) {
        return createPostRequest(uri, content, false);
    }

    private HttpRequest createPostRequest(URI uri, String content, boolean unhandledError) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(content, StandardCharsets.UTF_8))
                .header(CONTENT_TYPE, APPLICATION_JSON_UTF8);
        if (unhandledError) {
            builder.header(LAMBDA_RUNTIME_FUNCTION_ERROR_TYPE, UNHANDLED);
        }
        return builder.build();
    }

}
