package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class ClientLoginResponse {
    private String orgName;
    private Integer roleId;
    private Integer id;
}
