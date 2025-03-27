package dev.hv.csv;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getReadingSeparatorTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        assertEquals(";", parser.getSeparator());
    }

    @Test
    void getCustomerSeparatorTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_CUSTOMER_CONTENT));
        assertEquals(",", parser.getSeparator());
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
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_CUSTOMER_CONTENT));
        List<String> expectedHeader = Arrays.asList("UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum");
        assertIterableEquals(expectedHeader, parser.getReadingHeader());
    }

    @Test
    void getReadingValuesTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_READING_CONTENT));
        List<List<String>> expectedValues = List.of(
                List.of("01.02.2018", "5,965"),
                List.of("01.04.2018", "6,597")
        );
        assertIterableEquals(expectedValues, parser.getReadingValues());
    }

    @Test
    void getCustomerValuesTest() throws IOException
    {
        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(formatter.formatReadingCsv(CSV_CUSTOMER_CONTENT));
        List<List<String>> expectedValues = List.of(
                List.of("ec617965-88b4-4721-8158-ee36c38e4db3", "Herr", "Pumukel", "Kobold", "21.02.1962"),
                List.of("848c39a1-0cbb-427a-ac6f-a88941943dc8", "Herr", "André", "Schöne", "16.02.1928")
        );
        assertIterableEquals(expectedValues, parser.getReadingValues());
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
}
