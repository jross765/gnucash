package org.gnucash.api.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.generated.GncV2;
import org.gnucash.api.generated.GncGncJob;
import org.gnucash.api.generated.GncGncJob.JobOwner;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.read.spec.GnucashJobInvoice;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;


/**
 * A job needs to be done. Once it or a part of it<br>
 * is done an invoice can be created and later be Paid by the customer<br>
 * of this job.
 * @see GnucashGenerInvoice
 * @see GnucashCustomer
 */
public interface GnucashGenerJob {

    public static final GCshOwner.Type TYPE_CUSTOMER = GCshOwner.Type.CUSTOMER;
    public static final GCshOwner.Type TYPE_VENDOR   = GCshOwner.Type.VENDOR;

    // -----------------------------------------------------------------

    @SuppressWarnings("exports")
    GncGncJob getJwsdpPeer();

    /**
     * The gnucash-file is the top-level class to contain everything.
     * 
     * @return the file we are associated with
     */
    GnucashFile getFile();

    // -----------------------------------------------------------------

    /**
     * @return the unique-id to identify this object with across name- and
     *         hirarchy-changes
     */
    GCshID getID();

    /**
     * @return true if the job is still active
     */
    boolean isActive();

    /**
     *
     * @return the user-defines number of this job (may contain non-digits)
     */
    String getNumber();

    /**
     *
     * @return the name the user gave to this job.
     */
    String getName();

    // ----------------------------

    /**
     * Not used.
     * 
     * @return CUSTOMETYPE_CUSTOMER
     */
    GCshOwner.Type getOwnerType();

    /**
     *
     * @return the id of the customer this job is from.
     * @see #getCustomer()
     */
    GCshID getOwnerID();
    
    // ---------------------------------------------------------------

    /**
     * Date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     * 
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    int getNofOpenInvoices() throws WrongInvoiceTypeException, UnknownAccountTypeException, IllegalArgumentException;

    /**
     * @return the sum of payments for invoices to this client
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getIncomeGenerated() throws UnknownAccountTypeException, IllegalArgumentException;

    /**
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     * @see #getIncomeGenerated() Formatted according to the current locale's
     *      currency-format
     */
    String getIncomeGeneratedFormatted() throws UnknownAccountTypeException, IllegalArgumentException;

    /**
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     * @see #getIncomeGenerated() Formatted according to the given locale's
     *      currency-format
     */
    String getIncomeGeneratedFormatted(Locale lcl) throws UnknownAccountTypeException, IllegalArgumentException;

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getOutstandingValue() throws UnknownAccountTypeException, IllegalArgumentException;

    /**
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     * @see #getOutstandingValue() Formatted according to the current locale's
     *      currency-format
     */
    String getOutstandingValueFormatted() throws UnknownAccountTypeException, IllegalArgumentException;

    /**
     *
     * @throws UnknownAccountTypeException 
     * @throws 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws WrongInvoiceTypeException
     * @see #getOutstandingValue() Formatted according to the given locale's
     *      currency-format
     */
    String getOutstandingValueFormatted(Locale lcl) throws UnknownAccountTypeException, IllegalArgumentException;

    // ---------------------------------------------------------------

    Collection<GnucashJobInvoice> getInvoices() throws WrongInvoiceTypeException, IllegalArgumentException;

    Collection<GnucashJobInvoice> getPaidInvoices() throws WrongInvoiceTypeException, UnknownAccountTypeException, IllegalArgumentException;

    Collection<GnucashJobInvoice> getUnpaidInvoices() throws WrongInvoiceTypeException, UnknownAccountTypeException, IllegalArgumentException;

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    JobOwner getOwnerPeerObj();

}
