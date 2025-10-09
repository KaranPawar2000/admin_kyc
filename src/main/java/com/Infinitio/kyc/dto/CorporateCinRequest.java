package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CorporateCinRequest {

    @JsonProperty("CIN_NO")
    private String idNumber;
}
