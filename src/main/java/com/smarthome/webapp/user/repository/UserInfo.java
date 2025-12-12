package com.smarthome.webapp.user.repository;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document("user-info")
public class UserInfo {
    @Id
    private String id;

    private String userId;
    private String firstName;
    private String email;
    private String phoneNum;
    
    private List<Object> rooms;
    private Dashboard dashboard;
}
