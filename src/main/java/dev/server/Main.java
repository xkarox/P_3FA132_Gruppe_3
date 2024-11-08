package dev.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
	public static void main(String[] args)
    {
		Server.startServer("{{ DatabaseUrl }}");
	}
}
