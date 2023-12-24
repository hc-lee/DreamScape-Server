package com.hansol.dreamscape.exceptions;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<?> handleException(Exception e) {
        JSONObject errorJSON = new JSONObject();
        errorJSON.put("errorCode", "500: INTERNAL_SERVER_ERROR");
        errorJSON.put("errorMessage", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJSON.toString());
    }

}