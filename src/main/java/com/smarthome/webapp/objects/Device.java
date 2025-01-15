package com.smarthome.webapp.objects;

import javax.persistence.Entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document("devices")
public class Device {

    private String userId;
    private String deviceType;
    private String deviceName;
    private String deviceID;
    private int displayWidth;
    private int displayHeight;
    private int posX;
    private int posY;
    private Object item;
    private String mqttData;
}