package com.Infinitio.kyc.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDTO {
    private Integer id;
    private String name;
    private String emailId;
    private String mobileNo;
    private Byte isActive;
    private String password;
    private LocalDateTime createdModifiedDate;
    private String readOnly;
    private String archiveFlag;
    private Integer roleId;
    private String roleName;
    private Integer clientId;
}

