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

    @PostMapping("public/create")
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

    @PostMapping("public/login") 
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

    @GetMapping("private/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        String jwt = token.substring(7);
        
        try {
            resp = this.authService.handleLogout(jwt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("private/verify")
    public ResponseEntity<String> verifyUserKey(@RequestHeader("Authorization") String token) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        String jwt = token.substring(7);
        
        try {
            resp = this.authService.verifyUserKey(jwt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @GetMapping("private/test")
    public ResponseEntity<HashMap<String,Object>> test() {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        responseBody.put("message", "It Works!!!!");
        responseBody.put("success", true);
                
        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    @GetMapping("private/get/user")
    public ResponseEntity<String> getUserByToken(@RequestHeader("Authorization") String token) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        
        try {
            resp = this.authService.getUserByToken(token);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return resp;
    }

    @PostMapping("private/add/room")
    public ResponseEntity<String> addRoom(@RequestHeader("Authorization") String token, @RequestBody String roomInfo) {
        ResponseEntity<String> resp = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        String jwt = token.substring(7);

        try {
            String username = authService.getJwtSubject(jwt);
            String userId = authService.getUserIdFromUsername(username);
            JsonNode roomJson = objectMapper.readValue(roomInfo, JsonNode.class);

            if (roomJson != null) {
                resp = this.authService.addRoom(userId, roomJson);
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return resp;
    }
}
