package uk.co.littlestickyleaves.aws.lambda.base;

import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaIOHandler;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaIOHandlerFactory;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaInputWithId;

/**
 * This class ia the core class for the runtime.
 * To start the runtime, create one of these and call loop() on it.
 * It needs a LambdaWorker, which does the specific lambda task.
 */
public class LambdaRunner {

    private final LambdaIOHandler lambdaIOHandler;

    private final LambdaWorker lambdaWorker;

    public LambdaRunner(LambdaIOHandler lambdaIOHandler, LambdaWorker lambdaWorker) {
        this.lambdaIOHandler = lambdaIOHandler;
        this.lambdaWorker = lambdaWorker;
    }

    public LambdaRunner(LambdaWorker lambdaWorker) {
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
