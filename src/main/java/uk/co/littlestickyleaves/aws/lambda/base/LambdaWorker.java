package uk.co.littlestickyleaves.aws.lambda.base;

public interface LambdaWorker {
    String handleRaw(String rawInput);
}
