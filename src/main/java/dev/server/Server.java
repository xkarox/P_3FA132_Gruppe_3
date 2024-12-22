package dev.server;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Server {
    private static HttpServer serverInstance;

    public static void startServer(String url) {
        final String pack = "dev.server.controller";
        String _url = url;
        System.out.println("Start server");
        System.out.println(_url);
        final ResourceConfig rc = new ResourceConfig().packages(pack);
        serverInstance = JdkHttpServerFactory.createHttpServer(
                URI.create(_url), rc);
        System.out.println("Ready for Requests....");
    }

    public static void stopServer() {
        if (serverInstance != null) {
            serverInstance.stop(0);
        }
    }
}

