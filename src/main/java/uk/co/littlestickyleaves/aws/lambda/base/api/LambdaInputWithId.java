package uk.co.littlestickyleaves.aws.lambda.base.api;

/**
 * A POJO to link an AWS Request Id with its corresponding input String.
 */
public class LambdaInputWithId {

    private String awsRequestId;

    private String rawInput;

    public LambdaInputWithId() {
    }

    public LambdaInputWithId(String awsRequestId, String rawInput) {
        this.awsRequestId = awsRequestId;
        this.rawInput = rawInput;
    }

    public void setAwsRequestId(String awsRequestId) {
        this.awsRequestId = awsRequestId;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }

    public String getAwsRequestId() {
        return awsRequestId;
    }

    public String getRawInput() {
        return rawInput;
    }

}
