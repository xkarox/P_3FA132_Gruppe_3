package dev.misc.performance;

import java.security.SecureRandom;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;

public class StringPerformanceTest {

    public static void main(String[] args) {
        int iterations = 100000;

        for(int i = 0; i < 10; i++)
        {

            System.out.println("Vergleich von String-Konkatenation und StringBuilder:");

            // Test für String-Konkatenation mit dem + Operator
            Instant startTimeConcat = Instant.now();
            testStringConcatRandom(iterations);
            Instant endTimeConcat = Instant.now();
            System.out.println("String-Konkatenation (+ Operator): " + Duration.between(startTimeConcat, endTimeConcat).toMillis() + " ms");

            // Test für StringBuilder
            Instant startTimeBuilder = Instant.now();
            testStringBuilderRandom(iterations);
            Instant endTimeBuilder = Instant.now();
            System.out.println("StringBuilder: " + Duration.between(startTimeBuilder, endTimeBuilder).toMillis() + " ms");
        }
    }

    private static void testStringConcat(int iterations)
    {
        String result = "";
        for (int i = 0; i < iterations; i++)
        {
            result += "a";
        }
    }

    private static void testStringBuilder(int iterations)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < iterations; i++)
        {
            builder.append("a");
        }
    }

    private static void testStringConcatRandom(int iterations)
    {
        String result = "";
        for (int i = 0; i < iterations; i++) {
            result += "a" + System.currentTimeMillis();
        }
    }

    private static void testStringBuilderRandom(int iterations)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < iterations; i++)
        {
            builder.append("a");
            builder.append(System.currentTimeMillis());
        }
    }
}

