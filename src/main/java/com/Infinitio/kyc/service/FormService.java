package com.Infinitio.kyc.service;

import com.Infinitio.kyc.dto.FormDTO;
import com.Infinitio.kyc.entity.TbFormMaster;
import com.Infinitio.kyc.repository.TbFormMasterRepository;
import com.Infinitio.kyc.utils.DTOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FormService {

    private static final Logger logger = LoggerFactory.getLogger(FormService.class);

    @Autowired
    private TbFormMasterRepository formRepository;

    @Autowired
    private DTOService dtoService;

    public List<FormDTO> getAllForms() {
        try {
            logger.info("Fetching all forms from Form Master");
            List<TbFormMaster> forms = formRepository.findAll();

            if (forms.isEmpty()) {
                logger.info("No forms found in Form Master");
                return Collections.emptyList();
            }

            List<FormDTO> formDTOs = forms.stream()
                    .filter(Objects::nonNull)
                    .map(dtoService::convertFormToDTO)
                    .collect(Collectors.toList());

            logger.info("Found {} forms", formDTOs.size());
            return formDTOs;

        } catch (Exception e) {
            logger.error("Error while fetching forms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch forms", e);
        }
    }

    public FormDTO getFormById(Integer id) {
        try {
            logger.info("Fetching form with ID: {}", id);
            return formRepository.findById(id)
                    .map(dtoService::convertFormToDTO)
                    .orElseThrow(() -> new RuntimeException("Form not found with ID: " + id));
        } catch (Exception e) {
            logger.error("Error while fetching form with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch form", e);
        }
    }
}
