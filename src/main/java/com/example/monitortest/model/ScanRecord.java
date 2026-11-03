package com.example.monitortest.model;

public class ScanRecord {

    private final String date;
    private final String device;
    private final String type;
    private final String co2;

    public ScanRecord(String date, String device, String use, String co2) {
        this.date = date;
        this.device = device;
        this.type = use;
        this.co2 = co2;
    }

    public String getDate() { return date; }
    public String getDevice() { return device; }
    public String getType() { return type; }
    public String getCo2() { return co2; }

    // One line in the CSV file
    public String toCsvLine() {
        return String.join(",", date, device, type, co2);
    }
}