package com.wifi.access.vk;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wifi.access.config.VkApiConfig;
import com.wifi.access.exception.VkApiException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VkApiClient {

    private final VkApiConfig config;
    private final OkHttpClient httpClient;

    public VkApiClient(VkApiConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    // ...existing code...

    /**
     * Проверяет, подписан ли пользователь на группу
     * @param userId ID пользователя ВКонтакте
     * @return true если подписан, false если нет
     */
    public boolean isMember(Long userId) {
        try {
            String url = String.format(
                    "%s/groups.isMember?group_id=%d&user_id=%d&access_token=%s&v=%s",
                    config.getBaseUrl(),
                    config.getGroupId(),
                    userId,
                    config.getAccessToken(),
                    config.getApiVersion()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new VkApiException("VK API returned status " + response.code());
                }

                String responseBody = null;
                if (response.body() != null) {
                    responseBody = response.body().string();
                }

                if (responseBody == null) {
                    throw new VkApiException("Empty response from VK API");
                }

                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonObject.has("error")) {
                    JsonObject error = jsonObject.getAsJsonObject("error");
                    String errorMsg = error.get("error_msg").getAsString();
                    log.error("VK API error: {}", errorMsg);
                    throw new VkApiException("VK API error: " + errorMsg);
                }

                return jsonObject.get("response").getAsInt() == 1;
            }
        } catch (IOException e) {
            log.error("Error calling VK API isMember", e);
            throw new VkApiException("Error calling VK API", e);
        }
    }

    /**
     * Отправляет личное сообщение пользователю от группы
     * @param userId ID пользователя ВКонтакте
     * @param message Текст сообщения
     * @return ID отправленного сообщения
     */
    public Long sendMessage(Long userId, String message) {
        try {
            String url = String.format(
                    "%s/messages.send?user_id=%d&message=%s&access_token=%s&v=%s&random_id=%d",
                    config.getBaseUrl(),
                    userId,
                    java.net.URLEncoder.encode(message, StandardCharsets.UTF_8),
                    config.getAccessToken(),
                    config.getApiVersion(),
                    System.nanoTime()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new VkApiException("VK API returned status " + response.code());
                }

                String responseBody = null;
                if (response.body() != null) {
                    responseBody = response.body().string();
                }

                if (responseBody == null) {
                    throw new VkApiException("Empty response from VK API");
                }

                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

                if (jsonObject.has("error")) {
                    JsonObject error = jsonObject.getAsJsonObject("error");
                    String errorMsg = error.get("error_msg").getAsString();
                    log.error("VK API error: {}", errorMsg);
                    throw new VkApiException("VK API error: " + errorMsg);
                }

                long messageId = jsonObject.get("response").getAsLong();
                log.info("Message sent to user {} with ID {}", userId, messageId);
                return messageId;
            }
        } catch (IOException e) {
            log.error("Error sending message via VK API", e);
            throw new VkApiException("Error sending message", e);
        }
    }
}

