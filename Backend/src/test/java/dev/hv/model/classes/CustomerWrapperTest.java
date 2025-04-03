package dev.hv.model.classes;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerWrapperTest
{
    @Test
    void defaultConstructorTest() {
        CustomerWrapper wrapper = new CustomerWrapper();

        assertNotNull(wrapper);
        assertNull(wrapper.getCustomers());
    }

    @Test
    void parameterizedConstructorTest() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();

        List<Customer> customers = Arrays.asList(customer1, customer2);
        CustomerWrapper wrapper = new CustomerWrapper(customers);

        assertNotNull(wrapper.getCustomers());
        assertEquals(2, wrapper.getCustomers().size());
        assertEquals(customer1, wrapper.getCustomers().get(0));
        assertEquals(customer2, wrapper.getCustomers().get(1));
    }

    @Test
    void setCustomerTest() {
        CustomerWrapper wrapper = new CustomerWrapper();
        Customer customer1 = new Customer(UUID.randomUUID());
        Customer customer2 = new Customer(UUID.randomUUID());
        List<Customer> newCustomers = Arrays.asList(customer1, customer2);

        wrapper.setCustomers(newCustomers);

        assertNotNull(wrapper.getCustomers());
        assertEquals(2, wrapper.getCustomers().size());
        assertEquals(customer1, wrapper.getCustomers().get(0));
        assertEquals(customer2, wrapper.getCustomers().get(1));


    }
}
