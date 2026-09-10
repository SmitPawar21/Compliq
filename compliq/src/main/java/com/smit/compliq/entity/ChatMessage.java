package com.smit.compliq.entity;

import java.util.Date;

import com.smit.compliq.enums.MessageRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="chat_message")
public class ChatMessage {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long messageId;

	@ManyToOne
	@JoinColumn(name = "session_id", nullable = false)
	private ChatSession chatSession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageRole role;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(columnDefinition = "TEXT")
	private String metadata; // JSON representation for traces/references

	@Column
	private Date createdAt;

	public ChatMessage() {}

	public ChatMessage(ChatSession chatSession, MessageRole role, String content, String metadata, Date createdAt) {
		this.chatSession = chatSession;
		this.role = role;
		this.content = content;
		this.metadata = metadata;
		this.createdAt = createdAt;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public ChatSession getChatSession() {
		return chatSession;
	}

	public void setChatSession(ChatSession chatSession) {
		this.chatSession = chatSession;
	}

	public MessageRole getRole() {
		return role;
	}

	public void setRole(MessageRole role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
