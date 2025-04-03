package dev.hv.model.classes;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "CustomerWrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerWrapper {

    @XmlElement(name = "Customers")
    private List<Customer> customers;

    public CustomerWrapper() {}

    public CustomerWrapper(List<Customer> customers) {
        this.customers = customers;
    }


    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
