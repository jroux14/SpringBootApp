package com.smarthome.webapp.objects;

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

    public void setPassword(String password){
        this.pwd = password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setID(String id){
        this.userID = id;
    }

    public void setName(String name){
        this.firstName = name;
    }
}
