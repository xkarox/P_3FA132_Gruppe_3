package ace.services.logService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogLevelTest
{
    @Test
    void infoTest()
    {
        assertEquals("\u001B[34mINFO\u001B[0m", LogLevel.INFO.toString(), "Should return a blue INFO string");
    }

    @Test
    void debugTest()
    {
        assertEquals("\u001B[36mDEBUG\u001B[0m", LogLevel.DEBUG.toString(), "Should return a cyan DEBUG string");
    }

    @Test
    void warningTest()
    {
        assertEquals("\u001B[33mWARNING\u001B[0m", LogLevel.WARNING.toString(), "Should return a yellow WARNING string");
    }

    @Test
    void errorTest()
    {
        assertEquals("\u001B[31mERROR\u001B[0m", LogLevel.ERROR.toString(), "Should return a red ERROR string");
    }
}
