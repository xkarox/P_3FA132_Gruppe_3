package dev.hv.model.classes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalDateAdapterTest
{

    private final LocalDateAdapter adapter = new LocalDateAdapter();

    @Test
    void unmarshalValidDateTest() {
        String dateString = "2024-03-25";

        LocalDate result = adapter.unmarshal(dateString);

        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 3, 25), result);

    }

    @Test
    void marshalValidDate() {
        LocalDate date = LocalDate.of(2024, 3, 25);
        String result = adapter.marshal(date);

        assertNotNull(result);
        assertEquals("2024-03-25", result);

    }
}
