package com.smarthome.webapp.objects;

import java.io.Console;

import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public class User {
    private String userID;
    private String username;
    private String firstName;
    private String pwd;

    public User() {}

    public User(JSONObject user) {
        super();
        this.firstName = (String)user.get("firstName");
        this.userID = (String)user.get("userID");
        this.username = (String)user.get("username");
        this.pwd = (String)user.get("pwd");
    }

    public String getPassword(){
        return this.pwd;
    }

    public String getUsername(){
        return this.username;
    }

    public String getFirstName(){
        return this.firstName;
    }
    
    public String getUserID(){
        return this.userID;
    }
}
