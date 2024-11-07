package server;
import ace.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class Main {
	public static void main(String[] args) throws IOException, SQLException, ReflectiveOperationException
    {
		Server.startServer("skdmflmsdl;fms");
	}
}

