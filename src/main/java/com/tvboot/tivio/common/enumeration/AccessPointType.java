package com.tvboot.tivio.common.enumeration;

public enum AccessPointType {
    INTEGRATED,
    INTEGRATED_QRCODE,
    INTEGRATED_NO_QRCODE,// Built-in to the TV (WiFi Direct)
    EXTERNAL , // Standalone AP shared by multiple terminals
    EXTERNAL_QRCODE
}
