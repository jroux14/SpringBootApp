package com.smarthome.webapp.objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("devices")
public class Device {

        @Id
        private String id;

        private String name;
        private String userID;
        private String mqttData;
        
        public Device() {}

        public Device(String name, String userID, String mqttData) {
            super();
            this.name = name;
            this.userID = userID;
            this.mqttData = mqttData;
        }
}