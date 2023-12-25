package org.gnucash.api.write.aux;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.gnucash.api.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.api.basetypes.complex.GCshCmdtyID;
import org.gnucash.api.basetypes.complex.GCshCurrID;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.GnucashCommodity;
import org.gnucash.api.read.aux.GCshPrice;
import org.gnucash.api.write.GnucashWritableObject;

public interface GCshWritablePrice extends GCshPrice, 
                                           GnucashWritableObject
{

    void setFromCmdtyCurrQualifID(GCshCmdtyCurrID qualifID);

    void setFromCommodityQualifID(GCshCmdtyID qualifID);

    void setFromCurrencyQualifID(GCshCurrID qualifID);

    void setFromCommodity(GnucashCommodity cmdty);

    void setFromCurrencyCode(String code);

    void setFromCurrency(GnucashCommodity curr);
    
    // ----------------------------

    void setToCurrencyQualifID(GCshCmdtyCurrID qualifID) throws InvalidCmdtyCurrTypeException;

    void setToCurrencyQualifID(GCshCurrID qualifID);

    void setToCurrencyCode(String code);

    void setToCurrency(GnucashCommodity curr) throws InvalidCmdtyCurrTypeException;

    // ----------------------------

    void setDate(LocalDate date);

    void setDateTime(LocalDateTime dateTime);

    void setSource(Source src);

    void setType(Type type);

    void setValue(FixedPointNumber val);

}
