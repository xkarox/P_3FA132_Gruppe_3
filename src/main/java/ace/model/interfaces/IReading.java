package ace.model.interfaces;

import java.time.LocalDate;

public interface IReading extends IId, IDbItem
{

   enum KindOfMeter {
      HEIZUNG, STROM, UNBEKANNT, WASSER
   }

   String getComment();

   ICustomer getCustomer();

   LocalDate getDateOfReading();

   KindOfMeter getKindOfMeter();

   Double getMeterCount();

   String getMeterId();

   Boolean getSubstitute();

   String printDateOfReading();

   void setComment(String comment);

   void setCustomer(ICustomer customer);

   void setDateOfReading(LocalDate dateOfReading);

   void setKindOfMeter(KindOfMeter kindOfMeter);

   void setMeterCount(double meterCount);

   void setMeterId(String meterId);

   void setSubstitute(boolean substitute);

}
