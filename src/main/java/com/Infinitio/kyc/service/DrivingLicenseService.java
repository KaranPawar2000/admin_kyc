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

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DrivingLicenseService {

    private final WebClient webClient;
    private final TbClientMasterRepository clientMasterRepository;
    private final TbUsageHistoryRepository usageHistoryRepository;
    private final TbApiTypeMasterRepository apiTypeMasterRepository;

    @Value("${surepass.base.url}")
    private String baseUrl;

    @Value("${surepass.bearer.token}")
    private String authToken;

    public DrivingLicenseService(WebClient webClient,
                                 TbClientMasterRepository clientMasterRepository,
                                 TbUsageHistoryRepository usageHistoryRepository,
                                 TbApiTypeMasterRepository apiTypeMasterRepository) {
        this.webClient = webClient;
        this.clientMasterRepository = clientMasterRepository;
        this.usageHistoryRepository = usageHistoryRepository;
        this.apiTypeMasterRepository = apiTypeMasterRepository;
    }

//    public Map<String, Object> verifyDrivingLicense(DrivingLicenseRequest request, String apiKey) {
//        ObjectMapper mapper = new ObjectMapper();
//
//        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
//        if (client == null) {
//            throw new RuntimeException("Client not found for apiKey: " + apiKey);
//        }
//
//        String url = baseUrl + "/api/v1/driving-license/driving-license";
//
//        System.out.println("DL Request - NUMBER: " + String.valueOf(request.getNumber()) +
//                ", DOB: " + String.valueOf(request.getDob()));
//        System.out.println("Received API Key for DL: " + apiKey);
//
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("id_number", request.getNumber());
//        payload.put("dob", formatDateToIso(request.getDob()));
////        payload.put("dob", request.getDob());
//        System.out.println("Payload for Surepass DL: " + payload);
//
//        Map<String, Object> vendorResponse;
//        try {
//            vendorResponse = webClient.post()
//                    .uri(url)
//                    .header("Authorization", authToken)
//                    .header("Content-Type", "application/json")
//                    .bodyValue(payload)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//            System.out.println("Vendor Response 1: " + vendorResponse);
//        } catch (WebClientResponseException e) {
//            vendorResponse = new HashMap<>();
//            vendorResponse.put("status_code", e.getStatusCode().value());
//            vendorResponse.put("error", e.getResponseBodyAsString());
//            System.out.println("Vendor Response 1: " + vendorResponse);
//        }
//
//        // Save usage history
//        try {
//            TbUsageHistory log = new TbUsageHistory();
//            log.setApiRequestBody(mapper.writeValueAsString(request));
//            log.setVendorRequestBody(mapper.writeValueAsString(payload));
//            log.setApiResponseBody(mapper.writeValueAsString(vendorResponse));
//            log.setStatus(vendorResponse.containsKey("status_code") &&
//                    (int) vendorResponse.get("status_code") == 200 ? 1 : 0);
//            log.setMessage(String.valueOf(vendorResponse.get("message")));
//            log.setMessageCode(vendorResponse.containsKey("message_code")
//                    ? vendorResponse.get("message_code").toString() : null);
//            log.setReadOnly("N");
//            log.setArchiveFlag("F");
//            log.setClient(client);
//            log.setSentTime(LocalDateTime.now());
//            log.setCreatedModifiedDate(LocalDateTime.now());
//
//            TbApiTypeMaster apiType = apiTypeMasterRepository.findById(6)
//                    .orElseThrow(() -> new RuntimeException("API Type with ID 6 not found"));
//            log.setApiType(apiType);
//
//            usageHistoryRepository.save(log);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Error serializing usage log", e);
//        }
//
//        // Transform response
//        Map<String, Object> finalResponse = new HashMap<>();
//        finalResponse.put("code", vendorResponse.get("status_code"));
//        finalResponse.put("data", Map.of("result", mapDrivingLicenseData(vendorResponse)));
//
//        return finalResponse;
//    }


    public Map<String, Object> verifyDrivingLicense(DrivingLicenseRequest request, String apiKey) {
        ObjectMapper mapper = new ObjectMapper();

        // 1️⃣ Fetch client
        TbClientMaster client = clientMasterRepository.findByApiKey(apiKey);
        if (client == null) {
            throw new RuntimeException("Client not found for apiKey: " + apiKey);
        }

        String url = baseUrl + "/api/v1/driving-license/driving-license";

        // 2️⃣ Prepare payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("id_number", request.getNumber());
        payload.put("dob", formatDateToIso(request.getDob())); // yyyy-MM-dd

        // 3️⃣ Call vendor API
        Map<String, Object> vendorResponse;
        try {
            vendorResponse = webClient.post()
                    .uri(url)
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            vendorResponse = new HashMap<>();
            vendorResponse.put("status_code", e.getStatusCode().value());
            vendorResponse.put("error", e.getResponseBodyAsString());
        }

        // 4️⃣ Extract vendor data
        Map<String, Object> vendorData = new HashMap<>();
        if (vendorResponse != null && vendorResponse.containsKey("data")) {
            Map<String, Object> dataMap = (Map<String, Object>) vendorResponse.get("data");
            if (dataMap != null && dataMap.containsKey("result")) {
                vendorData = (Map<String, Object>) dataMap.get("result");
            }
        }

        // 5️⃣ Build final mapped response
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dlNumber", vendorData.getOrDefault("dlNumber", ""));
        result.put("dob", formatDateToDdMmYyyy(vendorData.get("dob")));

        // Badge details
        Map<String, Object> badgeDetail = new LinkedHashMap<>();
        badgeDetail.put("badgeIssueDate", "");
        badgeDetail.put("badgeNo", "");
        badgeDetail.put("classOfVehicle", vendorData.getOrDefault("classOfVehicle", List.of()));
        result.put("badgeDetails", List.of(badgeDetail));

        // DL validity
        Map<String, Object> dlValidity = new LinkedHashMap<>();
        Map<String, Object> nonTransport = new LinkedHashMap<>();
        nonTransport.put("from", formatDateToDdMmYyyy(vendorData.get("doi")));
        nonTransport.put("to", formatDateToDdMmYyyy(vendorData.get("doe")));
        Map<String, Object> transport = new LinkedHashMap<>();
        transport.put("from", formatDateToDdMmYyyy(vendorData.get("transport_doi")));
        transport.put("to", formatDateToDdMmYyyy(vendorData.get("transport_doe")));
        dlValidity.put("nonTransport", nonTransport);
        dlValidity.put("transport", transport);
        dlValidity.put("hazardousValidTill", "");
        dlValidity.put("hillValidTill", "");
        result.put("dlValidity", dlValidity);

        // Driving Licence details
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("dateOfIssue", formatDateToDdMmYyyy(vendorData.get("doi")));
        details.put("dateOfLastTransaction", "");
        details.put("status", vendorData.getOrDefault("status", "ACTIVE"));
        details.put("lastTransactedAt", "");
        details.put("name", vendorData.getOrDefault("name", ""));
        details.put("fatherOrHusbandName", vendorData.getOrDefault("fatherOrHusbandName", ""));
        details.put("photo", vendorData.getOrDefault("photo", ""));
        details.put("covDetails", List.of());

        // Addresses
        String permanentAddr = (String) vendorData.getOrDefault("permanentAddress", "");
        String tempAddr = (String) vendorData.getOrDefault("temporaryAddress", permanentAddr);
        String state = (String) vendorData.getOrDefault("state", "");
        String permanentPincode = (String) vendorData.getOrDefault("permanentPincode", "");
        String tempPincode = (String) vendorData.getOrDefault("temporaryPincode", permanentPincode);

        Map<String, Object> permanentAddress = buildDynamicAddress(permanentAddr, permanentPincode, state, "permanent");
        Map<String, Object> temporaryAddress = buildDynamicAddress(tempAddr, tempPincode, state, "temporary");

        details.put("addressList", List.of(permanentAddress, temporaryAddress));
        details.put("address", permanentAddr);
        details.put("splitAddress", permanentAddress.get("splitAddress"));

        result.put("detailsOfDrivingLicence", details);

        // 6️⃣ Wrap final response
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("code", 200);
        finalResponse.put("data", Map.of("result", result));

        return finalResponse;
    }



    /**
     * Maps the Surepass vendor response into the required nested structure.
     */
//    private Map<String, Object> mapDrivingLicenseData(Map<String, Object> data) {
//        Map<String, Object> result = new LinkedHashMap<>();
//
//        // 1️⃣ Basic info
//        result.put("dlNumber", data.get("dl_number"));
//        result.put("dob", formatDateToDdMmYyyy(data.get("dob")));
//
//        // 2️⃣ Badge details
//        Map<String, Object> badgeDetail = new LinkedHashMap<>();
//        badgeDetail.put("badgeIssueDate", formatDateToDdMmYyyy(data.get("badge_issue_date")));
//        badgeDetail.put("badgeNo", data.get("badge_no"));
//        badgeDetail.put("classOfVehicle", data.getOrDefault("class_of_vehicle", List.of()));
//        result.put("badgeDetails", List.of(badgeDetail));
//
//        // 3️⃣ DL validity
//        Map<String, Object> dlValidity = new LinkedHashMap<>();
//
//        Map<String, Object> nonTransport = new LinkedHashMap<>();
//        nonTransport.put("from", formatDateToDdMmYyyy(data.get("doi")));
//        nonTransport.put("to", formatDateToDdMmYyyy(data.get("doe")));
//
//        Map<String, Object> transport = new LinkedHashMap<>();
//        transport.put("from", formatDateToDdMmYyyy(data.get("transport_doi")));
//        transport.put("to", formatDateToDdMmYyyy(data.get("transport_doe")));
//
//        dlValidity.put("nonTransport", nonTransport);
//        dlValidity.put("transport", transport);
//        dlValidity.put("hazardousValidTill", "");
//        dlValidity.put("hillValidTill", "");
//        result.put("dlValidity", dlValidity);
//
//        // 4️⃣ Details of Driving Licence
//        Map<String, Object> details = new LinkedHashMap<>();
//        details.put("dateOfIssue", formatDateToDdMmYyyy(data.get("doi")));
//        details.put("status", data.getOrDefault("current_status", "ACTIVE"));
//        details.put("name", data.get("name"));
//        details.put("fatherOrHusbandName", data.get("father_name"));
//
//        // 5️⃣ Address list (permanent + temporary)
//        Map<String, Object> permanentAddress = buildDynamicAddress(
//                (String) data.get("permanent_address"),
//                (String) data.get("permanent_zip"),
//                (String) data.get("state"),
//                "permanent"
//        );
//
//        Map<String, Object> temporaryAddress = buildDynamicAddress(
//                (String) data.getOrDefault("temporary_address", data.get("permanent_address")),
//                (String) data.get("permanent_zip"),
//                (String) data.get("state"),
//                "temporary"
//        );
//
//        details.put("addressList", List.of(permanentAddress, temporaryAddress));
//        result.put("detailsOfDrivingLicence", details);
//
//        // ✅ Final wrapped response
//        Map<String, Object> finalResponse = new LinkedHashMap<>();
//        finalResponse.put("code", 200);
//        finalResponse.put("data", Map.of("result", result));
//
//        return finalResponse;
//    }


    private Map<String, Object> mapDrivingLicenseData(Map<String, Object> vendorResponse) {
        Map<String, Object> vendorData = (Map<String, Object>) vendorResponse.get("data");
        if (vendorData == null) vendorData = new HashMap<>();

        Map<String, Object> result = new LinkedHashMap<>();

        // 1️⃣ Basic info
        result.put("dlNumber", vendorData.get("license_number"));
        result.put("dob", formatDateToDdMmYyyy(vendorData.get("dob")));

        // 2️⃣ Badge details
        Map<String, Object> badgeDetail = new LinkedHashMap<>();
        badgeDetail.put("badgeIssueDate", ""); // Not provided in vendor response
        badgeDetail.put("badgeNo", ""); // Not provided in vendor response
        badgeDetail.put("classOfVehicle", vendorData.getOrDefault("vehicle_classes", List.of()));
        result.put("badgeDetails", List.of(badgeDetail));

        // 3️⃣ DL validity
        Map<String, Object> dlValidity = new LinkedHashMap<>();

        Map<String, Object> nonTransport = new LinkedHashMap<>();
        nonTransport.put("from", formatDateToDdMmYyyy(vendorData.get("doi")));
        nonTransport.put("to", formatDateToDdMmYyyy(vendorData.get("doe")));

        Map<String, Object> transport = new LinkedHashMap<>();
        transport.put("from", formatDateToDdMmYyyy(vendorData.get("transport_doi")));
        transport.put("to", formatDateToDdMmYyyy(vendorData.get("transport_doe")));

        dlValidity.put("nonTransport", nonTransport);
        dlValidity.put("transport", transport);
        dlValidity.put("hazardousValidTill", "");
        dlValidity.put("hillValidTill", "");
        result.put("dlValidity", dlValidity);

        // 4️⃣ Details of Driving Licence
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("dateOfIssue", formatDateToDdMmYyyy(vendorData.get("doi")));
        details.put("status", vendorData.getOrDefault("current_status", "ACTIVE"));
        details.put("name", vendorData.get("name"));
        details.put("fatherOrHusbandName", vendorData.get("father_or_husband_name"));

        // 5️⃣ Address list
        Map<String, Object> permanentAddress = buildDynamicAddress(
                (String) vendorData.get("permanent_address"),
                (String) vendorData.get("permanent_zip"),
                (String) vendorData.get("state"),
                "permanent"
        );

        Map<String, Object> temporaryAddress = buildDynamicAddress(
                (String) vendorData.getOrDefault("temporary_address", vendorData.get("permanent_address")),
                (String) vendorData.getOrDefault("temporary_zip", vendorData.get("permanent_zip")),
                (String) vendorData.get("state"),
                "temporary"
        );

        details.put("addressList", List.of(permanentAddress, temporaryAddress));
        result.put("detailsOfDrivingLicence", details);

        // ✅ Wrap final response
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("code", 200);
        finalResponse.put("data", Map.of("result", result));

        return finalResponse;
    }

    private Map<String, Object> buildDynamicAddress(String fullAddress, String pincode, String state, String type) {
        Map<String, Object> addressMap = new LinkedHashMap<>();

        // Default empty fields
        String city = "";
        String district = "";
        String village = "";
        String tehsil = "";
        String block = "";
        String subDistrict = "";

        // Split the address by comma or other delimiters
        if (fullAddress != null && !fullAddress.isEmpty()) {
            String[] parts = fullAddress.split(",");
            List<String> splitList = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    splitList.add(trimmed);
                }
            }

            // Heuristic mapping (adjust based on your data)
            if (splitList.size() > 0) village = splitList.get(0);
            if (splitList.size() > 1) tehsil = splitList.get(1);
            if (splitList.size() > 2) district = splitList.get(2);
            if (splitList.size() > 3) city = splitList.get(3);
            if (splitList.size() > 4) block = splitList.get(4);
            if (splitList.size() > 5) subDistrict = splitList.get(5);

            addressMap.put("splitAddress", splitList);
        } else {
            addressMap.put("splitAddress", List.of());
        }

        // Populate the address map
        addressMap.put("addressType", type);
        addressMap.put("fullAddress", fullAddress != null ? fullAddress : "");
        addressMap.put("village", village);
        addressMap.put("tehsil", tehsil);
        addressMap.put("district", district);
        addressMap.put("city", city);
        addressMap.put("block", block);
        addressMap.put("subDistrict", subDistrict);
        addressMap.put("state", state != null ? state : "");
        addressMap.put("pincode", pincode != null ? pincode : "");

        return addressMap;
    }


    // Example: returns state code dynamically
    private String stateCode(String state) {
        if (state == null) return "";
        switch (state.toUpperCase()) {
            case "MAHARASHTRA": return "MH";
            case "DELHI": return "DL";
            case "GUJARAT": return "GJ";
            // Add all states as needed
            default: return "";
        }
    }

    // Format date yyyy-MM-dd → dd/MM/yyyy
    private String formatDateToDdMmYyyy(Object dateObj) {
        if (dateObj == null || dateObj.toString().isEmpty()) return "";
        String date = dateObj.toString();
        try {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {
            return date;
        }
    }


    // Format date to ISO yyyy-MM-dd (for vendor API)
    private String formatDateToIso(Object dateObj) {
        if (dateObj == null || dateObj.toString().isEmpty()) return "";
        String date = dateObj.toString();

        // If date is already in dd/MM/yyyy, convert to yyyy-MM-dd
        if (date.contains("/")) {
            String[] parts = date.split("/");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }

        // If date is already in yyyy-MM-dd
        return date;
    }



}
