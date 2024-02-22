package org.gnucash.api.read.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.numbers.FixedPointNumber;
import org.gnucash.api.generated.GncGncVendor;
import org.gnucash.api.generated.ObjectFactory;
import org.gnucash.api.read.GnucashFile;
import org.gnucash.api.read.GnucashGenerInvoice;
import org.gnucash.api.read.GnucashGenerJob;
import org.gnucash.api.read.GnucashVendor;
import org.gnucash.api.read.UnknownAccountTypeException;
import org.gnucash.api.read.aux.GCshAddress;
import org.gnucash.api.read.aux.GCshBillTerms;
import org.gnucash.api.read.aux.GCshTaxTable;
import org.gnucash.api.read.impl.aux.GCshAddressImpl;
import org.gnucash.api.read.impl.hlp.GnucashObjectImpl;
import org.gnucash.api.read.impl.spec.GnucashVendorJobImpl;
import org.gnucash.api.read.spec.GnucashJobInvoice;
import org.gnucash.api.read.spec.GnucashVendorBill;
import org.gnucash.api.read.spec.GnucashVendorJob;
import org.gnucash.api.read.spec.SpecInvoiceCommon;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorImpl extends GnucashObjectImpl 
                               implements GnucashVendor 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncGncVendor jwsdpPeer;

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
     * @param gcshFile the file to register under
     */
    @SuppressWarnings("exports")
    public GnucashVendorImpl(final GncGncVendor peer, final GnucashFile gcshFile) {
	super((peer.getVendorSlots() == null) ? new ObjectFactory().createSlotsType() : peer.getVendorSlots(), gcshFile);

	if (peer.getVendorSlots() == null) {
	    peer.setVendorSlots(getSlots());
	}

	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncGncVendor getJwsdpPeer() {
	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getID() {
	return new GCshID(jwsdpPeer.getVendorGuid().getValue());
    }

    /**
     * {@inheritDoc}
     */
    public String getNumber() {
	return jwsdpPeer.getVendorId();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
	return jwsdpPeer.getVendorName();
    }

    /**
     * {@inheritDoc}
     */
    public GCshAddress getAddress() {
	return new GCshAddressImpl(jwsdpPeer.getVendorAddr(), getGnucashFile());
    }

    /**
     * {@inheritDoc}
     */
    public String getNotes() {
	return jwsdpPeer.getVendorNotes();
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

    /**
     * {@inheritDoc}
     */
    public GCshID getTaxTableID() {
	GncGncVendor.VendorTaxtable vendTaxtable = jwsdpPeer.getVendorTaxtable();
	if (vendTaxtable == null) {
	    return null;
	}

	return new GCshID( vendTaxtable.getValue() );
    }

    /**
     * {@inheritDoc}
     */
    public GCshTaxTable getTaxTable() {
	GCshID id = getTaxTableID();
	if (id == null) {
	    return null;
	}
	return getGnucashFile().getTaxTableByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public GCshID getTermsID() {
	GncGncVendor.VendorTerms vendTerms = jwsdpPeer.getVendorTerms();
	if (vendTerms == null) {
	    return null;
	}

	return new GCshID( vendTerms.getValue() );
    }

    /**
     * {@inheritDoc}
     */
    public GCshBillTerms getTerms() {
	GCshID id = getTermsID();
	if (id == null) {
	    return null;
	}
	return getGnucashFile().getBillTermsByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     *
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     *  
     */
    @Override
    public int getNofOpenBills() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_direct(this).size();
    }

    // -------------------------------------

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     *  
     */
    public FixedPointNumber getExpensesGenerated(GnucashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
	    return getExpensesGenerated_direct();
	else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
	    return getExpensesGenerated_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     *  
     */
    public FixedPointNumber getExpensesGenerated_direct() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashVendorBill bllSpec : getPaidBills_direct()) {
//		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//		      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getExpensesGenerated_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the net sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     *  
     */
    public FixedPointNumber getExpensesGenerated_viaAllJobs() throws UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashJobInvoice bllSpec : getPaidBills_viaAllJobs()) {
//		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//		      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getExpensesGenerated_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     *  
     * @see #getExpensesGenerated(org.gnucash.api.read.GnucashGenerInvoice.ReadVariant)
     */
    public String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar) throws UnknownAccountTypeException {
	return getCurrencyFormat().format(getExpensesGenerated(readVar));

    }

    /**
     * @param lcl the locale to format for
     * @return formatted according to the given locale's currency-format
     * @throws UnknownAccountTypeException 
     *  
     * @see #getExpensesGenerated(org.gnucash.api.read.GnucashGenerInvoice.ReadVariant)
     */
    public String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar, final Locale lcl) throws UnknownAccountTypeException {
	return NumberFormat.getCurrencyInstance(lcl).format(getExpensesGenerated(readVar));
    }

    // -------------------------------------

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * 
     * @see #getOutstandingValue_direct()
     * @see #getOutstandingValue_viaAllJobs()
     */
    public FixedPointNumber getOutstandingValue(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException, UnknownAccountTypeException {
	if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
	    return getOutstandingValue_direct();
	else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
	    return getOutstandingValue_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue_viaAllJobs()
     */
    public FixedPointNumber getOutstandingValue_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashVendorBill bllSpec : getUnpaidBills_direct()) {
//            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//              GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue_direct()
     */
    public FixedPointNumber getOutstandingValue_viaAllJobs() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashJobInvoice bllSpec : getUnpaidBills_viaAllJobs()) {
//            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//              GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getID().equals(this.getID())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return Formatted according to the current locale's currency-format
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue(org.gnucash.api.read.GnucashGenerInvoice.ReadVariant)
     */
    public String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getCurrencyFormat().format(getOutstandingValue(readVar));
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     *  
     * @see #getOutstandingValue(org.gnucash.api.read.GnucashGenerInvoice.ReadVariant)
     */
    public String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar, final Locale lcl) throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return NumberFormat.getCurrencyInstance(lcl).format(getOutstandingValue(readVar));
    }

    // -----------------------------------------------------------------

    /**
     * @return the jobs that have this vendor associated with them.
     * @throws WrongInvoiceTypeException 
     */
    public java.util.Collection<GnucashVendorJob> getJobs() throws WrongInvoiceTypeException {

	List<GnucashVendorJob> retval = new ArrayList<GnucashVendorJob>();

	for ( GnucashGenerJob jobGener : getGnucashFile().getGenerJobs() ) {
	    if ( jobGener.getOwnerType() == GnucashGenerJob.TYPE_VENDOR ) {
		GnucashVendorJob jobSpec = new GnucashVendorJobImpl(jobGener);
		if ( jobSpec.getVendorID().equals(getID()) ) {
		    retval.add(jobSpec);
		}
	    }
	}

	return retval;
    }

    // -----------------------------------------------------------------

    @Override
    public Collection<GnucashGenerInvoice> getBills() throws WrongInvoiceTypeException {
	Collection<GnucashGenerInvoice> retval = new ArrayList<GnucashGenerInvoice>();

	for ( GnucashVendorBill invc : getGnucashFile().getBillsForVendor_direct(this) ) {
	    retval.add(invc);
	}
	
	for ( GnucashJobInvoice invc : getGnucashFile().getBillsForVendor_viaAllJobs(this) ) {
	    retval.add(invc);
	}
	
	return retval;
    }

    @Override
    public Collection<GnucashVendorBill> getPaidBills_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnucashFile().getPaidBillsForVendor_direct(this);
    }

    @Override
    public Collection<GnucashJobInvoice> getPaidBills_viaAllJobs() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnucashFile().getPaidBillsForVendor_viaAllJobs(this);
    }

    @Override
    public Collection<GnucashVendorBill> getUnpaidBills_direct() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_direct(this);
    }

    @Override
    public Collection<GnucashJobInvoice> getUnpaidBills_viaAllJobs() throws WrongInvoiceTypeException, UnknownAccountTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_viaAllJobs(this);
    }

    // ------------------------------------------------------------

    public static int getHighestNumber(GnucashVendor vend) {
	return ((GnucashFileImpl) vend.getGnucashFile()).getHighestVendorNumber();
    }

    public static String getNewNumber(GnucashVendor vend) {
	return ((GnucashFileImpl) vend.getGnucashFile()).getNewVendorNumber();
    }

    // -----------------------------------------------------------------

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("GnucashVendorImpl [");
	
	buffer.append("id=");
	buffer.append(getID());
	
	buffer.append(", number='");
	buffer.append(getNumber() + "'");
	
	buffer.append(", name='");
	buffer.append(getName() + "'");
	
	buffer.append("]");
	return buffer.toString();
    }
    
}
