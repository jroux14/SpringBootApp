package com.smarthome.webapp;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://smarthome-webapp:4200")
@RestController
public class HelloWorldController {

    @GetMapping("/test")
	public String index() {
		return "{ \"test\" : \"Greetings from Spring Boot!\" }";
	}
}
