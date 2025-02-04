package dev.server;

import dev.server.Main;
import dev.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest
{
    @AfterEach
    void tearDown()
    {
        Server.stopServer();
    }

    @Test
    void mainTest() throws ReflectiveOperationException, SQLException, IOException
    {
//        String[] args = new String[]{};
//        Main.main(args);
//
//        assertNotNull(Server.getAppContext(), "AppContext shouldn't be null");
    }
}
