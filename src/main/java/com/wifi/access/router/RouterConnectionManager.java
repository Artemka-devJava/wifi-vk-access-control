package com.wifi.access.router;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wifi.access.config.RouterConfig;
import com.wifi.access.exception.RouterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
@Slf4j
@RequiredArgsConstructor
public class RouterConnectionManager {

    private final RouterConfig routerConfig;
    private Session session;

    /**
     * Подключается к роутеру по SSH
     */
    public void connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(
                    routerConfig.getUsername(),
                    routerConfig.getHost(),
                    routerConfig.getSshPort()
            );
            session.setPassword(routerConfig.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password");
            session.connect(10000);
            log.info("Connected to router {}", routerConfig.getHost());
        } catch (JSchException e) {
            log.error("Failed to connect to router", e);
            throw new RouterException("Failed to connect to router", e);
        }
    }

    /**
     * Отключается от роутера
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("Disconnected from router");
        }
    }

    /**
     * Выполняет команду на роутере через SSH
     */
    public String executeCommand(String command) {
        if (session == null || !session.isConnected()) {
            connect();
        }

        try {
            Channel channel = session.openChannel("exec");
            ChannelExec execChannel = (ChannelExec) channel;
            execChannel.setCommand(command);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(execChannel.getInputStream())
            );
            channel.connect();

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitStatus = execChannel.getExitStatus();
            channel.disconnect();

            if (exitStatus != 0) {
                log.warn("Command '{}' returned exit status {}", command, exitStatus);
            }

            log.debug("Command executed: {}", command);
            return output.toString();
        } catch (JSchException | IOException e) {
            log.error("Error executing command on router", e);
            throw new RouterException("Error executing command", e);
        }
    }

    /**
     * Проверяет подключение к роутеру
     */
    public boolean isConnected() {
        return session != null && session.isConnected();
    }
}

