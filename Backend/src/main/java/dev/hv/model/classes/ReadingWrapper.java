package dev.hv.model.classes;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "ReadingWrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadingWrapper
{
    @XmlElement(name = "Readings")
    private List<Reading> readings;

    public ReadingWrapper()
    {
    }

    public ReadingWrapper(List<Reading> readings)
    {
        this.readings = readings;
    }


    public List<Reading> getReadings()
    {
        return readings;
    }

    public void setReadings(List<Reading> readings)
    {
        this.readings = readings;
    }
}
