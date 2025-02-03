package dev.hv.model;

import dev.hv.model.interfaces.IDbItem;

import java.time.LocalDate;

public interface ICustomer extends IId, IDbItem
{

    enum Gender
    {
        D, // divers
        M, // männlich
        U, // unbekannt
        W // weiblich
    }

    LocalDate getBirthDate();

    String getFirstName();

    Gender getGender();

    String getLastName();

    void setBirthDate(LocalDate birthDate);

    void setFirstName(String firstName);

    void setGender(Gender gender);

    void setLastName(String lastName);

}
