package uk.co.littlestickyleaves.aws.lambda.base;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaIOHandler;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaInputWithId;
import uk.co.littlestickyleaves.aws.lambda.base.api.LambdaException;

import static org.mockito.Mockito.*;

public class LambdaRunnerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LambdaIOHandler mockLambdaIOHandler = mock(LambdaIOHandler.class);
    private LambdaWorker mockLambdaWorker = mock(LambdaWorker.class);

    private LambdaRunner<LambdaWorker> testObject;

    @Before
    public void setUp() {
        testObject = new LambdaRunner<>(mockLambdaIOHandler, mockLambdaWorker);
    }

    @Test
    public void failsInitialization() throws Exception {
        // arrange
        Exception exception = new LambdaException("Could not read input");
        when(mockLambdaIOHandler.getLambdaInput()).thenThrow(exception);
        doThrow(new LambdaException("All initialization errors cause system to exit"))
                .when(mockLambdaIOHandler).returnInitializationError(any(Exception.class));
        expectedException.expect(LambdaException.class);

        // act
        testObject.loop();

        // assert
        verify(mockLambdaIOHandler).returnInitializationError(exception);
        verifyNoMoreInteractions(mockLambdaIOHandler, mockLambdaWorker);
    }

    @Test
    public void failsInvocation() throws Exception {
        // arrange
        String id = "id";
        String rawInput = "rawInput";
        LambdaInputWithId inputWithId = new LambdaInputWithId(id, rawInput);
        Exception testEndingException = new LambdaException("Necessary to exit test");
        when(mockLambdaIOHandler.getLambdaInput())
                .thenReturn(inputWithId)
                .thenThrow(testEndingException);
        Exception lambdaFailed = new RuntimeException("Lambda invocation failed");
        when(mockLambdaWorker.handleRaw(rawInput)).thenThrow(lambdaFailed);
        doThrow(new LambdaException("All initialization errors cause system to exit"))
                .when(mockLambdaIOHandler).returnInitializationError(any(Exception.class));
        expectedException.expect(LambdaException.class);

        // act
        testObject.loop();

        // assert
        verify(mockLambdaIOHandler).returnInvocationError(id, lambdaFailed);
        verify(mockLambdaIOHandler).returnInitializationError(testEndingException);
    }

    @Test
    public void successfulRun() throws Exception {
        // arrange
        String id = "id";
        String rawInput = "rawInput";
        LambdaInputWithId inputWithId = new LambdaInputWithId(id, rawInput);
        Exception exception = new LambdaException("Necessary to exit test");
        when(mockLambdaIOHandler.getLambdaInput())
                .thenReturn(inputWithId)
                .thenThrow(exception);
        String rawOutput = "rawOutput";
        when(mockLambdaWorker.handleRaw(rawInput)).thenReturn(rawOutput);
        doThrow(new LambdaException("All initialization errors cause system to exit"))
                .when(mockLambdaIOHandler).returnInitializationError(any(Exception.class));
        expectedException.expect(LambdaException.class);

        // act
        testObject.loop();

        // assert
        verify(mockLambdaIOHandler).returnLambdaOutput(id, rawOutput);
        verify(mockLambdaIOHandler).returnInitializationError(exception);
    }
}