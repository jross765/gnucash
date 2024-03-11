package org.gnucash.api.read.impl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.gnucash.api.Const;
import org.gnucash.api.generated.GncGncInvoice;
import org.gnucash.api.generated.GncGncInvoice.InvoiceOwner;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashFile;
import org.gnucash.api.read.GnuCashGenerInvoice;
import org.gnucash.api.read.GnuCashGenerInvoiceEntry;
import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.read.impl.aux.GCshTaxedSumImpl;
import org.gnucash.api.read.impl.hlp.GnuCashObjectImpl;
import org.gnucash.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.gnucash.api.read.impl.spec.GnuCashJobInvoiceImpl;
import org.gnucash.api.read.spec.GnuCashJobInvoice;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.base.numbers.FixedPointNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GnuCashInvoice that uses JWSDP.
 */
public class GnuCashGenerInvoiceImpl extends GnuCashObjectImpl
									 implements GnuCashGenerInvoice 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GnuCashGenerInvoiceImpl.class);

	protected static final DateTimeFormatter DATE_OPENED_FORMAT       = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
	protected static final DateTimeFormatter DATE_OPENED_FORMAT_BOOK  = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
	protected static final DateTimeFormatter DATE_OPENED_FORMAT_PRINT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// ::TODO Outdated
	// Cf.
	// https://stackoverflow.com/questions/10649782/java-cannot-format-given-object-as-a-date
	protected static final DateFormat DATE_OPENED_FORMAT_1 = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);
	protected static final DateFormat DATE_POSTED_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

	// -----------------------------------------------------------------

	/**
	 * the JWSDP-object we are facading.
	 */
	protected final GncGncInvoice jwsdpPeer;

	// ------------------------------

	/**
	 * @see GnuCashGenerInvoice#getDateOpened()
	 */
	protected ZonedDateTime dateOpened;

	/**
	 * @see GnuCashGenerInvoice#getDatePosted()
	 */
	protected ZonedDateTime datePosted;

	/**
	 * The entries of this invoice.
	 */
	protected List<GnuCashGenerInvoiceEntry> entries = new ArrayList<GnuCashGenerInvoiceEntry>();

	/**
	 * The transactions that are paying for this invoice.
	 */
	private final List<GnuCashTransaction> payingTransactions = new ArrayList<GnuCashTransaction>();

	// ------------------------------

	/**
	 * @see #getDateOpenedFormatted()
	 * @see #getDatePostedFormatted()
	 */
	private DateFormat dateFormat = null;

	/**
	 * The currencyFormat to use for default-formating.<br/>
	 * Please access only using {@link #getCurrencyFormat()}.
	 * 
	 * @see #getCurrencyFormat()
	 */
	private NumberFormat currencyFormat = null;

	// -----------------------------------------------------------------

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @param gcshFile the file to register under
	 */
	@SuppressWarnings("exports")
	public GnuCashGenerInvoiceImpl(final GncGncInvoice peer, final GnuCashFile gcshFile) {
		super(gcshFile);

//		if ( peer.getInvoiceSlots() == null ) {
//			peer.setInvoiceSlots(new ObjectFactory().createSlotsType());
//		}

		this.jwsdpPeer = peer;
	}

	// Copy-constructor
	public GnuCashGenerInvoiceImpl(final GnuCashGenerInvoice invc) {
		super(invc.getGnuCashFile());

//		if ( invc.getJwsdpPeer().getInvoiceSlots() == null ) {
//			invc.getJwsdpPeer().setInvoiceSlots(new ObjectFactory().createSlotsType());
//		}

		this.jwsdpPeer = invc.getJwsdpPeer();

		for ( GnuCashGenerInvoiceEntry entr : invc.getGenerEntries() ) {
			addGenerEntry(entr);
		}
	}

