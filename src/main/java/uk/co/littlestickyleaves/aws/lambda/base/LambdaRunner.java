package uk.co.littlestickyleaves.aws.lambda.base;

import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaIOHandler;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaIOHandlerFactory;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaInputWithId;

public class LambdaRunner<T extends LambdaWorker> {

    private final LambdaIOHandler lambdaIOHandler;

    private final T lambdaWorker;

    public LambdaRunner(LambdaIOHandler lambdaIOHandler, T lambdaWorker) {
        this.lambdaIOHandler = lambdaIOHandler;
        this.lambdaWorker = lambdaWorker;
    }

    public LambdaRunner(T lambdaWorker) {
        this(LambdaIOHandlerFactory.simple(), lambdaWorker);
    }

    public void loop() throws Exception {

        while (true) {
            LambdaInputWithId lambdaInputWithId = null;
            try {
                lambdaInputWithId = lambdaIOHandler.getLambdaInput();
                String result = lambdaWorker.handleRaw(lambdaInputWithId.getRawInput());
                lambdaIOHandler.returnLambdaOutput(lambdaInputWithId.getAwsRequestId(), result);
            } catch (Exception ex) {
                if (lambdaInputWithId == null) {
                    lambdaIOHandler.returnInitializationError(ex);
                } else {
                    lambdaIOHandler.returnInvocationError(lambdaInputWithId.getAwsRequestId(), ex);
                }
            }
        }
    }

}
