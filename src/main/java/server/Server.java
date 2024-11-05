package server;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class Server
{
    private static ApplicationContext _appContext;

//    url probably refers to the database url -> ask teacher
    public static void startServer(String url){
        _appContext = SpringApplication.run(Main.class);
    }

    public static void stopServer()
    {
        SpringApplication.exit(_appContext, () -> 0);
    }

}
