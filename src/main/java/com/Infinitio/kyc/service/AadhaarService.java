package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.AadhaarInitializeRequest;
import com.Infinitio.kyc.entity.TbApiTypeMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;


import java.time.LocalDateTime;


@Service
public class AadhaarService {

    @Autowired
    private TbClientMasterRepository clientMasterRepository;

    @Autowired
    private TbUsageHistoryRepository usageHistoryRepository;

    @Autowired
    private TbApiTypeMasterRepository apiTypeMasterRepository;



    private final WebClient webClient;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public AadhaarService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Calls the Digilocker Initialize API to start the flow.
     * Returns the raw map containing client_id, token, etc.
     */
//    public Map<String, Object> initializeDigilocker(AadhaarInitializeRequest request,String apiKey) {
//        boolean signupFlow = request.isSignupFlow();
//        boolean skipMainScreen = request.isSkipMainScreen();
//        String logoUrl = request.getLogoUrl();
//
////        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
////        if (client == null) {
////            throw new RuntimeException("Client not found for apiKey: " + apiKey);
////        }
//
//        String url = baseUrl + "/api/v1/digilocker/initialize";
//        System.out.println(url);
//        Map<String, Object> payload = new HashMap<>();
//        Map<String, Object> data = new HashMap<>();
//        data.put("signup_flow", signupFlow);
//        data.put("skip_main_screen", skipMainScreen);
//        //        if (logoUrl != null && !logoUrl.isBlank()) {
//        //            data.put("logo_url", logoUrl);
//        //        }
//        payload.put("data", data);
//
//        try {
//            return webClient.post()
//                    .uri(url)
//                    .header("Authorization",authToken)
//                    .header("Content-Type", "application/json")
//                    .bodyValue(payload)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//
//        } catch (WebClientResponseException e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("status", e.getStatusCode().value());
//            error.put("error", e.getResponseBodyAsString());
//            return error;
//        }
//
//
//    }



    public Map<String, Object> initializeDigilocker(AadhaarInitializeRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
        if (client == null) {
            throw new RuntimeException("Client not found for apiKey: " + apiKey);
        }

        String url = baseUrl + "/api/v1/digilocker/initialize";
        System.out.println(url);

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("signup_flow", request.isSignupFlow());
        data.put("skip_main_screen", request.isSkipMainScreen());
        if (request.getLogoUrl() != null && !request.getLogoUrl().isBlank()) {
            data.put("logo_url", request.getLogoUrl());
        }
        payload.put("data", data);

        Map<String, Object> responseMap;
        try {
            responseMap = webClient.post()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            responseMap = new HashMap<>();
            responseMap.put("status", e.getStatusCode().value());
            responseMap.put("error", e.getResponseBodyAsString());
        }

        // === Save Usage History ===

        try {
            TbUsageHistory log = new TbUsageHistory();
// ✅ Save formatted JSON for better readability in DB
            log.setApiRequestBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            log.setVendorRequestBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));

            log.setApiResponseBody(mapper.writeValueAsString(responseMap)); // raw response
            log.setVendorRequestBody(mapper.writeValueAsString(payload)); // payload to vendor
            log.setData(mapper.writeValueAsString(responseMap.get("data"))); // formatted data (if applicable)

            System.out.println("Status Code:-"+ responseMap.get("status_code"));
            log.setStatus(responseMap.containsKey("status_code") && (int) responseMap.get("status_code") == 200 ? 1 : 0);
            log.setMessage(responseMap.containsKey("message") ? responseMap.get("message").toString() : "No message");
            log.setMessageCode(responseMap.containsKey("message_code") ? responseMap.get("message_code").toString() : null);
            log.setReadOnly("N");
            log.setArchiveFlag("F");
            log.setClient(client);
            log.setSentTime(LocalDateTime.now());
            log.setCreatedModifiedDate(LocalDateTime.now());

            // Set API Type for Digilocker Initialize, suppose ID = 3
            TbApiTypeMaster apiType = apiTypeMasterRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("API Type with ID 3 not found"));
            log.setApiType(apiType);

            usageHistoryRepository.save(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing log data", e);
        }

        return responseMap;
    }


    /**
     * Downloads Aadhaar XML & metadata using client_id after verification completes.
     */

    public Map<String, Object> downloadAadhaar(String clientId, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
        if (client == null) {
            throw new RuntimeException("Client not found for apiKey: " + apiKey);
        }

        String url = baseUrl + "/api/v1/digilocker/download-aadhaar/" + clientId;
        Map<String, Object> responseMap;

        try {
            responseMap = webClient.get()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            responseMap = new HashMap<>();
            responseMap.put("status", e.getStatusCode().value());
            responseMap.put("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            responseMap = new HashMap<>();
            responseMap.put("status", 500);
            responseMap.put("error", e.getMessage());
        }

        // === Save Usage History ===
        try {
            TbUsageHistory log = new TbUsageHistory();
            // ✅ Save request as JSON object
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("client_id", clientId);
            log.setApiRequestBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestMap));

            // ✅ Save empty JSON for vendor request body (GET call)
            log.setVendorRequestBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new HashMap<>())); // No request body for GET, just store clientId
            log.setApiResponseBody(mapper.writeValueAsString(responseMap)); // Response from vendor

            log.setData(mapper.writeValueAsString(responseMap.get("data"))); // Extract data if present

            log.setStatus(responseMap.containsKey("status_code") && (int) responseMap.get("status_code") == 200 ? 1 : 0);
            log.setMessage(responseMap.containsKey("message") ? responseMap.get("message").toString() : "No message");
            log.setMessageCode(responseMap.containsKey("message_code") ? responseMap.get("message_code").toString() : null);
            log.setReadOnly("N");
            log.setArchiveFlag("F");
            log.setClient(client);
            log.setSentTime(LocalDateTime.now());
            log.setCreatedModifiedDate(LocalDateTime.now());

            // Set API Type for Download Aadhaar, suppose ID = 4
            TbApiTypeMaster apiType = apiTypeMasterRepository.findById(5)
                    .orElseThrow(() -> new RuntimeException("API Type with ID 4 not found"));
            log.setApiType(apiType);

            usageHistoryRepository.save(log);
        } catch (Exception e) {
            throw new RuntimeException("Error saving usage history log: " + e.getMessage(), e);
        }

        return responseMap;
    }


//    public Map<String, Object> downloadAadhaar(String clientId) {
//        String url = baseUrl + "/api/v1/digilocker/download-aadhaar/" + clientId;
//
//        try {
//            return webClient.get()
//                    .uri(url)
//                    .header("Authorization", authToken)
//                    .header("Content-Type", "application/json")
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//        } catch (WebClientResponseException e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("status", e.getStatusCode().value());
//            error.put("error", e.getResponseBodyAsString());
//            return error;
//        } catch (Exception e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("status", 500);
//            error.put("error", e.getMessage());
//            return error;
//        }
//    }
}
