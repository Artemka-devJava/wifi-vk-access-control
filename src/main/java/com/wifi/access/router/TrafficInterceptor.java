package com.wifi.access.router;

import com.wifi.access.config.ProxyConfig;
import com.wifi.access.config.RouterConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TrafficInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TrafficInterceptor.class);
    private final RouterConnectionManager routerConnectionManager;
    private final RouterConfig routerConfig;
    private final ProxyConfig proxyConfig;

    /**
     * Устанавливает правила iptables для перехвата трафика неподписанных пользователей
     * Трафик перенаправляется на proxy сервер
     */
    public void setupTrafficInterception() {
        try {
            // Очищаем старые правила
            clearTrafficInterception();

            // Создаем новую цепь iptables
            String createChain = String.format(
                    "iptables -t nat -N WIFI_CONTROL_%s",
                    routerConfig.getWifiInterface().toUpperCase()
            );
            routerConnectionManager.executeCommand(createChain);

            // Редирект HTTP трафика на proxy
            String redirectHttp = String.format(
                    "iptables -t nat -A WIFI_CONTROL_%s -p tcp --dport 80 -j REDIRECT --to-port %d",
                    routerConfig.getWifiInterface().toUpperCase(),
                    proxyConfig.getPort()
            );
            routerConnectionManager.executeCommand(redirectHttp);

            // Редирект HTTPS трафика на proxy
            String redirectHttps = String.format(
                    "iptables -t nat -A WIFI_CONTROL_%s -p tcp --dport 443 -j REDIRECT --to-port %d",
                    routerConfig.getWifiInterface().toUpperCase(),
                    proxyConfig.getPort()
            );
            routerConnectionManager.executeCommand(redirectHttps);

            // Применяем правила к входящему трафику с Wi-Fi интерфейса
            String applyRules = String.format(
                    "iptables -t nat -A PREROUTING -i %s -j WIFI_CONTROL_%s",
                    routerConfig.getWifiInterface(),
                    routerConfig.getWifiInterface().toUpperCase()
            );
            routerConnectionManager.executeCommand(applyRules);

            // Сохраняем правила iptables
            routerConnectionManager.executeCommand("iptables-save > /etc/iptables.rules");

            log.info("Traffic interception rules installed on interface {}",
                    routerConfig.getWifiInterface());
        } catch (Exception e) {
            log.error("Failed to setup traffic interception", e);
        }
    }

    /**
     * Удаляет правила iptables для перехвата трафика
     */
    public void clearTrafficInterception() {
        try {
            String flushChain = String.format(
                    "iptables -t nat -F WIFI_CONTROL_%s 2>/dev/null || true",
                    routerConfig.getWifiInterface().toUpperCase()
            );
            routerConnectionManager.executeCommand(flushChain);

            String deleteChain = String.format(
                    "iptables -t nat -X WIFI_CONTROL_%s 2>/dev/null || true",
                    routerConfig.getWifiInterface().toUpperCase()
            );
            routerConnectionManager.executeCommand(deleteChain);

            String deletePrerouting = String.format(
                    "iptables -t nat -D PREROUTING -i %s -j WIFI_CONTROL_%s 2>/dev/null || true",
                    routerConfig.getWifiInterface(),
                    routerConfig.getWifiInterface().toUpperCase()
            );
            routerConnectionManager.executeCommand(deletePrerouting);

            log.info("Traffic interception rules removed");
        } catch (Exception e) {
            log.error("Failed to clear traffic interception", e);
        }
    }

    /**
     * Получает список подключенных устройств из arp таблицы роутера
     */
    public String getConnectedDevices() {
        try {
            return routerConnectionManager.executeCommand("arp -a");
        } catch (Exception e) {
            log.error("Failed to get connected devices", e);
            return "";
        }
    }

    /**
     * Парсит MAC адреса из вывода arp команды
     */
    public String extractMacAddress(String arpOutput) {
        Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");
        Matcher matcher = pattern.matcher(arpOutput);
        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }
        return null;
    }

    /**
     * Получает информацию о подключенных клиентах через dnsmasq
     */
    public String getConnectedClients() {
        try {
            return routerConnectionManager.executeCommand("cat /tmp/dhcp.leases");
        } catch (Exception e) {
            log.error("Failed to get DHCP leases", e);
            return "";
        }
    }
}

