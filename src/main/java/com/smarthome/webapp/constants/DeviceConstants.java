package com.smarthome.webapp.constants;

import java.util.Set;

public final class DeviceConstants {
    private DeviceConstants() {}

    public static String BROKER_URL = "tcp://localhost:1883";
    public static String MQTT_ID = "SmartHomeServer";
    public static String MQTT_USER = "smarthome";
    public static String MQTT_PWD = "admin";

    public static Set<String> VALID_OUTLET_SENSORS = Set.of("voltage", "current", "power", "energy");
}