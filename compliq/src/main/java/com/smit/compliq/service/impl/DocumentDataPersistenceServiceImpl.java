package com.smit.compliq.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.smit.compliq.dto.ContractDTO;
import com.smit.compliq.dto.InvoiceDTO;
import com.smit.compliq.dto.PurchaseOrderDTO;
import com.smit.compliq.entity.Contract;
import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.Invoice;
import com.smit.compliq.entity.PurchaseOrder;
import com.smit.compliq.entity.Vendor;
import com.smit.compliq.enums.VendorStatus;
import com.smit.compliq.repository.ContractRepository;
import com.smit.compliq.repository.ExtractedFieldRepository;
import com.smit.compliq.repository.InvoiceRepository;
import com.smit.compliq.repository.PurchaseOrderRepository;
import com.smit.compliq.repository.VendorRepostiory;
import com.smit.compliq.service.DocumentDataPersistenceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentDataPersistenceServiceImpl implements DocumentDataPersistenceService {
	
	private final InvoiceRepository invoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ContractRepository contractRepository;
    private final VendorRepostiory vendorRepository;
    private final ExtractedFieldRepository extractedFieldRepository;
    
	public DocumentDataPersistenceServiceImpl(InvoiceRepository invoiceRepository,
			PurchaseOrderRepository purchaseOrderRepository, ContractRepository contractRepository, VendorRepostiory vendorRepository, ExtractedFieldRepository extractedFieldRepository) {
		super();
		this.invoiceRepository = invoiceRepository;
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.contractRepository = contractRepository;
		this.vendorRepository = vendorRepository;
		this.extractedFieldRepository = extractedFieldRepository;
	}
	
	private String getFieldValue(Document document, String fieldName) {
	    return extractedFieldRepository.findByDocumentAndFieldName(document, fieldName)
	            .map(field -> field.getFieldValue())
	            .orElse(null);
	}
	
	@Override
    @Transactional
    public void persistExtractedData(Document document) {

        switch (document.getDocumentType()) {

            case INVOICE ->
            	saveInvoice(document);

            case PURCHASE_ORDER ->
            	savePurchaseOrder(document);

            case CONTRACT ->
            	saveContract(document);

            default ->
            	throw new IllegalArgumentException("Unsupported document type");
        }
    }
	
	private Vendor getOrCreateVendor(String vendorName) {

	    Vendor vend = vendorRepository.findByVendorName(vendorName);
        if(vend==null) {
            Vendor vendor = new Vendor();

            vendor.setVendorName(vendorName);

            vendor.setVendorStatus(VendorStatus.ACTIVE);

            return vendorRepository.save(vendor);
        }
        return vend;
	}
	
	private void saveInvoice(Document document) {

		String invoiceNumber = getFieldValue(document, "INVOICE_NUMBER");

	    String vendorName =getFieldValue(document, "VENDOR_NAME");

	    String totalAmount = getFieldValue(document, "TOTAL_AMOUNT");

	    Vendor vendor = getOrCreateVendor(vendorName);

	    Invoice invoice = new Invoice();

	    invoice.setDocument(document);

	    invoice.setInvoiceNumber(invoiceNumber);

	    if(totalAmount != null) {
	    	invoice.setTotalAmount(Double.valueOf(totalAmount));
	    }

	    invoice.setVendor(vendor);

	    invoiceRepository.save(invoice);
	}
	
	private void savePurchaseOrder(Document document) {

	    String poNumber = getFieldValue(document,"PO_NUMBER");

	    String vendorName = getFieldValue(document,"VENDOR_NAME");

	    String totalAmount = getFieldValue(document,"TOTAL_AMOUNT");

	    Vendor vendor = getOrCreateVendor(vendorName);

	    PurchaseOrder po = new PurchaseOrder();

	    po.setPoNumber(poNumber);

	    if(totalAmount != null) {
	        po.setTotalAmount(Double.valueOf(totalAmount));
	    }

	    po.setVendor(vendor);

	    po.setDocument(document);

	    purchaseOrderRepository.save(po);
	}
	
	private void saveContract(Document document) {

	    String contractNumber = getFieldValue(document, "CONTRACT_NUMBER");

	    String vendorName = getFieldValue(document, "VENDOR_NAME");

	    String startDate = getFieldValue(document, "START_DATE");

	    String endDate = getFieldValue(document, "END_DATE");

	    Vendor vendor = getOrCreateVendor(vendorName);

	    Contract contract = new Contract();

	    contract.setContractNumber(contractNumber);
	    
	    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	    try {

	        if(startDate != null) {
	            contract.setStartDate(formatter.parse(startDate));
	        }

	        if(endDate != null) {
	            contract.setEndDate(formatter.parse(endDate));
	        }

	    } catch (ParseException e) {
	        throw new RuntimeException("Failed to parse contract dates", e);
	    }

	    contract.setVendor(vendor);

	    contract.setDocument(document);

	    contractRepository.save(contract);
	}
    
    
}
