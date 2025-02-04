package dev.server;

import com.sun.net.httpserver.HttpServer;
import dev.server.config.JerseyConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Server {
    private static HttpServer serverInstance;

    public static void startServer(String url) {
        final String pack = "dev.server.controller";
        System.out.println("Start server");
        System.out.println(url);
        final ResourceConfig rc = new JerseyConfig();
        serverInstance = JdkHttpServerFactory.createHttpServer(
                URI.create(url), rc);
        System.out.println("Ready for Requests....");
    }

    public static void stopServer() {
        if (serverInstance != null) {
            serverInstance.stop(0);
        }
    }
}

