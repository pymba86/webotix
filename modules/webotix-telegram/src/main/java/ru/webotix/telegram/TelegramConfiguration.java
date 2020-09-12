package ru.webotix.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelegramConfiguration {

    private String botToken;

    private String chatId;

    @JsonProperty
    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    @JsonProperty
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
