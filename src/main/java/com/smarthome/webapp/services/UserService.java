package com.smarthome.webapp.services;

import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

    @Override
    public UserAccount loadUserByUsername(String username) throws UsernameNotFoundException {
        // Retrieves user details by username from the database
        return userAccountRepo.findByUsername(username);
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
}
