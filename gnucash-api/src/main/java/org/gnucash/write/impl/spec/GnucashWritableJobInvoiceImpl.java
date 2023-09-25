package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.impl.spec.GnucashJobInvoiceEntryImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.write.spec.GnucashWritableJobInvoice;
import org.gnucash.write.spec.GnucashWritableJobInvoiceEntry;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableJobInvoiceImpl extends GnucashWritableGenerInvoiceImpl 
                                           implements GnucashWritableJobInvoice 
{

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableJobInvoiceImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param file the file we are associated with.
	 */
	protected GnucashWritableJobInvoiceImpl(
			final GnucashWritableFileImpl file,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyFromTo,
			final LocalDate dueDate) {
		super(createJobInvoice(file, invoiceNumber, job, accountToTransferMoneyFromTo, dueDate), file);
	}
	
	/**
	 * @param file the file we are associated with.
	 * @throws WrongInvoiceTypeException 
	 */
	public GnucashWritableJobInvoiceImpl(final GnucashWritableGenerInvoiceImpl invc) throws WrongInvoiceTypeException {
	    super(invc.getJwsdpPeer(), invc.getFile());

	    // No, we cannot check that first, because the super() method
	    // always has to be called first.
	    if ( ! invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_JOB) )
	      throw new WrongInvoiceTypeException();

	    // ::TODO
//	    for ( GnucashGenerInvoiceEntry entry : invc.getGenerEntries() )
//	    {
//	      addEntry(new GnucashWritableJobInvoiceEntryImpl(entry));
//	    }

	    for ( GnucashTransaction trx : invc.getPayingTransactions() )
	    {
	      for ( GnucashTransactionSplit splt : trx.getSplits() ) 
	      {
	        String lot = splt.getLotID();
	        if ( lot != null ) {
	            for ( GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices() ) {
	                String lotID = invc1.getLotID();
	                if ( lotID != null &&
	                     lotID.equals(lot) ) {
	                    // Check if it's a payment transaction. 
	                    // If so, add it to the invoice's list of payment transactions.
	                    if ( splt.getAction().equals(GnucashTransactionSplit.ACTION_PAYMENT) ) {
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
	    protected GnucashWritableFileImpl getWritingFile() {
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
	    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
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
	    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
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
	    public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
		    myPropertyChange.removePropertyChangeListener(propertyName, listener);
		}
	    }

	    /**
	     * Remove a PropertyChangeListener from the listener list. This removes a
	     * PropertyChangeListener that was registered for all properties.
	     *
	     * @param listener The PropertyChangeListener to be removed
	     */
	    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
		    myPropertyChange.removePropertyChangeListener(listener);
		}
	    }


	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setGenerJob(GnucashGenerJob job) throws WrongInvoiceTypeException {
	    // ::TODO
	    Object old = getJob();
	    if (old == job) {
		return; // nothing has changed
	    }
		
	    getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(job.getId());
	    getWritingFile().setModified(true);

	    // <<insert code to react further to this change here
	    PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	    if (propertyChangeFirer != null) {
		propertyChangeFirer.firePropertyChange("job", old, job);
	    }
	}

	@Override
	public void setCustomerJob(GnucashCustomerJob job) throws WrongInvoiceTypeException {
	    setGenerJob(job);
	}

	@Override
	public void setVendorJob(GnucashVendorJob job) throws WrongInvoiceTypeException {
	    setGenerJob(job);
	}

	// -----------------------------------------------------------

	/**
	 * create and add a new entry.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableJobInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity);
		return new GnucashWritableJobInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableJobInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice,
		final FixedPointNumber quantity,
		final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
		return new GnucashWritableJobInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableJobInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity,
		final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	    GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
	    return new GnucashWritableJobInvoiceEntryImpl(entryGener);
	}
	
	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashWritableJobInvoiceEntryImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		removeInvcEntry(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashWritableJobInvoiceEntryImpl#createCustInvoiceEntry(GnucashWritableJobInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of job-invoice-entries
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	protected void addEntry(final GnucashWritableJobInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		addJobEntry(entry);
	}

	protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		subtractInvcEntry(entry);
	}

	/**
	 * @return the ID of the Account to transfer the money from
	 * @throws WrongInvoiceTypeException 
	 */
	private String getAccountIDToTransferMoneyFrom(final GnucashJobInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		return getInvcAccountIDToTransferMoneyTo(entry);
	}

	protected String getBillAccountIDToTransferMoneyFrom(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void attemptChange() {
		if (!isModifiable()) {
			throw new IllegalStateException("this job invoice is NOT changable because there are already payment for it made!");
		}
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableGenerEntryById(java.lang.String)
	 */
	public GnucashWritableJobInvoiceEntry getWritableEntryById(final String id) {
		return new GnucashWritableJobInvoiceEntryImpl(getGenerEntryById(id));
	}

	    // ---------------------------------------------------------------
	    
	    /**
	     * {@inheritDoc}
	     */
	    public String getJobId() {
	        return getOwnerId();
	    }

	    /**
	     * {@inheritDoc}
	     */
	    public GnucashGenerJob getJob() {
	        return getFile().getGenerJobByID(getJobId());
	    }

}
