package com.Infinitio.kyc.controller;

import com.Infinitio.kyc.dto.FormDTO;
import com.Infinitio.kyc.service.FormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    @Autowired
    private FormService formService;

    @GetMapping("/all")
    public ResponseEntity<List<FormDTO>> getAllForms() {
        logger.info("Received request to fetch all forms");
        List<FormDTO> forms = formService.getAllForms();
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormDTO> getFormById(@PathVariable Integer id) {
        logger.info("Received request to fetch form with ID: {}", id);
        try {
            FormDTO form = formService.getFormById(id);
            return ResponseEntity.ok(form);
        } catch (RuntimeException e) {
            logger.error("Form not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
