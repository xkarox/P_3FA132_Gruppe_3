package ace;

public class utils
{
    public static <T> void checkValueEquals(T result, T expectedValue, ErrorMessages errorMessage) {
        if (!result.equals(expectedValue)) {
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
