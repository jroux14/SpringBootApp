package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.ApplicationConstants;
import com.smarthome.webapp.DeviceConstants;
import com.smarthome.webapp.repositories.DeviceRepository;

@RestController
public class CommunicationController {
	@Autowired
	private DeviceRepository deviceRepository;

	private ApplicationConstants appConstants = new ApplicationConstants();
	private DeviceConstants deviceConstants = new DeviceConstants();
	private DeviceController deviceController = new DeviceController();

	int testNum = 0;

    // @GetMapping("/test")
	// public String testCall() {
	// 	deviceController.createDevice("test"+testNum, "12345", "test data", deviceRepository);
	// 	testNum += 1;
	// 	return "{ \"test\" : \"Greetings from Spring Boot!\" }";
	// }
}
