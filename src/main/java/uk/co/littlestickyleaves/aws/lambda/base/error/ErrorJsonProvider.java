package uk.co.littlestickyleaves.aws.lambda.base.error;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which can turn an Exception into the sort of json String required by the AWS Lambda API.
 * Uses the ErrorJsonShape POJO
 */
public class ErrorJsonProvider {

    private static final String DEFAULT_JSON = "{\"errorType\":\"JsonSerializationError\", " +
            "\"errorMessage\":\"Failure in serializing exception to Json\"}";

    public String transform(Exception exception) {
        List<String> stackTraceAsStrings = Arrays.stream(exception.getStackTrace())
                .map(Object::toString)
                .collect(Collectors.toList());

        ErrorJsonShape errorJsonShape = new ErrorJsonShape(exception.getClass().getName(),
                exception.getMessage(), stackTraceAsStrings);

        try {
            return JSON.std.asString(errorJsonShape);
        } catch (IOException e) {
            return DEFAULT_JSON;
        }
    }

}
