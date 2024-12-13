package dev.hv;

import org.junit.jupiter.api.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvFormatterTest
{
    enum LineType {
        LINES_ADDED("Lines added"),
        LINES_AFTER_FORMAT("Lines after format");

        private final String description;

        LineType(String description) {
            this.description = description;
        }
        @Override
        public String toString()
        {
            return description;
        }
    }

    private static long _entriesInDirectory = 0;

    private static final String _directory = "src/test/resources/";
    private static final String _csvCustomerFileNameFlawless = _directory + "csvCustomerTestFlawless.csv";
    private static final String _csvCustomerFileNameWithEndingComma = _directory + "csvCustomerTestWithEndingComma.csv";
    private static final String _csvCustomerFileNameWithEmptyValues = _directory + "csvCustomerTestWithEmptyValues.csv";
    private static final String _csvCustomerFileNameWithEmptyLines = _directory + "csvCustomerTestWithEmptyLines.csv";

    private static final String _csvReadingFileNameFlawless = _directory + "csvReadingTestFlawless.csv";
    private static final String _csvReadingFileNameWithoutComments = _directory + "csvReadingTestWithoutComments.csv";
    private static final String _csvReadingFileNameWithEmptyLines = _directory + "csvReadingTestWithEmptyLines";
    private static final String _csvReadingFileNameWithMixedValues = _directory + "csvReadingTestWithMixedValues";

    private static final String _csvCustomerFileHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";
    private static final String _csvReadingFileHeader = "Datum;Zählerstand in m³;Kommentar\n";
    private static final String _csvReadingMetaDataFlawless =
            "\"Kunde\";\"ec617965-88b4-4721-8158-ee36c38e4db3\";\n" +
                    "\"Zählernummer\";\"MST-af34569\";\n";

    private static final String _csvReadingMetaDataWithEmptyLines =
            "\"Kunde\";\"ec617965-88b4-4721-8158-ee36c38e4db3\";\n" +
                    "\"Zählernummer\";\"MST-af34569\";\n" +
                    "\n" +
                    ";;\n";

    private static final File _csvCustomerFileFlawless = new File(_csvCustomerFileNameFlawless);
    private static final File _csvCustomerFileWithEndingComma = new File(_csvCustomerFileNameWithEndingComma);
    private static final File _csvCustomerFileWithEmptyValues = new File(_csvCustomerFileNameWithEmptyValues);
    private static final File _csvCustomerFileWithEmptyLines = new File(_csvCustomerFileNameWithEmptyLines);

    private static final File _csvReadingFileFlawless = new File(_csvReadingFileNameFlawless);
    private static final File _csvReadingFileWithoutComments = new File(_csvReadingFileNameWithoutComments);
    private static final File _csvReadingFileWithEmptyLines = new File(_csvReadingFileNameWithEmptyLines);
    private static final File _csvReadingFileWithMixedValues = new File(_csvReadingFileNameWithMixedValues);

    private static final Map<File, Map<String, Integer>> _mockedCustomerFiles = new HashMap<>();
    private static final Map<File, Map<String, Integer>> _mockedReadingFiles = new HashMap<>();

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

        try (FileWriter writer = new FileWriter(_csvCustomerFileNameFlawless))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _customerValuesFlawless =
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                            "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                            "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968\n";
            writer.write(_csvCustomerFileHeader);
            writer.write(_customerValuesFlawless);
            linesCount.put(LineType.LINES_ADDED.toString(), 4);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 4);
            _mockedCustomerFiles.put(_csvCustomerFileFlawless, linesCount);

        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer flawless file", e);
        }

        try (FileWriter writer = new FileWriter(_csvCustomerFileNameWithEndingComma))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _customerValuesWithEndingComma =
                    "f2683104-974d-44eb-a060-82ed72737cbe,Frau,Elgine,Karras,\n" +
                            "2a284519-4141-409c-a5d6-ad77bba13523,Frau,Karolina,Hamburger,\n";
            writer.write(_csvCustomerFileHeader);
            writer.write(_customerValuesWithEndingComma);
            linesCount.put(LineType.LINES_ADDED.toString(), 3);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 3);
            _mockedCustomerFiles.put(_csvCustomerFileWithEndingComma, linesCount);

        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer file with ending comma");
        }

        try (FileWriter writer = new FileWriter(_csvCustomerFileNameWithEmptyValues))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _customerValuesWithEmptyValues =
                    ",Frau,Annedorle,Luber,\n" +
                            "51a57f7c-2080-4fcc-8107-17b613b2b948,,Anastasia,Weißmann,\n" +
                            "b64627ef-18cf-49b5-8998-5aa5200e3459,Herr,Hilar,Angenendt,09.02.1936\n" +
                            "4e7726e2-3ca9-4d4c-904a-bff033688162,Frau,Heide,Gierl,\n" +
                            "c8d83f41-e84e-49b2-bf36-42544266e46e,Frau,Friedlies,Mertins,\n" +
                            "1d9ad1bb-f4eb-4e3f-bc87-65d5e8ffb0c9,Frau,,Stark,\n" +
                            "1152b31d-50ad-40fb-ae96-d06d5517b7d8,Herr,Heiner,Strohm,05.02.1997\n";
            writer.write(_csvCustomerFileHeader);
            writer.write(_customerValuesWithEmptyValues);
            linesCount.put(LineType.LINES_ADDED.toString(), 8);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 8);
            _mockedCustomerFiles.put(_csvCustomerFileWithEmptyValues, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer file with empty Values");
        }

        try (FileWriter writer = new FileWriter(_csvCustomerFileNameWithEmptyLines))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _customerValuesWithEmptyLines =
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                            "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                            ",,\n" +
                            ",\n" +
                            "\n" +
                            ",,,,,,,,,\n" +
                            "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968\n";

            writer.write(_csvCustomerFileHeader);
            writer.write(_customerValuesWithEmptyLines);
            linesCount.put(LineType.LINES_ADDED.toString(), 8);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 4);
            _mockedCustomerFiles.put(_csvCustomerFileWithEmptyLines, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating customer file with empty lines");
        }

        try (FileWriter writer = new FileWriter(_csvReadingFileNameFlawless))
        {

            Map<String, Integer> linesCount = new HashMap<>();
            String _readingValuesFlawless =
                    "03.03.2020;0;\"Zählertausch: neue Nummer 786523123\"\n" +
                            "22.06.2021;0;\"Zählertausch: neue Nummer Xr-2021-2312434xz\"\n";

            writer.write(_csvReadingMetaDataFlawless);
            writer.write(_csvReadingFileHeader);
            writer.write(_readingValuesFlawless);
            linesCount.put(LineType.LINES_ADDED.toString(), 5);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 5);
            _mockedReadingFiles.put(_csvReadingFileFlawless, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating reading flawless file");
        }

        try (FileWriter writer = new FileWriter(_csvReadingFileNameWithoutComments))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _readingValuesWithoutComments =
                    "01.10.2018;565;\n" +
                            "01.11.2018;574;\n" +
                            "01.12.2018;584;\n" +
                            "31.12.2018;594;\n" +
                            "31.12.2018;596;\n" +
                            "01.02.2019;604;\n";
            writer.write(_csvReadingMetaDataFlawless);
            writer.write(_csvReadingFileHeader);
            writer.write(_readingValuesWithoutComments);
            linesCount.put(LineType.LINES_ADDED.toString(), 9);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 9);
            _mockedReadingFiles.put(_csvReadingFileWithoutComments, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error when creating reading file without comments");
        }
        try (FileWriter writer = new FileWriter(_csvReadingFileNameWithEmptyLines))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _readingValuesWithEmptyLines =
                    "01.10.2018;565;\n" +
                            "01.11.2018;574;\n" +
                            "01.12.2018;584;\n" +
                            "\n" +
                            ";\n" +
                            ";;\n";
            writer.write(_csvReadingMetaDataWithEmptyLines);
            writer.write(_csvReadingFileHeader);
            writer.write(_readingValuesWithEmptyLines);
            linesCount.put(LineType.LINES_ADDED.toString(), 11);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 6);
            _mockedReadingFiles.put(_csvReadingFileWithEmptyLines, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error when creating reading file with empty lines");
        }
        try (FileWriter writer = new FileWriter(_csvReadingFileNameWithMixedValues))
        {
            Map<String, Integer> linesCount = new HashMap<>();
            String _readingValuesWithMixedValues =
                    "01.02.2020;728;\n" +
                            "01.03.2020;;\n" +
                            ";737;\n" +
                            "03.03.2020;0;\"Zählertausch: neue Nummer 786523123\"\n";
            writer.write(_csvReadingMetaDataWithEmptyLines);
            writer.write(_csvReadingFileHeader);
            writer.write(_readingValuesWithMixedValues);
            linesCount.put(LineType.LINES_ADDED.toString(), 9);
            linesCount.put(LineType.LINES_AFTER_FORMAT.toString(), 7);
            _mockedReadingFiles.put(_csvReadingFileWithMixedValues, linesCount);
        } catch (IOException e)
        {
            throw new RuntimeException("Error when creating reading file with nixed values");
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
            assertEquals(_entriesInDirectory + _mockedCustomerFiles.size() + _mockedReadingFiles.size(), actualFileCount, "It should be the same amount of files counted");

        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }
    }

    @Test
    @Order(2)
    void verifyLineCountMatchesFirstInteger()
    {
        for (Map.Entry<File, Map<String, Integer>> entry : _mockedCustomerFiles.entrySet())
        {
            File file = entry.getKey();
            Map<String, Integer> lineCountMap = entry.getValue();

            int linesAdded = lineCountMap.get(LineType.LINES_ADDED.toString());
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

            assertEquals(linesAdded, actualLineCount,
                    "Line count mismatch for file: " + file.getName() +
                            " (Expected: " + linesAdded + ", Actual: " + actualLineCount + ")");
        }
        for (Map.Entry<File, Map<String, Integer>> entry : _mockedReadingFiles.entrySet())
        {
            File file = entry.getKey();
            Map<String, Integer> lineCountMap = entry.getValue();

            int linesAdded = lineCountMap.get(LineType.LINES_ADDED.toString());
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
            assertEquals(linesAdded, actualLineCount,
                    "line count mismatch for file: " + file.getName() +
                            " (Expected: " + linesAdded + ", Actual: " + actualLineCount + ")");
        }
    }

    @Test
    @Order(3)
    void removeEmptyLinesTest()
    {
        CsvFormatter formatter = new CsvFormatter();

        for (Map.Entry<File, Map<String, Integer>> entry : _mockedCustomerFiles.entrySet())
        {
            File originalFile = entry.getKey();
            Map<String, Integer> lineCountMap = entry.getValue();

            File formattedFile = formatter.formatFile(originalFile, ',');

            int actualLineCount = 0;
            try (Scanner scanner = new Scanner(formattedFile))
            {
                while (scanner.hasNextLine())
                {
                    scanner.nextLine();
                    actualLineCount++;
                }
            } catch (IOException e)
            {
                throw new RuntimeException("Error when trying to count lines in formatted file: " + formattedFile.getName(), e);
            }

            for (Map.Entry<String, Integer> integerEntry : lineCountMap.entrySet()) {
                int value = integerEntry.getValue();
            }

            int expectedLineCount = lineCountMap.get(LineType.LINES_AFTER_FORMAT.toString());
            assertEquals(expectedLineCount, actualLineCount,
                    "Line count mismatch for file: " + originalFile.getName() +
                            " (Expected: " + expectedLineCount + ", Actual: " + actualLineCount + ")");
        }
        for (Map.Entry<File, Map<String , Integer>> entry : _mockedReadingFiles.entrySet())
        {
            File originalFile = entry.getKey();
            Map<String, Integer> lineCountMap = entry.getValue();

            File formattedFile = formatter.formatFile(originalFile, ';');

            int actualLineCount = 0;
            try (Scanner scanner = new Scanner(formattedFile))
            {
                while (scanner.hasNextLine())
                {
                    scanner.nextLine();
                    actualLineCount++;
                }
            } catch (IOException e)
            {
                throw new RuntimeException("Error whn trying to count lines in formatted file: " + formattedFile.getName(), e);
            }

            int expectedLineCount = lineCountMap.get(LineType.LINES_AFTER_FORMAT.toString());
            assertEquals(expectedLineCount, actualLineCount, "Line count mismatch for file: " + originalFile.getName() +
                    " (Expected: " + expectedLineCount + ", Actual: " + actualLineCount + ")");
        }
    }


    @AfterAll
    static void afterAll()
    {
        for (File file : _mockedCustomerFiles.keySet())
        {
            if (file.exists())
            {
                boolean deleted = file.delete();
                if (!deleted)
                {
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
        _mockedCustomerFiles.clear();

        for (File file : _mockedReadingFiles.keySet())
        {
            if (file.exists())
            {
                boolean deleted = file.delete();
                if (!deleted)
                {
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
        _mockedCustomerFiles.clear();
    }
}
