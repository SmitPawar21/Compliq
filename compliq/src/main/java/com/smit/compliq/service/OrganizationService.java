package com.smit.compliq.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.smit.compliq.entity.Organization;
import com.smit.compliq.entity.RuleConfiguration;
import com.smit.compliq.enums.Severity;
import com.smit.compliq.repository.OrganizationRepository;
import com.smit.compliq.repository.RuleConfigurationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RuleConfigurationRepository ruleConfigurationRepository;

    @Transactional
    public Organization createOrganization(Organization organization) {
        Organization savedOrg = organizationRepository.save(organization);
        
        // Default rules
        RuleConfiguration invoiceVariance = new RuleConfiguration(savedOrg, "Invoice Variance Threshold", "0.02", Severity.MEDIUM, true);
        RuleConfiguration contractExpiry = new RuleConfiguration(savedOrg, "Contract Expiring Soon", "30", Severity.LOW, true);
        
        ruleConfigurationRepository.saveAll(Arrays.asList(invoiceVariance, contractExpiry));
        
        return savedOrg;
    }
}
