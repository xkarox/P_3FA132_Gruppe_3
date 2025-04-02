package dev.hv;

import dev.hv.csv.CsvParser;
import dev.hv.database.services.CustomerService;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SerializerTest
{
    private CsvParser parserMock;

    @BeforeEach
    void setUp() {
        parserMock = mock(CsvParser.class);
    }

    @Test
    void deserializeCustomerCsvTest() throws ReflectiveOperationException, SQLException, IOException
    {
        String csv = "UUID;Anrede;Vorname;Nachname;Geburtsdatum\nec617965-88b4-4721-8158-ee36c38e4db3;Herr;Max;Müller;01.01.1990";
        List<Customer> expectedCustomers = Arrays.asList(new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Max", "Müller", LocalDate.of(1990, 1, 1), ICustomer.Gender.M));

        when(parserMock.getCustomerHeader()).thenReturn(Arrays.asList("UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"));
        when(parserMock.createCustomerFromCsv()).thenReturn(expectedCustomers);

        List<?> result = Serializer.deserializeCsv(csv);

        assertNotNull(result);
        assertEquals(expectedCustomers, result);
    }

    @Test
    void deserializeDefaultReadingWaterTest() throws ReflectiveOperationException, SQLException, IOException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in m³;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeCsv(csv);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.WASSER);

    }

    @Test
    void deserializeDefaultReadingHeatTest() throws ReflectiveOperationException, SQLException, IOException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in MWh;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeCsv(csv);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getCustomerId(), null);
        assertEquals(reading.getMeterId(), "112233");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.HEIZUNG);
    }

    @Test
    void deserializeDefaultReadingElectricityTest() throws ReflectiveOperationException, SQLException, IOException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in kWh;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeCsv(csv);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getCustomerId(), null);
        assertEquals(reading.getMeterId(), "112233");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.STROM);
    }

    @Test
    void deserializeDefaultReadingWithExistingCustomer() throws ReflectiveOperationException, SQLException, IOException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in kWh;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeCsv(csv);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getCustomer().getId(), UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"));
        assertEquals(reading.getMeterId(), "112233");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.STROM);
    }
}
