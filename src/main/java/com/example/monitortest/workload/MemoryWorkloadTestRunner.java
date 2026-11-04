package com.example.monitortest.workload;

import com.example.monitortest.model.MemoryTestResult;

public class MemoryWorkloadTestRunner {

    public static void main(String[] args) {
        try {
            System.out.println("Starting EcoScan RAM Benchmark...");

            // Run the memory benchmark independently of the EcoScan GUI.
            MemoryWorkload workload = new MemoryWorkload();
            MemoryTestResult result = workload.run();

            System.out.println("\nRAM Benchmark completed successfully.");
            System.out.println("Result object: "
                    + result.getClass().getSimpleName());

        } catch (Exception e) {
            System.err.println("RAM Benchmark failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}