package com.tvboot.tivio.entities;

public class WifiConfig {
    private String ssid;
    private String password;
    private Protocol protocol;
    public enum Protocol {
        WPA, WEP, OPEN, OTHER
    }
}
