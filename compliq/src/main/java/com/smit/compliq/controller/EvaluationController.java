package com.smit.compliq.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.entity.ChatbotEvaluation;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.UserRepository;
import com.smit.compliq.service.ChatbotEvaluationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final ChatbotEvaluationService evaluationService;
    private final UserRepository userRepository;

    /**
     * Trigger evaluation for a specific trace.
     */
    @PostMapping("/trace/{traceId}")
    public ResponseEntity<?> evaluateTrace(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable long traceId) {
        try {
            User user = resolveUser(userDetails);
            ChatbotEvaluation evaluation = evaluationService.evaluateTrace(traceId, user);
            return ResponseEntity.ok(evaluation);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to evaluate trace", "message", e.getMessage()));
        }
    }

    /**
     * Get all evaluations for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<?> getUserEvaluations(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = resolveUser(userDetails);
            List<ChatbotEvaluation> evaluations = evaluationService.getUserEvaluations(user);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()));
    }
}
