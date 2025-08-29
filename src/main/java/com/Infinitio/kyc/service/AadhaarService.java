package com.Infinitio.kyc.service;

import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AadhaarService {

    @Autowired
    private TbClientMasterRepository clientMasterRepository;

    private final WebClient webClient;

    //    @Value("${surepass.base.url}")
    private String baseUrl="https://kyc-api.surepass.app";

    //    @Value("${surepass.auth.token}")
    private String authToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTcyNTQzNDI1NCwianRpIjoiZWU1NGE3YTktNTY0OS00MzkyLTllYTItYjhkNDNhNDY1MDA0IiwidHlwZSI6ImFjY2VzcyIsImlkZW50aXR5IjoiZGV2LmluZmluaXRpb0BzdXJlcGFzcy5pbyIsIm5iZiI6MTcyNTQzNDI1NCwiZXhwIjoyMzU2MTU0MjU0LCJlbWFpbCI6ImluZmluaXRpb0BzdXJlcGFzcy5pbyIsInRlbmFudF9pZCI6Im1haW4iLCJ1c2VyX2NsYWltcyI6eyJzY29wZXMiOlsidXNlciJdfX0.qvbVu_z4jaEvbgfmhTimWwJZhQkp27oVp_a6fja8Yz0";

    public AadhaarService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls the Digilocker Initialize API to start the flow.
     * Returns the raw map containing client_id, token, etc.
     */
    public Map<String, Object> initializeDigilocker(boolean signupFlow, boolean skipMainScreen, String logoUrl) {

//        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
//        if (client == null) {
//            throw new RuntimeException("Client not found for apiKey: " + apiKey);
//        }

        String url = baseUrl + "/api/v1/digilocker/initialize";
        System.out.println(url);
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("signup_flow", signupFlow);
        data.put("skip_main_screen", skipMainScreen);
        //        if (logoUrl != null && !logoUrl.isBlank()) {
        //            data.put("logo_url", logoUrl);
        //        }
        payload.put("data", data);

        try {
            return webClient.post()
                    .uri(url)
                    .header("Authorization",authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", e.getStatusCode().value());
            error.put("error", e.getResponseBodyAsString());
            return error;
        }
    }

    /**
     * Downloads Aadhaar XML & metadata using client_id after verification completes.
     */
    public Map<String, Object> downloadAadhaar(String clientId) {
        String url = baseUrl + "/api/v1/digilocker/download-aadhaar/" + clientId;

        try {
            return webClient.get()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", e.getStatusCode().value());
            error.put("error", e.getResponseBodyAsString());
            return error;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", 500);
            error.put("error", e.getMessage());
            return error;
        }
    }
}
