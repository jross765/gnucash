package org.gnucash.api.write.impl.aux;

import org.gnucash.api.Const;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.api.generated.GncGncTaxTable;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.impl.aux.GCshTaxTableEntryImpl;
import org.gnucash.api.write.GnuCashWritableFile;
import org.gnucash.api.write.aux.GCshWritableTaxTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Extension of GCshTaxTableEntryImpl to allow read-write access instead of
 * read-only access.
 */
public class GCshWritableTaxTableEntryImpl extends GCshTaxTableEntryImpl 
                                           implements GCshWritableTaxTableEntry 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GCshWritableTaxTableEntryImpl.class);

    // ---------------------------------------------------------------

    @SuppressWarnings("exports")
    public GCshWritableTaxTableEntryImpl(
	    final GncGncTaxTable.TaxtableEntries.GncGncTaxTableEntry jwsdpPeer,
	    final GnuCashWritableFile gcshFile) {
	super(jwsdpPeer, gcshFile);
    }

    public GCshWritableTaxTableEntryImpl(final GCshTaxTableEntryImpl entr) {
	super(entr.getJwsdpPeer(), entr.getGnuCashFile());
    }

    // ---------------------------------------------------------------

    @Override
    public void setType(final Type type) {
	setTypeStr(type.toString());
    }

    @Override
    public void setTypeStr(final String typeStr) {
	if ( typeStr == null ) {
	    throw new IllegalArgumentException("null type given!");
	}
	
	if ( typeStr.trim().length() == 0 ) {
	    throw new IllegalArgumentException("empty type given!");
	}

	getJwsdpPeer().setTteType(typeStr);
    }

    /**
     * @param acctId ID of the account to set.
     */
    @Override
    public void setAccountID(final GCshID acctId) {
	if ( acctId == null ) {
	    throw new IllegalArgumentException("null account-ID given!");
	}
	
	if ( ! acctId.isSet() ) {
	    throw new IllegalArgumentException("unset account-ID given!");
	}

	myAccountID = acctId;
	
	getJwsdpPeer().getTteAcct().setType(Const.XML_DATA_TYPE_GUID);
	getJwsdpPeer().getTteAcct().setValue(acctId.toString());
    }

    /**
     * @param acct The account to set.
     * @link #myAccount
     */
    @Override
    public void setAccount(final GnuCashAccount acct) {
	if ( acct == null ) {
	    throw new IllegalArgumentException("null account given!");
	}

	myAccount = acct;

	setAccountID(acct.getID());
    }

    @Override
    public void setAmount(final FixedPointNumber amt) {
	getJwsdpPeer().setTteAmount(amt.toGnuCashString());
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
	String result = "GCshWritableTaxTableEntryImpl [";
	
	result += "type=" + getType(); 
	result += ", account-id=" + getAccountID(); 
	result += ", amount=" + getAmount(); 
		                          
	result += "]";
	
	return result;
    }

}
