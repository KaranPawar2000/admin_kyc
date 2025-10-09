package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.DrivingLicenseRequest;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class DrivingLicenseService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientRepo;
    private final TbUsageHistoryRepository usageRepo;
    private final TbApiTypeMasterRepository apiTypeRepo;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public DrivingLicenseService(WebClient webClient,
                                 TbClientMasterRepository clientRepo,
                                 TbUsageHistoryRepository usageRepo,
                                 TbApiTypeMasterRepository apiTypeRepo) {
        this.webClient = webClient;
        this.clientRepo = clientRepo;
        this.usageRepo = usageRepo;
        this.apiTypeRepo = apiTypeRepo;
    }

    public Map<String, Object> verifyDrivingLicense(DrivingLicenseRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        TbClientMaster client = clientRepo.findByApiKey(apiKey);
        if (client == null) throw new RuntimeException("Client not found for apiKey: " + apiKey);

        String url = baseUrl + "/api/v1/driving-license/driving-license";

        Map<String, Object> payload = new HashMap<>();
        payload.put("id_number", request.getIdNumber());
        payload.put("dob", request.getDob());

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

            Map<String, Object> errResult = Map.of("error", e.getResponseBodyAsString());
            formattedResponse = Map.of(
                    "code", e.getStatusCode().value(),
                    "message", message,
                    "data", Map.of("result", errResult)
            );

        } catch (Exception e) {
            status = 0;
            message = "Internal Server Error: " + e.getMessage();

            apiResponse.put("status", 500);
            apiResponse.put("error", e.getMessage());

            Map<String, Object> errResult = Map.of("error", e.getMessage());
            formattedResponse = Map.of(
                    "code", 500,
                    "message", message,
                    "data", Map.of("result", errResult)
            );
        } finally {
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

                TbApiTypeMaster apiType = apiTypeRepo.findById(8) // Change ID if needed
                        .orElseThrow(() -> new RuntimeException("API Type with ID 8 not found"));
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
        if (data == null) data = new HashMap<>();

        DateTimeFormatter inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Format DOB
        String dob = formatDateString((String) data.get("dob"), outFmt);
        String doe = formatDateString((String) data.get("doe"), outFmt);
        String doi = formatDateString((String) data.get("doi"), outFmt);

        // Format name
        String fullName = Optional.ofNullable(data.get("name")).orElse("").toString();
        String fatherOrHusband = Optional.ofNullable(data.get("father_or_husband_name")).orElse("").toString();

        // Vehicle classes
        List<String> vehicleClasses = (List<String>) data.getOrDefault("vehicle_classes", new ArrayList<>());

        // Address formatting
        String address = Optional.ofNullable(data.get("permanent_address")).orElse("").toString();
        String state = Optional.ofNullable(data.get("state")).orElse("").toString();
        String zip = Optional.ofNullable(data.get("permanent_zip")).orElse("").toString();
        String city = Optional.ofNullable(data.get("city_name")).orElse("").toString();

        Map<String, Object> splitAddress = Map.of(
                "district", List.of(zip.isEmpty() ? "" : zip),
                "state", List.of(List.of(state.toUpperCase(), state.length() >= 2 ? state.substring(0,2).toUpperCase() : "")),
                "city", List.of(city),
                "pincode", zip,
                "country", List.of("IN", "IND", "INDIA"),
                "addressLine", address.replace(",", "")
        );

        Map<String, Object> permAddress = Map.of(
                "completeAddress", address + ", " + state + ", " + zip,
                "type", "permanent",
                "splitAddress", splitAddress
        );

        Map<String, Object> tempAddress = Map.of(
                "completeAddress", address + ", " + state + ", " + zip,
                "type", "temporary",
                "splitAddress", splitAddress
        );

        Map<String, Object> badgeDetails = Map.of(
                "badgeIssueDate", "",
                "badgeNo", "",
                "classOfVehicle", vehicleClasses
        );

        Map<String, Object> dlValidity = Map.of(
                "nonTransport", Map.of("to", doe, "from", doi),
                "hazardousValidTill", "",
                "transport", Map.of("to", "", "from", ""),
                "hillValidTill", ""
        );

        Map<String, Object> detailsOfDL = new HashMap<>();
        detailsOfDL.put("dateOfIssue", doi);
        detailsOfDL.put("dateOfLastTransaction", "");
        detailsOfDL.put("status", "ACTIVE");
        detailsOfDL.put("lastTransactedAt", "");
        detailsOfDL.put("name", fullName);
        detailsOfDL.put("fatherOrHusbandName", fatherOrHusband);
        detailsOfDL.put("addressList", List.of(permAddress, tempAddress));
        detailsOfDL.put("address", address + ", " + state + ", " + zip);
        detailsOfDL.put("photo", Optional.ofNullable(data.get("profile_image")).orElse("").toString());
        detailsOfDL.put("splitAddress", splitAddress);
        detailsOfDL.put("covDetails", new ArrayList<>());
        detailsOfDL.put("photoData", Optional.ofNullable(data.get("profile_image")).orElse("").toString());


        Map<String, Object> result = Map.of(
                "dlNumber", Optional.ofNullable(data.get("license_number")).orElse("").toString(),
                "dob", dob,
                "badgeDetails", List.of(badgeDetails),
                "dlValidity", dlValidity,
                "detailsOfDrivingLicence", detailsOfDL
        );

        return Map.of(
                "code", 200,
                "message", "Success",
                "data", Map.of("result", result)
        );
    }

    private String formatDateString(String isoDate, DateTimeFormatter outFmt) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            LocalDate d = LocalDate.parse(isoDate);
            return d.format(outFmt);
        } catch (DateTimeParseException e) {
            return isoDate;
        }
    }
}
