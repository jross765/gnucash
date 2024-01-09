package org.gnucash.api.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.generated.GncGncInvoice;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.read.GnucashAccount;
import org.gnucash.api.read.GnucashFile;
import org.gnucash.api.read.GnucashGenerInvoice;
import org.gnucash.api.read.GnucashGenerInvoiceEntry;
import org.gnucash.api.read.GnucashTransaction;
import org.gnucash.api.read.GnucashTransactionSplit;
import org.gnucash.api.read.GnucashVendor;
import org.gnucash.api.read.IllegalTransactionSplitActionException;
import org.gnucash.api.read.TaxTableNotFoundException;
import org.gnucash.api.read.aux.GCshOwner;
import org.gnucash.api.read.aux.GCshTaxTable;
import org.gnucash.api.read.impl.GnucashAccountImpl;
import org.gnucash.api.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.api.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.api.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.api.read.impl.spec.GnucashVendorBillEntryImpl;
import org.gnucash.api.read.impl.spec.GnucashVendorBillImpl;
import org.gnucash.api.read.spec.GnucashVendorBill;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.api.write.GnucashWritableGenerInvoice;
import org.gnucash.api.write.impl.GnucashWritableFileImpl;
import org.gnucash.api.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.api.write.spec.GnucashWritableVendorBill;
import org.gnucash.api.write.spec.GnucashWritableVendorBillEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vendor bill that can be modified {@link #isModifiable()} returns true.
 * 
 * @see GnucashVendorBill
 * 
 * @see GnucashWritableCustomerInvoiceImpl
 * @see GnucashWritableEmployeeVoucherImpl
 * @see GnucashWritableJobInvoiceImpl
 */
public class GnucashWritableVendorBillImpl extends GnucashWritableGenerInvoiceImpl 
                                           implements GnucashWritableVendorBill
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableVendorBillImpl.class);

	// ---------------------------------------------------------------

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param gcshFile      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncGncInvoice, GnucashFile)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableVendorBillImpl(final GncGncInvoice jwsdpPeer, final GnucashFile gcshFile) {
		super(jwsdpPeer, gcshFile);
	}

	/**
	 * @param file the file we are associated with.
	 * @param number 
	 * @param vend 
	 * @param expensesAcct 
	 * @param payableAcct 
	 * @param openedDate 
	 * @param postDate 
	 * @param dueDate 
	 * @throws WrongOwnerTypeException
	 * @throws InvalidCmdtyCurrTypeException
	 * @throws IllegalTransactionSplitActionException
	 */
	public GnucashWritableVendorBillImpl(final GnucashWritableFileImpl file, final String number,
			final GnucashVendor vend, final GnucashAccountImpl expensesAcct, final GnucashAccountImpl payableAcct,
			final LocalDate openedDate, final LocalDate postDate, final LocalDate dueDate)
			throws WrongOwnerTypeException, InvalidCmdtyCurrTypeException, IllegalTransactionSplitActionException {
		super(createVendorBill_int(file, number, vend, false, // <-- caution!
				expensesAcct, payableAcct, openedDate, postDate, dueDate), file);
	}

	/**
	 * @param invc 
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 */
	public GnucashWritableVendorBillImpl(final GnucashWritableGenerInvoiceImpl invc)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {
		super(invc.getJwsdpPeer(), invc.getFile());

		// No, we cannot check that first, because the super() method
		// always has to be called first.
		if ( invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT) != GCshOwner.Type.VENDOR )
			throw new WrongInvoiceTypeException();

		// Caution: In the following two loops, we may *not* iterate directly over
		// invc.getGenerEntries(), because else, we will produce a
		// ConcurrentModificationException.
		// (It only works if the invoice has one single entry.)
		// Hence the indirection via the redundant "entries" hash set.
		Collection<GnucashGenerInvoiceEntry> entries = new HashSet<GnucashGenerInvoiceEntry>();
		for ( GnucashGenerInvoiceEntry entry : invc.getGenerEntries() ) {
			entries.add(entry);
		}

		for ( GnucashGenerInvoiceEntry entry : entries ) {
			addEntry(new GnucashWritableVendorBillEntryImpl(entry));
		}

		// Caution: Indirection via a redundant "trxs" hash set.
		// Same reason as above.
		Collection<GnucashTransaction> trxs = new HashSet<GnucashTransaction>();
		for ( GnucashTransaction trx : invc.getPayingTransactions() ) {
			trxs.add(trx);
		}

		for ( GnucashTransaction trx : trxs ) {
			for ( GnucashTransactionSplit splt : trx.getSplits() ) {
				GCshID lot = splt.getLotID();
				if ( lot != null ) {
					for ( GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices() ) {
						GCshID lotID = invc1.getLotID();
						if ( lotID != null && lotID.equals(lot) ) {
							// Check if it's a payment transaction.
							// If so, add it to the invoice's list of payment transactions.
							if ( splt.getAction() == GnucashTransactionSplit.Action.PAYMENT ) {
								addPayingTransaction(splt);
							}
						} // if lotID
					} // for invc
				} // if lot
			} // for splt
		} // for trx
	}

	// ---------------------------------------------------------------

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	protected GnucashWritableFileImpl getWritableFile() {
		return (GnucashWritableFileImpl) getFile();
	}

	/**
	 * support for firing PropertyChangeEvents. (gets initialized only if we really
	 * have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	@SuppressWarnings("exports")
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPropertyChange == null ) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will be
	 * invoked only when a call on firePropertyChange names that specific property.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	@SuppressWarnings("exports")
	public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if ( myPropertyChange == null ) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	@SuppressWarnings("exports")
	public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if ( myPropertyChange != null ) {
			myPropertyChange.removePropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	@SuppressWarnings("exports")
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPropertyChange != null ) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void setVendor(GnucashVendor vend) throws WrongInvoiceTypeException {
		// ::TODO
		GnucashVendor oldVend = getVendor();
		if ( oldVend == vend ) {
			return; // nothing has changed
		}

		getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(vend.getID().toString());
		getWritableFile().setModified(true);

		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if ( propertyChangeFirer != null ) {
			propertyChangeFirer.firePropertyChange("vendor", oldVend, vend);
		}
	}

	// -----------------------------------------------------------

	/**
	 * create and add a new entry.
	 * 
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 */
	public GnucashWritableVendorBillEntry createEntry(
			final GnucashAccount acct, 
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {
		GnucashWritableVendorBillEntry entry = createVendBllEntry(acct, singleUnitPrice, quantity);
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the SKR03.
	 * 
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 */
	public GnucashWritableVendorBillEntry createEntry(
			final GnucashAccount acct, 
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity, 
			final String taxTabName)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {
		GnucashWritableVendorBillEntry entry = createVendBllEntry(acct, singleUnitPrice, quantity, taxTabName);
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 */
	public GnucashWritableVendorBillEntry createEntry(
			final GnucashAccount acct, 
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity, 
			final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {
		GnucashWritableVendorBillEntry entry = createVendBllEntry(acct, singleUnitPrice, quantity, taxTab);
		LOGGER.info("createEntry: Created vendor bill entry: " + entry.getID());
		return entry;
	}

	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashWritableVendorBillEntryImpl entry)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {

		removeBillEntry(entry);
		LOGGER.info("removeEntry: Removed vendor bill entry: " + entry.getID());
	}

	/**
	 * Called by
	 * ${@link GnucashWritableVendorBillEntryImpl#createVendBillEntry_int(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of vendor-bill-entries
	 * @throws WrongInvoiceTypeException
	 * @throws TaxTableNotFoundException
	 * @throws InvalidCmdtyCurrTypeException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 */
	protected void addEntry(final GnucashWritableVendorBillEntryImpl entry)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {

		addBillEntry(entry);
		LOGGER.info("addEntry: Added vendor bill entry: " + entry.getID());
	}

	protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, InvalidCmdtyCurrTypeException {
		subtractBillEntry(entry);
		LOGGER.info("subtractEntry: Subtracted vendor bill entry: " + entry.getID());
	}

	// ---------------------------------------------------------------
	
	/**
	 * @return the ID of the Account to transfer the money from
	 * @throws WrongInvoiceTypeException
	 */
	@SuppressWarnings("unused")
	private GCshID getPostAccountID(final GnucashVendorBillEntryImpl entry) throws WrongInvoiceTypeException {
		return getVendBllPostAccountID(entry);
	}

	/**
	 * Do not use
	 */
	@Override
	protected GCshID getCustInvcPostAccountID(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	/**
	 * Do not use
	 */
	@Override
	protected GCshID getEmplVchPostAccountID(final GnucashGenerInvoiceEntryImpl entry)
			throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	/**
	 * Do not use
	 */
	@Override
	protected GCshID getJobInvcPostAccountID(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	// ---------------------------------------------------------------
	
	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void attemptChange() {
		if ( !isModifiable() ) {
			throw new IllegalStateException(
					"this vendor bill is NOT changeable because there are already payment for it made!");
		}
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableGenerEntryByID(java.lang.String)
	 */
	public GnucashWritableVendorBillEntry getWritableEntryByID(final GCshID id) {
		return new GnucashWritableVendorBillEntryImpl(getGenerEntryByID(id));
	}

	// ---------------------------------------------------------------

	/**
	 * @return
	 */
	public GCshID getVendorID() {
		return getOwnerID();
	}

	/**
	 * @return
	 */
	public GnucashVendor getVendor() {
		return getFile().getVendorByID(getVendorID());
	}

	// ---------------------------------------------------------------

	@Override
	public void post(final GnucashAccount expensesAcct, final GnucashAccount payablAcct, final LocalDate postDate,
			final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException,
			InvalidCmdtyCurrTypeException, IllegalTransactionSplitActionException {
		postVendorBill(getFile(), this, getVendor(), expensesAcct, payablAcct, postDate, dueDate);
	}

	// ---------------------------------------------------------------

	public static GnucashVendorBillImpl toReadable(GnucashWritableVendorBillImpl invc) {
		GnucashVendorBillImpl result = new GnucashVendorBillImpl(invc.getJwsdpPeer(), invc.getFile());
		return result;
	}

}
