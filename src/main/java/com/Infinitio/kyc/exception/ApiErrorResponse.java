package com.Infinitio.kyc.exception;

import lombok.Data;

@Data
public class ApiErrorResponse {
    private String error;
    private String message;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }
}