package org.gnucash.api.write;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.gnucash.base.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.base.basetypes.complex.GCshCmdtyID;
import org.gnucash.base.basetypes.complex.GCshCurrID;
import org.gnucash.base.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.GnucashPrice;
import org.gnucash.api.write.hlp.GnucashWritableObject;
import org.gnucash.api.read.GnucashCommodity;

/**
 * Price that can be modified.
 * 
 * @see GnucashPrice
 */
public interface GnucashWritablePrice extends GnucashPrice, 
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
