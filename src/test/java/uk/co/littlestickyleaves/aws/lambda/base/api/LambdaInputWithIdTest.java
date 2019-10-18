package uk.co.littlestickyleaves.aws.lambda.base.api;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LambdaInputWithIdTest {

    @Test
    public void jacksonJrRoundTripToCheckPojoAcceptable() throws IOException {
        // arrange
        JSON json = JSON.std;
        LambdaInputWithId lambdaInputWithId = new LambdaInputWithId("id", "input");

        // act
        String serialized = json.asString(lambdaInputWithId);
        LambdaInputWithId deserialized =json.beanFrom(LambdaInputWithId.class, serialized);
        String reserialized = json.asString(deserialized);

        // assert
        assertEquals(lambdaInputWithId.getAwsRequestId(), deserialized.getAwsRequestId());
        assertEquals(lambdaInputWithId.getRawInput(), deserialized.getRawInput());
        assertEquals(serialized, reserialized);
    }
}