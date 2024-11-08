package server;
import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.database.services.CustomerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {

	public static void main(String[] args) throws IOException
    {

		Server.startServer("{{ DatabaseUrl }}");
	}
}

