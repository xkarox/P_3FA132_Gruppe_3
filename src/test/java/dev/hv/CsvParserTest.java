package dev.hv;

import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

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

    // private static final Map<File, Integer> _mockedFiles = new HashMap<>()

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
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer file", e);
        }
        try (FileWriter writer = new FileWriter(_csvReadingName))
        {

        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating reading file", e);
        }
    }
}
