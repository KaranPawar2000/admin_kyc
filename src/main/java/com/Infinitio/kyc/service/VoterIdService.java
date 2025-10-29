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
import java.util.*;

@Service
public class VoterIdService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public VoterIdService(WebClient webClient, TbClientMasterRepository clientRepo,
                          TbUsageHistoryRepository usageRepo, TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyVoterId(VoterIdRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();
        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/voter-id/voter-id";
        Map<String, Object> payload = Map.of("id_number", request.getIdNumber());
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> formattedResponse = new HashMap<>();

        int status = 1;
        String message = "Success";
        String message_code = "sucess";

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
            message_code = "fail";
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
            message_code = "fail";
            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            formattedResponse = Map.of(
                    "code", 500,
                    "message", "Internal Server Error",
                    "data", Map.of("error", e.getMessage())
            );

        } finally {
            // === Always Save Usage History (even if error occurs) ===
            try {
                TbUsageHistory log = new TbUsageHistory();
                log.setApiRequestBody(mapper.writeValueAsString(request));
                log.setVendorRequestBody(mapper.writeValueAsString(payload));
                log.setApiResponseBody(mapper.writeValueAsString(apiResponse));
                log.setData(mapper.writeValueAsString(formattedResponse.get("data")));
                log.setStatus(status);
                log.setMessage(message);
                log.setMessageCode(message_code);
                log.setReadOnly("N");
                log.setArchiveFlag("F");
                log.setClient(client);
                log.setSentTime(LocalDateTime.now());
                log.setCreatedModifiedDate(LocalDateTime.now());

                TbApiTypeMaster apiType = apiTypeRepo.findById(7)
                        .orElseThrow(() -> new RuntimeException("API Type with ID 7 not found"));
                log.setApiType(apiType);

                usageRepo.save(log);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return formattedResponse;
    }

    private Map<String, Object> formatResponse(Map<String, Object> apiResponse) {
        Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
        if (data == null) {
            return Map.of("code", 500, "message", "Invalid response from Surepass", "data", Map.of());
        }

        // Convert helper: null -> "" and convert to uppercase
        java.util.function.Function<Object, String> upper = v -> v == null ? "" : v.toString().toUpperCase();

        // === Split Address ===
        Map<String, Object> splitAddress = new LinkedHashMap<>();
        splitAddress.put("district", List.of(upper.apply(data.get("district"))));
        splitAddress.put("state", List.of(List.of(upper.apply(data.get("state")))));
        splitAddress.put("city", List.of(upper.apply(data.get("assembly_constituency"))));
        splitAddress.put("pincode", "");
        splitAddress.put("country", List.of("IN", "IND", "INDIA"));
        splitAddress.put("addressLine", upper.apply(data.get("polling_station")));

        // === Result Data (ordered) ===
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", upper.apply(data.get("name")));
        result.put("nameInRegionalLang", data.getOrDefault("name_v1", ""));
        result.put("age", data.getOrDefault("age", ""));
        result.put("relationType", data.getOrDefault("relation_type", ""));
        result.put("relationName", upper.apply(data.get("relation_name")));
        result.put("relationNameInRegionalLang", data.getOrDefault("rln_name_v1", ""));
        result.put("fatherName", upper.apply(data.get("relation_name")));
        result.put("dob", data.getOrDefault("dob", "") == null ? "" : data.get("dob"));
        result.put("gender", "F".equalsIgnoreCase((String) data.get("gender")) ? "FEMALE" : "MALE");
        result.put("address", upper.apply(data.get("polling_station")));
        result.put("splitAddress", splitAddress);
        result.put("epicNumber", upper.apply(data.get("epic_no")));
        result.put("state", upper.apply(data.get("state")));
        result.put("assemblyConstituencyNo", upper.apply(data.get("assembly_constituency_number")));
        result.put("assemblyConstituency", upper.apply(data.get("assembly_constituency")));
        result.put("parliamentaryConstituencyNo", upper.apply(data.get("parliamentary_number")));
        result.put("parliamentaryConstituency", upper.apply(data.get("parliamentary_constituency")));
        result.put("partNo", upper.apply(data.get("part_number")));
        result.put("partName", upper.apply(data.get("part_name")));
        result.put("serialNo", upper.apply(data.get("slno_inpart")));
        result.put("pollingStation", upper.apply(data.get("polling_station")));
        result.put("photo", "");

        // === Final Structured Response ===
        Map<String, Object> finalData = new LinkedHashMap<>();
        finalData.put("result", result);

        Map<String, Object> formattedResponse = new LinkedHashMap<>();
        formattedResponse.put("code", 200);
        formattedResponse.put("message", "sucess"); // as per your exact spelling
        formattedResponse.put("data", finalData);

        return formattedResponse;
    }


}
