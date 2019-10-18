package uk.co.littlestickyleaves.aws.lambda.base.error;

/**
 * An exception for when things go wrong in the Lambda
 */
public class LambdaException extends Exception {

    public LambdaException(String message) {
        super(message);
    }

    public LambdaException(String message, Throwable cause) {
        super(message, cause);
    }
}
