package com.Infinitio.kyc.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClientDTOAdd {

    private String orgName;
    private String emailId;
    private String mobileNo;
    private String password;
    private LocalDate expiryDate;
    private Integer clientCount;
    private String address;
    private String logo;
    private String pincode;
    private Byte status;
    private byte isEncrypted;
    private String apiKey;
    private String archiveFlag;
    private String readOnly;

    private Integer roleId;     // ✅ Send role ID back
    private String roleName;
}
