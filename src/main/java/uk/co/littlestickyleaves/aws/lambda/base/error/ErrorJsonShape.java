package uk.co.littlestickyleaves.aws.lambda.base.error;

import java.util.List;

/**
 * This POJO represents the correct shape for a JSON error message in the AWS Lambda API
 */
public class ErrorJsonShape {

    private String errorType;
    private String errorMessage;
    private List<String> stackTrace;

    public ErrorJsonShape() {
    }

    ErrorJsonShape(String errorType, String errorMessage, List<String> stackTrace) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }
}
