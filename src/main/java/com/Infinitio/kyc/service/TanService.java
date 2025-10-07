package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.TanRequest;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TanService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public TanService(WebClient webClient,
                      TbClientMasterRepository clientRepo,
                      TbUsageHistoryRepository usageRepo,
                      TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyTan(TanRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/tan/";

        Map<String, Object> payload = new HashMap<>();
        payload.put("id_number", request.getIdNumber());

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> formattedResponse = new HashMap<>();

        int status = 1;
        String message = "Success";

        try {
            apiResponse = webClient.post()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            formattedResponse = formatResponse(apiResponse);

        } catch (WebClientResponseException e) {
            status = 0;
            message = "Surepass API Error: " + e.getStatusCode().value();

            apiResponse.put("status", e.getStatusCode().value());
            apiResponse.put("error", e.getResponseBodyAsString());

            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "message", "Surepass API error",
                    "data", Map.of("error", e.getResponseBodyAsString())
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            formattedResponse = Map.of(
                    "code", 500,
                    "message", "Internal Server Error",
                    "data", Map.of("error", e.getMessage())
            );

        } finally {
            // Always save usage log
            try {
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(payload));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status);
                log.setMessage(message);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                // CHANGE ID if your DB uses different API Type for TAN
                TbApiTypeMaster apiType = apiTypeRepo.findById(10)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 10 not found"));
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
        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");

        Map<String, Object> result = new LinkedHashMap<>();
        if (data != null) {
            result.put("name", data.getOrDefault("name", ""));
            result.put("number", data.getOrDefault("tan", ""));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", "200");
        out.put("message", "Success");
        out.put("data", result);

        return out;
    }
}
