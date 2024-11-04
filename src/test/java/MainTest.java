import org.junit.jupiter.api.Test;

class MainTest
{

    @Test
    void mainTest()
    {
        Main.main(new String[]{});
    }

    @Test
        // Just for coverage in jacoco report
    void staticTest()
    {
        Main main = new Main();
    }
}