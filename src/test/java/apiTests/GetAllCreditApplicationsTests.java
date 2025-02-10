package apiTests;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.ContentTypes.ACCEPT;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
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

public class GetAllCreditApplicationsTests extends AbstractTestBase {

    static WireMockServer mWireMockServer;

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
            WireMock.post("/test/credit-applications")
                .withHeader(ACCEPT, containing("application/json"))
                .withRequestBody(equalToJson(
                    """
                       {
                          "customerId": "1234"
                       }
                    """.trim()))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBodyFile("get_all_applications_response.json")
                    .withHeader(CONTENT_TYPE, "application/json")
                )
        );

        mWireMockServer.givenThat(
            WireMock.post("/test/credit-applications")
                .withHeader(ACCEPT, containing("application/json"))
                .withRequestBody(equalToJson(
                    """
                       {
                          "customerId": "5678"
                       }
                    """.trim()))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withBodyFile("applications_not_found_response.json")
                    .withHeader(CONTENT_TYPE, "application/json")
                )
        );
    }

    @Test
    public void matchGetAllApplicationsRequestAndResponse() {
        final Response response = RestAssured
            .given()
            .accept(ContentType.JSON)
            .body(
                """
                    {
                        "customerId": "1234"
                    }
                """.trim()
            )
            .when()
            .post("/test/credit-applications");

        response.then().statusCode(200).contentType(ContentType.JSON);

        List<Map<String, Object>> creditApplications = response.jsonPath().getList("creditApplications");
        assertThat(creditApplications, notNullValue());
        Map<String, Object> id1Application = creditApplications.stream()
            .filter(app -> "1" .equals(app.get("creditApplicationId")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Application not found"));
        assertThat(id1Application.get("customerId"), is("1234"));
        assertThat(id1Application.get("applicantName"), is("John Doe"));
        assertThat(id1Application.get("amount"), is(5000));
        assertThat(id1Application.get("currency"), is("GBP"));
        assertThat(id1Application.get("term"), is(12));
        assertThat(id1Application.get("status"), is("approved"));
        assertThat(id1Application.get("applicationDate"), is("2025-02-09"));

        Map<String, Object> id2Application = creditApplications.stream()
            .filter(app -> "2" .equals(app.get("creditApplicationId")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Application not found"));
        assertThat(id2Application.get("customerId"), is("1234"));
        assertThat(id2Application.get("applicantName"), is("John Doe"));
        assertThat(id2Application.get("amount"), is(10000));
        assertThat(id2Application.get("currency"), is("GBP"));
        assertThat(id2Application.get("term"), is(24));
        assertThat(id2Application.get("status"), is("pending"));
        assertThat(id2Application.get("applicationDate"), is("2025-02-08"));
    }

    @Test
    public void matchGetAllApplicationsRequestAndResponseNotFound() {
        final Response response = RestAssured
            .given()
            .accept(ContentType.JSON)
            .body(
                """
                   {
                      "customerId": "5678"
                   }
                """.trim()
            )
            .when()
            .post("/test/credit-applications");

            response.then().statusCode(404).contentType(ContentType.JSON);
            Map<String, Object> errorResponse = response.jsonPath().getMap("");
            assertThat(errorResponse, notNullValue());
            assertThat(errorResponse.get("errorMessage"), is("No applications found for this customer"));
            assertThat(errorResponse.get("errorCode"), is("NOT_FOUND"));
        }
}
