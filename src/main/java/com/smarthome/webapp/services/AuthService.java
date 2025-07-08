package com.smarthome.webapp.services;

import java.util.HashMap;
import java.util.UUID;
import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthome.webapp.jwt.JwtUtil;
import com.smarthome.webapp.objects.Dashboard;
import com.smarthome.webapp.objects.Device;
import com.smarthome.webapp.objects.Panel;
import com.smarthome.webapp.objects.UserAccount;
import com.smarthome.webapp.objects.UserInfo;
import com.smarthome.webapp.repositories.UserAccountRepository;
import com.smarthome.webapp.repositories.UserInfoRepository;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserAccountRepository userAccountRepo;

    @Autowired
    private UserInfoRepository userInfoRepo;

    @Override
    public UserAccount loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieves user details by username from the database
        return userAccountRepo.findByUsername(username);
    }

    public String getJwtSubject(String jwt) {
        return this.jwtUtil.extractUsername(jwt);
    }

    public UserAccount loadUserByRefreshToken(String refreshToken) {
        return userAccountRepo.findByRefreshToken(refreshToken);
    }

    public String getUserIdFromUsername(String username) {
        return this.loadUserByUsername(username).getUserId();
    }

    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoRepo.findByUserId(userId);
    }

    public void saveNewUserAccount(UserAccount user) {
        userAccountRepo.save(user);
    }

    public void saveNewUserInfo(UserInfo user) {
        userInfoRepo.save(user);
    }

    public void saveRefreshToken(UserAccount user, HashMap<String,Object> tokenData) {
        user.setRefreshToken(tokenData.get("token").toString());
        user.setRefreshTokenExp((Date)tokenData.get("exp"));

        userAccountRepo.save(user);
    }

    public ResponseEntity<String> addRoom(String userId, JsonNode roomInfo) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String roomName = roomInfo.get("name").asText();
            if(!userInfoRepo.roomExistsForUser(userId, roomName)) {
                HashMap<String,Object> roomObject = new HashMap<String,Object>();
                roomObject.put("roomName", roomName);
                roomObject.put("roomId", UUID.randomUUID().toString());

                userInfoRepo.addRoom(userId, roomObject);
                responseBody.put("success", true);
                responseBody.put("room", roomObject);
            } else {
                responseBody.put("success", false);
                responseBody.put("error", "Room already exists");
            }
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> addPanel(String userId, Panel panel) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String panelId = UUID.randomUUID().toString();

            panel.setPanelId(panelId);

            userInfoRepo.addPanel(userId, panel);
            responseBody.put("success", true);
            responseBody.put("panel", panel);
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        String resp = objectMapper.writeValueAsString(responseBody);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> createUser(JsonNode userData) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String username = userData.get("username").asText();
            String password = userData.get("password").asText();

            String userId = UUID.randomUUID().toString();
            String dashboardId = UUID.randomUUID().toString();

            Dashboard startingDashboard = Dashboard.builder()
            .dashboardId(dashboardId)
            .panels(Collections.emptyList())
            .build();

            UserAccount user = UserAccount.builder()
            .userId(userId)
            .username(username)
            .password(passwordEncoder.encode(password))
            .authorities("defaultUser")
            .build();

            UserInfo userInfo = UserInfo.builder()
            .userId(userId)
            .firstName(userData.get("firstName").asText())
            .email(userData.get("email").asText())
            .phoneNum(userData.get("phone").asText())
            .rooms(Collections.emptyList())
            .dashboard(startingDashboard)
            .build();

            this.saveNewUserAccount(user);
            this.saveNewUserInfo(userInfo);

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

            UserAccount userAccount = this.loadUserByUsername(username);
            if (userAccount != null) {
                if (passwordEncoder.matches(password, userAccount.getPassword())) {
                    // Success
                    String token = jwtUtil.generateToken(userAccount);
                    HashMap<String,Object> refreshToken = jwtUtil.generateRefreshToken(userAccount);
                    this.saveRefreshToken(userAccount, refreshToken);
    
                    UserInfo userInfo = this.getUserInfoByUserId(userAccount.getUserId());
                    
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
            String userId = this.getUserIdFromUsername(username);
            UserInfo user = this.getUserInfoByUserId(userId);
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
            String userId = this.getUserIdFromUsername(username);

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

    public ResponseEntity<String> getUserByToken(String token) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        String jwt = token.substring(7);
        
        try {
            String username = this.getJwtSubject(jwt);
            String userId = this.getUserIdFromUsername(username);
            UserInfo user = this.getUserInfoByUserId(userId);
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

    public ResponseEntity<String> updatePanel(String userId, Panel panel) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        UserInfo user = this.userInfoRepo.findByUserId(userId);

        if (user == null) {
            responseBody.put("success", false);
            responseBody.put("error", "User not found");
        } else {
            Dashboard dashboard = user.getDashboard();
            if (dashboard != null && dashboard.getPanels() != null) {
                dashboard.getPanels().forEach(currentPanel -> {
                    if (currentPanel.getPanelId().equals(panel.getPanelId())) {
                        currentPanel.setData(panel.getData());
                    }
                });
                this.userInfoRepo.save(user);
            } else {
                responseBody.put("success", false);
                responseBody.put("error", "Dashboard or panel not found");
            }

            responseBody.put("success", true);
        }

        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    public ResponseEntity<String> deletePanel(String userId, Panel panel) throws JsonProcessingException {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        UserInfo user = this.userInfoRepo.findByUserId(userId);
        if (user == null) {
            responseBody.put("success", false);
            responseBody.put("error", "User not found");
        } else {
            Dashboard dashboard = user.getDashboard();
            if (dashboard != null && dashboard.getPanels() != null) {
                dashboard.getPanels().removeIf(oldPanel -> oldPanel.getPanelId().equals(panel.getPanelId()));
                userInfoRepo.save(user);
                responseBody.put("success", true);
            } else {
                responseBody.put("success", false);
                responseBody.put("error", "Dashboard or panel not found");
            }
        }

        String resp = objectMapper.writeValueAsString(responseBody);

        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
