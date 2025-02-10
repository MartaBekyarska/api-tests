package apiTests;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Map;
import static wiremock.com.google.common.net.HttpHeaders.ACCEPT;
import static wiremock.com.google.common.net.HttpHeaders.CONTENT_TYPE;

public class WireMockJUnit4Tests extends AbstractTestBase {

    protected WireMockServer mWireMockServer;
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

    private void setupStubs() {

        mWireMockServer.givenThat(
                WireMock.get("/test/credit-applications")
                        .withHeader(ACCEPT, containing("application/json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBodyFile("get_all_applications_response.json")
                                .withHeader(CONTENT_TYPE, "application/json")
                        )
        );

        mWireMockServer.givenThat(
                WireMock.get("/test/credit-applications/1")
                        .withHeader(ACCEPT, containing("application/json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBodyFile("get_application_by_id_response.json")
                                .withHeader(CONTENT_TYPE, "application/json")
                        )
        );

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
    public void matchGetAllApplicationsRequestAndResponse() {
        // Perform the HTTP request using RestAssured
        final Response response = RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/test/credit-applications");

        // Validate the response
        response.then().statusCode(200).contentType(ContentType.JSON);

        // Verify the elements in the creditApplications array
        List<Map<String, Object>> creditApplications = response.jsonPath().getList("creditApplications");
        assertThat(creditApplications, notNullValue());
        Map<String, Object> id1Application = creditApplications.stream()
                .filter(app -> "1".equals(app.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Application with id: 1 was not found"));
        assertThat(id1Application.get("applicantName"), is("John Doe"));
        assertThat(id1Application.get("amount"), is(5000));
        assertThat(id1Application.get("currency"), is("GBP"));
        assertThat(id1Application.get("term"), is(12));
        assertThat(id1Application.get("status"), is("approved"));
        assertThat(id1Application.get("applicationDate"), is("2025-02-09"));

        Map<String, Object> id2Application = creditApplications.stream()
                .filter(app -> "2".equals(app.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Application with id: 2 was not found"));
        assertThat(id2Application.get("applicantName"), is("Jane Smith"));
        assertThat(id2Application.get("amount"), is(10000));
        assertThat(id2Application.get("currency"), is("GBP"));
        assertThat(id2Application.get("term"), is(24));
        assertThat(id2Application.get("status"), is("pending"));
        assertThat(id2Application.get("applicationDate"), is("2025-02-08"));
    }

    @Test
    public void matchGetApplicationByIdRequestAndResponse() {
        // Perform the HTTP request using RestAssured
        final Response response = RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/test/credit-applications/1");

        // Validate the response
        response.then().statusCode(200).contentType(ContentType.JSON);

        // Verify the elements in the creditApplications array
        Map<String, Object> creditApplication = response.jsonPath().getMap("");
        assertThat(creditApplication, notNullValue());
        assertThat(creditApplication.get("id"), is("1"));
        assertThat(creditApplication.get("applicantName"), is("John Doe"));
        assertThat(creditApplication.get("amount"), is(5000));
        assertThat(creditApplication.get("currency"), is("GBP"));
        assertThat(creditApplication.get("term"), is(12));
        assertThat(creditApplication.get("status"), is("approved"));
        assertThat(creditApplication.get("applicationDate"), is("2025-02-09"));
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
