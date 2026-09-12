package com.smit.compliq.service.tools;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.smit.compliq.dto.ComplianceReportDTO;
import com.smit.compliq.dto.ComplianceReportRequest;
import com.smit.compliq.entity.Document;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.DocumentRepository;
import com.smit.compliq.service.ComplianceReportService;

import lombok.RequiredArgsConstructor;

/**
 * Tool: RUN_COMPLIANCE_REPORT
 * Triggers the existing compliance report workflow.
 * This is a HIGH-IMPACT action that requires human approval.
 * Authorization: verifies all three documents belong to the authenticated user before execution.
 */
@Service
@RequiredArgsConstructor
public class ComplianceReportTool {

    private static final Logger log = LoggerFactory.getLogger(ComplianceReportTool.class);

    private final DocumentRepository documentRepository;
    private final ComplianceReportService complianceReportService;

    /**
     * Result of a compliance report tool invocation.
     */
    public static class ReportToolResult {
        private final boolean requiresApproval;
        private final String approvalToken;
        private final String description;
        private final ComplianceReportDTO report;

        private ReportToolResult(boolean requiresApproval, String approvalToken,
                                  String description, ComplianceReportDTO report) {
            this.requiresApproval = requiresApproval;
            this.approvalToken = approvalToken;
            this.description = description;
            this.report = report;
        }

        public static ReportToolResult pendingApproval(String description) {
            return new ReportToolResult(true, UUID.randomUUID().toString(), description, null);
        }

        public static ReportToolResult completed(ComplianceReportDTO report) {
            return new ReportToolResult(false, null, "Report generated successfully.", report);
        }

        public boolean isRequiresApproval() { return requiresApproval; }
        public String getApprovalToken() { return approvalToken; }
        public String getDescription() { return description; }
        public ComplianceReportDTO getReport() { return report; }
    }

    /**
     * Check document ownership and request approval to generate a compliance report.
     * Returns a pending approval result (does NOT execute the report).
     */
    public ReportToolResult requestReport(User user, long contractDocId, long invoiceDocId, long poDocId) {
        // Verify all documents belong to the user
        Optional<Document> contract = documentRepository.findByDocIdAndUploadedBy(contractDocId, user);
        Optional<Document> invoice = documentRepository.findByDocIdAndUploadedBy(invoiceDocId, user);
        Optional<Document> po = documentRepository.findByDocIdAndUploadedBy(poDocId, user);

        if (contract.isEmpty()) {
            return ReportToolResult.pendingApproval("ERROR: Contract document " + contractDocId + " not found or not owned by you.");
        }
        if (invoice.isEmpty()) {
            return ReportToolResult.pendingApproval("ERROR: Invoice document " + invoiceDocId + " not found or not owned by you.");
        }
        if (po.isEmpty()) {
            return ReportToolResult.pendingApproval("ERROR: Purchase order document " + poDocId + " not found or not owned by you.");
        }

        String description = String.format(
            "Generate a full compliance report using:\n- Contract: %s (ID: %d)\n- Invoice: %s (ID: %d)\n- Purchase Order: %s (ID: %d)\n\nThis will run the full AI analysis workflow. Do you approve?",
            contract.get().getFileName(), contractDocId,
            invoice.get().getFileName(), invoiceDocId,
            po.get().getFileName(), poDocId
        );

        return ReportToolResult.pendingApproval(description);
    }

    /**
     * Execute the compliance report after approval.
     * Authorization is re-verified before execution.
     */
    public String executeReport(User user, long contractDocId, long invoiceDocId, long poDocId) {
        // Re-verify ownership
        if (documentRepository.findByDocIdAndUploadedBy(contractDocId, user).isEmpty()
            || documentRepository.findByDocIdAndUploadedBy(invoiceDocId, user).isEmpty()
            || documentRepository.findByDocIdAndUploadedBy(poDocId, user).isEmpty()) {
            return "ERROR: One or more documents are not accessible. Report generation denied.";
        }

        try {
            ComplianceReportRequest request = new ComplianceReportRequest();
            request.setContractDocumentId(contractDocId);
            request.setInvoiceDocumentId(invoiceDocId);
            request.setPoDocumentId(poDocId);

            ComplianceReportDTO report = complianceReportService.generateReport(request);

            StringBuilder sb = new StringBuilder();
            sb.append("Compliance Report Generated Successfully!\n\n");

            if (report.getRiskAssessment() != null) {
                sb.append("Risk Level: ").append(report.getRiskAssessment().getRiskLevel()).append("\n");
                sb.append("Confidence Score: ").append(report.getRiskAssessment().getConfidenceScore()).append("\n\n");
            }

            if (report.getContractSummary() != null) {
                sb.append("Contract Purpose: ").append(report.getContractSummary().getContractPurpose()).append("\n\n");
            }

            if (report.getClauseAnalysis() != null && report.getClauseAnalysis().getMissingClauses() != null) {
                sb.append("Missing Clauses: ").append(report.getClauseAnalysis().getMissingClauses()).append("\n\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Compliance report generation failed for user {}: {}", user.getId(), e.getMessage());
            return "Failed to generate compliance report: " + e.getMessage();
        }
    }
}
