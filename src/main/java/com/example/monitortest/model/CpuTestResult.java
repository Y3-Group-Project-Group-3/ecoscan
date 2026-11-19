package com.example.monitortest.model;

public class CpuTestResult {

    private final double singleThreadMedian;
    private final double multiThreadMedian;
    private final double speedup;

    private final String singleChecksum;
    private final String multiChecksum;

    private final boolean checksumPassed;

    public CpuTestResult(
            double singleThreadMedian,
            double multiThreadMedian,
            double speedup,
            String singleChecksum,
            String multiChecksum,
            boolean checksumPassed) {

        this.singleThreadMedian = singleThreadMedian;
        this.multiThreadMedian = multiThreadMedian;
        this.speedup = speedup;
        this.singleChecksum = singleChecksum;
        this.multiChecksum = multiChecksum;
        this.checksumPassed = checksumPassed;
    }

    public double getSingleThreadMedian() {
        return singleThreadMedian;
    }

    public double getMultiThreadMedian() {
        return multiThreadMedian;
    }

    public double getSpeedup() {
        return speedup;
    }

    public String getSingleChecksum() {
        return singleChecksum;
    }

    public String getMultiChecksum() {
        return multiChecksum;
    }

    public boolean isChecksumPassed() {
        return checksumPassed;
    }
}