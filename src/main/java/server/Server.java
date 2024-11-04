package server;

import org.springframework.boot.SpringApplication;

public class Server
{
//    url probably refers to the database url -> ask teacher
    static void startServer(String url){
        SpringApplication.run(Main.class);
    }

    static void stopServer(){}
}
