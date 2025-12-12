package com.smarthome.webapp.auth.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import com.smarthome.webapp.auth.security.JwtUtil;
import com.smarthome.webapp.device.service.DeviceService;
import com.smarthome.webapp.user.repository.Dashboard;
import com.smarthome.webapp.device.domain.Device;
import com.smarthome.webapp.user.repository.Panel;
import com.smarthome.webapp.auth.repository.AuthAccountDocument;
import com.smarthome.webapp.user.repository.UserInfo;
import com.smarthome.webapp.auth.repository.AuthAccountRepository;
import com.smarthome.webapp.user.repository.UserInfoRepository;
import com.smarthome.webapp.user.service.UserSessionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final JwtUtil jwtUtil;
    private final AuthAccountRepository userAccountRepo;
    private final UserInfoRepository userInfoRepo;
    private final UserSessionService userSessionService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthAccountDocument loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieves user details by username from the database
        return userAccountRepo.findByUsername(username);
    }

    public String getJwtSubject(String jwt) {
        return this.jwtUtil.extractUsername(jwt);
    }

    public AuthAccountDocument loadUserByRefreshToken(String refreshToken) {
        return userAccountRepo.findByRefreshToken(refreshToken);
    }

    public String getUserIdFromUsername(String username) {
        return this.loadUserByUsername(username).getUserId();
    }

    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoRepo.findByUserId(userId);
    }

    public void saveNewUserAccount(AuthAccountDocument user) {
        userAccountRepo.save(user);
    }

    public void saveNewUserInfo(UserInfo user) {
        userInfoRepo.save(user);
    }

    public void saveRefreshToken(AuthAccountDocument user, HashMap<String,Object> tokenData) {
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

            AuthAccountDocument user = AuthAccountDocument.builder()
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

            AuthAccountDocument userAccount = this.loadUserByUsername(username);
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
                        userSessionService.onLogin(userAccount.getUserId());
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
                userSessionService.onLogin(userId);
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
                userSessionService.onLogin(userId);
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
                userSessionService.onLogin(userId);
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
        boolean updated = false;

        if (user == null) {
            responseBody.put("success", false);
            responseBody.put("error", "User not found");
        } else {
            Dashboard dashboard = user.getDashboard();
            if (dashboard != null && dashboard.getPanels() != null) {
                ListIterator<Panel> iterator = dashboard.getPanels().listIterator();
                while (iterator.hasNext()) {
                    Panel current = iterator.next();
                    if (current.getPanelId().equals(panel.getPanelId())) {
                        iterator.set(panel);
                        updated = true;
                        break;
                    }
                }
                this.userInfoRepo.save(user);
            } else {
                responseBody.put("success", false);
                responseBody.put("error", "Dashboard or panels not found");
            }

            if(updated) {
                responseBody.put("success", true);
            } else {
                responseBody.put("success", false);
                responseBody.put("error", "Panel not found");
            }
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
