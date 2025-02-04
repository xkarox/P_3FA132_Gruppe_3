package dev.server;

import com.sun.net.httpserver.HttpServer;
import dev.server.config.JerseyConfig;
import dev.server.controller.CustomerController;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Server {
    private static HttpServer serverInstance;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);


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

