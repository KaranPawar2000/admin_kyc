package com.Infinitio.kyc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrivingLicenseRequest {
    @JsonProperty("NUMBER")
    private String number;

    @JsonProperty("DOB")
    private String dob;
}
