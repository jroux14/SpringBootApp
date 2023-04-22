package com.smarthome.webapp.controllers;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.services.EncryptionService;
import com.smarthome.webapp.interfaces.CommunicationInterface;
import com.smarthome.webapp.objects.User;
import com.smarthome.webapp.repositories.UserRepository;
import static com.smarthome.webapp.ApplicationConstants.*;

@RestController
@RequestMapping("smarthome/")
public class AuthenticationController {
    EncryptionService encryptor = new EncryptionService();
	@Autowired
	private UserRepository userRepository;
    private Charset charset = StandardCharsets.US_ASCII;

    public AuthenticationController() {}

    @PutMapping(value = { "user/create/", "user/create/{confirmPwd}" })
	public JSONObject createUser(@RequestBody JSONObject user, @PathVariable Optional<String> confirmPwd) {
        JSONObject response = new JSONObject();
        User newUser = new User(user);
        String hashedUsername = encryptor.encrypt(newUser.getUsername(), ENCRYPTION_KEY);
        User existingUser = this.userRepository.findByUsername(hashedUsername);
        response.put("success", false);
        response.put("passwordConfirmError", false);
        response.put("userExistsError", false);
        response.put("fillAllError", false);

        if(confirmPwd.isPresent()) {
            if(newUser.getPassword() != "" && newUser.getUsername() != "" && newUser.getFirstName() != "" && newUser.getPassword().equals(confirmPwd.get()) && existingUser == null) {
                newUser.setName(encryptor.encrypt(newUser.getFirstName(), ENCRYPTION_KEY));
                newUser.setUsername(hashedUsername);
                newUser.setPassword(encryptor.encrypt(newUser.getPassword(), ENCRYPTION_KEY));
                response.put("success", true);
                this.userRepository.save(newUser);
            } else {
                if(!newUser.getPassword().equals(confirmPwd.get())) {
                    response.put("success", false);
                    response.put("passwordConfirmError", true);
                }
                if(existingUser != null) {
                    response.put("success", false);
                    response.put("userExistsError", true);
                }
                if(newUser.getPassword() == "" || newUser.getUsername() == "" || newUser.getFirstName() == "") {
                    response.put("success", false);
                    response.put("fillAllError", true);
                }
            }
        } else {
            response.put("success", false);
            response.put("fillAllError", true);
        }

        return response;
	}

    @GetMapping(value = { "user/login/{username}/{pwd}", "user/login" })
    public JSONObject attemptLogin(@PathVariable Optional<String> username, @PathVariable Optional<String> pwd) {
        JSONObject response = new JSONObject();
        response.put("success", false);
        response.put("badCredentialsError", false);
        response.put("fillAllError", false);

        if(username.isPresent() && pwd.isPresent()) {
            String hashedUsername = encryptor.encrypt(username.get(), ENCRYPTION_KEY);
            User userExists = this.userRepository.findByUsername(hashedUsername);
            if(userExists != null) {
                String decryptedPwd = encryptor.decrypt(userExists.getPassword(), ENCRYPTION_KEY);
                if(decryptedPwd.equals(pwd.get())) {
                    userExists.setName(encryptor.decrypt(userExists.getFirstName(), ENCRYPTION_KEY));
                    response.put("success", true);
                    response.put("user", userExists);
                }
            } else {
                if(userExists == null) {
                    response.put("success", false);
                    response.put("badCredentialsError", true);
                }
                if(userExists != null && !userExists.getPassword().equals(pwd.get())) {
                    response.put("success", false);
                    response.put("invalidPasswordError", true);
                }
            }
        } else {
            response.put("success", false);
            response.put("fillAllError", true);
        }

        return response;
    }

    @GetMapping("user/get/id/{userID}")
    public User getUserByUserID(@PathVariable String userID) {
        User results = this.userRepository.findByUserID(userID);
        return results;
    }

    @GetMapping("user/get/username/{username}")
    public User getUserByUsername(@PathVariable String username) {
        User results = this.userRepository.findByUsername(username);
        return results;
    }
}
