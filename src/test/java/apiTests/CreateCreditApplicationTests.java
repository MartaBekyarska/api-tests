package apiTests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateCreditApplicationTests extends AbstractTestBase {

    static WireMockServer mWireMockServer;
    String requestBody = """
                {
                    "applicantName": "Alice Brown",
                    "amount": 7500,
                    "currency": "GBP",
                    "term": 18
                }
                """.trim();

    @Before
    public void setup() {
        initializeRestAssuredHttp();

        mWireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        mWireMockServer.start();
        setupStubs();
    }

    @After
    public void tearDown() {
        mWireMockServer.stop();
    }

    void setupStubs() {
        mWireMockServer.givenThat(
                WireMock.post("/test/credit-applications")
                        .withHeader(CONTENT_TYPE, containing("application/json"))
                        .withRequestBody(equalToJson(requestBody))
                        .willReturn(aResponse()
                                .withStatus(201)
                                .withBodyFile("post_application_response.json")
                                .withHeader(CONTENT_TYPE, "application/json")
                        )
        );
    }

    @Test
    public void matchPostApplicationRequestAndResponse() {
        // Perform the HTTP request using RestAssured
        final Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/test/credit-applications");

        // Validate the response
        response.then().statusCode(201).contentType(ContentType.JSON);

        // Verify the elements in the creditApplications array
        Map<String, Object> creditApplication = response.jsonPath().getMap("");
        assertThat(creditApplication, notNullValue());
        assertThat(creditApplication.get("id"), is("3"));
        assertThat(creditApplication.get("applicantName"), is("Alice Brown"));
        assertThat(creditApplication.get("amount"), is(7500));
        assertThat(creditApplication.get("currency"), is("GBP"));
        assertThat(creditApplication.get("term"), is(18));
        assertThat(creditApplication.get("status"), is("pending"));
        assertThat(creditApplication.get("applicationDate"), is("2025-02-07"));
    }
}
