package com.cinemago.models;

public class LlamaChatResponse {
    private Message message;
    private boolean done;

    public Message getMessage() {
        return message;
    }

    public boolean isDone() {
        return done;
    }

    public static class Message {
        private String role;
        private String content;

        public String getContent() {
            return content;
        }

        public String getRole() {
            return role;
        }
    }
}