# rjr-aws-lambda-base
Implements the API for a lambda layer: polls for input text, processes it, and posts back the result.
It has the concept of a LambdaWorker, which is the class that does the work specific to that lambda,
analogous to the RequestHandler when using a standard AWS Java runtime.

This is just the runtime part.  To see how to use it, look at the [rjr-aws-lambda-dummy](https://github.com/annesadleir/rjr-aws-lambda-dummy) repo 
which has an example with a piece of code that just repeats Strings. 
(That repo's readme also has deployment instructions.)

This code is not intended as a general-purpose layer which can be deployed separately as a dynamic base for different lambdas.
Instead it is always intended to be used as a dependency of a specific lambda's code, and compiled into a deployment package with it.
Therefore it can avoid using reflection to instantiate the LambdaWorker at runtime.

There is a `reflect-config.json` file in `META-INF/native-image` for use with GraalVM native compilation.

## todos
* proper logging rather than System.out.println()?
* better error messaging -- since I first wrote this it looks like the Lambda Runtime API takes more specific error types in the header

## how I got here
I used this guide as my starting point, and my code is heavily based on it (though it's not in Kotlin): \
['Fighting cold startup issues for your Kotlin Lambda with GraalVM' by Mathias Düsterhöft](https://medium.com/@mathiasdpunkt/fighting-cold-startup-issues-for-your-kotlin-lambda-with-graalvm-39d19b297730) \
I also used the AWS custom runtime API definition: \
[AWS Lambda Runtime Interface](https://docs.aws.amazon.com/lambda/latest/dg/runtimes-api.html) \
which includes a link to a definition of the API in OpenAPI/Swagger format.  I also used \
['How to Deploy Java Application with Docker and GraalVM' by Vladimír Oraný](https://medium.com/agorapulse-stories/how-to-deploy-java-application-with-docker-and-graalvm-464629d95dbd) \
and various other web resources like the GraalVM docs and other AWS docs.  And lots of StackOverflow, obvs. 

