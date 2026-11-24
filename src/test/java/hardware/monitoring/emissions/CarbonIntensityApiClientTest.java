package hardware.monitoring.emissions;

import com.example.monitortest.hardware.monitoring.emissions.CarbonIntensityApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for the NESO carbon intensity API response parser.
// Tests use local JSON fixtures and do not require internet access.
class CarbonIntensityApiClientTest {

    // LIV-UT-040
    // Verify that actual intensity takes priority over forecast intensity.
    @Test
    @DisplayName("LIV-UT-040 - Uses actual carbon intensity when available")
    void usesActualCarbonIntensityWhenAvailable() throws IOException {

        // Create the API client without sending a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate an API response containing both actual and forecast values.
        String json = """
                {
                  "data": [
                    {
                      "intensity": {
                        "forecast": 150,
                        "actual": 125,
                        "index": "moderate"
                      }
                    }
                  ]
                }
                """;

        // Parse the carbon intensity from the simulated response.
        double intensity = client.parseCarbonIntensity(json);

        // The actual intensity should take priority over the forecast.
        assertEquals(
                125.0,
                intensity,
                0.001,
                "The API client should prefer the actual intensity."
        );
    }


    // LIV-UT-041
    // Verify that the API client uses the forecast when the actual carbon intensity has not yet been published.
    @Test
    @DisplayName("LIV-UT-041 - Uses forecast carbon intensity when actual is unavailable")
    void usesForecastWhenActualUnavailable() throws IOException {

        // Create the API client without making a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate a response where the actual reading is unavailable.
        String json = """
                {
                  "data": [
                    {
                      "intensity": {
                        "forecast": 145,
                        "actual": null,
                        "index": "moderate"
                      }
                    }
                  ]
                }
                """;

        // Parse the available carbon intensity.
        double intensity = client.parseCarbonIntensity(json);

        // The valid forecast should be selected.
        assertEquals(
                145.0,
                intensity,
                0.001,
                "The API client should use the forecast when actual is null."
        );
    }


    // LIV-UT-042
    // Verify that an invalid actual intensity is rejected
    // while a valid forecast remains available.
    @Test
    @DisplayName("LIV-UT-042 - Uses forecast when actual carbon intensity is invalid")
    void usesForecastWhenActualIntensityIsInvalid() throws IOException {

        // Create the API client without making a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate an invalid negative actual value with a valid forecast.
        String json = """
                {
                  "data": [
                    {
                      "intensity": {
                        "forecast": 140,
                        "actual": -25,
                        "index": "moderate"
                      }
                    }
                  ]
                }
                """;

        // Parse the carbon intensity from the response.
        double intensity = client.parseCarbonIntensity(json);

        // Reject the negative actual value and select the valid forecast.
        assertEquals(
                140.0,
                intensity,
                0.001,
                "The API client should ignore invalid actual readings."
        );
    }


    // LIV-UT-043
    // Verify that the API parser reports a failure when neither actual nor forecast carbon intensity is available.
    @Test
    @DisplayName("LIV-UT-043 - Rejects response when both intensity values are unavailable")
    void rejectsResponseWhenBothIntensityValuesUnavailable() {

        // Create the API client without making a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate an API response with no usable intensity values.
        String json = """
                {
                  "data": [
                    {
                      "intensity": {
                        "forecast": null,
                        "actual": null,
                        "index": "moderate"
                      }
                    }
                  ]
                }
                """;

        // Verify that the parser rejects the incomplete response.
        IOException exception = assertThrows(
                IOException.class,
                () -> client.parseCarbonIntensity(json)
        );

        // Confirm the exception explains why parsing failed.
        assertEquals(
                "No valid carbon intensity was returned.",
                exception.getMessage()
        );
    }



    // LIV-UT-044
    // Verify that an API response without the expected data array is rejected with an informative exception.
    @Test
    @DisplayName("LIV-UT-044 - Rejects API response with missing data array")
    void rejectsResponseWithMissingDataArray() {

        // Create the API client without making a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate valid JSON that does not contain a data array.
        String json = """
                {
                  "message": "No carbon intensity data available"
                }
                """;

        // Verify that parsing the incomplete response throws an exception.
        IOException exception = assertThrows(
                IOException.class,
                () -> client.parseCarbonIntensity(json)
        );

        // Confirm that the exception explains the missing data.
        assertEquals(
                "Carbon intensity response contains no data.",
                exception.getMessage()
        );
    }


    // LIV-UT-045
    // Verify that malformed JSON is rejected without producing an incorrect carbon intensity measurement.
    @Test
    @DisplayName("LIV-UT-045 - Rejects malformed carbon intensity JSON")
    void rejectsMalformedJson() {

        // Create the API client without making a network request.
        CarbonIntensityApiClient client = new CarbonIntensityApiClient();

        // Simulate an invalid JSON response from the API.
        String json = """
                {
                  "data": [
                    {
                      "intensity": {
                        "forecast": 150,
                        "actual": 125
                """;

        // Jackson should report a parsing error as an IOException.
        assertThrows(
                IOException.class,
                () -> client.parseCarbonIntensity(json),
                "Malformed JSON should not produce a carbon intensity value."
        );
    }




}
