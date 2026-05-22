package com.cinemago.models;

import java.util.List;

public class LlamaChatRequest {
    private String model;
    private List<Message> messages;
    private boolean stream;

    public LlamaChatRequest(String model, List<Message> messages, boolean stream) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}