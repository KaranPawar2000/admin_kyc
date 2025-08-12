package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PanRequest {
    @JsonProperty("PAN_NO")
    private String panNo;
    // getters/setters
}
