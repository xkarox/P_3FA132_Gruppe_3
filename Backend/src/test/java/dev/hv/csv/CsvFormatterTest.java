package dev.hv.csv;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvFormatterTest
{

    enum LineType
    {
        LINES_ADDED("Lines added"),
        LINES_AFTER_FORMAT("Lines after format");

        private final String description;

        LineType(String description)
        {
            this.description = description;
        }

        @Override
        public String toString()
        {
            return description;
        }
    }

    private static final Map<String, Map<String, Integer>> mockedCustomerData = new HashMap<>();
    private static final Map<String, Map<String, Integer>> mockedReadingData = new HashMap<>();
    private static final List<String> formattedMockedCustomerData = new ArrayList<>();
    private static final List<String> formattedMockedReadingData = new ArrayList<>();

    private static final String csvCustomerFileHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";
    private static final String csvReadingFileHeader = "\"Datum\";\"Zählerstand in MWh\";\"Kommentar\"\n";

    private static final String csvReadingMetaData = "\"Kunde\";\"ec617965-88b4-4721-8158-ee36c38e4db3\";\n" +
            "\"Zählernummer\";\"Xr-2018-2312456ab\";\n";

    @BeforeAll
    static void beforeAll()
    {
        addMockCustomerData(mockedCustomerData, csvCustomerFileHeader,
                "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                        "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                        "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968\n", 4, 4);

        addMockCustomerData(mockedCustomerData, csvCustomerFileHeader,
                "f2683104-974d-44eb-a060-82ed72737cbe,Frau,Elgine,Karras,\n" +
                        "2a284519-4141-409c-a5d6-ad77bba13523,Frau,Karolina,Hamburger,\n", 3, 3);

        addMockCustomerData(mockedCustomerData, csvCustomerFileHeader,
                "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                        "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                        "\n\n\n" +
                        "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968\n", 7, 4);

        addMockReadingData(mockedReadingData, csvReadingFileHeader, csvReadingMetaData,
                "01.02.2018;5,965;\n" +
                        "01.04.2018;6,597;\n", 5, 5);

        addMockReadingData(mockedReadingData, csvReadingFileHeader, csvReadingMetaData,
                "01.02.2018;5,965;\n" +
                        "01.04.2018;6,597;\n" +
                        "01.05.2018;6,859;\n" +
                        "01.07.2018;7,344;\n" +
                        "01.08.2018;7,558;\n", 8, 8);

        addMockReadingData(mockedReadingData, csvReadingFileHeader, csvReadingMetaData,
                "01.02.2018;5,965;\n" +
                        "01.04.2018;6,597;\n" +
                        "\n\n\n" + "01.04.2018;6,597;\n", 9, 6);
    }

    private static void addMockCustomerData(Map<String, Map<String, Integer>> mockData, String header, String values, int added, int afterFormat)
    {
        Map<String, Integer> linesCount = new HashMap<>();
        linesCount.put(LineType.LINES_ADDED.toString(), added);
        linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), afterFormat);
        mockData.put(header + values, linesCount);
    }

    private static void addMockReadingData(Map<String, Map<String, Integer>> mockData, String header, String metaData, String values, int added, int afterFormat)
    {
        Map<String, Integer> linesCount = new HashMap<>();
        linesCount.put(LineType.LINES_ADDED.toString(), added);
        linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), afterFormat);
        mockData.put(metaData + header + values, linesCount);
    }

    @Test
    @Order(1)
    void verifyLineCountMatchesFirstInteger()
    {
        for (var entry : mockedCustomerData.entrySet())
        {
            String content = entry.getKey();
            int expectedLines = entry.getValue().get(LineType.LINES_ADDED.toString());
            int actualLines = content.split("\n").length;
            assertEquals(expectedLines, actualLines, "Line count mismatch");
        }

        for (var entry : mockedReadingData.entrySet())
        {
            String content = entry.getKey();
            int expectedLines = entry.getValue().get(LineType.LINES_ADDED.toString());
            int actualLines = content.split("\n").length;
            assertEquals(expectedLines, actualLines, "Line count mismatch");
        }
    }

    @Test
    @Order(2)
    void removeEmptyLinesTest()
    {
        CsvFormatter formatter = new CsvFormatter();
        for (var entry : mockedCustomerData.entrySet())
        {
            String originalContent = entry.getKey();
            String formattedContent = formatter.formatReadingCsv(originalContent);
            formattedMockedCustomerData.add(formattedContent);

            int expectedLines = entry.getValue().get(LineType.LINES_AFTER_FORMAT.toString());
            int actualLines = formattedContent.split("\n").length;
            assertEquals(expectedLines, actualLines, "Formatted line count mismatch");
        }

        for (var entry : mockedReadingData.entrySet())
        {
            String originalContent = entry.getKey();
            String formattedContent = formatter.formatReadingCsv(originalContent);
            formattedMockedReadingData.add(formattedContent);

            int expectedLines = entry.getValue().get(LineType.LINES_AFTER_FORMAT.toString());
            int actualLines = formattedContent.split("\n").length;
            assertEquals(expectedLines, actualLines, "Formatted line count mismatch");
        }
    }
}
