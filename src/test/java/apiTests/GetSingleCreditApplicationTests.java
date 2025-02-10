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
import static com.github.tomakehurst.wiremock.common.ContentTypes.ACCEPT;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetSingleCreditApplicationTests extends AbstractTestBase {

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
                WireMock.get("/test/credit-applications/1")
                        .withHeader(ACCEPT, containing("application/json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBodyFile("get_application_by_id_response.json")
                                .withHeader(CONTENT_TYPE, "application/json")
                        )
        );

        mWireMockServer.givenThat(
                WireMock.get("/test/credit-applications/3")
                        .withHeader(ACCEPT, containing("application/json"))
                        .willReturn(aResponse()
                                .withStatus(404)
                                .withBody("Application not found")
                                .withHeader(CONTENT_TYPE, "application/json")
                        )
        );
    }

    @Test
    public void matchGetApplicationByIdRequestAndResponse() {
        final Response response = RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/test/credit-applications/1");

        response.then().statusCode(200).contentType(ContentType.JSON);

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
    public void matchGetApplicationByIdRequestAndResponseNotFound() {
        final Response response = RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get("/test/credit-applications/3");


        response.then().statusCode(404).contentType(ContentType.JSON);
        assertThat(response.getBody().asString(), is("Application not found"));
    }
}
