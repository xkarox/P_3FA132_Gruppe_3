package dev.hv.model.classes;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingWrapperTest
{
    @Test
    void defaultConstructorTest() {
        ReadingWrapper wrapper = new ReadingWrapper();

        assertNotNull(wrapper);
        assertNull(wrapper.getReadings());
    }

    @Test
    void parameterizedConstructorTest() {
        Reading reading1 = new Reading();
        Reading reading2 = new Reading();

        List<Reading> readings = Arrays.asList(reading1, reading2);
        ReadingWrapper wrapper = new ReadingWrapper(readings);

        assertNotNull(wrapper.getReadings());
        assertEquals(2, wrapper.getReadings().size());
        assertEquals(reading1, wrapper.getReadings().get(0));
        assertEquals(reading2, wrapper.getReadings().get(1));
    }

    @Test
    void setReadingTest() {
        ReadingWrapper wrapper = new ReadingWrapper();
        Reading reading1 = new Reading(UUID.randomUUID());
        Reading reading2 = new Reading(UUID.randomUUID());
        List<Reading> newReadings = Arrays.asList(reading1, reading2);

        wrapper.setReadings(newReadings);

        assertNotNull(wrapper.getReadings());
        assertEquals(2, wrapper.getReadings().size());
        assertEquals(reading1, wrapper.getReadings().get(0));
        assertEquals(reading2, wrapper.getReadings().get(1));


    }
}
