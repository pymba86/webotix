package ru.webotix.telegram;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    private final WebTarget telegramTarget;
    private final TelegramConfiguration configuration;

    @Inject
    public TelegramService(TelegramConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.telegramTarget =
                client.target("https://api.telegram.org/bot" + configuration.getBotToken());
    }

    void sendMessage(String message) {
        try {

            final Response response =
                    telegramTarget
                    .path("sendMessage")
                    .request()
                    .post(Entity.entity(
                            ImmutableMap.of("chat_id", configuration.getChatId(), "text", message),
                            MediaType.APPLICATION_JSON));

            if (response.getStatus() != 200) {
                log.error("Could not send message: {}", response.readEntity(String.class));
            }

        } catch (Exception e) {
            log.error("Could not send message: {}", e.getMessage(), e);
        }
    }
}
