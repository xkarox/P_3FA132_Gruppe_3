package ace.services.logService;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LogService
{
    public static void log(Type classType, LogLevel level, String message)
    {
        log(classType.getTypeName(), level, message);
    }

    public static void log(String classType, LogLevel level, String message)
    {
        LocalDateTime ldt = LocalDateTime.now();
        String currentDateTime = getCurrentDateTimeMessage(ldt);
        String coreMessage = getCoreMessage(classType, level, message);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(currentDateTime).append(" ").append(coreMessage);
        System.out.println(strBuilder);
    }

    private static String getCurrentDateTimeMessage(LocalDateTime ldt)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ldt.getDayOfMonth()).append("/")
                .append(ldt.getMonthValue()).append("/")
                .append(ldt.getYear())
                .append(" ").append(ldt.getHour()).append(":").append(ldt.getMinute()).append(":").append(ldt.getSecond());
        return sb.toString();
    }

    private static String getCoreMessage(String classType, LogLevel level, String message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(classType).append(") ").append(level).append(": ").append(message);
        return sb.toString();
    }

    private static String getLineNumberMessage(int lineNumber)
    {
        return "(Line " + lineNumber + ") ";
    }
}
