package dev.hv;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvParserTest
{
    private static long _entriesInDirectory = 0;

    private static final String _directory = "src/test/resources/";

    private static final String _csvReadingName = _directory + "csvReadingTest.csv";
    private static final String _csvCustomerName = _directory + "csvCustomerTest.csv";

    private static final File _csvReadingFile = new File(_csvReadingName);
    private static final File _csvCustomerFile = new File(_csvCustomerName);

    private static final String _csvCustomerFileHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";
    private static final String _csvReadingFileHeader = "Datum;Zählerstand in m³;Kommentar\n";
    private static final String _csvReadingFileMetaData = "\"Kunde\";\"ec617965-88b4-4721-8158-ee36c38e4db3\";\n" +
            "\"Zählernummer\";\"MST-af34569\";\n";

    private static final Map<File, Integer> _mockedFiles = new HashMap<>();

    @BeforeAll
    static void beforeAll()
    {
        try
        {
            _entriesInDirectory = Files.list(Paths.get(_directory)).filter(Files::isRegularFile).count();
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }

        try (FileWriter writer = new FileWriter(_csvCustomerName))
        {
            String _customerValuesFlawless =
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                            "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                            "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968\n";
            writer.write(_csvCustomerFileHeader);
            writer.write(_customerValuesFlawless);
            _mockedFiles.put(_csvCustomerFile, 4);
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer file", e);
        }
        try (FileWriter writer = new FileWriter(_csvReadingName))
        {
            String _readingValuesFlawless =
                    "01.02.2018;5,965;\n" +
                            "01.04.2018;6,597;\n" +
                            "01.05.2018;6,859;\n";
            writer.write(_csvReadingFileMetaData);
            writer.write(_csvReadingFileHeader);
            writer.write(_readingValuesFlawless);
            _mockedFiles.put(_csvReadingFile, 6);
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating reading file", e);
        }
    }

    @Test
    @Order(1)
    void numberOfFilesInDirectoryTest()
    {
        try
        {
            long actualFileCount = Files.list(Paths.get(_directory))
                    .filter(Files::isRegularFile)
                    .count();
            assertEquals(_entriesInDirectory + _mockedFiles.size(), actualFileCount, "It should be the same amount of files counted");

        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }
    }

    @Test
    @Order(2)
    void verifyLineCountMatchesFirstIntegerTest()
    {
        for (Map.Entry<File, Integer> entry : _mockedFiles.entrySet())
        {
            File file = entry.getKey();
            int numberOfLines = entry.getValue();

            int actualLineCount = 0;

            try (Scanner scanner = new Scanner(file))
            {
                while (scanner.hasNextLine())
                {
                    scanner.nextLine();
                    actualLineCount++;
                }
            } catch (IOException e)
            {
                throw new RuntimeException("Error when trying to count lines in file: " + file.getName(), e);
            }
            assertEquals(numberOfLines, actualLineCount,
                    "Line count mismatch for file: " + file.getName() +
                            " (Expected: " + numberOfLines + ", Actual: " + actualLineCount + ")");
        }
    }

    @Test
    void getReadingSeparatorTest()
    {
        CsvParser parser = new CsvParser(_csvReadingFile);
        assertEquals(';', parser.getSeparator());
    }

    @Test
    void getCustomerSeparatorTest()
    {
        CsvParser parser = new CsvParser(_csvCustomerFile);
        assertEquals(',', parser.getSeparator());
    }

    @Test
    void getReadingHeaderTest()
    {
        CsvParser parser = new CsvParser(_csvReadingFile);
        Iterable<String> header = parser.getHeader();

        List<String> expectedHeader = Arrays.asList("Datum", "Zählerstand in m³", "Kommentar");

        Iterator<String> headerIterator = header.iterator();
        for (String expectedColumn : expectedHeader)
        {
            assertTrue(headerIterator.hasNext());
            assertEquals(expectedColumn, headerIterator.next());
        }
        assertFalse(headerIterator.hasNext());
    }

    @Test
    void getCustomerHeaderTest()
    {
        CsvParser parser = new CsvParser(_csvCustomerFile);
        Iterable<String> header = parser.getHeader();

        List<String> expectedHeader = new ArrayList<>();
        expectedHeader.add("UUID");
        expectedHeader.add("Anrede");
        expectedHeader.add("Vorname");
        expectedHeader.add("Nachname");
        expectedHeader.add("Geburtsdatum");

        assertEquals(header, expectedHeader);
    }

    @Test
    void getReadingValuesTest()
    {
        CsvParser parser = new CsvParser(_csvReadingFile);

        List<Map<String, String>> expectedValues = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("Datum", "01.02.2018");
        row1.put("Zählerstand in m³", "5,965");
        row1.put("Kommentar", "");
        expectedValues.add(row1);

        Map<String, String> row2 = new HashMap<>();
        row2.put("Datum", "01.04.2018");
        row2.put("Zählerstand in m³", "6,597");
        row2.put("Kommentar", "");
        expectedValues.add(row2);

        Map<String, String> row3 = new HashMap<>();
        row3.put("Datum", "01.05.2018");
        row3.put("Zählerstand in m³", "6,859");
        row3.put("Kommentar", "");
        expectedValues.add(row3);

        Iterable<Map<String, String>> actualValues = parser.getValues();

        Iterator<Map<String, String>> actualIterator = actualValues.iterator();
        for (Map<String, String> expectedRow : expectedValues)
        {
            assertTrue(actualIterator.hasNext(), "more lines should exist");
            Map<String, String> actualRow = actualIterator.next();
            assertEquals(expectedRow, actualRow, "lines are not the same");
        }
        assertFalse(actualIterator.hasNext(), "no more lines should exist");
    }

    @Test
    void getCustomerValuesTest()
    {
        CsvParser parser = new CsvParser(_csvCustomerFile);

        List<Map<String, String>> expectedValues = new ArrayList<>();

        Map<String, String> row1 = new HashMap<>();
        row1.put("UUID", "ec617965-88b4-4721-8158-ee36c38e4db3");
        row1.put("Anrede", "Herr");
        row1.put("Vorname", "Pumukel");
        row1.put("Nachname", "Kobold");
        row1.put("Geburtsdatum", "21.02.1962");
        expectedValues.add(row1);

        Map<String, String> row2 = new HashMap<>();
        row2.put("UUID", "848c39a1-0cbb-427a-ac6f-a88941943dc8");
        row2.put("Anrede", "Herr");
        row2.put("Vorname", "André");
        row2.put("Nachname", "Schöne");
        row2.put("Geburtsdatum", "16.02.1928");
        expectedValues.add(row2);

        Iterable<Map<String, String>> actualValues = parser.getValues();

        Iterator<Map<String, String>> actualIterator = actualValues.iterator();

        for (Map<String, String> expectedRow : expectedValues)
        {
            assertTrue(actualIterator.hasNext(), "more lines should exist");
            Map<String, String> actualRow = actualIterator.next();
            assertEquals(expectedRow, actualRow, "lines are not the same");
        }
    }

    @Test
    void getReadingMetaDataTest()
    {
        CsvParser parser = new CsvParser(_csvReadingFile);

        List<Map<String, String>> expectedValues = new ArrayList<>();

        Map<String, String> row1 = new HashMap<>();
        row1.put("Kunde", "ec617965-88b4-4721-8158-ee36c38e4db3");
        expectedValues.add(row1);

        Map<String, String> row2 = new HashMap<>();
        row2.put("Zählernummer", "MST-af34569");
        expectedValues.add(row2);

        Iterable<Map<String, String>> actualValues = parser.getMetaData();

        Iterator<Map<String, String>> actualIterator = actualValues.iterator();
        for (Map<String, String> expectedRow : expectedValues) {
            assertTrue(actualIterator.hasNext(), "more lines should exist");
            Map<String, String> actualRow = actualIterator.next();
            assertEquals(expectedRow, actualRow, "lines are not the same");
        }
        assertFalse(actualIterator.hasNext(), "no more lines should exist");
    }

    @Test
    void getCustomerMetaDataTest()
    {
        CsvParser parser = new CsvParser(_csvCustomerFile);

        Iterable<Map<String, String>> actualValues = parser.getMetaData();

        assertNotNull(actualValues, "should not be null");
        assertFalse(actualValues.iterator().hasNext(), "list should be empty");
    }
    @AfterAll
    static void afterAll()
    {
        for (File file : _mockedFiles.keySet()) {
            if (file.exists())
            {
                boolean deleted = file.delete();
                if (!deleted)
                {
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
        _mockedFiles.clear();
    }

}

