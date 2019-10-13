package uk.co.littlestickyleaves.aws.lambda.base.api;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Tests an implementation of LambdaIOHandler to see that it fulfils the Open API specification.
 * Available in the test/resources folder, downloaded Oct 2019 from https://docs.aws.amazon.com/lambda/latest/dg/samples/runtime-api.zip
 */
public class LambdaIOHandlerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String LOCALHOST = "localhost";
    private static final int HOST_PORT = 8089;
    private static final String LOCALHOST_PORT = LOCALHOST + ":" + HOST_PORT;

    private static final JSON JSON_JR = JSON.std;

    private static final String AWS_ID = "awsId";

    private LambdaIOHandler testObject;

    @Before
    public void setUp() throws Exception {
        testObject = new LambdaIOHandlerSimple(LOCALHOST_PORT, new ErrorJsonProvider());
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(HOST_PORT);

    ///////// Tests for "/runtime/invocation/next" /////////

    // 200 ok -- runtime continues
    @Test
    public void getLambdaInput200() throws Exception {
        // arrange
        String lambdaInputString = "body";
        stubFor(get(urlEqualTo("/2018-06-01/runtime/invocation/next"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Lambda-Runtime-Aws-Request-Id", AWS_ID)
                        .withBody(lambdaInputString)));

        // act
        LambdaInputWithId result = testObject.getLambdaInput();

        // assert
        assertEquals(AWS_ID, result.getAwsRequestId());
        assertEquals(lambdaInputString, result.getRawInput());
    }

    // 200 ok but no aws request id header -- no point in continuing
    @Test
    public void getLambdaInput200NoId() throws Exception {
        // arrange
        String lambdaInputString = "body";
        stubFor(get(urlEqualTo("/2018-06-01/runtime/invocation/next"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(lambdaInputString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("GET for input returned no header value for Lambda-Runtime-Aws-Request-Id");

        // act
        testObject.getLambdaInput();
    }

    // 403 Forbidden -- no point in continuing
    @Test
    public void getLambdaInput403() throws Exception {
        // arrange
        String errorString = "Forbidden error";
        stubFor(get(urlEqualTo("/2018-06-01/runtime/invocation/next"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody(errorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("GET for input resulted in status code 403 with message: " +
                "'Forbidden error'. Exiting with exception");

        // act
        testObject.getLambdaInput();
    }

    // 500 Container error -- requires exit
    @Test
    public void getLambdaInput500() throws Exception {
        // arrange
        String errorString = "Container error";
        stubFor(get(urlEqualTo("/2018-06-01/runtime/invocation/next"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(errorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("GET for input resulted in status code 500 with message: " +
                "'Container error'. Exiting with exception");

        // act
        testObject.getLambdaInput();
    }

    ///////// Tests for "/runtime/invocation/{AwsRequestId}/response" /////////

    // 202 accepted -- runtime continues
    @Test
    public void returnLambdaOutput202() throws Exception {
        // arrange
        String lambdaResultString = "lambdaResultString";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/response"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withRequestBody(equalTo(lambdaResultString))
                .willReturn(status(202)));

        // act
        testObject.returnLambdaOutput(AWS_ID, lambdaResultString);
    }

    // 400 bad request -- no point in continuing
    @Test
    public void returnLambdaOutput400() throws Exception {
        // arrange
        String lambdaResultString = "lambdaResultString";
        String errorString = "Bad request";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/response"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withRequestBody(equalTo(lambdaResultString))
                .willReturn(status(400)
                        .withBody(errorString)));
        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing processed output for awsRequestId awsId resulted " +
                "in status code 400 with message: 'Bad request'. Exiting with exception");

        // act
        testObject.returnLambdaOutput(AWS_ID, lambdaResultString);
    }

    // 403 forbidden -- no point in continuing
    @Test
    public void returnLambdaOutput403() throws Exception {
        // arrange
        String lambdaResultString = "lambdaResultString";
        String errorString = "Forbidden";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/response"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withRequestBody(equalTo(lambdaResultString))
                .willReturn(status(403)
                        .withBody(errorString)));
        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing processed output for awsRequestId awsId resulted " +
                "in status code 403 with message: 'Forbidden'. Exiting with exception");

        // act
        testObject.returnLambdaOutput(AWS_ID, lambdaResultString);
    }

    // 413 payload too large -- no point in continuing
    @Test
    public void returnLambdaOutput413() throws Exception {
        // arrange
        String lambdaResultString = "lambdaResultString";
        String errorString = "Payload too large";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/response"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withRequestBody(equalTo(lambdaResultString))
                .willReturn(status(413)
                        .withBody(errorString)));
        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing processed output for awsRequestId awsId resulted " +
                "in status code 413 with message: 'Payload too large'. Exiting with exception");

        // act
        testObject.returnLambdaOutput(AWS_ID, lambdaResultString);
    }

    // 500 Container error -- requires exit
    @Test
    public void returnLambdaOutput500() throws Exception {
        // arrange
        String lambdaResultString = "lambdaResultString";
        String errorString = "Container error";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/response"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withRequestBody(equalTo(lambdaResultString))
                .willReturn(status(500)
                .withBody(errorString)));
        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing processed output for awsRequestId awsId resulted " +
                "in status code 500 with message: 'Container error'. Exiting with exception");

        // act
        testObject.returnLambdaOutput(AWS_ID, lambdaResultString);
    }


    ///////// Tests for "/runtime/invocation/{AwsRequestId}/error" /////////

    // 202 accepted -- runtime continues
    @Test
    public void returnInvocationError202() throws Exception {
        // arrange
        String lambdaError = "Something wrong happened";
        Exception exception = new LambdaException(lambdaError);
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(202)));

        // act
        testObject.returnInvocationError(AWS_ID, exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    // 400 bad request -- no point in continuing
    @Test
    public void returnInvocationError400() throws Exception {
        // arrange
        String lambdaError = "Something wrong happened";
        Exception exception = new LambdaException(lambdaError);
        String awsErrorString = "Bad request";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(400)
                .withBody(awsErrorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing invocation error for awsRequestId awsId resulted " +
                "in status code 400 with message: 'Bad request'. Exiting with exception");

        // act
        testObject.returnInvocationError(AWS_ID, exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    // 403 forbidden -- no point in continuing
    @Test
    public void returnInvocationError403() throws Exception {
        // arrange
        String lambdaError = "Something wrong happened";
        Exception exception = new LambdaException(lambdaError);
        String awsErrorString = "Forbidden";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(403)
                        .withBody(awsErrorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing invocation error for awsRequestId awsId resulted " +
                "in status code 403 with message: 'Forbidden'. Exiting with exception");

        // act
        testObject.returnInvocationError(AWS_ID, exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    // 500 Container error -- requires exit
    @Test
    public void returnInvocationError500() throws Exception {
        // arrange
        String lambdaError = "Something wrong happened";
        Exception exception = new LambdaException(lambdaError);
        String awsErrorString = "Container error";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(500)
                        .withBody(awsErrorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("POSTing invocation error for awsRequestId awsId resulted " +
                "in status code 500 with message: 'Container error'. Exiting with exception");

        // act
        testObject.returnInvocationError(AWS_ID, exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/invocation/" + AWS_ID + "/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    ///////// Tests for "/runtime/init/next" /////////

    // 202 accepted -- runtime should exit
    @Test
    public void returnInitializationError202() throws Exception {
        // arrange
        String lambdaError = "Bad activity";
        Exception exception = new RuntimeException(lambdaError);
        stubFor(post(urlEqualTo("/2018-06-01/runtime/init/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(202)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("Runtime unable to continue.  POSTed initialization error, " +
                "receiving status: 202");

        // act
        testObject.returnInitializationError(exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/init/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    // 403 forbidden -- runtime should exit
    @Test
    public void returnInitializationError403() throws Exception {
        // arrange
        String lambdaError = "Bad activity";
        Exception exception = new RuntimeException(lambdaError);
        String awsErrorString = "Forbidden";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/init/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(403)
                .withBody(awsErrorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("Runtime unable to continue.  POSTed initialization error, " +
                "receiving status: 403");

        // act
        testObject.returnInitializationError(exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/init/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    // 500 Container error -- runtime should exit
    @Test
    public void returnInitializationError500() throws Exception {
        // arrange
        String lambdaError = "Bad activity";
        Exception exception = new RuntimeException(lambdaError);
        String awsErrorString = "Forbidden";
        stubFor(post(urlEqualTo("/2018-06-01/runtime/init/error"))
                .withHeader("Content-Type", equalTo("application/json; utf-8"))
                .withHeader("Lambda-Runtime-Function-Error-Type", equalTo("Unhandled"))
                .willReturn(status(500)
                        .withBody(awsErrorString)));

        expectedException.expect(LambdaException.class);
        expectedException.expectMessage("Runtime unable to continue.  POSTed initialization error, " +
                "receiving status: 500");

        // act
        testObject.returnInitializationError(exception);

        // assert
        verify(postRequestedFor(urlMatching("/2018-06-01/runtime/init/error"))
                .andMatching(bodyMatchesAwsErrorFormat(exception.getClass().getName(), lambdaError)));
    }

    private ValueMatcher<Request> bodyMatchesAwsErrorFormat(String className, String error) {
        return request -> {
            String body = request.getBodyAsString();
            try {
                ErrorJsonShape errorJsonShape = JSON_JR.beanFrom(ErrorJsonShape.class, body);
                if (errorJsonShape.getErrorMessage().equals(error)
                        && errorJsonShape.getErrorType().equals(className)
                        && !errorJsonShape.getStackTrace().isEmpty()) {
                    return MatchResult.exactMatch();
                } else {
                    return MatchResult.noMatch();
                }
            } catch (IOException e) {
                return MatchResult.noMatch();
            }
        };
    }

}