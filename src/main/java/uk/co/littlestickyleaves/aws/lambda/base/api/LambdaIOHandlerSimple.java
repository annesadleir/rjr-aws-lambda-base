package uk.co.littlestickyleaves.aws.lambda.base.api;


import uk.co.littlestickyleaves.aws.lambda.base.error.ErrorJsonProvider;
import uk.co.littlestickyleaves.aws.lambda.base.error.LambdaException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of the LambdaIOHandler interface using Java 1.1's HttpUrlConnection.
 * (I got fed up with trying various libraries which caused GraalVM to complain, so I did this old-school implementation.)
 * Q. proper logging sensible or not?
 */
public class LambdaIOHandlerSimple implements LambdaIOHandler {

    private static final String LAMBDA_RUNTIME_AWS_REQUEST_ID = "Lambda-Runtime-Aws-Request-Id";

    private static final String HTTP = "http://";
    private static final String RUNTIME = "/2018-06-01/runtime/";
    private static final String INVOCATION = "invocation/";
    private static final String NEXT = "next";
    private static final String RESPONSE = "/response";
    private static final String INIT = "init";
    private static final String ERROR = "/error";

    private final String runtimeApiRoot;
    private final ErrorJsonProvider errorJsonProvider;

    public LambdaIOHandlerSimple(String runtimeApiEndpoint, ErrorJsonProvider errorJsonProvider) {
        this.runtimeApiRoot = HTTP + runtimeApiEndpoint + RUNTIME;
        this.errorJsonProvider = errorJsonProvider;
    }

    @Override
    public LambdaInputWithId getLambdaInput() throws Exception {
        try {
            URL inputUrl = new URL(runtimeApiRoot + INVOCATION + NEXT);

            System.out.println("Doing GET for next input at " + inputUrl.toString());

            HttpURLConnection httpURLConnection = (HttpURLConnection) inputUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");

            int status = httpURLConnection.getResponseCode();

            if (status > 299) {
                handleHttpProblem("GET for input", status, httpURLConnection);
            }

            String awsRequestId = httpURLConnection.getHeaderField(LAMBDA_RUNTIME_AWS_REQUEST_ID);

            if (awsRequestId == null) {
                throwException("GET for input returned no header value for " + LAMBDA_RUNTIME_AWS_REQUEST_ID);
            }

            String content = contentFromHttpUrlConnection(httpURLConnection, uncheckedInputStreamFetcher());
            return new LambdaInputWithId(awsRequestId, content);

        } catch (IOException exception) {
            throw new LambdaException("GET for input resulted in " + exception.getClass().getSimpleName() +
                    " with message " + exception.getMessage());
        }
    }

    @Override
    public void returnLambdaOutput(String awsRequestId, String result) throws Exception {
        URL outputUrl = new URL(runtimeApiRoot + INVOCATION + awsRequestId + RESPONSE);
        System.out.println("POSTing result for awsRequestId " + awsRequestId + " to " + outputUrl.toString() +
                "\n" + result);

        HttpURLConnection httpURLConnection = setUpPost(outputUrl, result, false);

        int status = httpURLConnection.getResponseCode();

        if (status > 299) {
            handleHttpProblem("POSTing processed output for awsRequestId " + awsRequestId, status, httpURLConnection);
        }
    }

    @Override
    public void returnInvocationError(String awsRequestId, Exception exception) throws Exception {
        URL outputUrl = new URL(runtimeApiRoot + INVOCATION + awsRequestId + ERROR);
        System.out.println("POSTing invocation error for awsRequestId " + awsRequestId + " to " + outputUrl.toString() +
                " with message '" + exception.getMessage() + "'");

        String payload = errorJsonProvider.transform(exception);
        HttpURLConnection httpURLConnection = setUpPost(outputUrl, payload, true);

        int status = httpURLConnection.getResponseCode();
        if (status > 299) {
            handleHttpProblem("POSTing invocation error for awsRequestId " + awsRequestId, status, httpURLConnection);
        }

        System.out.println("Posted processing error for " + awsRequestId + ", receiving: " + status); // proper logging
    }

    @Override
    public void returnInitializationError(Exception exception) throws Exception {
        URL outputUrl = new URL(runtimeApiRoot + INIT + ERROR);
        System.out.println("POSTing initialization error to " + outputUrl.toString() +
                " with message '" + exception.getMessage() + "'");

        String payload = errorJsonProvider.transform(exception);

        HttpURLConnection httpURLConnection = setUpPost(outputUrl, payload, true);
        int status = httpURLConnection.getResponseCode();

        throwException("Runtime unable to continue.  POSTed initialization error, receiving status: " + status);
    }

    private String contentFromHttpUrlConnection(HttpURLConnection connection,
                                                Function<HttpURLConnection, InputStream> streamFetcher) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(streamFetcher.apply(connection)))) {
            return bufferedReader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void handleHttpProblem(String action, int statusCode, HttpURLConnection httpURLConnection) throws LambdaException {
        String errorMessage = action + " resulted in status code " + statusCode;
        try {
            String errorContent = contentFromHttpUrlConnection(httpURLConnection, HttpURLConnection::getErrorStream);
            errorMessage += " with message: '" + errorContent;
        } catch (IOException e) {
            errorMessage += ": unable to read message because of " + e.getClass().getSimpleName() + " with message '" +
                    e.getMessage();
        }
        errorMessage += "'. Exiting with exception";
        throwException(errorMessage);
    }

    private void throwException(String errorMessage) throws LambdaException {
        System.out.println(errorMessage); // todo proper logging?
        throw new LambdaException(errorMessage);
    }

    private HttpURLConnection setUpPost(URL url, String payloadString, boolean error) throws IOException {
        byte[] payloadBytes = payloadString.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        if (error) {
            httpURLConnection.setRequestProperty("Lambda-Runtime-Function-Error-Type", "Unhandled");
        }
        httpURLConnection.setDoOutput(true);

        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(payloadBytes, 0, payloadBytes.length);
        }
        return httpURLConnection;
    }

    private Function<HttpURLConnection, InputStream> uncheckedInputStreamFetcher() {
        return httpUrlConnection -> {
            try {
                return httpUrlConnection.getInputStream();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

}
