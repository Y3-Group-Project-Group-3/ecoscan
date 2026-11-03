package com.example.monitortest.workload;

import com.example.monitortest.model.CpuTestResult;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class CpuWorkload {

    private static final BigInteger START_VALUE =
            new BigInteger(
                    "12345678901234567890123456789012345678901234567890"
            );

    private static final BigInteger MULTIPLIER =
            new BigInteger(
                    "987654321098765432109876543210987654321"
            );

    private static final BigInteger MODULUS =
            new BigInteger(
                    "134078079299425970995740249982058461274793658205923933" +
                    "77723561443721764030073546976801874298166903427690031"
            );

    private static final int OPERATIONS_PER_UNIT = 2000;
    private static final int TEST_UNITS = 5000;
    private static final int WARMUP_UNITS = 1000;
    private static final int RUNS =5; // Increased number of runs for better accuracy
    //private static final int RUNS = 3; 


    public CpuTestResult run() throws Exception {

        int logicalProcessors =
                Runtime.getRuntime().availableProcessors();

        System.out.println();
        System.out.println("EcoScan CPU Workload");
        System.out.println("--------------------");
        System.out.println(
                "Logical processors: " + logicalProcessors
        );
        System.out.println(
                "Total work units:    " + TEST_UNITS
        );
        System.out.println(
                "Operations per unit: " + OPERATIONS_PER_UNIT
        );


        // JVM WARM-UP

        System.out.println();
        System.out.println("Warming up JVM...");

        runSingleThreadTest(WARMUP_UNITS);

        runMultiThreadTest(
                WARMUP_UNITS,
                logicalProcessors
        );

        System.out.println("Warm-up complete.");

        Thread.sleep(1000);


        // SINGLE-THREAD TEST

        System.out.println();
        System.out.println("Running single-thread tests...");

        double[] singleTimes =
                new double[RUNS];

        String singleChecksum = "";

        for (int run = 0; run < RUNS; run++) {

            InternalTestResult result =
                    runSingleThreadTest(TEST_UNITS);

            singleTimes[run] =
                    result.seconds;

            singleChecksum =
                    result.checksum;

            System.out.printf(
                    "Single run %d: %.3f s%n",
                    run + 1,
                    result.seconds
            );

            Thread.sleep(500);
        }


        // SHORT PAUSE

        System.out.println();
        System.out.println("Cooling pause...");

        Thread.sleep(2000);


        // MULTI-THREAD TEST

        System.out.println();
        System.out.println(
                "Running multi-thread tests using "
                        + logicalProcessors
                        + " workers..."
        );

        double[] multiTimes =
                new double[RUNS];

        String multiChecksum = "";

        for (int run = 0; run < RUNS; run++) {

            InternalTestResult result =
                    runMultiThreadTest(
                            TEST_UNITS,
                            logicalProcessors
                    );

            multiTimes[run] =
                    result.seconds;

            multiChecksum =
                    result.checksum;

            System.out.printf(
                    "Multi run %d: %.3f s%n",
                    run + 1,
                    result.seconds
            );

            Thread.sleep(500);
        }


        // CALCULATE RESULTS

        double singleMedian =
                calculateMedian(singleTimes);

        double multiMedian =
                calculateMedian(multiTimes);

        double speedup =
                singleMedian / multiMedian;

        boolean checksumPassed =
                singleChecksum.equals(multiChecksum);


        // RETURN RESULT TO THE APPLICATION

        return new CpuTestResult(
                singleMedian,
                multiMedian,
                speedup,
                singleChecksum,
                multiChecksum,
                checksumPassed
        );
    }


    // SINGLE-THREAD WORKLOAD

    private InternalTestResult runSingleThreadTest(
            int units) throws Exception {

        long start =
                System.nanoTime();

        BigInteger combined =
                BigInteger.ZERO;

        for (int i = 0; i < units; i++) {

            BigInteger result =
                    performWorkUnit(i);

            combined =
                    combined.xor(result);
        }

        long end =
                System.nanoTime();

        double seconds =
                (end - start)
                        / 1_000_000_000.0;

        return new InternalTestResult(
                seconds,
                createChecksum(combined)
        );
    }


    // MULTI-THREAD WORKLOAD

    private InternalTestResult runMultiThreadTest(
            int totalUnits,
            int threads) throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(threads);

        List<Future<BigInteger>> futures =
                new ArrayList<>();

        int baseUnits =
                totalUnits / threads;

        int remainder =
                totalUnits % threads;

        int startUnit = 0;

        long start =
                System.nanoTime();


        // One task for each logical processor

        for (int worker = 0;
             worker < threads;
             worker++) {

            int unitsForWorker =
                    baseUnits;

            if (worker < remainder) {
                unitsForWorker++;
            }

            final int workerStart =
                    startUnit;

            final int workerEnd =
                    startUnit + unitsForWorker;

            futures.add(

                    executor.submit(() -> {

                        BigInteger localResult =
                                BigInteger.ZERO;

                        for (int unit = workerStart;
                             unit < workerEnd;
                             unit++) {

                            localResult =
                                    localResult.xor(
                                            performWorkUnit(unit)
                                    );
                        }

                        return localResult;
                    })
            );

            startUnit =
                    workerEnd;
        }


        // Combine results from all workers

        BigInteger combined =
                BigInteger.ZERO;

        for (Future<BigInteger> future : futures) {

            combined =
                    combined.xor(
                            future.get()
                    );
        }

        long end =
                System.nanoTime();

        executor.shutdown();

        executor.awaitTermination(
                1,
                TimeUnit.MINUTES
        );

        double seconds =
                (end - start)
                        / 1_000_000_000.0;

        return new InternalTestResult(
                seconds,
                createChecksum(combined)
        );
    }


    // CONTROLLED WORK UNIT

    private BigInteger performWorkUnit(
            int unitNumber) {

        BigInteger value =
                START_VALUE.add(
                        BigInteger.valueOf(
                                unitNumber
                        )
                );

        for (int i = 0;
             i < OPERATIONS_PER_UNIT;
             i++) {

            value =
                    value
                            .multiply(MULTIPLIER)
                            .add(
                                    BigInteger.valueOf(i)
                            )
                            .mod(MODULUS);
        }

        return value;
    }


    // MEDIAN

    private double calculateMedian(
            double[] values) {

        double[] copy =
                Arrays.copyOf(
                        values,
                        values.length
                );

        Arrays.sort(copy);

        return copy[
                copy.length / 2
        ];
    }


    // SHA-256 CHECKSUM

    private String createChecksum(
            BigInteger value) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance(
                        "SHA-256"
                );

        byte[] hash =
                digest.digest(
                        value.toByteArray()
                );

        StringBuilder result =
                new StringBuilder();

        for (byte b : hash) {

            result.append(
                    String.format(
                            "%02x",
                            b
                    )
            );
        }

        return result.toString();
    }


    // INTERNAL RESULT USED DURING EACH RUN

    private static class InternalTestResult {

        double seconds;
        String checksum;

        InternalTestResult(
                double seconds,
                String checksum) {

            this.seconds =
                    seconds;

            this.checksum =
                    checksum;
        }
    }
}