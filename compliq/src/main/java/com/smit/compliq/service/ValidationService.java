package com.smit.compliq.service;

import org.springframework.stereotype.Service;

import com.smit.compliq.drools.RuleExecutionService;
import com.smit.compliq.drools.ValidationContext;
import com.smit.compliq.drools.ValidationIssue;
import com.smit.compliq.drools.ValidationResult;
import com.smit.compliq.dto.ValidationIssueDTO;
import com.smit.compliq.dto.ValidationResultDTO;
import com.smit.compliq.entity.Contract;
import com.smit.compliq.entity.Invoice;
import com.smit.compliq.entity.PurchaseOrder;
import com.smit.compliq.entity.Vendor;
import com.smit.compliq.exception.ContractNotFoundException;
import com.smit.compliq.exception.InvoiceNotFoundException;
import com.smit.compliq.exception.PurchaseOrderNotFoundException;
import com.smit.compliq.repository.ContractRepository;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.repository.InvoiceRepository;
import com.smit.compliq.repository.PurchaseOrderRepository;
import java.util.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {
	private final RuleExecutionService ruleExecutionService;

    private final DocumentRepository documentRepository;
    private InvoiceRepository invoiceRepository;
    private PurchaseOrderRepository poRepository;
    private ContractRepository contractRepository;
    
	public ValidationService(RuleExecutionService ruleExecutionService, DocumentRepository documentRepository,
			InvoiceRepository invoiceRepository, PurchaseOrderRepository poRepository,
			ContractRepository contractRepository) {
		super();
		this.ruleExecutionService = ruleExecutionService;
		this.documentRepository = documentRepository;
		this.invoiceRepository = invoiceRepository;
		this.poRepository = poRepository;
		this.contractRepository = contractRepository;
	}

	public ValidationResultDTO validateDocument(long invoiceDocId,long poDocId,long contractDocId) {

		Invoice invoice = invoiceRepository
	                    .findByDocument_DocId(invoiceDocId)
	                    .orElseThrow(() ->
	                            new InvoiceNotFoundException(
	                                    "Invoice not found"+ invoiceDocId));

	    PurchaseOrder po = poRepository
	                    .findByDocument_DocId(poDocId)
	                    .orElseThrow(() ->
	                            new PurchaseOrderNotFoundException(
	                                    "PO not found"+ poDocId));

	    Contract contract =
	            contractRepository
	                    .findByDocument_DocId(contractDocId)
	                    .orElseThrow(() ->
	                            new ContractNotFoundException(
	                                    "Contract not found: "+ contractDocId));

	    Vendor vendor = contract.getVendor();
	    
	    ValidationContext context =
	            new ValidationContext(
	                    invoice,
	                    po,
	                    contract,
	                    vendor
	            );

	    ValidationResult result = ruleExecutionService.validate(context);

	    return mapToDTO(result);
    }
	
	private ValidationIssueDTO mapToDTO(ValidationIssue issue) {

	    ValidationIssueDTO dto = new ValidationIssueDTO();

	    dto.setRuleName(issue.getRuleName());
	    dto.setMessage(issue.getMessage());
	    dto.setSeverity(issue.getSeverity());

	    return dto;
	}

	private ValidationResultDTO mapToDTO(ValidationResult result) {

	    List<ValidationIssueDTO> violations =
	            result.getViolations()
	                    .stream()
	                    .map(this::mapToDTO)
	                    .toList();

	    return new ValidationResultDTO(violations);
	}
}
