
package com.example.monitortest.workload;

import com.example.monitortest.model.CpuTestResult;

public class CpuWorkloadTestRunner {

    public static void main(String[] args) {
        try {
            System.out.println("Starting EcoScan CPU Benchmark...");

            CpuWorkload workload = new CpuWorkload();

            // Run the benchmark independently of the EcoScan GUI.
            CpuTestResult result = workload.run();

            System.out.println("\n=== CPU Benchmark Results ===");
            System.out.printf("Single-thread median: %.3f s%n",
                    result.getSingleThreadMedian());
            System.out.printf("Multi-thread median: %.3f s%n",
                    result.getMultiThreadMedian());
            System.out.printf("Speed-up: %.2fx%n",
                    result.getSpeedup());
            System.out.println("Checksum passed: "
                    + result.isChecksumPassed());

        } catch (Exception e) {
            System.err.println("CPU Benchmark failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}