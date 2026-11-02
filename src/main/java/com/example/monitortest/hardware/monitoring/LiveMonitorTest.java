package com.example.monitortest.hardware.monitoring;

//Temporary test for repeated CPU and GPU power measurements.
public class LiveMonitorTest {

    public static void main(String[] args) {

        LiveHardwareReader reader = new LiveHardwareReader();

        // Retrieve five snapshots to check sensor updates and retrieval speed.
        for (int i = 1; i <= 5; i++) {

            long startTime = System.nanoTime();

            LiveMonitorSnapshot snapshot = reader.readSnapshot();

            long elapsedTime = System.nanoTime() - startTime;
            double elapsedMs = elapsedTime / 1_000_000.0;

            System.out.println("Reading " + i);
            System.out.printf("CPU Power: %.1f W%n", snapshot.cpuPower());
            System.out.printf("GPU Power: %.1f W%n", snapshot.gpuPower());
            System.out.printf("Retrieval time: %.0f ms%n%n", elapsedMs);

            // Wait briefly before requesting another snapshot.
            if (i < 5) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
