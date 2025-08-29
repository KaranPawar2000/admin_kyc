package com.Infinitio.kyc.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AadhaarInitializeRequest {

    @NotNull(message = "signupFlow is required")
    private boolean signupFlow = true;      // default value
    private boolean skipMainScreen = false; // default value
    private String logoUrl;
}
