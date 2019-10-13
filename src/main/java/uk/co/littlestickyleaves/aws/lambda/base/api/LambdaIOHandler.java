package uk.co.littlestickyleaves.aws.lambda.base.api;

/**
 * Interface definition of the Lambda API for input and output.
 * See https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html.
 * It deals with String payloads and does not care about their meaning.
 * It has four options:
 * -- GET input
 * -- POST successful output for a specific input
 * -- POST an error for a specific input
 * -- POST a general initialization error
 */
public interface LambdaIOHandler {

    /**
     * The call made to get the next input available for this lambda
     * https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html#runtimes-api-next
     * @return LambdaInputWithId, a pair of String awsRequestId and String input
     * @throws Exception if there's an error in getting the input from the lambda API
     */
    LambdaInputWithId getLambdaInput() throws Exception;

    /**
     * The call made to return the output on successful completion of the lambda.
     * @param awsRequestId the unique id for this lambda input
     * @param result a String value of the lambda's output
     * @throws Exception if there's an error in communicating with the lambda API
     */
    void returnLambdaOutput(String awsRequestId, String result) throws Exception;

    /**
     * The call made to tell the Lambda API that the Lambda invocation has failed.
     * https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html#runtimes-api-invokeerror
     * @param awsRequestId the unique id for the lambda invocation that resulted in failure
     * @param exception an exception holding information which should be sent to the Lambda API
     * @throws Exception if a problem occurs
     */
    void returnInvocationError(String awsRequestId, Exception exception) throws Exception;

    /**
     * The call made to tell the Lambda API that the initialization of this lambda has failed.
     * https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html#runtimes-api-initerror
     * @param exception an exception holding information which should be sent to the Lambda API
     * @throws Exception if a problem occurs
     */
    void returnInitializationError(Exception exception) throws Exception;
}
