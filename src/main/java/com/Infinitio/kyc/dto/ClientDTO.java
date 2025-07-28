package com.Infinitio.kyc.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClientDTO {
    private Integer id;
    private String orgName;
    private String emailId;
    private String mobileNo;
    private String pincode;
    private byte isEncrypted;
    private Byte status;
    private String address;
    private LocalDate expiryDate;
    private String logo;
    private String apiKey;
}
