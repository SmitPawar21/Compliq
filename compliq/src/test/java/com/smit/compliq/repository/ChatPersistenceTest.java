package com.smit.compliq.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.smit.compliq.entity.ChatMessage;
import com.smit.compliq.entity.ChatSession;
import com.smit.compliq.entity.Organization;
import com.smit.compliq.entity.User;
import com.smit.compliq.enums.MessageRole;
import com.smit.compliq.enums.Role;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Disabled("Requires a running database or proper testcontainer setup")
public class ChatPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    public void testSaveAndRetrieveChatSession() {
        Organization org = new Organization();
        org.setName("Test Org");
        entityManager.persist(org);

        User user = new User("testuser", "test@example.com", "pass", Role.ADMIN, org, new Date());
        entityManager.persist(user);

        ChatSession session = new ChatSession(user, "Test Session", new Date(), new Date());
        session = chatSessionRepository.save(session);

        ChatMessage msg1 = new ChatMessage(session, MessageRole.USER, "Hello", "{}", new Date());
        ChatMessage msg2 = new ChatMessage(session, MessageRole.AI, "Hi there!", "{}", new Date());

        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);

        entityManager.flush();
        entityManager.clear();

        List<ChatSession> sessions = chatSessionRepository.findByUser(user);
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getTitle()).isEqualTo("Test Session");

        List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContent()).isEqualTo("Hello");
        assertThat(messages.get(1).getContent()).isEqualTo("Hi there!");
    }
}
