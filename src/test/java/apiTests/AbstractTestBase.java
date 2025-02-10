package apiTests;

import io.restassured.RestAssured;

/**
 * Abstract base-class for tests containing common constants and methods.
 */
public abstract class AbstractTestBase {
    /* Constant(s): */

    protected final static int HTTP_ENDPOINT_PORT = 8123;
    protected final static int HTTPS_ENDPOINT_PORT = 8443;
    protected static final String BASE_PATH = "/wiremocktest/hello";
    protected static final String BASE_HTTP_URL =
            "http://localhost:" + HTTP_ENDPOINT_PORT + BASE_PATH;
    protected static final String BASE_HTTPS_URL =
            "https://localhost:" + HTTPS_ENDPOINT_PORT + BASE_PATH;
    protected static final int DEFAULT_TIMEOUT = 5000;
    /* Client keystore and truststore. Self-signed. */
    protected static final String CLIENT_KEYSTORE_PATH = "client/client_keystore.jks";
    protected static final String CLIENT_KEYSTORE_PASSWORD = "secret";
    protected static final String CLIENT_TRUSTSTORE_PATH = "client/client_cacerts.jks";
    protected static final String CLIENT_TRUSTSTORE_PASSWORD = "secret";
    /* Server keystore and truststore. Self-signed. */
    protected static final String SERVER_KEYSTORE_PATH = "client/server/server_keystore.jks";
    protected static final String SERVER_KEYSTORE_PASSWORD = "secret";
    protected static final String SERVER_TRUSTSTORE_PATH = "client/server/server_cacerts.jks";
    protected static final String SERVER_TRUSTSTORE_PASSWORD = "secret";

    /**
     * Initializes REST Assured for plain HTTP communication. To be called before each test.
     */
    protected void initializeRestAssuredHttp() {
        RestAssured.reset();
        RestAssured.port = HTTP_ENDPOINT_PORT;
    }
}
