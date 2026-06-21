package com.smit.compliq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.ComplianceReportRequest;
import com.smit.compliq.service.ComplianceReportService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/compliance-report")
public class ComplianceReportController {
	private final ComplianceReportService complianceReportService;
	
	public ComplianceReportController(ComplianceReportService complianceReportService) {
		this.complianceReportService = complianceReportService;
	}

    @PostMapping
    public ResponseEntity<?> generateReport(@RequestBody ComplianceReportRequest request) {
        try {        	
        	return ResponseEntity.ok(complianceReportService.generateReport(request));
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e);
        }
    }
}
