package dev.hv.model.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.decorator.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

// Req. Nr.: 2
public class Reading implements IReading
{
    @JsonProperty("id")
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;

    @JsonProperty("comment")
    @IFieldInfo(fieldName = "comment", fieldType = String.class)
    private String _comment;

    @JsonProperty("customerId")
    @IFieldInfo(fieldName = "customerId", fieldType = String.class)
    private UUID _customerId;

    @JsonProperty("customer")
    private Customer _customer;

    @JsonProperty("dateOfReading")
    @IFieldInfo(fieldName = "dateOfReading", fieldType = LocalDate.class)
    private LocalDate _dateOfReading;

    @JsonProperty("kindOfMeter")
    @IFieldInfo(fieldName = "kindOfMeter", fieldType = int.class)
    private KindOfMeter _kindOfMeter;

    @JsonProperty("meterCount")
    @IFieldInfo(fieldName = "meterCount", fieldType = Double.class)
    private Double _meterCount;

    @JsonProperty("meterId")
    @IFieldInfo(fieldName = "meterId", fieldType = String.class)
    private String _meterId;

    @JsonProperty("substitute")
    @IFieldInfo(fieldName = "substitute", fieldType = Boolean.class)
    private Boolean _substitute;

    public Reading(){}

    public Reading(UUID id)
    {
        this._id = (id == null) ? UUID.randomUUID() : id;
    }

    public Reading(UUID id, String comment, UUID customerId, Customer customer
            , LocalDate dateOfReading, KindOfMeter kindOfMeter, Double meterCount
            , String meterId, Boolean substitute)
    {
        this._id = id;
        this._comment = comment;
        this._customerId = customerId;
        this._customer = customer;
        this._dateOfReading = dateOfReading;
        this._kindOfMeter = kindOfMeter;
        this._meterCount = meterCount;
        this._meterId = meterId;
        this._substitute = substitute;
    }

    @JsonIgnore
    @Nullable
    @Override
    public String getComment()
    {
        return this._comment;
    }

    @JsonIgnore
    @Nullable
    @Override
    public ICustomer getCustomer()
    {
        return this._customer;
    }

    public UUID getCustomerId() { return this._customerId; }

    @JsonIgnore
    @Nullable
    @Override
    public LocalDate getDateOfReading()
    {
        return this._dateOfReading;
    }

    @JsonIgnore
    @Nullable
    @Override
    public KindOfMeter getKindOfMeter()
    {
        return this._kindOfMeter;
    }

    @JsonIgnore
    @Override
    public Double getMeterCount()
    {
        return this._meterCount;
    }

    @JsonIgnore
    @Nullable
    @Override
    public String getMeterId()
    {
        return this._meterId;
    }

    @JsonIgnore
    @Override
    public Boolean getSubstitute()
    {
        return this._substitute;
    }

    @JsonIgnore
    @Override
    public String printDateOfReading()
    {
        return this._dateOfReading.toString();
    }

    @JsonIgnore
    @Override
    public void setComment(String comment)
    {
        this._comment = comment;
    }

    @JsonIgnore
    @Override
    public void setCustomer(ICustomer customer)
    {
        this._customer = (Customer) customer;
    }

    @JsonIgnore
    @Override
    public void setDateOfReading(LocalDate dateOfReading)
    {
        this._dateOfReading = dateOfReading;
    }

    @JsonIgnore
    @Override
    public void setKindOfMeter(KindOfMeter kindOfMeter)
    {
        this._kindOfMeter = kindOfMeter;
    }

    @JsonIgnore
    @Override
    public void setMeterCount(double meterCount)
    {
        this._meterCount = meterCount;
    }

    @JsonIgnore
    @Override
    public void setMeterId(String meterId)
    {
        this._meterId = meterId;
    }

    @JsonIgnore
    @Override
    public void setSubstitute(boolean substitute)
    {
        this._substitute = substitute;
    }

    @JsonIgnore
    @Override
    public UUID getId()
    {
        return this._id;
    }

    @JsonIgnore
    @Override
    public void setId(UUID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this._id = id;
    }

    @Override
    public IDbItem dbObjectFactory(Object... args)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse((String) args[3], formatter);

        this._id = UUID.fromString((String) args[0]);
        this._comment = (String) args[1];
        this._customerId = args[2] != null ? UUID.fromString((String) args[2]): null;
        this._dateOfReading = date;
        this._kindOfMeter = IReading.KindOfMeter.values()[(int) args[4]];
        this._meterCount = (Double) args[5];
        this._meterId = (String) args[6];
        this._substitute = (Boolean) args[7];
        return this;
    }

    @JsonIgnore
    @Override
    public String getSerializedStructure()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("id UUID PRIMARY KEY NOT NULL,");
        strBuilder.append("comment VARCHAR(120),");
        strBuilder.append("customerId UUID,");
        strBuilder.append("dateOfReading DATE NOT NULL,");
        strBuilder.append("kindOfMeter int NOT NULL,"); // Longest element in enum is 9 chars long
        strBuilder.append("meterCount REAL NOT NULL,");
        strBuilder.append("meterId VARCHAR(60) NOT NULL,"); // Check length
        strBuilder.append("substitute BOOLEAN NOT NULL");
//    strBuilder.append("FOREIGN KEY(customerId) REFERENCES customer(id)");
        return strBuilder.toString();
    }

    @JsonIgnore
    @Override
    public String getSerializedTableName()
    {
        return "reading";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Reading item = (Reading) obj;

        return Objects.equals(this.getId(), item.getId())
                && Objects.equals(this.getComment(), item.getComment())
                && Objects.equals(this.getDateOfReading(), item.getDateOfReading())
                && Objects.equals(this.getKindOfMeter(), item.getKindOfMeter())
                && Objects.equals(this.getMeterCount(), item.getMeterCount())
                && Objects.equals(this.getMeterId(), item.getMeterId())
                && Objects.equals(this.getSubstitute(), item.getSubstitute());
    }
}