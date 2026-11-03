package com.example.monitortest.model;

public class ScanRecord {

    private final String date;
    private final String device;
    private final String use;
    private final String score;
    private final String co2;
    private final String result;

    public ScanRecord(String date, String device, String use,
                      String score, String co2, String result) {
        this.date = date;
        this.device = device;
        this.use = use;
        this.score = score;
        this.co2 = co2;
        this.result = result;
    }

    public String getDate() { return date; }
    public String getDevice() { return device; }
    public String getUse() { return use; }
    public String getScore() { return score; }
    public String getCo2() { return co2; }
    public String getResult() { return result; }

    // One line in the CSV file
    public String toCsvLine() {
        return String.join(",", date, device, use, score, co2, result);
    }
}