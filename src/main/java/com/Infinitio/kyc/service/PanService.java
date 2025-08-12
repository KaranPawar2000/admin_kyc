package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.PanRequest;
import com.Infinitio.kyc.dto.PanResponse;
import com.Infinitio.kyc.entity.TbClientMaster;
import com.Infinitio.kyc.entity.TbUsageHistory;
import com.Infinitio.kyc.repository.TbClientMasterRepository;
import com.Infinitio.kyc.repository.TbUsageHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PanService {

    @Autowired
    private TbClientMasterRepository clientMasterRepository;

    @Autowired
    private TbUsageHistoryRepository usageHistoryRepository;

    public PanResponse verifyAndPersist(PanRequest panRequest, String apiKey) {
        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
        if (client == null) {
            throw new RuntimeException("Client not found for apiKey: " + apiKey);
        }

        ObjectMapper mapper = new ObjectMapper();
        String requestJson;
        try {
            requestJson = mapper.writeValueAsString(Map.of("id_number", panRequest.getPanNo()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing request", e);
        }

        // Call external API
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTcyNTQzNDI1NCwianRpIjoiZWU1NGE3YTktNTY0OS00MzkyLTllYTItYjhkNDNhNDY1MDA0IiwidHlwZSI6ImFjY2VzcyIsImlkZW50aXR5IjoiZGV2LmluZmluaXRpb0BzdXJlcGFzcy5pbyIsIm5iZiI6MTcyNTQzNDI1NCwiZXhwIjoyMzU2MTU0MjU0LCJlbWFpbCI6ImluZmluaXRpb0BzdXJlcGFzcy5pbyIsInRlbmFudF9pZCI6Im1haW4iLCJ1c2VyX2NsYWltcyI6eyJzY29wZXMiOlsidXNlciJdfX0.qvbVu_z4jaEvbgfmhTimWwJZhQkp27oVp_a6fja8Yz0");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> externalResponse = restTemplate.postForEntity(
                "https://kyc-api.surepass.app/api/v1/pan/pan-comprehensive", entity, String.class);

        JsonNode root;
        JsonNode dataNode;
        try {
            root = mapper.readTree(externalResponse.getBody());
            dataNode = root.path("data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing response", e);
        }

        PanResponse.Data responseData = new PanResponse.Data(
                dataNode.path("full_name").asText(),
                dataNode.path("pan_number").asText(),
                dataNode.path("category").asText(),
                dataNode.path("status").asText(),
                dataNode.path("aadhaar_linked").asBoolean() ? "Yes" : "No",
                ""
        );

        PanResponse response = new PanResponse("200", "Success", responseData);

        // Persist request/response
        TbUsageHistory log = new TbUsageHistory();
        log.setApiType(null); // Set your TbApiTypeMaster instance for PAN (e.g. from a repository)
        try {
            log.setApiRequestBody(mapper.writeValueAsString(panRequest));
            log.setApiResponseBody(mapper.writeValueAsString(response));
            log.setData(mapper.writeValueAsString(response.getData()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing log data", e);
        }
        log.setMessageCode(root.path("message_code").asText());
        log.setStatus(root.path("success").asBoolean() ? 1 : 0);
        log.setMessage(root.path("message").isNull() ? null : root.path("message").asText());
        log.setReadOnly("N");
        log.setArchiveFlag("F");
        log.setClient(client);
        log.setSentTime(LocalDateTime.now());
        log.setCreatedModifiedDate(LocalDateTime.now());

        usageHistoryRepository.save(log);

        return response;
    }
}