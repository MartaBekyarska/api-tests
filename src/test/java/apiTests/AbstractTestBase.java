package apiTests;

import io.restassured.RestAssured;

public abstract class AbstractTestBase {
    protected final static int HTTP_ENDPOINT_PORT = 8123;

    protected void initializeRestAssuredHttp() {
        RestAssured.reset();
        RestAssured.port = HTTP_ENDPOINT_PORT;
    }
}
