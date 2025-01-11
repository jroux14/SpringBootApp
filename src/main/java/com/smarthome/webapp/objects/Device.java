package com.smarthome.webapp.objects;

import org.springframework.data.mongodb.core.mapping.Document;

@Document("devices")
public class Device {

        private String userID;
        private String deviceType;
        private String deviceName;
        private String deviceID;
        private int displayWidth;
        private int displayHeight;
        private int posX;
        private int posY;
        private Object item;
        private String mqttData;
        
        public Device() {}

        public Device(String userID, String deviceType, String deviceName, String deviceID, int displayWidth, int displayHeight, int posX, int posY, Object item, String mqttData) {
            super();
            this.userID = userID;
            this.deviceType = deviceType;
            this.deviceName = deviceName;
            this.deviceID = deviceID;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.posX = posX;
            this.posY = posY;
            this.item = item;
            this.mqttData = mqttData;
        }
}