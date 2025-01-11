package com.smarthome.webapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.services.UserService;

@RestController
@RequestMapping("smarthome/user/")
public class UserController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public ResponseEntity<HashMap<String,Object>> test() {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        responseBody.put("message", "It Works!!!!");
        responseBody.put("success", true);
                
        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    @GetMapping("/getUser")
    public ResponseEntity<HashMap<String,Object>> getUserByUsername(@RequestParam String username) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        
        try {
            UserDetails user = userService.loadUserByUsername(username);
            responseBody.put("user", user);
            responseBody.put("success", true);
        } catch (UsernameNotFoundException e) {
            responseBody.put("notFound", true);
            responseBody.put("success", false);
        } catch (Exception e) {
            System.out.println(e);
        }

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }
}
