package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class AdminDTOAdd {
    private String name;
    private String emailId;
    private String password;
    private String mobileNo;
    private Byte isActive;
    private Integer roleId;
    private Integer clientId;
}
