package com.smarthome.webapp.objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
@Document("user-info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    private String userId;
    private String firstName;
    private String email;
    private String phoneNum;
}
