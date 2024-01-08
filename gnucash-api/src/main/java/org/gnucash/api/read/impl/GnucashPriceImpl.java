package org.gnucash.api.read.impl;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

import org.gnucash.api.Const;
import org.gnucash.api.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.api.basetypes.complex.GCshCmdtyCurrNameSpace;
import org.gnucash.api.basetypes.complex.GCshCmdtyID;
import org.gnucash.api.basetypes.complex.GCshCurrID;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrIDException;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.generated.Price;
import org.gnucash.api.generated.Price.PriceCommodity;
import org.gnucash.api.generated.Price.PriceCurrency;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.GnucashCommodity;
import org.gnucash.api.read.GnucashFile;
import org.gnucash.api.read.GnucashPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashPriceImpl extends GnucashObjectImpl
                              implements GnucashPrice
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashPriceImpl.class);

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    protected static final DateTimeFormatter DATE_FORMAT_FALLBACK = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // -----------------------------------------------------------

    /**
     * The JWSDP-object we are wrapping.
     */
    protected final Price jwsdpPeer;

    protected ZonedDateTime dateTime;
    protected NumberFormat currencyFormat = null;

    // -----------------------------------------------------------

    /**
     * @param newPeer the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GnucashPriceImpl(final Price newPeer, final GnucashFile gncFile) {
	super(gncFile);

	this.jwsdpPeer = newPeer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public Price getJwsdpPeer() {
	return jwsdpPeer;
    }

    // -----------------------------------------------------------

    @Override
    public GCshID getID() {
	if ( jwsdpPeer.getPriceId() == null )
	    return null;
		    
	return new GCshID( jwsdpPeer.getPriceId().getValue() );
    }

    // ----------------------------
    
    @Override
    public GCshCmdtyCurrID getFromCmdtyCurrQualifID() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCommodity() == null )
	    return null;
		
	PriceCommodity cmdty = jwsdpPeer.getPriceCommodity();
	if ( cmdty.getCmdtySpace() == null ||
	     cmdty.getCmdtyId() == null )
	    return null;
		    
	GCshCmdtyCurrID result = new GCshCmdtyCurrID(cmdty.getCmdtySpace(), cmdty.getCmdtyId());
	    
	return result;
    }

    @Override
    public GCshCmdtyID getFromCommodityQualifID() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	GCshCmdtyCurrID cmdtyCurrID = getFromCmdtyCurrQualifID();
	return new GCshCmdtyID(cmdtyCurrID);
    }

    @Override
    public GCshCurrID getFromCurrencyQualifID() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	GCshCmdtyCurrID cmdtyCurrID = getFromCmdtyCurrQualifID();
	return new GCshCurrID(cmdtyCurrID);
    }

    @Override
    public GnucashCommodity getFromCommodity() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	GCshCmdtyID cmdtyID = getFromCommodityQualifID();
	GnucashCommodity cmdty = getGnucashFile().getCommodityByQualifID(cmdtyID);
	return cmdty;
    }
    
    @Override
    public String getFromCurrencyCode() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getFromCurrencyQualifID().getCurrency().getCurrencyCode();
    }

    @Override
    public GnucashCommodity getFromCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	GCshCurrID currID = getFromCurrencyQualifID(); 
	GnucashCommodity cmdty = getGnucashFile().getCommodityByQualifID(currID);
	return cmdty;
    }
    
    // ----------------------------
    
    @Override
    public GCshCurrID getToCurrencyQualifID() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
	
	GCshCurrID result = new GCshCurrID(curr.getCmdtySpace(), curr.getCmdtyId());
		    
	return result;
    }

    @Override
    public String getToCurrencyCode() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
	
	if ( ! curr.getCmdtySpace().equals(GCshCmdtyCurrNameSpace.CURRENCY) )
	    throw new InvalidCmdtyCurrTypeException();
	
	return curr.getCmdtyId();
    }

    @Override
    public GnucashCommodity getToCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( getToCurrencyQualifID() == null )
	    return null;
	
	GnucashCommodity cmdty = getGnucashFile().getCommodityByQualifID(getToCurrencyQualifID());
	
	return cmdty;
    }

    // ----------------------------
    
    /**
     * @return The currency-format to use for formating.
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    private NumberFormat getCurrencyFormat() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

//	// the currency may have changed
//	if ( ! getCurrencyQualifID().getType().equals(CmdtyCurrID.Type.CURRENCY) )
//	    throw new InvalidCmdtyCurrTypeException();
	    
	Currency currency = Currency.getInstance(getToCurrencyCode());
	currencyFormat.setCurrency(currency);

	return currencyFormat;
    }

    @Override
    public LocalDate getDate() {
	if ( jwsdpPeer.getPriceTime() == null )
	    return null;
	
	String dateStr = jwsdpPeer.getPriceTime().getTsDate();
	try {
	    return ZonedDateTime.parse(dateStr, DATE_FORMAT).toLocalDate();
	} catch (Exception e) {
	    LOGGER.error("unparsable date '" + dateStr + "' (1st try)!");
//	    IllegalStateException ex = new IllegalStateException("unparsable date '" + dateStr + "' (1st try)!");
//	    ex.initCause(e);
//	    throw ex;
	    try {
		return LocalDate.parse(dateStr, DATE_FORMAT_FALLBACK);
	    } catch (Exception e2) {
		LOGGER.error("unparsable date '" + dateStr + "' (2nd try)!");
		IllegalStateException ex2 = new IllegalStateException("unparsable date '" + dateStr + "' (2nd try)!");
		ex2.initCause(e2);
		throw ex2;
	    }
	}
    }

    @Override
    public Source getSource() {
	return Source.valueOff(getSourceStr());
    }

    public String getSourceStr() {
	if ( jwsdpPeer.getPriceSource() == null )
	    return null;
	
	return jwsdpPeer.getPriceSource();
    }

    @Override
    public Type getType() {
	return Type.valueOff(getTypeStr());
    }

    public String getTypeStr() {
	if ( jwsdpPeer.getPriceType() == null )
	    return null;
	
	return jwsdpPeer.getPriceType();
    }

    @Override
    public FixedPointNumber getValue() {
	if ( jwsdpPeer.getPriceValue() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getPriceValue());
    }

    @Override
    public String getValueFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getCurrencyFormat().format(getValue());
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	String result = "GCshPriceImpl [";
	
	result += "id=" + getID();
	
	try {
	    result += ", cmdty-qualif-id='" + getFromCmdtyCurrQualifID() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", cmdty-qualif-id=" + "ERROR";
	}
	
	try {
	    result += ", curr-qualif-id='" + getToCurrencyQualifID() + "'";
	} catch (Exception e) {
	    result += ", curr-qualif-id=" + "ERROR";
	}
	
	result += ", date=" + getDate(); 
	
	try {
	    result += ", value=" + getValueFormatted();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    result += ", value=" + "ERROR";
	}
	
	result += ", type=" + getType();
	result += ", source=" + getSource(); 

	result += "]"; 

	return result;
    }
    
}