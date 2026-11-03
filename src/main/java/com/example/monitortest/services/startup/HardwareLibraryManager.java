package com.example.monitortest.services.startup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class HardwareLibraryManager {

    // DLL resources packaged inside EcoScan's application JAR.
    private static final String[] LIBRARIES = {
            "HidSharp.dll",
            "LibreHardwareMonitorLib.dll",
            "System.Memory.dll",
            "System.Numerics.Vectors.dll",
            "System.Runtime.CompilerServices.Unsafe.dll"
    };

    private HardwareLibraryManager() {
        // Utility class; instances are not required.
    }

    // Prepare the native libraries before hardware sensor retrieval begins.
    public static void prepareLibraries(Path directory) throws IOException {
        Files.createDirectories(directory);

        for (String library : LIBRARIES) {
            Path destination = directory.resolve(library);

            // Replace missing or empty DLLs; preserve existing nonempty files.
            if (Files.isRegularFile(destination) && Files.size(destination) > 0) {
                continue;
            }

            try (InputStream input = HardwareLibraryManager.class.getResourceAsStream("/lib/" + library)) {

                if (input == null) {
                    throw new IOException("Missing packaged DLL resource: " + library);
                }

                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
