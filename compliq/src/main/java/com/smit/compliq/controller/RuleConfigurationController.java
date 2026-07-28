package com.smit.compliq.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.RuleConfigurationDTO;
import com.smit.compliq.entity.Organization;
import com.smit.compliq.entity.RuleConfiguration;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.RuleConfigurationRepository;
import com.smit.compliq.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleConfigurationController {

    private final RuleConfigurationRepository ruleConfigRepo;
    private final UserRepository userRepo;

    private User getAuthenticatedUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/")
    public ResponseEntity<List<RuleConfigurationDTO>> getRules(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Organization org = user.getOrganization();
        
        List<RuleConfiguration> rules = ruleConfigRepo.findByOrganization(org);
        
        List<RuleConfigurationDTO> dtos = rules.stream().map(r -> {
            RuleConfigurationDTO dto = new RuleConfigurationDTO();
            dto.setId(r.getId());
            dto.setRuleName(r.getRuleName());
            dto.setConditionValue(r.getConditionValue());
            dto.setSeverity(r.getSeverity());
            dto.setEnabled(r.isEnabled());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleConfigurationDTO> updateRule(
            @PathVariable Long id, 
            @RequestBody RuleConfigurationDTO dto, 
            Authentication authentication) {
        
        User user = getAuthenticatedUser(authentication);
        Organization org = user.getOrganization();
        
        RuleConfiguration rule = ruleConfigRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        
        // Ensure user can only edit their own org's rules
        if (!rule.getOrganization().getId().equals(org.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        rule.setConditionValue(dto.getConditionValue());
        rule.setEnabled(dto.isEnabled());
        
        RuleConfiguration updated = ruleConfigRepo.save(rule);
        
        dto.setId(updated.getId());
        dto.setRuleName(updated.getRuleName());
        dto.setSeverity(updated.getSeverity());
        
        return ResponseEntity.ok(dto);
    }
}
