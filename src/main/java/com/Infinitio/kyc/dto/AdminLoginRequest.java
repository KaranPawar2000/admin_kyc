package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class AdminLoginRequest {
    private String emailId;
    private String password;
}
