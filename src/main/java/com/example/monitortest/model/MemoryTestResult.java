package com.example.monitortest.model;

/**
 * Stores the results of a controlled RAM workload test.
 *
 * The results include median execution times,
 * estimated data transfer rates and validation status.
 */
public class MemoryTestResult {

    private final int bufferSizeMiB;

    private final double writeMedianSeconds;
    private final double readMedianSeconds;

    private final double writeSpeedMiBps;
    private final double readSpeedMiBps;

    private final boolean validationPassed;

    /**
     * Creates a result object containing RAM test measurements.
     */
    public MemoryTestResult(
            int bufferSizeMiB,
            double writeMedianSeconds,
            double readMedianSeconds,
            double writeSpeedMiBps,
            double readSpeedMiBps,
            boolean validationPassed) {

        this.bufferSizeMiB = bufferSizeMiB;
        this.writeMedianSeconds = writeMedianSeconds;
        this.readMedianSeconds = readMedianSeconds;
        this.writeSpeedMiBps = writeSpeedMiBps;
        this.readSpeedMiBps = readSpeedMiBps;
        this.validationPassed = validationPassed;
    }

    public int getBufferSizeMiB() {
        return bufferSizeMiB;
    }

    public double getWriteMedianSeconds() {
        return writeMedianSeconds;
    }

    public double getReadMedianSeconds() {
        return readMedianSeconds;
    }

    public double getWriteSpeedMiBps() {
        return writeSpeedMiBps;
    }

    public double getReadSpeedMiBps() {
        return readSpeedMiBps;
    }

    public boolean isValidationPassed() {
        return validationPassed;
    }
}
