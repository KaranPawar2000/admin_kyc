package com.Infinitio.kyc.dto;


import lombok.Data;

@Data
public class RoleDTO {
    private Integer id;
    private String name;
    private Byte status;
    private String description;
}
