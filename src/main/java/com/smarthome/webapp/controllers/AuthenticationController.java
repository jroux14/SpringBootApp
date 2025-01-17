package com.smarthome.webapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.services.AuthService;

@RestController
@RequestMapping("smarthome/auth/")
public class AuthenticationController {

    @Autowired
    AuthService authService;

    @PostMapping("create")
    public ResponseEntity<String> create(@RequestBody String userInfo){
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            JsonNode infoJson = objectMapper.readValue(userInfo, JsonNode.class);

            if (infoJson != null) {
                resp = this.authService.createUser(infoJson);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }

    @PostMapping("login") 
    public ResponseEntity<String> login(@RequestBody String creds) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            JsonNode credsJson = objectMapper.readValue(creds, JsonNode.class);

            if (credsJson != null) {
                resp = this.authService.confirmLogin(credsJson);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }

    @GetMapping("verify")
    public ResponseEntity<String> verifyUserKey(@RequestHeader("Authorization") String token) throws JsonProcessingException {
        String jwt = token.substring(7);
        
        return this.authService.verifyUserKey(jwt);
    }
}
