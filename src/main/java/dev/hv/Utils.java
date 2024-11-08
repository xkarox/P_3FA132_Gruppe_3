package dev.hv;

public class Utils
{
    public static <T> void checkValueEquals(T expectedValue, T result, ErrorMessages errorMessage)
    {
        if (!result.equals(expectedValue))
        {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("Expected: ");
            strBuilder.append(expectedValue);
            strBuilder.append(", but got: ");
            strBuilder.append(result);
            strBuilder.append(" | ");
            strBuilder.append(errorMessage.toString());
            String resultString = strBuilder.toString();
            throw new IllegalArgumentException(resultString);
        }
    }
}
