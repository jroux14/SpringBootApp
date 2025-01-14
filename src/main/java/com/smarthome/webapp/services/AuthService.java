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

import com.smarthome.webapp.jwt.JwtUtil;
import com.smarthome.webapp.objects.UserAccount;
import com.smarthome.webapp.objects.UserInfo;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<HashMap<String,Object>> createUser(HashMap<String, String> userData) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        try {
            String username = userData.get("username");
            String password = userData.get("password");

            String userID = UUID.randomUUID().toString();

            UserAccount user = UserAccount.builder()
            .userId(userID)
            .username(username)
            .password(passwordEncoder.encode(password))
            .authorities("defaultUser")
            .build();

            UserInfo userInfo = UserInfo.builder()
            .userId(userID)
            .firstName(userData.get("firstName"))
            .email(userData.get("email"))
            .phoneNum(userData.get("phoneNum"))
            .build();

            this.userService.saveNewUserAccount(user);
            this.userService.saveNewUserInfo(userInfo);

            responseBody.put("success", true);
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("error", "Server error");
            responseBody.put("success", false);
        }

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    public ResponseEntity<HashMap<String,Object>> confirmLogin(HashMap<String,String> userData) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        try {
            String username = userData.get("username");
            String password = userData.get("password");

            UserAccount userAccount = this.userService.loadUserByUsername(username);
            if (userAccount != null) {
                if (passwordEncoder.matches(password, userAccount.getPassword())) {
                    // Success
                    String token = jwtUtil.generateToken(userAccount);
                    HashMap<String,Object> refreshToken = jwtUtil.generateRefreshToken(userAccount);
                    this.userService.saveRefreshToken(userAccount, refreshToken);
    
                    UserInfo userInfo = this.userService.getUserInfoByUserId(userAccount.getUserId());
    
                    responseBody.put("user", userInfo);
                    responseBody.put("token", token);
                    responseBody.put("refreshToken", refreshToken.get("token"));
                    responseBody.put("success", true);
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

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    public String getJwtSubject(String jwt) {
        return this.jwtUtil.extractUsername(jwt);
    }
}
