package org.gnucash.read.aux;

import java.time.LocalDate;

import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CommodityID;
import org.gnucash.currency.CurrencyID;
import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCommodity;

public interface GCshPrice {

    // Cf. https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gnc-pricedb.h
    public enum Source {
	EDIT_DLG,         // "user:price-editor"
	FQ,               // "Finance::Quote"
	USER_PRICE,       // "user:price"
	XFER_DLG_VAL,     // "user:xfer-dialog"
	SPLIT_REG,        // "user:split-register"
	SPLIT_IMPORT,     // "user:split-import"
	STOCK_SPLIT,      // "user:stock-split"
	STOCK_TRANSACTION,// "user:stock-transaction"
	INVOICE,          // "user:invoice-post"
	TEMP,             // "temporary"
	INVALID,          // "invalid"    
    }
	
    // ---------------------------------------------------------------
	
    String getId();

    // ----------------------------

    CmdtyCurrID getFromCmdtyCurrQualifId() throws InvalidCmdtyCurrTypeException;

    CommodityID getFromCommodityQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    CurrencyID getFromCurrencyQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    GnucashCommodity getFromCommodity() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException;

    String getFromCurrencyCode() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    GnucashCommodity getFromCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException;
    
    // ----------------------------

    CurrencyID getToCurrencyQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    String getToCurrencyCode() throws InvalidCmdtyCurrTypeException;

    GnucashCommodity getToCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException;

    // ----------------------------

    LocalDate getDate();

    String getSource();

    String getType();

    FixedPointNumber getValue();
    
    String getValueFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;
    
}