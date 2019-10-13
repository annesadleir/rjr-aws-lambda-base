package uk.co.littlestickyleaves.aws.lambda.base.api;

public class LambdaIOHandlerFactory {

    public static LambdaIOHandler simple() {
        String runtimeApiEndpoint = System.getenv("AWS_LAMBDA_RUNTIME_API");
        return new LambdaIOHandlerSimple(runtimeApiEndpoint, new ErrorJsonProvider());
    }
}
