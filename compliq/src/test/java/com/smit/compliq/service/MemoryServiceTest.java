package com.smit.compliq.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.User;
import com.smit.compliq.entity.UserMemory;
import com.smit.compliq.enums.MemoryCategory;
import com.smit.compliq.repository.ChatMessageRepository;
import com.smit.compliq.repository.UserMemoryRepository;
import com.smit.compliq.service.tools.MemoryTool;

@ExtendWith(MockitoExtension.class)
class MemoryServiceTest {

    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private UserMemoryRepository userMemoryRepository;

    @InjectMocks
    private MemoryService memoryService;

    private User testUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        testSession = new ChatSession();
        testSession.setSessionId(1L);
        testSession.setUser(testUser);
    }

    @Nested
    @DisplayName("Episodic Memory Tests")
    class EpisodicMemoryTests {

        @Test
        @DisplayName("Should return last 10 messages from session")
        void shouldReturnLast10Messages() {
            List<ChatMessage> messages = new java.util.ArrayList<>();
            for (int i = 0; i < 15; i++) {
                ChatMessage msg = new ChatMessage();
                msg.setContent("Message " + i);
                messages.add(msg);
            }

            when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(testSession))
                .thenReturn(messages);

            List<ChatMessage> result = memoryService.getEpisodicMemory(testSession);

            assertEquals(10, result.size());
            assertEquals("Message 5", result.get(0).getContent());
        }

        @Test
        @DisplayName("Should handle null session gracefully")
        void shouldHandleNullSession() {
            List<ChatMessage> result = memoryService.getEpisodicMemory(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle empty message list")
        void shouldHandleEmptyMessages() {
            when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(testSession))
                .thenReturn(List.of());

            List<ChatMessage> result = memoryService.getEpisodicMemory(testSession);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Long-Term Memory Tests")
    class LongTermMemoryTests {

        @Test
        @DisplayName("Should search by keyword when query provided")
        void shouldSearchByKeyword() {
            UserMemory mem = new UserMemory(testUser, "role", "compliance officer", MemoryCategory.FACT);
            when(userMemoryRepository.searchByKeyword(testUser, "role"))
                .thenReturn(List.of(mem));

            List<UserMemory> result = memoryService.getLongTermMemories(testUser, "role");

            assertEquals(1, result.size());
            assertEquals("role", result.get(0).getMemoryKey());
        }

        @Test
        @DisplayName("Should return all memories when query is blank")
        void shouldReturnAllWhenNoQuery() {
            UserMemory mem1 = new UserMemory(testUser, "role", "officer", MemoryCategory.FACT);
            UserMemory mem2 = new UserMemory(testUser, "pref", "dark mode", MemoryCategory.PREFERENCE);
            when(userMemoryRepository.findByUserOrderByUpdatedAtDesc(testUser))
                .thenReturn(List.of(mem1, mem2));

            List<UserMemory> result = memoryService.getLongTermMemories(testUser, "");

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should limit to top 5 memories")
        void shouldLimitToTop5() {
            List<UserMemory> memories = new java.util.ArrayList<>();
            for (int i = 0; i < 10; i++) {
                memories.add(new UserMemory(testUser, "key" + i, "value" + i, MemoryCategory.FACT));
            }
            when(userMemoryRepository.findByUserOrderByUpdatedAtDesc(testUser))
                .thenReturn(memories);

            List<UserMemory> result = memoryService.getLongTermMemories(testUser, null);

            assertEquals(5, result.size());
        }
    }

    @Nested
    @DisplayName("Memory Tool - Creation Policy Tests")
    class MemoryCreationPolicyTests {

        @Mock private UserMemoryRepository memToolRepo;
        @InjectMocks private MemoryTool memoryTool;

        @Test
        @DisplayName("Should reject sensitive data (credit card)")
        void shouldRejectCreditCard() {
            String result = memoryTool.storeMemory(testUser, "card", "4111-1111-1111-1111", "FACT");
            assertTrue(result.contains("sensitive data"));
        }

        @Test
        @DisplayName("Should reject sensitive data (SSN)")
        void shouldRejectSSN() {
            String result = memoryTool.storeMemory(testUser, "ssn", "123-45-6789", "FACT");
            assertTrue(result.contains("sensitive data"));
        }

        @Test
        @DisplayName("Should reject sensitive data (password)")
        void shouldRejectPassword() {
            String result = memoryTool.storeMemory(testUser, "creds", "password: mySecret123", "FACT");
            assertTrue(result.contains("sensitive data"));
        }

        @Test
        @DisplayName("Should reject empty key")
        void shouldRejectEmptyKey() {
            String result = memoryTool.storeMemory(testUser, "", "some value", "FACT");
            assertTrue(result.contains("cannot be empty"));
        }

        @Test
        @DisplayName("Should reject empty value")
        void shouldRejectEmptyValue() {
            String result = memoryTool.storeMemory(testUser, "key", "", "FACT");
            assertTrue(result.contains("cannot be empty"));
        }

        @Test
        @DisplayName("Should update existing memory with same key")
        void shouldUpdateExistingMemory() {
            UserMemory existing = new UserMemory(testUser, "role", "old role", MemoryCategory.FACT);
            when(memToolRepo.findByUserAndMemoryKey(testUser, "role")).thenReturn(Optional.of(existing));
            when(memToolRepo.save(any())).thenReturn(existing);

            String result = memoryTool.storeMemory(testUser, "role", "new role", "FACT");

            assertTrue(result.contains("updated"));
            verify(memToolRepo).save(any());
        }

        @Test
        @DisplayName("Should default to FACT category on invalid input")
        void shouldDefaultToFactCategory() {
            when(memToolRepo.findByUserAndMemoryKey(any(), anyString())).thenReturn(Optional.empty());
            when(memToolRepo.countByUser(any())).thenReturn(0L);
            when(memToolRepo.save(any())).thenReturn(new UserMemory());

            String result = memoryTool.storeMemory(testUser, "key", "value", "INVALID_CATEGORY");

            assertTrue(result.contains("stored"));
        }
    }
}
