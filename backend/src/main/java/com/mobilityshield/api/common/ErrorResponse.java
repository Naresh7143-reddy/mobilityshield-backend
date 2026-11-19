package com.mobilityshield.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private String timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String message, String errorCode) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String message, String errorCode, Map<String, String> validationErrors) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
        this.timestamp = Instant.now().toString();
    }

    public static ErrorResponse of(String message, String errorCode) {
        return new ErrorResponse(message, errorCode);
    }

    public static ErrorResponse of(String message, String errorCode, Map<String, String> validationErrors) {
        return new ErrorResponse(message, errorCode, validationErrors);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
