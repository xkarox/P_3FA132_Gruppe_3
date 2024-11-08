package dev.hv.services.logService;

import dev.hv.services.logService.LogLevel;
import dev.hv.services.logService.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static dev.hv.services.logService.LogService.log;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogServiceTest
{
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams()
    {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams()
    {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testLog()
    {
        LocalDateTime ldt = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append(ldt.getDayOfMonth()).append("/")
                .append(ldt.getMonthValue()).append("/")
                .append(ldt.getYear())
                .append(" ").append(ldt.getHour()).append(":").append(ldt.getMinute()).append(":").append(ldt.getSecond());

//        Test for String as type
        String expectedLog = new StringBuilder(sb.toString())
                .append(" (LogServiceTest) \u001B[34mINFO\u001B[0m: This is a INFO message\n").toString();
        log("LogServiceTest", LogLevel.INFO, "This is a INFO message");
        assertEquals(filterAnsi(expectedLog), filterAnsi(outContent.toString()));
//        Test for ClassType as Type
        outContent.reset();
        expectedLog = new StringBuilder(sb.toString())
                .append(" (ace.services.logService.LogServiceTest) \u001B[34mINFO\u001B[0m: This is a INFO message\n").toString();
        log(this.getClass(), LogLevel.INFO, "This is a INFO message");
        assertEquals(filterAnsi(expectedLog), filterAnsi(outContent.toString()));
    }

    @Test
        // Just for coverage in jacoco report
    void staticTest()
    {
        LogService logService = new LogService();
    }

    private String filterAnsi(String input)
    {
        if (input == null)
        {
            return null;
        }
        input = input.replaceAll("[\\r\\n]+", "");
        return input.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