//	// -----------------------------------------------------------------
//
//	public GnuCashObjectImpl getGnuCashObject() {
//		return helper;
//	}
//
	// -----------------------------------------------------------

	@Override
	public String getURL() {
		return getUserDefinedAttribute(Const.SLOT_KEY_ASSOC_URI);
	}

	// -----------------------------------------------------------------

	@Override
	public String getUserDefinedAttribute(final String name) {
		return HasUserDefinedAttributesImpl
				.getUserDefinedAttributeCore(jwsdpPeer.getInvoiceSlots(), name);
	}

	@Override
	public List<String> getUserDefinedAttributeKeys() {
		return HasUserDefinedAttributesImpl
				.getUserDefinedAttributeKeysCore(jwsdpPeer.getInvoiceSlots());
	}

	// -----------------------------------------------------------------

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isCustInvcFullyPaid() {
		return !isNotCustInvcFullyPaid();
	}

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isNotCustInvcFullyPaid() {
		return getCustInvcAmountWithTaxes().isGreaterThan(getCustInvcAmountPaidWithTaxes(), Const.DIFF_TOLERANCE);
	}

	// ------------------------------

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isVendBllFullyPaid() {
		return !isNotVendBllFullyPaid();
	}

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isNotVendBllFullyPaid() {
		return getVendBllAmountWithTaxes().isGreaterThan(getVendBllAmountPaidWithTaxes(), Const.DIFF_TOLERANCE);
	}

	// ------------------------------

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isEmplVchFullyPaid() {
		return !isNotEmplVchFullyPaid();
	}

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isNotEmplVchFullyPaid() {
		return getEmplVchAmountWithTaxes().isGreaterThan(getEmplVchAmountPaidWithTaxes(), Const.DIFF_TOLERANCE);
	}

	// ------------------------------

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isJobInvcFullyPaid() {
		return !isNotInvcJobFullyPaid();
	}

	/**
	 * @return getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes())
	 * 
	 * @see GnuCashGenerInvoice#isNotCustInvcFullyPaid()
	 */
	public boolean isNotInvcJobFullyPaid() {
		return getJobInvcAmountWithTaxes().isGreaterThan(getJobInvcAmountPaidWithTaxes(), Const.DIFF_TOLERANCE);
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void addPayingTransaction(final GnuCashTransactionSplit trans) {
		payingTransactions.add(trans.getTransaction());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTransaction(final GnuCashTransaction trans) {
		//

	}

	/**
	 * {@inheritDoc}
	 */
	public List<GnuCashTransaction> getPayingTransactions() {
		return payingTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshID getPostAccountID() {
		try {
			return new GCshID(jwsdpPeer.getInvoicePostacc().getValue());
		} catch (NullPointerException exc) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshID getPostTransactionID() {
		try {
			return new GCshID(jwsdpPeer.getInvoicePosttxn().getValue());
		} catch (NullPointerException exc) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public GnuCashAccount getPostAccount() {
		if ( getPostAccountID() == null ) {
			return null;
		}
		return getGnuCashFile().getAccountByID(getPostAccountID());
	}

	/**
	 * @return the transaction that transferes the money from the customer to the
	 *         account for money you are to get and the one you owe the taxes.
	 */
	public GnuCashTransaction getPostTransaction() {
		if ( getPostTransactionID() == null ) {
			return null;
		}
		return getGnuCashFile().getTransactionByID(getPostTransactionID());
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getCustInvcAmountUnpaidWithTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		return ((FixedPointNumber) getCustInvcAmountWithTaxes().clone()).subtract(getCustInvcAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getCustInvcAmountPaidWithTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		FixedPointNumber takenFromReceivableAccount = new FixedPointNumber();
		for ( GnuCashTransaction trx : getPayingTransactions() ) {
			for ( GnuCashTransactionSplit split : trx.getSplits() ) {
				if ( split.getAccount().getType() == GnuCashAccount.Type.RECEIVABLE ) {
					if ( !split.getValue().isPositive() ) {
						takenFromReceivableAccount.subtract(split.getValue());
					}
				}
			} // split
		} // trx

		return takenFromReceivableAccount;
	}

	@Override
	public FixedPointNumber getCustInvcAmountPaidWithoutTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getCustInvcSumExclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getCustInvcAmountWithTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		FixedPointNumber retval = new FixedPointNumber();

		// TODO: we should sum them without taxes grouped by tax% and
		// multiply the sums with the tax% to be calculating
		// correctly

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getCustInvcSumInclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getCustInvcAmountWithoutTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getCustInvcSumExclTaxes());
			}
		}

		return retval;
	}

	// ------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustInvcAmountUnpaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getCustInvcAmountUnpaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustInvcAmountPaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getCustInvcAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustInvcAmountPaidWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getCustInvcAmountPaidWithoutTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustInvcAmountWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getCustInvcAmountWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCustInvcAmountWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getCustInvcAmountWithoutTaxes());
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getVendBllAmountUnpaidWithTaxes() {

		// System.err.println("debug: GnuCashInvoiceImpl.getAmountUnpaid(): "
		// + "getBillAmountUnpaid()="+getBillAmountWithoutTaxes()+"
		// getBillAmountPaidWithTaxes()="+getAmountPaidWithTaxes() );

		return ((FixedPointNumber) getVendBllAmountWithTaxes().clone()).subtract(getVendBllAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getVendBllAmountPaidWithTaxes() {

		FixedPointNumber takenFromPayableAccount = new FixedPointNumber();
		for ( GnuCashTransaction trx : getPayingTransactions() ) {
			for ( GnuCashTransactionSplit split : trx.getSplits() ) {
				if ( split.getAccount().getType() == GnuCashAccount.Type.PAYABLE ) {
					if ( split.getValue().isPositive() ) {
						takenFromPayableAccount.add(split.getValue());
					}
				}
			} // split
		} // trx

		// System.err.println("getBillAmountPaidWithTaxes="+takenFromPayableAccount.doubleValue());

		return takenFromPayableAccount;
	}

	@Override
	public FixedPointNumber getVendBllAmountPaidWithoutTaxes() {
		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getVendBllSumExclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getVendBllAmountWithTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		// TODO: we should sum them without taxes grouped by tax% and
		// multiply the sums with the tax% to be calculating
		// correctly

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getVendBllSumInclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getVendBllAmountWithoutTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getVendBllSumExclTaxes());
			}
		}

		return retval;
	}

	// ------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVendBllAmountUnpaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getVendBllAmountUnpaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVendBllAmountPaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getVendBllAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVendBllAmountPaidWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getVendBllAmountPaidWithoutTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVendBllAmountWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getVendBllAmountWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVendBllAmountWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getVendBllAmountWithoutTaxes());
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getEmplVchAmountUnpaidWithTaxes() {

		// System.err.println("debug: GnuCashInvoiceImpl.getAmountUnpaid(): "
		// + "getVoucherAmountUnpaid()="+getVoucherAmountWithoutTaxes()+"
		// getVoucherAmountPaidWithTaxes()="+getAmountPaidWithTaxes() );

		return ((FixedPointNumber) getEmplVchAmountWithTaxes().clone()).subtract(getEmplVchAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getEmplVchAmountPaidWithTaxes() {

		FixedPointNumber takenFromPayableAccount = new FixedPointNumber();
		for ( GnuCashTransaction trx : getPayingTransactions() ) {
			for ( GnuCashTransactionSplit split : trx.getSplits() ) {
				if ( split.getAccount().getType() == GnuCashAccount.Type.PAYABLE ) {
					if ( split.getValue().isPositive() ) {
						takenFromPayableAccount.add(split.getValue());
					}
				}
			} // split
		} // trx

		// System.err.println("getVoucherAmountPaidWithTaxes="+takenFromPayableAccount.doubleValue());

		return takenFromPayableAccount;
	}

	@Override
	public FixedPointNumber getEmplVchAmountPaidWithoutTaxes() {
		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getEmplVchSumExclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getEmplVchAmountWithTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		// TODO: we should sum them without taxes grouped by tax% and
		// multiply the sums with the tax% to be calculating
		// correctly

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getEmplVchSumInclTaxes());
			}
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FixedPointNumber getEmplVchAmountWithoutTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				retval.add(entry.getEmplVchSumExclTaxes());
			}
		}

		return retval;
	}

	// ------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEmplVchAmountUnpaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getEmplVchAmountUnpaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEmplVchAmountPaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getEmplVchAmountPaidWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEmplVchAmountPaidWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getEmplVchAmountPaidWithoutTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEmplVchAmountWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getEmplVchAmountWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEmplVchAmountWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getEmplVchAmountWithoutTaxes());
	}

	// ---------------------------------------------------------------

	/**
	 * @return what the customer must still pay (incl. taxes)
	 */
	@Override
	public FixedPointNumber getJobInvcAmountUnpaidWithTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_CUSTOMER) )
			return getCustInvcAmountUnpaidWithTaxes();
		else if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_VENDOR) )
			return getVendBllAmountUnpaidWithTaxes();

		return null; // Compiler happy
	}

	/**
	 * @return what the customer has already pay (incl. taxes)
	 */
	@Override
	public FixedPointNumber getJobInvcAmountPaidWithTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_CUSTOMER) )
			return getCustInvcAmountPaidWithTaxes();
		else if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_VENDOR) )
			return getVendBllAmountPaidWithTaxes();

		return null; // Compiler happy
	}

	/**
	 * @return what the customer has already pay (incl. taxes)
	 */
	@Override
	public FixedPointNumber getJobInvcAmountPaidWithoutTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_CUSTOMER) )
			return getCustInvcAmountPaidWithoutTaxes();
		else if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_VENDOR) )
			return getVendBllAmountPaidWithoutTaxes();

		return null; // Compiler happy
	}

	/**
	 * @return what the customer needs to pay in total (incl. taxes)
	 */
	@Override
	public FixedPointNumber getJobInvcAmountWithTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_CUSTOMER) )
			return getCustInvcAmountWithTaxes();
		else if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_VENDOR) )
			return getVendBllAmountWithTaxes();

		return null; // Compiler happy
	}

	/**
	 * @return what the customer needs to pay in total (excl. taxes)
	 */
	@Override
	public FixedPointNumber getJobInvcAmountWithoutTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_CUSTOMER) )
			return getCustInvcAmountWithoutTaxes();
		else if ( jobInvc.getJobType().equals(GnuCashGenerJob.TYPE_VENDOR) )
			return getVendBllAmountWithoutTaxes();

		return null; // Compiler happy
	}

