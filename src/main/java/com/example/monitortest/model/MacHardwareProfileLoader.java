package com.example.monitortest.model;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


// Loads macOS hardware profiles from the application's resources.
public class MacHardwareProfileLoader {

    /**
     * Opens the JSON hardware profile file.
     * @return input stream for the file, or null if unavailable
     */
    public InputStream loadProfiles() {
        // Load the resource from Maven's classpath.
        return getClass().getClassLoader().getResourceAsStream("mac-hardware-profiles.json");
    }

    /**
     * Finds a hardware profile matching the supplied CPU name.
     * Falls back to the default profile if no match exists.
     *
     * @param cpuName CPU name retrieved through OSHI
     * @return matching JSON profile, or null if loading fails
     */
    public JsonObject findProfile(String cpuName) {

        // Open the JSON resource and close it automatically.
        try (InputStream stream = loadProfiles()) {

            if (stream == null) {
                return null;
            }

            // Parse the JSON file into an object.
            JsonObject root = JsonParser.parseReader( new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray profiles = root.getAsJsonArray("profiles");

            // Search for a matching CPU.
            for (JsonElement element : profiles) {

                JsonObject profile = element.getAsJsonObject();
                String profileCpu = profile.get("cpuName").getAsString();
                if (profileCpu.equalsIgnoreCase(cpuName)) {
                    return profile;
                }
            }

            // No CPU matched: use the default dummy values.
            return root.getAsJsonObject("default");

        } catch (IOException | JsonParseException | IllegalStateException e) {
            return null;
        }
    }
}
