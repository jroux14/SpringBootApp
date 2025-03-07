package com.smarthome.webapp.services;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.jwt.JwtUtil;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.objects.UserAccount;
import com.smarthome.webapp.objects.UserInfo;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<String> createUser(JsonNode userData) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String username = userData.get("username").asText();
            String password = userData.get("password").asText();

            String userID = UUID.randomUUID().toString();

            UserAccount user = UserAccount.builder()
            .userId(userID)
            .username(username)
            .password(passwordEncoder.encode(password))
            .authorities("defaultUser")
            .build();

            UserInfo userInfo = UserInfo.builder()
            .userId(userID)
            .firstName(userData.get("firstName").asText())
            .email(userData.get("email").asText())
            .phoneNum(userData.get("phoneNum").asText())
            .build();

            this.userService.saveNewUserAccount(user);
            this.userService.saveNewUserInfo(userInfo);

            responseBody.put("success", true);
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> confirmLogin(JsonNode creds) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String username = creds.get("username").asText();
            String password = creds.get("password").asText();

            UserAccount userAccount = this.userService.loadUserByUsername(username);
            if (userAccount != null) {
                if (passwordEncoder.matches(password, userAccount.getPassword())) {
                    // Success
                    String token = jwtUtil.generateToken(userAccount);
                    HashMap<String,Object> refreshToken = jwtUtil.generateRefreshToken(userAccount);
                    this.userService.saveRefreshToken(userAccount, refreshToken);
    
                    UserInfo userInfo = this.userService.getUserInfoByUserId(userAccount.getUserId());
                    
                    if (userInfo != null) {
                        responseBody.put("user", userInfo);
                        responseBody.put("token", token);
                        responseBody.put("refreshToken", refreshToken.get("token"));
                        Device[] userDevices = this.deviceService.getDevicesByUserId(userInfo.getUserId());
                        if (userDevices != null) {
                            for (Device device : userDevices) {
                                this.deviceService.addSubscription(device.getDeviceName() + "/#");
                            }
                            responseBody.put("devices", userDevices);
                        }
                        responseBody.put("success", true);
                    }
                } else {
                    // Incorrect Password
                    responseBody.put("error", "Invalid password");
                    responseBody.put("success", false);
                }
            } else {
                responseBody.put("error", "User not found");
                responseBody.put("success", false);
            }
        } catch (UsernameNotFoundException e) {
            // User doesn't exist
            responseBody.put("error", true);
            responseBody.put("success", false);
        } catch (Exception e) {
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> verifyUserKey(String key) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String username = this.jwtUtil.extractUsername(key);
            String userId = userService.getUserIdFromUsername(username);
            UserInfo user = userService.getUserInfoByUserId(userId);
            if (user != null) {
                responseBody.put("user", user);
                Device[] userDevices = this.deviceService.getDevicesByUserId(userId);
                if (userDevices != null) {
                    for (Device device : userDevices) {
                        this.deviceService.addSubscription(device.getDeviceName() + "/#");
                    }
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

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> handleLogout(String key) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String username = this.jwtUtil.extractUsername(key);
            String userId = userService.getUserIdFromUsername(username);

            if (userId != null) {
                for (Device device : this.deviceService.getDevicesByUserId(userId)) {
                    this.deviceService.removeSubscription(device.getDeviceName() + "/#");
                }
                responseBody.put("success", true);
            }
        } catch (UsernameNotFoundException e) {
            // User doesn't exist
            responseBody.put("error", true);
            responseBody.put("success", false);
        } catch (Exception e) {
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public String getJwtSubject(String jwt) {
        return this.jwtUtil.extractUsername(jwt);
    }
}
