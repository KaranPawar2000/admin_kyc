package com.Infinitio.kyc.dto;

import lombok.Data;

@Data
public class RoleDTOAdd{
    private String name;
    private Integer parentRoleId;
    private String shortCode;
    private Byte status;
    private Integer sequenceNo;
    private Integer parentId;
    private String parentName;
    private String type;
    private String description;
    private String readOnly;
    private String archiveFlag;
    private String startPage;
    private Integer clientId; // ID of TbClientMaster
}
