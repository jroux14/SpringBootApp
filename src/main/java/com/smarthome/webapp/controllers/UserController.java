package com.smarthome.webapp.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.objects.UserInfo;
import com.smarthome.webapp.services.AuthService;
import com.smarthome.webapp.services.DeviceService;
import com.smarthome.webapp.services.UserService;

@RestController
@RequestMapping("smarthome/user/")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/test")
    public ResponseEntity<HashMap<String,Object>> test() {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        responseBody.put("message", "It Works!!!!");
        responseBody.put("success", true);
                
        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<HashMap<String,Object>> getUserByUsername(@RequestHeader("Authorization") String token) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        String jwt = token.substring(7);
        
        try {
            String username = this.authService.getJwtSubject(jwt);
            String userId = userService.getUserIdFromUsername(username);
            UserInfo user = userService.getUserInfoByUserId(userId);
            if (user != null) {
                responseBody.put("user", user);
                Device[] userDevices = this.deviceService.getDevicesByUserId(userId);
                if (userDevices != null) {
                    responseBody.put("devices", userDevices);
                }
                responseBody.put("success", true);
            } else {
                responseBody.put("error", "User not found");
                responseBody.put("success", false);
            }
        } catch (UsernameNotFoundException e) {
            responseBody.put("error", "User not found");
            responseBody.put("success", false);
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Unknown Exception");
            responseBody.put("success", false);
        }

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }
}
