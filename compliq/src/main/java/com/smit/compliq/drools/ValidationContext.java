package com.smit.compliq.drools;

import com.smit.compliq.entity.Contract;
import com.smit.compliq.entity.Invoice;
import com.smit.compliq.entity.PurchaseOrder;
import com.smit.compliq.entity.Vendor;

public class ValidationContext {

    private Invoice invoice;

    private PurchaseOrder po;

    private Contract contract;

    private Vendor vendor;
    
    private ValidationResult validationResult;

	public ValidationContext(Invoice invoice, PurchaseOrder po, Contract contract, Vendor vendor) {
		super();
		this.invoice = invoice;
		this.po = po;
		this.contract = contract;
		this.vendor = vendor;
		this.validationResult = new ValidationResult();
	}

	public com.smit.compliq.entity.Organization getOrganization() {
        if (invoice != null && invoice.getDocument() != null) {
            return invoice.getDocument().getOrganization();
        }
        if (po != null && po.getDocument() != null) {
            return po.getDocument().getOrganization();
        }
        if (contract != null && contract.getDocument() != null) {
            return contract.getDocument().getOrganization();
        }
        return null;
    }
	
	public ValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(
            ValidationResult validationResult) {
        this.validationResult = validationResult;
    }

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public PurchaseOrder getPo() {
		return po;
	}

	public void setPo(PurchaseOrder po) {
		this.po = po;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

}
