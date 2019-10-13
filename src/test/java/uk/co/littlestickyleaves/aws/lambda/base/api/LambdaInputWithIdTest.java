package uk.co.littlestickyleaves.aws.lambda.base.api;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LambdaInputWithIdTest {

    @Test
    public void jacksonJrRoundTripToCheckPojoAcceptable() throws IOException {
        // arrange
        LambdaInputWithId lambdaInputWithId = new LambdaInputWithId("id", "input");

        // act
        String serialized = JSON.std.asString(lambdaInputWithId);
        LambdaInputWithId deserialized = JSON.std.beanFrom(LambdaInputWithId.class, serialized);

        // assert
        assertEquals(lambdaInputWithId.getAwsRequestId(), deserialized.getAwsRequestId());
        assertEquals(lambdaInputWithId.getRawInput(), deserialized.getRawInput());
    }
}