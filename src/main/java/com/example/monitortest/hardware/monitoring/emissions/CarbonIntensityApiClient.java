package com.example.monitortest.hardware.monitoring.emissions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Retrieves Great Britain's current electricity carbon intensity data from the National Energy System Operator (NESO) API.
// https://carbon-intensity.github.io/api-definitions/?utm_source=chatgpt.com#carbon-intensity-api-v2-0-0

public class CarbonIntensityApiClient {

    // Reuse the JSON parser for incoming API responses.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Official endpoint for the current national carbon intensity.
    private static final String API_URL = "https://api.carbonintensity.org.uk/intensity";

    // Reuse a single HTTP client for API requests.
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    // Retrieves the current carbon intensity API response as JSON text.
    // performs network I/O and must not run on Swing's EDT.
    public String fetchCurrentIntensityResponse()
            throws IOException, InterruptedException {

        // Construct the HTTP GET request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        // Send the request and read its response as a string.
        HttpResponse<String> response = httpClient.send( request, HttpResponse.BodyHandlers.ofString() );

        // Reject unsuccessful HTTP responses.
        if (response.statusCode() != 200) {
            throw new IOException( "Carbon intensity API returned HTTP " + response.statusCode() );
        }

        // Return the JSON for parsing in the next stage.
        return response.body();
    }


    // Extracts a valid carbon intensity from the API's JSON response.
    // Prefer the estimated actual value, falling back to the forecast.
    public double parseCarbonIntensity(String json) throws IOException {

        // Parse the response into a structured JSON tree.
        JsonNode root = objectMapper.readTree(json);

        // Retrieve the first national carbon intensity entry.
        JsonNode data = root.path("data");

        if (!data.isArray() || data.isEmpty()) {
            throw new IOException("Carbon intensity response contains no data.");
        }

        // Locate the intensity measurements.
        JsonNode intensity = data.get(0).path("intensity");

        // Prefer the estimated actual value when available.
        JsonNode actual = intensity.path("actual");

        if (isValidIntensity(actual)) {
            return actual.asDouble();
        }

        // Current periods may contain a forecast but no actual value.
        JsonNode forecast = intensity.path("forecast");

        if (isValidIntensity(forecast)) {
            return forecast.asDouble();
        }

        // Let the caller decide how to apply the offline fallback.
        throw new IOException("No valid carbon intensity was returned.");
    }


    // Retrieves the current carbon intensity from the NESO API.
    // Combines the existing HTTP request and JSON parsing operations.
    // This method performs network I/O and must run on a background thread.
    public double fetchCurrentCarbonIntensity() throws IOException, InterruptedException {

        // Retrieve the current API response as JSON text.
        String json = fetchCurrentIntensityResponse();

        // Extract the actual intensity, or forecast when actual is unavailable.
        return parseCarbonIntensity(json);
    }


    // Checks that an API intensity is a finite, non-negative number.
    private boolean isValidIntensity(JsonNode value) {

        return value.isNumber()
                && Double.isFinite(value.asDouble())
                && value.asDouble() >= 0;
    }

}
