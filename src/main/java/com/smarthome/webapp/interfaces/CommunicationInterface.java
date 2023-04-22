package com.smarthome.webapp.interfaces;

import com.smarthome.webapp.DeviceConstants;
import com.smarthome.webapp.controllers.DeviceController;

public interface CommunicationInterface {
	DeviceConstants deviceConstants = new DeviceConstants();
	DeviceController deviceController = new DeviceController();

	

    // @GetMapping("/test")
	// public String testCall() {
	// 	deviceController.createDevice("test"+testNum, "12345", "test data", deviceRepository);
	// 	testNum += 1;
	// 	return "{ \"test\" : \"Greetings from Spring Boot!\" }";
	// }
}