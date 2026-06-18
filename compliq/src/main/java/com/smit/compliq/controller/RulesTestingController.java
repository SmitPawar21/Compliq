package com.smit.compliq.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.drools.RuleExecutionService;
import com.smit.compliq.drools.ValidationContext;
import com.smit.compliq.drools.ValidationResult;
import com.smit.compliq.entity.Invoice;
import com.smit.compliq.entity.Vendor;
import com.smit.compliq.enums.VendorStatus;

@RestController
@RequestMapping("/api/rule-test")
public class RulesTestingController {

    private final RuleExecutionService ruleExecutionService;

    public RulesTestingController(RuleExecutionService ruleExecutionService) {

        this.ruleExecutionService = ruleExecutionService;
    }

    @PostMapping("/")
    public ValidationResult testRules() {

        Vendor vendor = new Vendor();

        vendor.setVendorName("ABC LTD");

        // Rule: Vendor Must Be Active
        vendor.setVendorStatus(VendorStatus.INACTIVE);

        Invoice invoice = new Invoice();

        // Rule: GST Format Validation
        invoice.setGstNumber("ABCDE1234");

        invoice.setVendor(vendor);

        ValidationContext context = new ValidationContext(invoice,null,null,vendor);

        return ruleExecutionService.validate(context);
    }
}