// ----------------------------

	/**
	 * Formating uses the default-locale's currency-format.
	 * 
	 * @return what the customer must still pay (incl. taxes)
	 */
	@Override
	public String getJobInvcAmountUnpaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getJobInvcAmountUnpaidWithTaxes());
	}

	/**
	 * @return what the customer has already pay (incl. taxes)
	 */
	@Override
	public String getJobInvcAmountPaidWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getJobInvcAmountPaidWithTaxes());
	}

	/**
	 * @return what the customer has already pay (incl. taxes)
	 */
	@Override
	public String getJobInvcAmountPaidWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getJobInvcAmountPaidWithoutTaxes());
	}

	/**
	 * Formating uses the default-locale's currency-format.
	 * 
	 * @return what the customer needs to pay in total (incl. taxes)
	 */
	@Override
	public String getJobInvcAmountWithTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getJobInvcAmountWithTaxes());
	}

	/**
	 * @return what the customer needs to pay in total (excl. taxes)
	 */
	@Override
	public String getJobInvcAmountWithoutTaxesFormatted() {
		return this.getCurrencyFormat().format(this.getJobInvcAmountWithoutTaxes());
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GCshTaxedSumImpl[] getCustInvcTaxes() {

		if ( getType() != TYPE_CUSTOMER && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		List<GCshTaxedSumImpl> taxedSums = new ArrayList<GCshTaxedSumImpl>();

		invoiceentries: for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				FixedPointNumber taxPerc = entry.getCustInvcApplicableTaxPercent();

				for ( GCshTaxedSumImpl taxedSum2 : taxedSums ) {
					GCshTaxedSumImpl taxedSum = taxedSum2;
					if ( taxedSum.getTaxpercent().equals(taxPerc) ) {
						taxedSum.setTaxsum(taxedSum.getTaxsum()
								.add(entry.getCustInvcSumInclTaxes().subtract(entry.getCustInvcSumExclTaxes())));
						continue invoiceentries;
					}
				}

				GCshTaxedSumImpl taxedSum = new GCshTaxedSumImpl(taxPerc,
						entry.getCustInvcSumInclTaxes().subtract(entry.getCustInvcSumExclTaxes()));
				taxedSums.add(taxedSum);
			} // type
		} // for

		return taxedSums.toArray(new GCshTaxedSumImpl[taxedSums.size()]);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GCshTaxedSumImpl[] getVendBllTaxes() {

		if ( getType() != TYPE_VENDOR && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		List<GCshTaxedSumImpl> taxedSums = new ArrayList<GCshTaxedSumImpl>();

		invoiceentries: for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				FixedPointNumber taxPerc = entry.getVendBllApplicableTaxPercent();

				for ( GCshTaxedSumImpl taxedSum2 : taxedSums ) {
					GCshTaxedSumImpl taxedSum = taxedSum2;
					if ( taxedSum.getTaxpercent().equals(taxPerc) ) {
						taxedSum.setTaxsum(taxedSum.getTaxsum()
								.add(entry.getVendBllSumInclTaxes().subtract(entry.getVendBllSumExclTaxes())));
						continue invoiceentries;
					}
				}

				GCshTaxedSumImpl taxedSum = new GCshTaxedSumImpl(taxPerc,
						entry.getVendBllSumInclTaxes().subtract(entry.getVendBllSumExclTaxes()));
				taxedSums.add(taxedSum);
			} // type
		} // for

		return taxedSums.toArray(new GCshTaxedSumImpl[taxedSums.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GCshTaxedSumImpl[] getEmplVchTaxes() {

		if ( getType() != TYPE_EMPLOYEE && getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		List<GCshTaxedSumImpl> taxedSums = new ArrayList<GCshTaxedSumImpl>();

		invoiceentries: for ( GnuCashGenerInvoiceEntry entry : getGenerEntries() ) {
			if ( entry.getType() == getType() ) {
				FixedPointNumber taxPerc = entry.getEmplVchApplicableTaxPercent();

				for ( GCshTaxedSumImpl taxedSum2 : taxedSums ) {
					GCshTaxedSumImpl taxedSum = taxedSum2;
					if ( taxedSum.getTaxpercent().equals(taxPerc) ) {
						taxedSum.setTaxsum(taxedSum.getTaxsum()
								.add(entry.getEmplVchSumInclTaxes().subtract(entry.getEmplVchSumExclTaxes())));
						continue invoiceentries;
					}
				}

				GCshTaxedSumImpl taxedSum = new GCshTaxedSumImpl(taxPerc,
						entry.getEmplVchSumInclTaxes().subtract(entry.getVendBllSumExclTaxes()));
				taxedSums.add(taxedSum);
			} // type
		} // for

		return taxedSums.toArray(new GCshTaxedSumImpl[taxedSums.size()]);
	}

	/**
	 * @return For a vendor bill: How much sales-taxes are to pay.
	 * 
	 * @see GCshTaxedSumImpl
	 */
	GCshTaxedSumImpl[] getJobTaxes() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashJobInvoice jobInvc = new GnuCashJobInvoiceImpl(this);
		if ( jobInvc.getJobType() == GCshOwner.Type.CUSTOMER )
			return getCustInvcTaxes();
		else if ( jobInvc.getJobType() == GCshOwner.Type.VENDOR )
			return getVendBllTaxes();
		else if ( jobInvc.getJobType() == GCshOwner.Type.EMPLOYEE )
			return getEmplVchTaxes();

		return null; // Compiler happy
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public GCshID getID() {
		return new GCshID(getJwsdpPeer().getInvoiceGuid().getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshOwner.Type getType() {
		return GCshOwner.Type.valueOff(getJwsdpPeer().getInvoiceOwner().getOwnerType());
	}

	@Deprecated
	public String getTypeStr() {
		return getJwsdpPeer().getInvoiceOwner().getOwnerType();
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshID getLotID() {
		if ( getJwsdpPeer().getInvoicePostlot() == null ) {
			return null; // unposted invoices have no postlot
		}

		return new GCshID(getJwsdpPeer().getInvoicePostlot().getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return getJwsdpPeer().getInvoiceNotes();
	}

	// ----------------------------

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("exports")
	public GncGncInvoice getJwsdpPeer() {
		return jwsdpPeer;
	}

	// ----------------------------

	/**
	 * {@inheritDoc}
	 */
	public GnuCashGenerInvoiceEntry getGenerEntryByID(final GCshID id) {
		for ( GnuCashGenerInvoiceEntry element : getGenerEntries() ) {
			if ( element.getID().equals(id) ) {
				return element;
			}

		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GnuCashGenerInvoiceEntry> getGenerEntries() {
		return entries;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addGenerEntry(final GnuCashGenerInvoiceEntry entry) {
		if ( !entries.contains(entry) ) {
			entries.add(new GnuCashGenerInvoiceEntryImpl(entry));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ZonedDateTime getDateOpened() {
		if ( dateOpened == null ) {
			String dateStr = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				// "2001-09-18 00:00:00 +0200"
				dateOpened = ZonedDateTime.parse(dateStr, DATE_OPENED_FORMAT);
			} catch (Exception e) {
				IllegalStateException ex = new IllegalStateException("unparsable date '" + dateStr + "' in invoice!");
				ex.initCause(e);
				throw ex;
			}

		}

		return dateOpened;
	}

	/**
	 * @see #getDateOpenedFormatted()
	 * @see #getDatePostedFormatted()
	 * @return the Dateformat to use.
	 */
	protected DateFormat getDateFormat() {
		if ( dateFormat == null ) {
			dateFormat = DateFormat.getDateInstance();
		}

		return dateFormat;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDateOpenedFormatted() {
		return getDateFormat().format(getDateOpened());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDatePostedFormatted() {
		return getDateFormat().format(getDatePosted());
	}

	/**
	 * {@inheritDoc}
	 */
	public ZonedDateTime getDatePosted() {
		if ( datePosted == null ) {
			String dateStr = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				// "2001-09-18 00:00:00 +0200"
				datePosted = ZonedDateTime.parse(dateStr, DATE_OPENED_FORMAT);
			} catch (Exception e) {
				IllegalStateException ex = new IllegalStateException(
						"unparsable date '" + dateStr + "' in invoice entry!");
				ex.initCause(e);
				throw ex;
			}

		}

		return datePosted;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNumber() {
		return getJwsdpPeer().getInvoiceId();
	}

	// -----------------------------------------------------------

	public GCshID getOwnerID() {
		return getOwnerId_direct();
	}

	public GCshID getOwnerID(ReadVariant readVar) {
		if ( readVar == ReadVariant.DIRECT )
			return getOwnerId_direct();
		else if ( readVar == ReadVariant.VIA_JOB )
			return getOwnerId_viaJob();

		return null; // Compiler happy
	}

	protected GCshID getOwnerId_direct() {
		assert getJwsdpPeer().getInvoiceOwner().getOwnerId().getType().equals(Const.XML_DATA_TYPE_GUID);
		return new GCshID(getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue());
	}

	protected GCshID getOwnerId_viaJob() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashGenerJob job = getGnuCashFile().getGenerJobByID(getOwnerID());
		return job.getOwnerID();
	}

	// ----------------------------

	@Override
	public GCshOwner.Type getOwnerType(ReadVariant readVar) {
		if ( readVar == ReadVariant.DIRECT )
			return getOwnerType_direct();
		else if ( readVar == ReadVariant.VIA_JOB )
			return getOwnerType_viaJob();

		return null; // Compiler happy
	}

	public GCshOwner.Type getOwnerType_direct() {
		return GCshOwner.Type.valueOff(getJwsdpPeer().getInvoiceOwner().getOwnerType());
	}

	@Deprecated
	public String getOwnerType_directStr() {
		return getJwsdpPeer().getInvoiceOwner().getOwnerType();
	}

	protected GCshOwner.Type getOwnerType_viaJob() {
		if ( getType() != TYPE_JOB )
			throw new WrongInvoiceTypeException();

		GnuCashGenerJob job = getGnuCashFile().getGenerJobByID(getOwnerID());
		return job.getOwnerType();
	}

	// -----------------------------------------------------------

	/**
	 * sorts primarily on the date the transaction happened and secondarily on the
	 * date it was entered.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @param otherInvc invoice to compare with
	 * @return -1 0 or 1
	 */
	public int compareTo(final GnuCashGenerInvoice otherInvc) {
		try {
			int compare = otherInvc.getDatePosted().compareTo(getDatePosted());
			if ( compare != 0 ) {
				return compare;
			}

			return otherInvc.getDateOpened().compareTo(getDateOpened());
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GnuCashGenerInvoiceImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", owner-id=");
		buffer.append(getOwnerID());

		buffer.append(", owner-type (dir.)=");
		try {
			buffer.append(getOwnerType(ReadVariant.DIRECT));
		} catch (WrongInvoiceTypeException e) {
			// TODO Auto-generated catch block
			buffer.append("ERROR");
		}

		buffer.append(", number='");
		buffer.append(getNumber() + "'");

		buffer.append(", description='");
		buffer.append(getDescription() + "'");

		buffer.append(", #entries=");
		buffer.append(entries.size());

		buffer.append(", date-opened=");
		try {
			buffer.append(getDateOpened().toLocalDate().format(DATE_OPENED_FORMAT_PRINT));
		} catch (Exception e) {
			buffer.append(getDateOpened().toLocalDate().toString());
		}

		buffer.append("]");
		return buffer.toString();
	}

	// ---------------------------------------------------------------

	/**
	 *
	 * @return the currency-format to use if no locale is given.
	 */
	protected NumberFormat getCurrencyFormat() {
		if ( currencyFormat == null ) {
			currencyFormat = NumberFormat.getCurrencyInstance();
		}

		return currencyFormat;
	}

	@SuppressWarnings("exports")
	@Override
	public InvoiceOwner getOwnerPeerObj() {
		return jwsdpPeer.getInvoiceOwner();
	}

}
