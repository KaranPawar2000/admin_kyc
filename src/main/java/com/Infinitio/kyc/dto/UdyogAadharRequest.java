package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UdyogAadharRequest {

    @JsonProperty("AADHAR_NO")
    private String AADHAR_NO;
}
