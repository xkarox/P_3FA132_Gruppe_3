package dev.server;

import dev.server.Main;
import dev.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest
{
    @AfterEach
    void tearDown()
    {
        try
        {
            Server.stopServer();
        } catch (Exception ignored) {    }
    }

    @Test
    void mainTest()
    {
        String[] args = new String[]{};
        Main.main(args);

        assertDoesNotThrow(Server::stopServer);
    }

    @Test
    void stopServerNUll() throws NoSuchFieldException, IllegalAccessException
    {
        java.lang.reflect.Field serverInstanceField = Server.class.getDeclaredField("serverInstance");
        serverInstanceField.setAccessible(true);
        serverInstanceField.set(null, null);

        Server.stopServer();
    }

    // Just for coverage
    @Test
    void staticServerConstructorTest()
    {
        Server server = new Server();
    }

    // Just for coverage
    @Test
    void staticMainConstructorTest()
    {
        Main main = new Main();
    }
}
