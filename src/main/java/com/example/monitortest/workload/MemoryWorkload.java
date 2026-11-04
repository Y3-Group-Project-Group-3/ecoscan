
package com.example.monitortest.workload;

import com.example.monitortest.model.MemoryTestResult;

import java.util.Arrays;

public class MemoryWorkload {

    // Initial test configuration.
    private static final int BUFFER_SIZE_MIB = 128;
    private static final int RUNS = 5;
    private static final int WARMUP_SIZE_MIB = 16;

    private static volatile long readSink;

    public MemoryTestResult run() {

        System.out.println();
        System.out.println("EcoScan RAM Workload");
        System.out.println("--------------------");
        System.out.println("Buffer size: " + BUFFER_SIZE_MIB + " MiB");
        System.out.println("Test runs:   " + RUNS);

        // Use a long array to store the test data.
        // Each long occupies 8 bytes.
        int elementCount = BUFFER_SIZE_MIB * 1024 * 1024 / Long.BYTES;
        long[] buffer = new long[elementCount];

        // Warm up the JVM using a smaller buffer.
        System.out.println();
        System.out.println("Warming up JVM...");

        long[] warmupBuffer =
                new long[WARMUP_SIZE_MIB * 1024 * 1024 / Long.BYTES];

        for (int i = 0; i < 3; i++) {
            writeBuffer(warmupBuffer);
            readSink = readBuffer(warmupBuffer);
        }

        System.out.println("Warm-up complete.");

        double[] writeTimes = new double[RUNS];
        double[] readTimes = new double[RUNS];

        boolean validationPassed = true;

        // Execute repeated write and read tests.
        for (int run = 0; run < RUNS; run++) {

            System.out.println();
            System.out.println("RAM test run " + (run + 1));

            // Measure sequential writing.
            long writeStart = System.nanoTime();

            writeBuffer(buffer);

            long writeEnd = System.nanoTime();

            writeTimes[run] =
                    (writeEnd - writeStart) / 1_000_000_000.0;

            // Measure sequential reading.
            long readStart = System.nanoTime();

            long actualChecksum = readBuffer(buffer);

            long readEnd = System.nanoTime();

            readTimes[run] =
                    (readEnd - readStart) / 1_000_000_000.0;

            // Expected sum of values 0 through n - 1.
            long n = buffer.length;
            long expectedChecksum = n * (n - 1) / 2;

            boolean runPassed =
                    actualChecksum == expectedChecksum;

            validationPassed &= runPassed;

            // Keep the read result observable.
            readSink = actualChecksum;

            System.out.printf(
                    "Write: %.4f s%n",
                    writeTimes[run]
            );

            System.out.printf(
                    "Read:  %.4f s%n",
                    readTimes[run]
            );

            System.out.println(
                    "Validation: " + (runPassed ? "PASS" : "FAIL")
            );
        }

        // Calculate median execution times.
        double writeMedian = calculateMedian(writeTimes);
        double readMedian = calculateMedian(readTimes);

        // Estimate throughput from buffer size and elapsed time.
        double writeSpeed = BUFFER_SIZE_MIB / writeMedian;
        double readSpeed = BUFFER_SIZE_MIB / readMedian;

        return new MemoryTestResult(
                BUFFER_SIZE_MIB,
                writeMedian,
                readMedian,
                writeSpeed,
                readSpeed,
                validationPassed
        );
    }

    /**
     * Writes predictable values to each array element.
     */
    private void writeBuffer(long[] buffer) {

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = i;
        }
    }

    /**
     * Reads all array elements and calculates a checksum.
     */
    private long readBuffer(long[] buffer) {

        long checksum = 0;

        for (long value : buffer) {
            checksum += value;
        }

        return checksum;
    }

    /**
     * Returns the median of the recorded execution times.
     */
    private double calculateMedian(double[] times) {

        double[] sorted = times.clone();
        Arrays.sort(sorted);

        int middle = sorted.length / 2;

        if (sorted.length % 2 == 0) {
            return (sorted[middle - 1] + sorted[middle]) / 2.0;
        }

        return sorted[middle];
    }
}
