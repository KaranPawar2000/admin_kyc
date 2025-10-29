package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.VoterIdRequest;
import com.Infinitio.kyc.entity.TbApiTypeMaster;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbApiTypeMasterRepository;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
public class Signzy_VoterIdService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${signzy.voterid.url}")
    private String signzyVoterIdUrl;

    @Value("${signzy.authorization.key}")
    private String authorizationKey;

    public Signzy_VoterIdService(WebClient webClient,
                          TbClientMasterRepository clientRepo,
                          TbUsageHistoryRepository usageRepo,
                          TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyVoterId(VoterIdRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("epicNumber", request.getIdNumber());
        payload.put("getAdditionalData", "true");

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        Map<String, Object> apiResponse = new LinkedHashMap<>();
        Map<String, Object> formattedResponse = new LinkedHashMap<>();

        int status = 1;
        String message = "Success";
        String message_Code ="sucess";
        try {
            // 🔹 Call Signzy API
            apiResponse = webClient.post()
                    .uri(signzyVoterIdUrl)
                    .header("Content-Type", "application/json")
                    .header("Authorization", authorizationKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            formattedResponse = formatResponse(apiResponse);
            int code = (int) formattedResponse.get("code");
            System.out.println("Signzy Voter ID API Response Code: " + code);

        } catch (WebClientResponseException e) {
            status = 0;
            message = "Signzy API Error: " + e.getStatusCode().value();
            message_Code ="fail";
            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "data", Map.of("result", Map.of("error", e.getResponseBodyAsString()))
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();
            message_Code ="fail";
            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            formattedResponse = Map.of(
                    "code", 500,
                    "data", Map.of("result", Map.of("error", e.getMessage()))
            );
        } finally {
            try {
                System.out.println("API Response: " + mapper.writeValueAsString(apiResponse));
                // 🔹 Log to Usage History
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(payload));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status);
                log.setMessage(message);
                log.setMessageCode(message_Code);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                // Replace 10 with your API type ID for Voter ID
                TbApiTypeMaster apiType = apiTypeRepo.findById(7)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 7 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing usage log", e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return formattedResponse;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> formatResponse(Map<String, Object> apiResponse) {
        Map<String, Object> result = (Map<String, Object>) apiResponse.get("result");
        if (result == null) result = new LinkedHashMap<>();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("result", result);

        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("code", 200);
        finalResponse.put("message", "Success");
        finalResponse.put("data", data);

        return finalResponse;
    }
}