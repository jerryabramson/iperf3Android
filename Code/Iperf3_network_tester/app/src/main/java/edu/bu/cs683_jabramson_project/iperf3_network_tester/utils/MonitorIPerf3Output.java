/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jerry
 */

class ConnectDetails {
    String localHost = "";
    String remoteHost = "";
    Long localPort = -1L;
    Long remotePort = -1L;
    Boolean gathered = false;
    String connectedString = "";
    String timeout = "";
    String lastResult = "";
    int resultEntry = 0;
    List<String> iperf3Messages = new ArrayList<>();
    double maxBitsBytesPerSec = Double.MIN_VALUE;
    String maxBitsBytesPerSecUnit = "";
    double minBitsBytesPerSec = Double.MAX_VALUE;
    String minBitsBytesPerSecUnit = "";
    double avgBitsBytesPerSec = 0;
    String avgBitsBytesPerSecUnit = "";
    boolean summaryResults = false;
    boolean finished = false;
    boolean lastOmitted = false;
    boolean isSingleThread = true;

    public void setMaxBitsBytesPerSec(double maxBitsBytesPerSec, String unit) {
        if (maxBitsBytesPerSec > this.maxBitsBytesPerSec) {
            this.maxBitsBytesPerSec = maxBitsBytesPerSec;
            this.maxBitsBytesPerSecUnit = unit;
        }
    }
    public void setMinBitsBytesPerSec(double minBitsBytesPerSec, String unit) {
        if (minBitsBytesPerSec < this.minBitsBytesPerSec) {
            this.minBitsBytesPerSec = minBitsBytesPerSec;
            this.minBitsBytesPerSecUnit = unit;
        }
    }

    public void setAvgBitsBytesPerSec(double avgBitsBytesPerSec, String unit) {
        this.avgBitsBytesPerSec = avgBitsBytesPerSec;
        this.avgBitsBytesPerSecUnit = unit;
    }
}
public class MonitorIPerf3Output {
    private static ConnectDetails conn = new ConnectDetails();
    private static final String WORD_DELIMITER_RE = "[ \t]++";
    protected static final int leftColumnMarker = 30;
    protected static int  rightColumnMarker;

    public static String getMaximumBitsBytesPerSec() {
        return conn.maxBitsBytesPerSec + " " + conn.maxBitsBytesPerSecUnit;
    }

    public static String getMinimumBitsBytesPerSec() {
        return conn.minBitsBytesPerSec + " " + conn.minBitsBytesPerSecUnit;
    }

    public static String getAverageBitsBytesPerSec() {
        return conn.avgBitsBytesPerSec + " " + conn.avgBitsBytesPerSecUnit;
    }

    public static List<String> getIperf3Messages() {
        return conn.iperf3Messages;
    }

    public static void resetGathered() {
        conn.gathered = false;
        conn.iperf3Messages.clear();
        conn.lastResult = "";
        conn.resultEntry = 0;
        conn.finished = false;
        conn.summaryResults = false;
        conn.lastOmitted = false;
        conn.isSingleThread = true;
        conn.maxBitsBytesPerSec = Double.MIN_VALUE;
        conn.minBitsBytesPerSec = Double.MAX_VALUE;
        conn.avgBitsBytesPerSec = 0;
        conn.maxBitsBytesPerSecUnit = "";
        conn.minBitsBytesPerSecUnit = "";
        conn.avgBitsBytesPerSecUnit = "";
    }

    public static String processLine(String line) {
        String output = "";
        String ID;
        int firstLeftBracket = line.indexOf("[");
        int firstRightBracket = line.indexOf("]");
        if (firstLeftBracket >= 0 && firstRightBracket > firstLeftBracket) {
            ID = line.substring(firstLeftBracket + 1, firstRightBracket);
            String[] restOfLine = line.substring(firstRightBracket + 1).split(WORD_DELIMITER_RE);
            if (ID.contains("ID")) {
                if (!conn.gathered) {
                    output =  "    Local Host/IP: " + conn.localHost +
                            "\n   Remote Host/IP: " + conn.remoteHost +
                            "\n      Remote Port: " + conn.remotePort;
                    conn.gathered = true;
                    conn.resultEntry = 0;
                    return output;
                }
            }
            if (!conn.gathered) {
                if (restOfLine.length == 10) {
                    conn.localHost = restOfLine[2];
                    conn.localPort = Long.parseLong(restOfLine[4]);
                    conn.remoteHost = restOfLine[7];
                    conn.remotePort = Long.parseLong(restOfLine[9]);
                    conn.connectedString = restOfLine[5];
                    conn.timeout = restOfLine[6];
                    output =   "    Local Host/IP: " + conn.localHost +
                             "\n   Remote Host/IP: " + conn.remoteHost +
                             "\n      Remote Port: " + conn.remotePort;
                    conn.gathered = true;
                    return output;
                } else {
                    // Other iperf3 information
                    if (!line.trim().isEmpty()) {
                        return line;
                    }
                }
            } else {
                // connIsGathered
                if (restOfLine.length >= 7) {
                    String interval = restOfLine[1];
                    String bitRate = restOfLine[5];
                    String bitRateUnit = restOfLine[6];
                    String sendOrReceive = "";
                    if (restOfLine.length > 7) sendOrReceive = restOfLine[7];
                    if (restOfLine.length > 8) sendOrReceive = restOfLine[8];
                    if (restOfLine.length > 9) sendOrReceive = restOfLine[9];
                    if (restOfLine.length > 10) sendOrReceive = restOfLine[10];
                    sendOrReceive = sendOrReceive.trim();
                    if (!sendOrReceive.toLowerCase().contains("sender") && !sendOrReceive.toLowerCase().contains("receiv") && !sendOrReceive.toLowerCase().contains("omit")) {
                        sendOrReceive = "";
                    }
                    String time = "Running";
                    boolean done = false;
                    if (ID.contains("SUM") || conn.isSingleThread) {
                        double bitRateValue = -1;
                        try {bitRateValue = Double.parseDouble(bitRate);} catch (NumberFormatException ignored) {}
                        if (sendOrReceive.isEmpty()) {
                            conn.setMaxBitsBytesPerSec(bitRateValue, bitRateUnit);
                            conn.setMinBitsBytesPerSec(bitRateValue, bitRateUnit);
                        }
                        switch (sendOrReceive.toLowerCase()) {
                            case "(omitted)":
                                time = "   Skipping";
                                conn.lastOmitted = true;
                                break;
                            case "sender":
                            case "receiver":
                                conn.lastOmitted = false;
                                if (!conn.finished) {
                                    conn.finished = true;
                                    time = "Results\n";
                                    conn.summaryResults = true;
                                    conn.setAvgBitsBytesPerSec(bitRateValue, bitRateUnit);
                                } else {
                                    time = "";
                                    done = true;
                                }
                                break;
                            default:
                                conn.resultEntry++;
                                conn.lastOmitted = false;
                                sendOrReceive = "";
                        }

                        output = time + " " + interval + " " + sendOrReceive + " " + bitRate + " " + bitRateUnit;
                        return output;
                    }
                }
            }
        } else {
            if (!line.startsWith("- -") &&
                    !line.toLowerCase().contains("iperf done") &&
                    !line.trim().isEmpty()) {
                conn.iperf3Messages.add(line);
            }
        }
        return output;
    }
}
