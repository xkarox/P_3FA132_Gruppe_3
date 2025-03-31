package dev.server;

import com.sun.net.httpserver.HttpServer;
import dev.server.config.JerseyConfig;
import dev.server.controller.CustomerController;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;

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

//    public static void startServerSecure(String url) {
//        try {
//            // Ensure HTTPS URL
//            url = url.replace("http://", "https://");
//            if (!url.startsWith("https://")) {
//                url = "https://" + url;
//            }
//
//            // Create SSL context with mkcert configuration
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//
//            // Load PKCS12 keystore from resources
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            InputStream keystoreStream = Server.class.getClassLoader().getResourceAsStream("localhost.p12");
//            keyStore.load(keystoreStream, "changeit".toCharArray());
//
//            // Configure KeyManager
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            kmf.init(keyStore, "changeit".toCharArray());
//
//            // TrustManager uses system CA bundle (includes mkcert)
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            tmf.init((KeyStore) null); // Use default trust store
//
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
//
//            // Create server with HTTPS
//            final ResourceConfig rc = new JerseyConfig();
//            serverInstance = JdkHttpServerFactory.createHttpServer(
//                    URI.create(url), rc, sslContext);
//
//            System.out.println("Ready for HTTPS Requests....");
//        } catch (Exception e) {
//            logger.error("Error starting HTTPS server", e);
//            throw new RuntimeException("Failed to start HTTPS server", e);
//        }
//    }


    public static void stopServer() {
        if (serverInstance != null) {
            serverInstance.stop(0);
        }
    }
}
