package uk.co.littlestickyleaves.aws.lambda.base;

/**
 * Interface to define what a LambdaWorker should be able to do.
 * It can take in a String as input, and returns a String.
 * (The specific implementation may use these Strings to hold json objects.)
 */
@FunctionalInterface
public interface LambdaWorker {
    String handleRaw(String rawInput) throws Exception;
}
