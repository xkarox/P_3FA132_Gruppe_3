package ace.services.logService;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LogService
{
    public static void log(Type classType, LogLevel level, String message)
    {
        LocalDateTime ldt = LocalDateTime.now();
        String currentDateTime = getCurrentDateTimeMessage(ldt);
        String coreMessage = getCoreMessage(classType, level, message);
        System.out.println(currentDateTime + " " + coreMessage);
    }

    public static void log(Type classType, int lineNumber, LogLevel level, String message)
    {
        LocalDateTime ldt = LocalDateTime.now();
        String currentDateTime = getCurrentDateTimeMessage(ldt);
        String lineNumberMessage = getLineNumberMessage(lineNumber);
        String coreMessage = getCoreMessage(classType, level, message);
        System.out.println(currentDateTime + " " + lineNumberMessage + coreMessage);
    }

    private static String getCurrentDateTimeMessage(LocalDateTime ldt)
    {
        return ldt.getDayOfMonth() + "/" + ldt.getMonthValue() + "/" + ldt.getYear() + " " + ldt.getHour() + ":" + ldt.getMinute() + ":" + ldt.getSecond();
    }

    private static String getCoreMessage(Type classType, LogLevel level, String message)
    {
        return "(" + classType + ") " + level + ": " + message;
    }

    private static String getLineNumberMessage(int lineNumber)
    {
        return "(Line " + lineNumber + ") ";
    }
}
