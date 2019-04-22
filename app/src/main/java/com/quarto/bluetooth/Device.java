package com.quarto.bluetooth;

public class Device {

    private String name;
    private String address;

    public Device() {
    }

    public Device(String name, String address){
        this.name = name;
        this.address = address;
    }

    public String getName() {return this.name;}
    public void setName(String kod) {this.name = name;}

    public String getAddress() {return this.address;}
    public void setAddress(String address) {this.address = address;}
}
