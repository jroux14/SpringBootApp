package com.smarthome.webapp.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.smarthome.webapp.device.domain.exception.DeviceNotFoundException;;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleDeviceNotFound(DeviceNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalState(IllegalStateException e) {
        return e.getMessage();
    }
}
