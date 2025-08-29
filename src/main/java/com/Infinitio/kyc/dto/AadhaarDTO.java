package com.Infinitio.kyc.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AadhaarDTO {

    @NotNull(message = "signupFlow is required")
    private boolean signupFlow;
    private boolean skipMainScreen;
    private String logoUrl;
    private String apiKey;

    public boolean getSkipMainScreen() {
        return signupFlow;
    }

    public boolean getSignupFlow() {
        return skipMainScreen;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
