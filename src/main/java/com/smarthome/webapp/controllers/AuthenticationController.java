package com.smarthome.webapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.services.AuthService;

@RestController
@RequestMapping("smarthome/auth/")
public class AuthenticationController {

    @Autowired
    AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<HashMap<String,Object>> create(@RequestBody HashMap<String, String> userInfo){
       return this.authService.createUser(userInfo);
    }

    @PostMapping("/login") 
    public ResponseEntity<HashMap<String,Object>> login(@RequestBody HashMap<String,String> userInfo) {
        return this.authService.confirmLogin(userInfo);
    }
}
