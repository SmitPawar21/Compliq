package com.smit.compliq.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.entity.ApprovalAuditLog;
import com.smit.compliq.entity.ChatbotTrace;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.ApprovalAuditLogRepository;
import com.smit.compliq.repository.ChatbotTraceRepository;
import com.smit.compliq.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ChatbotTraceRepository traceRepository;
    private final ApprovalAuditLogRepository approvalAuditLogRepository;
    private final UserRepository userRepository;

    @GetMapping("/traces")
    public ResponseEntity<?> getUserTraces(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = resolveUser(userDetails);
            List<ChatbotTrace> traces = traceRepository.findByUserOrderByCreatedAtDesc(user);
            return ResponseEntity.ok(traces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/traces/{requestId}")
    public ResponseEntity<?> getTraceDetails(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable String requestId) {
        try {
            User user = resolveUser(userDetails);
            ChatbotTrace trace = traceRepository.findByRequestId(requestId);
            
            if (trace == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Trace not found"));
            }
            if (trace.getUser().getId() != user.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
            }
            
            return ResponseEntity.ok(trace);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getUserMetrics(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = resolveUser(userDetails);
            
            Double totalCost = traceRepository.sumTotalCostByUser(user);
            Long totalTokens = traceRepository.sumTotalTokensByUser(user);
            Double avgLatency = traceRepository.averageLatencyByUser(user);
            long totalTraces = traceRepository.findByUserOrderByCreatedAtDesc(user).size();

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalCost", totalCost != null ? totalCost : 0.0);
            metrics.put("totalTokens", totalTokens != null ? totalTokens : 0L);
            metrics.put("avgLatency", avgLatency != null ? avgLatency : 0.0);
            metrics.put("totalTraces", totalTraces);

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/approvals")
    public ResponseEntity<?> getUserApprovals(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = resolveUser(userDetails);
            List<ApprovalAuditLog> approvals = approvalAuditLogRepository.findByUserOrderByCreatedAtDesc(user);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
