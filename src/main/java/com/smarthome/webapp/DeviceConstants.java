package com.smarthome.webapp;

public final class DeviceConstants {
    private DeviceConstants() {}

    public static String BROKER_URL = "tcp://localhost:1883";
    public static String MQTT_ID = "SmartHomeServer";
    public static String MQTT_USER = "smarthome";
    public static String MQTT_PWD = "admin";

    public static String[] VALID_OUTLET_SENSORS = {
        "voltage",
        "current",
        "power",
        "energy"
    };

}