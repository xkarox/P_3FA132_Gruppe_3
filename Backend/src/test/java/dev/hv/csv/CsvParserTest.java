package dev.hv.csv;

import dev.hv.Serializer;
import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.Csv;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvParserTest {
    private static final String CSV_CUSTOMER_CONTENT =
            "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n" +
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                    "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n";

    private static final String CSV_READING_CONTENT =
            "\"Kunde\";\"ec617965-88b4-4721-8158-ee36c38e4db3\";\n" +
                    "\"Zählernummer\";\"MST-af34569\";\n" +
                    "Datum;Zählerstand in m³;Kommentar\n" +
                    "01.02.2018;5,965;\n" +
                    "01.04.2018;6,597;\n";

    private static final String CSV_READING_CONTENT_CUSTOM =
            "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n" +
                    "01.10.2020;63.0;;ec617965-88b4-4721-8158-ee36c38e4db3;WASSER;786523123;false;\n" +
                    "31.12.2019;722.0;;ec617965-88b4-4721-8158-ee36c38e4db3;WASSER;23451007;false;\n";


    private static final Customer MOCKED_CUSTOMER1 = new Customer(UUID.randomUUID(), "Erik", "Mielke", LocalDate.of(2002, 7, 3), ICustomer.Gender.M);
    private static final Customer MOCKED_CUSTOMER2 = new Customer(UUID.randomUUID(), "Christopher", "Walther", LocalDate.of(1999, 3, 22), ICustomer.Gender.M);

    private static final Reading MOCKED_READING1 = new Reading(UUID.randomUUID(), "comment1", MOCKED_CUSTOMER1.getId(), MOCKED_CUSTOMER1, LocalDate.of(1999, 3, 14), IReading.KindOfMeter.STROM, 100.0, "129393", false);
    private static final Reading MOCKED_READING2 = new Reading(UUID.randomUUID(), "comment2", MOCKED_CUSTOMER1.getId(), MOCKED_CUSTOMER1, LocalDate.of(2021, 11, 2), IReading.KindOfMeter.WASSER, 1923.0, "428379", true);

    private static CsvFormatter mockFormatter;
    private static ReadingService mockReadingService;
    private static CustomerService mockCustomerService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    String formattedDate = MOCKED_READING2.getDateOfReading().format(formatter);



    @BeforeAll
    static void setUp() throws SQLException, IOException
    {
        mockFormatter = mock(CsvFormatter.class);

        mockReadingService = mock(ReadingService.class);
        mockCustomerService = mock(CustomerService.class);
        ServiceProvider.Services = mock(dev.hv.database.provider.InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);
    }

    @Test
    void getReadingSeparatorTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        assertEquals(";", parser.getSeparator());
    }

    @Test
    void getSeparatorTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_CUSTOMER_CONTENT));
        assertEquals(CsvParser.Separator.READING_SEPARATOR.toString(), parser.getSeparator());
    }

    @Test
    void getReadingHeaderTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        List<String> expectedHeader = Arrays.asList("Datum", "Zählerstand in m³", "Kommentar");
        assertIterableEquals(expectedHeader, parser.getReadingHeader());
    }

    @Test
    void getCustomerHeaderTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_CUSTOMER_CONTENT);
        List<String> expectedHeader = Arrays.asList("UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum");
        assertIterableEquals(expectedHeader, parser.getCustomerHeader());
    }

    @Test
    void getReadingValuesTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        List<List<String>> expectedValues = List.of(
                List.of("01.02.2018", "5.965"),
                List.of("01.04.2018", "6.597")
        );
        assertIterableEquals(expectedValues, parser.getDefaultReadingValues());
    }

    @Test
    void getReadingMetaDataTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        List<Map<String, String>> expectedValues = List.of(
                Map.of("Kunde", "ec617965-88b4-4721-8158-ee36c38e4db3"),
                Map.of("Zählernummer", "MST-af34569")
        );
        assertIterableEquals(expectedValues, parser.getMetaData());
    }

    @Test
    void getCustomerMetaDataTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_CUSTOMER_CONTENT));
        assertFalse(parser.getMetaData().iterator().hasNext(), "Customer CSV should have no metadata");
    }

    @Test
    void setAndGetCsvContentTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        String expectedContent = "test,data,123";

        parser.setCsvContent(expectedContent);
        assertEquals(parser.getCsvContent(), expectedContent, "Values are not equal");
    }

    @Test
    void getDefaultReadingValuesWithValidCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT);

        Iterable<List<String>> result = parser.getDefaultReadingValues();
        Iterator<List<String>> iterator = result.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(List.of("01.02.2018", "5.965"), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(List.of("01.04.2018", "6.597"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void getDefaultReadingValuesWithEmptyCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent("");

        Iterable<List<String>> result = parser.getDefaultReadingValues();
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void getCustomReadingValuesWithValidCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT_CUSTOM);

        Iterable<List<String>> result = parser.getCustomReadingValues();
        Iterator<List<String>> iterator = result.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(List.of("01.10.2020", "63.0", "", "ec617965-88b4-4721-8158-ee36c38e4db3", "WASSER", "786523123", "false"), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(List.of("31.12.2019", "722.0", "", "ec617965-88b4-4721-8158-ee36c38e4db3", "WASSER", "23451007", "false"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void getCustomReadingValuesWithEmptyCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent("");

        Iterable<List<String>> result = parser.getCustomReadingValues();
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void getCustomerValuesWithValidCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_CUSTOMER_CONTENT);

        Iterable<List<String>> result = parser.getCustomerValues();
        Iterator<List<String>> iterator = result.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(List.of("ec617965-88b4-4721-8158-ee36c38e4db3", "Herr", "Pumukel", "Kobold", "21.02.1962"), iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(List.of("848c39a1-0cbb-427a-ac6f-a88941943dc8", "Herr", "André", "Schöne", "16.02.1928"), iterator.next());
        assertFalse(iterator.hasNext());
    }


    @Test
    void getCustomerValuesWithEmptyCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent("");

        Iterable<List<String>> result = parser.getCustomerValues();
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void getReadingHeaderWithValidCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT);

        Iterable<String> result = parser.getReadingHeader();
        Iterator<String> iterator = result.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("Datum", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Zählerstand in m³", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Kommentar", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void getReadingHeaderWithEmptyCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent("");

        Iterable<String> result = parser.getReadingHeader();
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void getCustomerHeaderWithValidCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_CUSTOMER_CONTENT);

        Iterable<String> result = parser.getCustomerHeader();
        Iterator<String> iterator = result.iterator();

        assertTrue(iterator.hasNext());
        assertEquals("UUID", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Anrede", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Vorname", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Nachname", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Geburtsdatum", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void getCustomerHeaderWithEmptyCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent("");

        Iterable<String> result = parser.getCustomerHeader();
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void createReadingsByKindOfMeterWithValidReadings() throws SQLException, IOException, ReflectiveOperationException
    {
        CsvParser parser = new CsvParser();
        List<Reading> mockReadings = List.of(MOCKED_READING1, MOCKED_READING2);
        when(mockReadingService.getAll()).thenReturn(mockReadings);

        String result = parser.createReadingsByKindOfMeter(IReading.KindOfMeter.WASSER);

        assertTrue(result.contains("Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz"));

        String expectedReading2 = formattedDate + ";"
                + MOCKED_READING2.getMeterCount() + ";"
                + MOCKED_READING2.getComment() + ";"
                + MOCKED_READING2.getCustomerId() + ";"
                + MOCKED_READING2.getKindOfMeter() + ";"
                + MOCKED_READING2.getMeterId() + ";"
                + MOCKED_READING2.getSubstitute();
        assertTrue(result.contains(expectedReading2));
    }

    @Test
    void createReadingsByKindOfMeterWithEmptyDatabase() throws ReflectiveOperationException, SQLException, IOException
    {
        CsvParser parser = new CsvParser();
        when(mockReadingService.getAll()).thenReturn(List.of());

        String result = parser.createReadingsByKindOfMeter(IReading.KindOfMeter.WASSER);

        assertEquals("Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n", result);
    }

    @Test
    void createAllCustomersCsvWithValidCustomersTest() throws ReflectiveOperationException, SQLException, IOException
    {
        CsvParser parser = new CsvParser();
        List<Customer> mockCustomers = List.of(MOCKED_CUSTOMER1, MOCKED_CUSTOMER2);
        when(mockCustomerService.getAll()).thenReturn(mockCustomers);

        String expectedHeader = "UUID;Anrede;Vorname;Nachname;Geburtsdatum\n";
        String expectedData = Serializer.serializeIntoCsv(mockCustomers);
        String expectedCsv = expectedHeader + expectedData;

        String result = parser.createAllCustomerCsv();

        assertEquals(expectedCsv, result);
    }

    @Test
    void createAllCustomersCsvWithEmptyCustomersTest() throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        List<Customer> mockCustomers = List.of();
        when(mockCustomerService.getAll()).thenReturn(mockCustomers);

        String expectedHeader = "UUID;Anrede;Vorname;Nachname;Geburtsdatum\n";
        String expectedData = Serializer.serializeIntoCsv(mockCustomers);
        String expectedCsv = expectedHeader + expectedData;

        String result = parser.createAllCustomerCsv();

        assertEquals(expectedCsv, result);
    }

    @Test
    void createDefaultReadingsFromCsvWithWaterKindOfMeterTest() throws ReflectiveOperationException, SQLException, IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT);

        List<Reading> readings = parser.createDefaultReadingsFromCsv(false, true, false);


        assertEquals(2, readings.size());
        Reading reading = readings.get(0);
        assertEquals(IReading.KindOfMeter.WASSER, reading.getKindOfMeter());
        assertEquals("5.965", reading.getMeterCount().toString());
        assertEquals(LocalDate.parse("01.02.2018", DateTimeFormatter.ofPattern("dd.MM.yyyy")), reading.getDateOfReading());
        assertEquals("MST-af34569", reading.getMeterId());

    }

    @Test
    void createDefaultReadingsFromCsvWithElectricityKindOfMeterTest() throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT);

        List<Reading> readings = parser.createDefaultReadingsFromCsv(false, false, true);


        assertEquals(2, readings.size());
        Reading reading = readings.get(0);
        assertEquals(IReading.KindOfMeter.STROM, reading.getKindOfMeter());
        assertEquals("5.965", reading.getMeterCount().toString());
        assertEquals(LocalDate.parse("01.02.2018", DateTimeFormatter.ofPattern("dd.MM.yyyy")), reading.getDateOfReading());
        assertEquals("MST-af34569", reading.getMeterId());
    }

    @Test
    void createDefaultReadingsFromCsvWithHeatKindOfMeterTest() throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT);

        List<Reading> readings = parser.createDefaultReadingsFromCsv(true, false, false);


        assertEquals(2, readings.size());
        Reading reading = readings.get(0);
        assertEquals(IReading.KindOfMeter.HEIZUNG, reading.getKindOfMeter());
        assertEquals("5.965", reading.getMeterCount().toString());
        assertEquals(LocalDate.parse("01.02.2018", DateTimeFormatter.ofPattern("dd.MM.yyyy")), reading.getDateOfReading());
        assertEquals("MST-af34569", reading.getMeterId());
    }

    @Test
    void createCustomReadingsFromCsvTest() throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_READING_CONTENT_CUSTOM);

        List<Reading> readings = parser.createCustomReadingsFromCsv();
        Reading reading = readings.get(0);
        assertEquals(reading.getDateOfReading().toString(), "2020-10-01");
        assertEquals(reading.getMeterCount(), 63.0);
        assertEquals(reading.getComment(), "");
        assertEquals(reading.getCustomerId(), null);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.WASSER);
        assertEquals(reading.getMeterId(), "786523123");
        assertEquals(reading.getSubstitute(), false);

    }

    @Test
    void createCustomerFromCsvTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(CSV_CUSTOMER_CONTENT);

        List<Customer> customers = parser.createCustomerFromCsv();
        Customer customer = customers.get(0);

        assertEquals(customer.getId().toString(), "ec617965-88b4-4721-8158-ee36c38e4db3");
        assertEquals(customer.getGender(), ICustomer.Gender.M);
        assertEquals(customer.getFirstName(), "Pumukel");
        assertEquals(customer.getLastName(), "Kobold");
        assertEquals(customer.getBirthDate().toString(), "1962-02-21");


    }
}
