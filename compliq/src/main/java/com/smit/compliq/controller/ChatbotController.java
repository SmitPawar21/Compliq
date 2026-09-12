package com.smit.compliq.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smit.compliq.dto.chatbot.ChatRequestDTO;
import com.smit.compliq.dto.chatbot.ChatResponseDTO;
import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.User;
import com.smit.compliq.repository.ChatMessageRepository;
import com.smit.compliq.repository.ChatSessionRepository;
import com.smit.compliq.repository.UserRepository;
import com.smit.compliq.service.ChatbotAgentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotAgentService chatbotAgentService;
    private final UserRepository userRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    /**
     * Main chat endpoint — processes a user message through the agentic pipeline.
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@AuthenticationPrincipal UserDetails userDetails,
                                   @Valid @RequestBody ChatRequestDTO request) {
        try {
            User user = resolveUser(userDetails);
            ChatResponseDTO response = chatbotAgentService.processMessage(user, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of(
                    "error", "Failed to process chat message",
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
                ));
        }
    }

    /**
     * List all chat sessions for the authenticated user.
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getSessions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = resolveUser(userDetails);
            List<ChatSession> sessions = sessionRepository.findByUserOrderByUpdatedAtDesc(user);

            var response = sessions.stream().map(s -> java.util.Map.of(
                "sessionId", s.getSessionId(),
                "title", s.getTitle() != null ? s.getTitle() : "Untitled",
                "createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : "",
                "updatedAt", s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : ""
            )).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all messages for a specific session (owned by the authenticated user).
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> getMessages(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable long sessionId) {
        try {
            User user = resolveUser(userDetails);

            ChatSession session = sessionRepository.findBySessionIdAndUser(sessionId, user)
                .orElse(null);

            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "Session not found or access denied"));
            }

            List<ChatMessage> messages = messageRepository.findByChatSessionOrderByCreatedAtAsc(session);

            var response = messages.stream().map(m -> java.util.Map.of(
                "messageId", m.getMessageId(),
                "role", m.getRole().name(),
                "content", m.getContent(),
                "createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""
            )).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Confirm a pending approval.
     */
    @PostMapping("/approve/{approvalToken}")
    public ResponseEntity<?> approve(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable String approvalToken,
                                      @RequestBody(required = false) ChatRequestDTO request) {
        try {
            User user = resolveUser(userDetails);

            // Create a request with the approval token
            ChatRequestDTO approvalRequest = new ChatRequestDTO();
            approvalRequest.setApprovalToken(approvalToken);
            approvalRequest.setMessage("I approve this action.");
            if (request != null && request.getSessionId() != null) {
                approvalRequest.setSessionId(request.getSessionId());
            }

            ChatResponseDTO response = chatbotAgentService.processMessage(user, approvalRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resolve the full User entity from the authenticated UserDetails.
     */
    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()));
    }
}
