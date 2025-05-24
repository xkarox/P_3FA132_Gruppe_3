package dev.hv;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hv.csv.CsvParser;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.CustomerService;
import dev.hv.model.interfaces.ICustomer;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.interfaces.IDbItem;
import dev.provider.ServiceProvider;
import jakarta.xml.bind.JAXBException;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void deserializeCustomerCsvTest() throws ReflectiveOperationException, SQLException, IOException, JAXBException
    {
        String csv = "UUID;Anrede;Vorname;Nachname;Geburtsdatum\nec617965-88b4-4721-8158-ee36c38e4db3;Herr;Max;Müller;01.01.1990";
        List<Customer> expectedCustomers = Arrays.asList(new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Max", "Müller", LocalDate.of(1990, 1, 1), ICustomer.Gender.M));

        when(parserMock.getCustomerHeader()).thenReturn(Arrays.asList("UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"));
        when(parserMock.createCustomerFromCsv()).thenReturn(expectedCustomers);

        List<? extends IDbItem> result = Serializer.deserializeFile("text/plain", csv,  Customer.class);

        assertNotNull(result);
        assertEquals(expectedCustomers, result);
    }

    @Test
    void deserializeCustomerJsonTest() throws IOException, ReflectiveOperationException, SQLException, JAXBException
    {
        String json = "{\"customers\": [{\"id\": \"ec617965-88b4-4721-8158-ee36c38e4db3\", \"firstName\": \"Anna\", \"lastName\": \"Test\", \"gender\": \"W\", \"birthDate\": \"1962-02-21\"}]}";
        List<Customer> expectedCustomers = Arrays.asList(new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Anna", "Test", LocalDate.of(1962, 2 ,21), ICustomer.Gender.W));

        List<? extends IDbItem> result = Serializer.deserializeFile("application/json", json, Customer.class);

        assertNotNull(result);
        assertEquals(expectedCustomers, result);
    }

    @Test
    void deserializeReadingJsonTest() throws ReflectiveOperationException, SQLException, JAXBException, IOException
    {
        String xml = "{\n" +
                "  \"readings\": [\n" +
                "    {\n" +
                "      \"id\": \"b3bec254-dd57-47bc-85b0-01d84a85f509\",\n" +
                "      \"comment\": null,\n" +
                "      \"customer\": {\n" +
                "        \"id\": \"ec617965-88b4-4721-8158-ee36c38e4db3\",\n" +
                "        \"firstName\": \"Pumukel\",\n" +
                "        \"lastName\": \"Kobold\",\n" +
                "        \"birthDate\": null,\n" +
                "        \"gender\": \"M\"\n" +
                "      },\n" +
                "      \"dateOfReading\": \"2019-02-01\",\n" +
                "      \"kindOfMeter\": \"STROM\",\n" +
                "      \"meterCount\": 19471,\n" +
                "      \"meterId\": \"MST-af34569\",\n" +
                "      \"substitute\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M);

        Reading reading = new Reading();
        reading.setId(UUID.fromString("b3bec254-dd57-47bc-85b0-01d84a85f509"));
        reading.setCustomer(customer);
        reading.setDateOfReading(LocalDate.of(2019, 2, 1));
        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading.setMeterCount(19471.0);
        reading.setMeterId("MST-af34569");
        reading.setSubstitute(false);

        List<Reading> expectedReadings = Arrays.asList(reading);

        List<? extends IDbItem> result = Serializer.deserializeFile("application/json", xml, Reading.class);

        assertNotNull(result);
        assertEquals(expectedReadings, result);
    }

    @Test
    void deserializeCustomerXmlTest() throws ReflectiveOperationException, SQLException, JAXBException, IOException
    {
        String xml = "<CustomerWrapper>\n" +
                "    <Customers>\n" +
                "        <Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "        <FirstName>Anna</FirstName>\n" +
                "        <LastName>Test</LastName>\n" +
                "        <Gender>W</Gender>\n" +
                "        <BirthDate>1962-02-21</BirthDate>\n" +
                "    </Customers>\n" +
                "</CustomerWrapper>";
        List<Customer> expectedCustomers = Arrays.asList(new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Anna", "Test", LocalDate.of(1962, 2 ,21), ICustomer.Gender.W));

        List<? extends IDbItem> result = Serializer.deserializeFile("application/xml", xml, Customer.class);

        assertNotNull(result);
        assertEquals(expectedCustomers, result);
    }

    @Test
    void deserializeReadingXmlTest() throws ReflectiveOperationException, SQLException, JAXBException, IOException
    {
        String xml = "<ReadingWrapper>\n" +
                "<Readings>\n" +
                "<Id>b3bec254-dd57-47bc-85b0-01d84a85f509</Id>\n" +
                "<Customer>\n" +
                "<Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "<FirstName>Pumukel</FirstName>\n" +
                "<LastName>Kobold</LastName>\n" +
                "<Gender>M</Gender>\n" +
                "</Customer>\n" +
                "<DateOfReading>2019-02-01</DateOfReading>\n" +
                "<KindOfMeter>STROM</KindOfMeter>\n" +
                "<MeterCount>19471.0</MeterCount>\n" +
                "<MeterId>MST-af34569</MeterId>\n" +
                "<Substitute>false</Substitute>\n" +
                "</Readings>\n" +
                "</ReadingWrapper>";

        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M);

        Reading reading = new Reading();
        reading.setId(UUID.fromString("b3bec254-dd57-47bc-85b0-01d84a85f509"));
        reading.setCustomer(customer);
        reading.setDateOfReading(LocalDate.of(2019, 2, 1));
        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading.setMeterCount(19471.0);
        reading.setMeterId("MST-af34569");
        reading.setSubstitute(false);
        List<Reading> expectedReadings = Arrays.asList(reading);

        List<? extends IDbItem> result = Serializer.deserializeFile("application/xml", xml, Reading.class);

        assertNotNull(result);
        assertEquals(expectedReadings, result);

    }

    @Test
    void deserializeDefaultReadingWaterTest() throws ReflectiveOperationException, SQLException, IOException, JAXBException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in m³;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeFile("text/plain", csv, Reading.class);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.WASSER);

    }

    @Test
    void deserializeCustomReadingWaterTest() throws ReflectiveOperationException, SQLException, JAXBException, IOException
    {
        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"));

        String customReadingHeader = "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n";
        String customReadingContent = "01.10.2020;63.0;null;ec617965-88b4-4721-8158-ee36c38e4db3;WASSER;786523123;false;\n";

        String csv = customReadingHeader + customReadingContent;

        List<? extends IDbItem> readings = Serializer.deserializeFile("text/plain", csv, Reading.class);


        Reading reading1 = new Reading();
        reading1.setDateOfReading(LocalDate.of(2020,10,1));
        reading1.setMeterCount(63.0);
        reading1.setComment(null);
        reading1.setCustomer(customer);
        reading1.setKindOfMeter(IReading.KindOfMeter.WASSER);
        reading1.setMeterId("786523123");
        reading1.setSubstitute(false);

        List<Reading> expectedReadings = List.of(reading1);

        Reading expectedReading = expectedReadings.getFirst();
        Reading actualReading = (Reading) readings.getFirst();

        assertEquals(expectedReading.getDateOfReading(), actualReading.getDateOfReading());
        assertEquals(expectedReading.getMeterCount(), actualReading.getMeterCount());
        assertEquals(actualReading.getComment(), null);
        assertEquals(expectedReading.getCustomer(), actualReading.getCustomer());
        assertEquals(expectedReading.getKindOfMeter(), actualReading.getKindOfMeter());
        assertEquals(expectedReading.getMeterId(), actualReading.getMeterId());
        assertEquals(expectedReading.getSubstitute(), actualReading.getSubstitute());
    }

    @Test
    void deserializeDefaultReadingHeatTest() throws ReflectiveOperationException, SQLException, IOException, JAXBException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in MWh;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeFile("text/plain", csv, Reading.class);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getCustomerId(), UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"));
        assertEquals(reading.getMeterId(), "112233");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.HEIZUNG);
    }

    @Test
    void deserializeDefaultReadingElectricityTest() throws ReflectiveOperationException, SQLException, IOException, JAXBException
    {
        String metaData = "Kunde;ec617965-88b4-4721-8158-ee36c38e4db3\n" +
                "Zählernummer;112233\n";
        String csv = metaData + "Datum;Zählerstand in kWh;Kommentar\n25.03.2024;100;Test";
        List<?> result = Serializer.deserializeFile("text/plain", csv, Reading.class);

        Reading reading = (Reading) result.get(0);

        assertNotNull(reading);
        assertEquals(reading.getDateOfReading().toString(), "2024-03-25");
        assertEquals(reading.getCustomerId(), UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"));
        assertEquals(reading.getMeterId(), "112233");
        assertEquals(reading.getComment(), "Test");
        assertEquals(reading.getMeterCount(), 100);
        assertEquals(reading.getKindOfMeter(), IReading.KindOfMeter.STROM);
    }

    @Test
    void serializeIntoCustomersXmlTest() throws JAXBException
    {
        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M);

        List<Customer> customers = Arrays.asList(customer);
        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<CustomerWrapper>\n" +
                "    <Customers>\n" +
                "        <Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "        <FirstName>Pumukel</FirstName>\n" +
                "        <LastName>Kobold</LastName>\n" +
                "        <Gender>M</Gender>\n" +
                "    </Customers>\n" +
                "</CustomerWrapper>\n";

        String xml = Serializer.serializeIntoXml(customers);

        assertEquals(expectedXml, xml);
    }

    @Test
    void serializeIntoReadingsXmlTest() throws JAXBException
    {
        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M);

        Reading reading = new Reading();
        reading.setId(UUID.fromString("b3bec254-dd57-47bc-85b0-01d84a85f509"));
        reading.setCustomer(customer);
        reading.setDateOfReading(LocalDate.of(2019, 2, 1));
        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading.setMeterCount(19471.0);
        reading.setMeterId("MST-af34569");
        reading.setSubstitute(false);
        List<Reading> readings = Arrays.asList(reading);

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ReadingWrapper>\n" +
                "    <Readings>\n" +
                "        <Id>b3bec254-dd57-47bc-85b0-01d84a85f509</Id>\n" +
                "        <Customer>\n" +
                "            <Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "            <FirstName>Pumukel</FirstName>\n" +
                "            <LastName>Kobold</LastName>\n" +
                "            <Gender>M</Gender>\n" +
                "        </Customer>\n" +
                "        <DateOfReading>2019-02-01</DateOfReading>\n" +
                "        <KindOfMeter>STROM</KindOfMeter>\n" +
                "        <MeterCount>19471.0</MeterCount>\n" +
                "        <MeterId>MST-af34569</MeterId>\n" +
                "        <Substitute>false</Substitute>\n" +
                "    </Readings>\n" +
                "</ReadingWrapper>" +
                "\n";

        String xml = Serializer.serializeIntoXml(readings);

        assertEquals(expectedXml, xml);
    }

    @Test
    void serializeIntoCustomersCsvTest()
    {
        List<Customer> customers = List.of(new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M));

        String data = Serializer.serializeIntoCsv(customers);
        String expectedCsv = "ec617965-88b4-4721-8158-ee36c38e4db3;M;Pumukel;Kobold;null\n";

        assertEquals(expectedCsv, data);
    }

    @Test
    void serializeIntoCustomersCsvNullTest()
    {
        List<Customer> customers = List.of(new Customer(null, null, null, null, ICustomer.Gender.M));

        String data = Serializer.serializeIntoCsv(customers);
        String expectedCsv = "null;M;null;null;null\n";

        assertEquals(expectedCsv, data);
    }

    @Test
    void serializeIntoReadingsCsvTest()
    {
        Customer customer = new Customer(UUID.fromString("ec617965-88b4-4721-8158-ee36c38e4db3"), "Pumukel", "Kobold", null, ICustomer.Gender.M);

        Reading reading = new Reading();
        reading.setId(UUID.fromString("b3bec254-dd57-47bc-85b0-01d84a85f509"));
        reading.setCustomer(customer);
        reading.setDateOfReading(LocalDate.of(2019, 2, 1));
        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading.setMeterCount(19471.0);
        reading.setMeterId("MST-af34569");
        reading.setSubstitute(false);
        List<Reading> readings = Arrays.asList(reading);

        String data = Serializer.serializeIntoCsv(readings);
        String expectedCsv = "01.02.2019;19471.0;null;ec617965-88b4-4721-8158-ee36c38e4db3;STROM;MST-af34569;false\n";

        assertEquals(expectedCsv, data);
    }
}
