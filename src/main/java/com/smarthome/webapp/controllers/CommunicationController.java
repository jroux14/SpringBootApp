package com.smarthome.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.webapp.interfaces.CommunicationInterface;
import com.smarthome.webapp.repositories.DeviceRepository;

@RestController
public class CommunicationController implements CommunicationInterface {

	@Autowired
	private DeviceRepository deviceRepository;
	
}
