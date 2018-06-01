package com.example.wearable.bluetooth;

public class Item {

    private String address;
    private int rssi;
    private int txPower;
    private double distance;
    private String  name;

    public Item(String address, int rssi, int txPower, double distance, String name) {
        this.address = address;
        this.rssi = rssi;
        this.txPower = txPower;
        this.distance = distance;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return  rssi;
    }

    public int getTxPower() {
        return txPower;
    }

    public double getDistance() {  return distance;  }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}

