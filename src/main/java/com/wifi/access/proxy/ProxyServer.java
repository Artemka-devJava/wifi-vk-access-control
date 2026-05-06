package com.wifi.access.proxy;

import com.wifi.access.config.ProxyConfig;
import com.wifi.access.service.AccessController;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProxyServer {

    private final ProxyConfig proxyConfig;
    private final AccessController accessController;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new HttpRequestDecoder());
                            pipeline.addLast("encoder", new HttpResponseEncoder());
                            pipeline.addLast("handler", new ProxyHandler(accessController));
                        }
                    });

            serverChannel = bootstrap.bind(proxyConfig.getListenAddress(), proxyConfig.getPort())
                    .sync()
                    .channel();

            log.info("Proxy server started on {}:{}", proxyConfig.getListenAddress(), proxyConfig.getPort());
        } catch (Exception e) {
            log.error("Failed to start proxy server", e);
            throw new RuntimeException("Failed to start proxy server", e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }
            log.info("Proxy server stopped");
        } catch (Exception e) {
            log.error("Error stopping proxy server", e);
        }
    }
}

