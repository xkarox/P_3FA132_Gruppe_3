package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private CustomerService _customerService;
    private DatabaseConnection _databaseConnection;

    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), ICustomer.Gender.M);
        this._databaseConnection = new DatabaseConnection();
        this._databaseConnection.openConnection(DbHelperService.loadProperties());
        this._customerService = new CustomerService(_databaseConnection);
    }
//unfinished
    @Test
    void updateTest() {
//        StringBuilder sb = new StringBuilder("INSERT INTO ").append(_testCustomer.getSerializedTableName())
//                .append("(id, firstName, lastName, birthDate, gender) VALUES (")
//                .append(this._testCustomer.getId()).append(",")
//                .append(this._testCustomer.getFirstName()).append(",")
//                .append(this._testCustomer.getLastName()).append(",")
//                .append(this._testCustomer.getBirthDate()).append(",")
//                .append(this._testCustomer.getGender()).append(",");
//        this._databaseConnection.executeSqlUpdateCommand(sb.toString());
//        this._customerService.add(this._testCustomer);
//
////        modify customer
//        this._testCustomer.setFirstName("Peter");
//        this._testCustomer.setLastName("Griffin");
//        this._testCustomer.setBirthDate(LocalDate.of(2000, 11, 2));
//        this._testCustomer.setGender(ICustomer.Gender.W);
////        update customer
//        `sb = new StringBuilder("INSERT INTO ").append(_testCustomer.getSerializedTableName())
//                .append("(id, firstName, lastName, birthDate, gender) VALUES (")
//                .append(this._testCustomer.getId()).append(",")
//                .append(this._testCustomer.getFirstName()).append(",")
//                .append(this._testCustomer.getLastName()).append(",")
//                .append(this._testCustomer.getBirthDate()).append(",")
//                .append(this._testCustomer.getGender()).append(",");
//        this._databaseConnection.executeSqlUpdateCommand(sb.toString());
////        check if customer is updated correctly
//        sb = new StringBuilder("SELECT * FROM ").append(this._testCustomer.getSerializedTableName())
//                .append(" WHERE id=").append(this._testCustomer.getId());
//        ResultSet customer = this._databaseConnection.executeSqlQueryCommand(sb.toString());
//        System.out.println(customer);
////        get Customer


    }

    @AfterEach
    void tearDown() {

    }

}
