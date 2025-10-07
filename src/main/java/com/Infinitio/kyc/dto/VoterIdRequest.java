package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VoterIdRequest {

    @JsonProperty("EPIC_NUMBER")
    private String idNumber;
}
