package com.wifi.access.proxy;

import com.wifi.access.dto.AccessCheckResult;
import com.wifi.access.service.AccessController;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class ProxyHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final AccessController accessController;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {
        // Получаем MAC адрес из источника соединения
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = remoteAddress.getAddress().getHostAddress();

        // Пытаемся получить MAC адрес из IP
        // В реальной ситуации нужно использовать ARP таблицу или другой способ
        String macAddress = extractMacAddress(clientIp);

        if (macAddress == null) {
            // Если MAC не найден, перенаправляем на captive portal
            sendCaptivePortalResponse(ctx);
            return;
        }

        String requestedUrl = request.uri();
        log.info("Proxy received request from {} (MAC: {}) for {}", clientIp, macAddress, requestedUrl);

        // Проверяем доступ
        AccessCheckResult result = accessController.checkAccess(macAddress, clientIp, requestedUrl);

        if (result.getAllowed()) {
            // Доступ разрешен - пропускаем запрос дальше
            log.debug("Access allowed for {}", macAddress);
            ctx.fireChannelRead(request);
        } else {
            // Доступ запрещен - показываем страницу с уведомлением
            sendBlockedResponse(ctx, result);
        }
    }

    /**
     * Отправляет HTML страницу блокировки доступа
     */
    private void sendBlockedResponse(ChannelHandlerContext ctx, AccessCheckResult result) {
        String html = generateBlockedHtml(result);
        ByteBuf buf = Unpooled.copiedBuffer(html, StandardCharsets.UTF_8);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                buf
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }

    /**
     * Отправляет captive portal
     */
    private void sendCaptivePortalResponse(ChannelHandlerContext ctx) {
        String html = generateCaptivePortalHtml();
        ByteBuf buf = Unpooled.copiedBuffer(html, StandardCharsets.UTF_8);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                buf
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        ctx.writeAndFlush(response).addListener(future -> ctx.close());
    }

    /**
     * Генерирует HTML для captive portal
     */
    private String generateCaptivePortalHtml() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Wi-Fi Авторизация</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial; text-align: center; padding: 50px; }\n" +
                "        .container { max-width: 400px; margin: 0 auto; border: 1px solid #ccc; padding: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Требуется авторизация</h1>\n" +
                "        <p>Пожалуйста, введите ваш VK ID для получения доступа в интернет</p>\n" +
                "        <form method=\"POST\" action=\"/api/auth/link-device\">\n" +
                "            <input type=\"text\" name=\"vkUserId\" placeholder=\"VK ID\" required>\n" +
                "            <button type=\"submit\">Авторизоваться</button>\n" +
                "        </form>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Генерирует HTML для блокировки доступа
     */
    private String generateBlockedHtml(AccessCheckResult result) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Доступ заблокирован</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial; text-align: center; padding: 50px; }\n" +
                "        .container { max-width: 500px; margin: 0 auto; border: 2px solid red; padding: 20px; }\n" +
                "        .warning { color: red; font-weight: bold; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1 class=\"warning\">⚠ Доступ в интернет ограничен</h1>\n" +
                "        <p>Для получения полного доступа в интернет, необходимо подписаться на нашу группу ВКонтакте.</p>\n" +
                "        <p>Вы получите уведомление в личные сообщения группы с инструкциями по активации.</p>\n" +
                "        <hr>\n" +
                "        <p><a href=\"https://vk.com\" target=\"_blank\">Перейти в ВКонтакте</a></p>\n" +
                "        <p><small>Ваш MAC: " + result.getMacAddress() + "</small></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Извлекает MAC адрес по IP из ARP таблицы системы
     */
    private String extractMacAddress(String ipAddress) {
        try {
            log.debug("Extracting MAC for IP: {}", ipAddress);

            // Попытаемся получить MAC адрес из ARP таблицы
            String osName = System.getProperty("os.name").toLowerCase();
            String command;

            if (osName.contains("win")) {
                // Для Windows
                command = "arp -a " + ipAddress;
            } else {
                // Для Linux/Mac
                command = "arp -an | grep " + ipAddress;
            }

            Process process = Runtime.getRuntime().exec(command);
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                // Ищем MAC адрес в формате XX:XX:XX:XX:XX:XX или XX-XX-XX-XX-XX-XX
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"
                );
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String mac = matcher.group().toUpperCase();
                    log.debug("Found MAC for IP {}: {}", ipAddress, mac);
                    return mac;
                }
            }
            reader.close();
            process.waitFor();

        } catch (Exception e) {
            log.debug("Error extracting MAC address", e);
        }

        return null;
    }
}

