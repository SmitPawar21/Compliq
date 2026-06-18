package com.smit.compliq.drools;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RuleExecutionService {
	private final KieContainer kieContainer;
	
	public RuleExecutionService(KieContainer kieContainer) {
		this.kieContainer = kieContainer;
	}

	public ValidationResult validate(ValidationContext context) {

	    KieSession kieSession = kieContainer.newKieSession();

	    try {

	        if (context.getInvoice() != null) {
	            kieSession.insert(
	                    context.getInvoice());
	        }

	        if (context.getPo() != null) {
	            kieSession.insert(
	                    context.getPo());
	        }

	        if (context.getContract() != null) {
	            kieSession.insert(
	                    context.getContract());
	        }

	        if (context.getVendor() != null) {
	            kieSession.insert(
	                    context.getVendor());
	        }
	        
	        kieSession.setGlobal("validationContext", context);

	        kieSession.fireAllRules();

	        return context.getValidationResult();

	    } finally {

	        kieSession.dispose();
	    }
	}
}
