package ace;

public class utils
{
    public static <T> void checkValueEquals(T result, T expectedValue, ErrorMessages errorMessage) {
        if (!result.equals(expectedValue)) {
            throw new IllegalArgumentException("Expected: " + expectedValue + ", but got: " + result + " | " + errorMessage.toString());
        }
    }
}
