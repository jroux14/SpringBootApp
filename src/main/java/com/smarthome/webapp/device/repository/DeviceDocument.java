package com.smarthome.webapp.device.repository;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document("devices")
public class DeviceDocument {

    @Id
    private String id;

    private String userId;
    private String deviceName;
    private String deviceNameFriendly;
    private String roomId;
    private String deviceType;

    private Map<String, Object> data;
}
