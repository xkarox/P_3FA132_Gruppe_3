package server;
import ace.database.provider.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {
	public static void main(String[] args) throws IOException
    {
		ReadingService con1 = ServiceProvider.GetReadingService();
		try(ReadingService con2 = ServiceProvider.GetReadingService()){
			System.out.println("aksdlj");
			con2.add(new Reading());
		}
	}
}

