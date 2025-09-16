package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.ApiTypeRouteDTO;
import com.Infinitio.kyc.dto.ApiTypeRouteUpdateDTO;
import com.Infinitio.kyc.entity.TbApiRouteMaster;
import com.Infinitio.kyc.entity.TbApiTypeRouteMapping;
import com.Infinitio.kyc.repository.TbApiTypeRouteMappingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiMappingService {

    private final TbApiTypeRouteMappingRepository mappingRepository;

    public ApiMappingService(TbApiTypeRouteMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
    }

    public List<ApiTypeRouteDTO> getMappingsByClientUserId(Integer clientUserId) {
        List<TbApiTypeRouteMapping> mappings = mappingRepository.findByClientUserId(clientUserId);

        return mappings.stream().map(m -> new ApiTypeRouteDTO(
                m.getApiType().getId(),
                m.getApiType().getName(),
                m.getApiRoute() != null ? m.getApiRoute().getId() : null,
                m.getApiRoute() != null ? m.getApiRoute().getName() : null
        )).collect(Collectors.toList());
    }

    public void updateRoutesForApiTypes(List<ApiTypeRouteUpdateDTO> updates) {
        for (ApiTypeRouteUpdateDTO dto : updates) {
            TbApiTypeRouteMapping mapping = mappingRepository.findByApiTypeId(dto.getApiTypeId())
                    .orElseThrow(() -> new RuntimeException("Mapping not found for apiTypeId: " + dto.getApiTypeId()));

            if (dto.getRouteId() != null) {
                TbApiRouteMaster route = new TbApiRouteMaster();
                route.setId(dto.getRouteId());
                mapping.setApiRoute(route);
            } else {
                // Unassign route
                mapping.setApiRoute(null);
            }

            mappingRepository.save(mapping);
        }
    }

    public void updateRoutesForClient(Integer clientUserId, List<ApiTypeRouteUpdateDTO> updates) {
        List<TbApiTypeRouteMapping> clientMappings = mappingRepository.findByClientUserId(clientUserId);

        for (ApiTypeRouteUpdateDTO dto : updates) {
            TbApiTypeRouteMapping mapping = clientMappings.stream()
                    .filter(m -> m.getApiType().getId().equals(dto.getApiTypeId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Mapping not found for apiTypeId: "
                            + dto.getApiTypeId() + " for client: " + clientUserId));

            if (dto.getRouteId() != null) {
                TbApiRouteMaster route = new TbApiRouteMaster();
                route.setId(dto.getRouteId());
                mapping.setApiRoute(route);
            } else {
                mapping.setApiRoute(null); // unassign route
            }

            mappingRepository.save(mapping);
        }
    }

}
