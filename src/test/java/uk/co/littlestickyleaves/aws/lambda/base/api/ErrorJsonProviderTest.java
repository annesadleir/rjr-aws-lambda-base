package uk.co.littlestickyleaves.aws.lambda.base.api;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ErrorJsonProviderTest {

    private ErrorJsonProvider testObject;

    @Before
    public void setUp() {
        testObject = new ErrorJsonProvider();
    }

    @Test
    public void simpleException() throws IOException {
        // arrange
        String errorMessage = "Illegal attempt detected";
        LambdaException lambdaException = new LambdaException(errorMessage);

        // act
        String result = testObject.transform(lambdaException);

        // assert
        assertNotNull(result);

        Map<String, Object> jsonMapResult = JSON.std.mapFrom(result);
        assertTrue(jsonMapResult.containsKey("errorMessage"));
        assertEquals(errorMessage, jsonMapResult.get("errorMessage"));
        assertTrue(jsonMapResult.containsKey("errorType"));
        assertEquals(lambdaException.getClass().getName(), jsonMapResult.get("errorType"));
        assertTrue(jsonMapResult.containsKey("stackTrace"));
        List<String> stackTrace = (ArrayList<String>) jsonMapResult.get("stackTrace");
        assertFalse(stackTrace.isEmpty());
    }
}