package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.ApplicationConstants;
import com.smarthome.webapp.DeviceConstants;
import com.smarthome.webapp.repositories.DeviceRepository;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class CommunicationController {
	@Autowired
	private DeviceRepository deviceRepository;

	private ApplicationConstants appConstants = new ApplicationConstants();
	private DeviceConstants deviceConstants = new DeviceConstants();
	private DeviceController deviceController = new DeviceController();

    @GetMapping("/test")
	public String testCall() {
		deviceController.createDevice("test1", "12345", "test data", deviceRepository);
		return "{ \"test\" : \"Greetings from Spring Boot!\" }";
	}
}
