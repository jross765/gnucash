package org.gnucash.read.impl;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import org.gnucash.basetypes.simple.GCshID;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashEmployee;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.UnknownAccountTypeException;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.read.spec.GnucashEmployeeVoucher;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashEmployeeImpl extends GnucashObjectImpl 
                                 implements GnucashEmployee 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashEmployeeImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncGncEmployee jwsdpPeer;

    /**
     * The currencyFormat to use for default-formating.<br/>
     * Please access only using {@link #getCurrencyFormat()}.
     *
     * @see #getCurrencyFormat()
     */
    private NumberFormat currencyFormat = null;

    // ---------------------------------------------------------------

    /**
     * @param peer    the JWSDP-object we are facading.
     * @param gncFile the file to register under
     */
    protected GnucashEmployeeImpl(final GncV2.GncBook.GncGncEmployee peer, final GnucashFile gncFile) {
	super((peer.getEmployeeSlots() == null) ? new ObjectFactory().createSlotsType() : peer.getEmployeeSlots(), gncFile);

	if (peer.getEmployeeSlots() == null) {
	    peer.setEmployeeSlots(getSlots());
	}

	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncV2.GncBook.GncGncEmployee getJwsdpPeer() {
	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getId() {
	return new GCshID(jwsdpPeer.getEmployeeGuid().getValue());
    }

    /**
     * {@inheritDoc}
     */
    public String getNumber() {
	return jwsdpPeer.getEmployeeId();
    }

    /**
     * {@inheritDoc}
     */
    public String getUserName() {
	return jwsdpPeer.getEmployeeUsername();
    }

    /**
     * {@inheritDoc}
     */
    public GCshAddress getAddress() {
	return new GCshAddressImpl(jwsdpPeer.getEmployeeAddr());
    }

    /**
     * {@inheritDoc}
     */
    public String getLanguage() {
	return jwsdpPeer.getEmployeeLanguage();
    }

    /**
     * {@inheritDoc}
     */
    public String getNotes() {
	// ::TODO ::CHECK
	// return jwsdpPeer.getEmployeeNotes();
	return "NOT IMPLEMENTED YET";
    }

    // ---------------------------------------------------------------

    /**
     * @return the currency-format to use if no locale is given.
     */
    protected NumberFormat getCurrencyFormat() {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

	return currencyFormat;
    }

    // ---------------------------------------------------------------

//    /**
//     * {@inheritDoc}
//     */
//    public String getTaxTableID() {
//	GncV2.GncBook.GncGncEmployee.EmployeeTaxtable emplTaxtable = jwsdpPeer.getEmployeeTaxtable();
//	if (emplTaxtable == null) {
//	    return null;
//	}
//
//	return emplTaxtable.getValue();
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public GCshTaxTable getTaxTable() {
//	String id = getTaxTableID();
//	if (id == null) {
//	    return null;
//	}
//	return getGnucashFile().getTaxTableByID(id);
//    }

    // ---------------------------------------------------------------

    /**
     * date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     *
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    @Override
    public int getNofOpenVouchers() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getGnucashFile().getUnpaidVouchersForEmployee_direct(this).size();
    }

    // -------------------------------------

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public FixedPointNumber getExpensesGenerated() throws UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getExpensesGenerated_direct();
    }

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public FixedPointNumber getExpensesGenerated_direct() throws UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashEmployeeVoucher vchSpec : getPaidVouchers_direct()) {
//		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_EMPLOYEE) ) {
//		      GnucashEmployeeVoucher vchSpec = new GnucashEmployeeVoucherImpl(invcGen); 
		GnucashEmployee empl = vchSpec.getEmployee();
		if (empl.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) vchSpec).getAmountWithoutTaxes());
		}
//            } // if vch type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getExpensesGenerated_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @see #getExpensesGenerated()
     */
    public String getExpensesGeneratedFormatted() throws UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getCurrencyFormat().format(getExpensesGenerated());

    }

    /**
     * @param lcl the locale to format for
     * @return formatted according to the given locale's currency-format
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @see #getExpensesGenerated()
     */
    public String getExpensesGeneratedFormatted(final Locale lcl) throws UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return NumberFormat.getCurrencyInstance(lcl).format(getExpensesGenerated());
    }

    // -------------------------------------

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public FixedPointNumber getOutstandingValue() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getOutstandingValue_direct();
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public FixedPointNumber getOutstandingValue_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashEmployeeVoucher vchSpec : getUnpaidVouchers_direct()) {
//            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//              GnucashEmployeeVoucher vchSpec = new GnucashEmployeeVoucherImpl(invcGen); 
		GnucashEmployee empl = vchSpec.getEmployee();
		if (empl.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) vchSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return Formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @see #getOutstandingValue()
     */
    public String getOutstandingValueFormatted() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getCurrencyFormat().format(getOutstandingValue());
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @see #getOutstandingValue() Formatted according to the given locale's
     *      currency-format
     */
    public String getOutstandingValueFormatted(final Locale lcl) throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return NumberFormat.getCurrencyInstance(lcl).format(getOutstandingValue());
    }

    // -----------------------------------------------------------------

    @Override
    public Collection<GnucashGenerInvoice> getVouchers() throws WrongInvoiceTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	Collection<GnucashGenerInvoice> retval = new LinkedList<GnucashGenerInvoice>();

	for ( GnucashEmployeeVoucher invc : getGnucashFile().getVouchersForEmployee_direct(this) ) {
	    retval.add(invc);
	}
	
	return retval;
    }

    @Override
    public Collection<GnucashEmployeeVoucher> getPaidVouchers_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getGnucashFile().getPaidVouchersForEmployee_direct(this);
    }

    @Override
    public Collection<GnucashEmployeeVoucher> getUnpaidVouchers_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException, NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
	return getGnucashFile().getUnpaidVouchersForEmployee_direct(this);
    }

    // -----------------------------------------------------------------

    public static int getHighestNumber(GnucashEmployee empl) {
	return empl.getGnucashFile().getHighestEmployeeNumber();
    }

    // -----------------------------------------------------------------

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GnucashEmployeeImpl:");
	buffer.append(" id: ");
	buffer.append(getId());
	buffer.append(" number: '");
	buffer.append(getNumber() + "'");
	buffer.append(" name: '");
	buffer.append(getUserName() + "'");
	buffer.append("]");
	return buffer.toString();
    }

}
