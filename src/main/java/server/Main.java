package server;
import ace.database.provider.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class Main {
	public static void main(String[] args) throws IOException, SQLException, ReflectiveOperationException
    {
		ReadingService con1 = ServiceProvider.Services.getReadingService();
		try(ReadingService con2 = ServiceProvider.Services.getReadingService()){
			System.out.println("aksdlj");
			con2.add(new Reading());
		}
	}
}

