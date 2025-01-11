package com.smarthome.webapp.services;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smarthome.webapp.jwt.JwtUtil;
import com.smarthome.webapp.objects.UserAccount;
import com.smarthome.webapp.objects.UserInfo;
import com.smarthome.webapp.repositories.UserAccountRepository;
import com.smarthome.webapp.repositories.UserInfoRepository;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserAccountRepository userAccountRepo; // Injects the UserRepo for accessing user data

    @Autowired
    private UserInfoRepository userInfoRepo;

    @Autowired
    private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieves user details by username from the database
        return userAccountRepo.findByUsername(username);
    }

    public UserAccount getUserAccountByUsername(String username) {
        return userAccountRepo.findByUsername(username);
    }

    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoRepo.findByUserId(userId);
    }

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

            userAccountRepo.save(user);
            userInfoRepo.save(userInfo);

            responseBody.put("success", true);
        } catch (Exception e) {
            System.out.println(e);
            responseBody.put("success", false);
            return new ResponseEntity<HashMap<String, Object>>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }

    public ResponseEntity<HashMap<String,Object>> confirmLogin(HashMap<String,String> userData) {
        HashMap<String,Object> responseBody = new HashMap<String,Object>();

        try {
            String username = userData.get("username");
            String password = userData.get("password");

            // String encodedPassword = passwordEncoder.encode(password);
            UserAccount userAccount = getUserAccountByUsername(username);
            if (passwordEncoder.matches(password, userAccount.getPassword())) {
                // Success
                String token = jwtUtil.generateToken(userAccount);

                UserInfo userInfo = getUserInfoByUserId(userAccount.getUserId());

                responseBody.put("user", userInfo);
                responseBody.put("token", token);
                responseBody.put("success", true);
                
                return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
            } else {
                // Incorrect Password
                responseBody.put("invalidPwd", true);
                responseBody.put("success", false);
                return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
            }
        } catch (UsernameNotFoundException e) {
            // User doesn't exist
            responseBody.put("invalidUser", true);
            responseBody.put("success", false);
            return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e);
        }

        return new ResponseEntity<HashMap<String,Object>>(responseBody, HttpStatus.OK);
    }
}
