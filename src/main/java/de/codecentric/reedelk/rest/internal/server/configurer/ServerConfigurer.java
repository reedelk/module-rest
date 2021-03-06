package de.codecentric.reedelk.rest.internal.server.configurer;

import de.codecentric.reedelk.rest.component.RESTListenerConfiguration;
import de.codecentric.reedelk.rest.internal.commons.Defaults;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.Connection;
import reactor.netty.http.server.HttpServer;

import java.util.function.Consumer;

import static io.netty.channel.ChannelOption.*;

public class ServerConfigurer {

    public static void configure(ServerBootstrap bootstrap, RESTListenerConfiguration configuration) {
        setChannelOption(bootstrap, SO_BACKLOG, configuration.getSocketBacklog());
        setChannelOption(bootstrap, CONNECT_TIMEOUT_MILLIS, configuration.getConnectionTimeoutMillis());
        setChannelChildOption(bootstrap, SO_KEEPALIVE, configuration.getKeepAlive());
    }

    public static Consumer<Connection> onConnection(RESTListenerConfiguration configuration) {
        return connection -> {
            if (configuration.getReadTimeoutMillis() != null) {
                connection.addHandlerLast("readTimeout", new ReadTimeoutHandler(configuration.getReadTimeoutMillis()));
            }
        };
    }

    public static HttpServer configure(HttpServer server, RESTListenerConfiguration configuration) {
        server = server.host(Defaults.RestListener.host(configuration.getHost()));
        server = server.port(Defaults.RestListener.port(configuration.getPort(), configuration.getProtocol()));
        server = server.compress(Defaults.RestListener.compress(configuration.getCompress()));
        return server.httpRequestDecoder(decoder -> {
            decoder.validateHeaders(Defaults.RestListener.validateHeaders());
            decoder.maxChunkSize(Defaults.RestListener.maxChunkSize(configuration.getMaxChunkSize()));
            decoder.maxHeaderSize(Defaults.RestListener.maxHeaderSize(configuration.getMaxLengthOfAllHeaders()));
            return decoder;
        });
    }

    private static <T> void setChannelOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.option(channelOption, value);
        }
    }

    private static <T> void setChannelChildOption(ServerBootstrap serverBootstrap, ChannelOption<T> channelOption, T value) {
        if (value != null) {
            serverBootstrap.childOption(channelOption, value);
        }
    }
}
