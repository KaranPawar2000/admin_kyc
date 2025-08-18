package com.Infinitio.kyc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminLoginResponse {
    private String name;
    private Integer roleId;
    private Integer userId;
}
