package org.example.gnucashapi.write.gen.simple;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.gnucash.base.basetypes.complex.GCshCurrID;
import org.gnucash.base.numbers.FixedPointNumber;
import org.gnucash.api.read.GnuCashAccount;
import org.gnucash.api.read.GnuCashTransaction;
import org.gnucash.api.read.GnuCashTransactionSplit;
import org.gnucash.api.read.NoEntryFoundException;
import org.gnucash.api.read.TooManyEntriesFoundException;
import org.gnucash.api.write.GnuCashWritableTransaction;
import org.gnucash.api.write.GnuCashWritableTransactionSplit;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;

/**
 * Created by Deniss Larka
 */
public class GenTrx2 {
    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName  = "example_in.gnucash";
    private static String gcshOutFileName = "example_out.gnucash";
    private static String accountName     = "Root Account::Erträge::Honorar";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) throws IOException {
	try {
	    GenTrx2 tool = new GenTrx2();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    private void kernel() throws Exception {
	GnuCashWritableFileImpl gnucashFile = new GnuCashWritableFileImpl(new File(gcshInFileName));
	Collection<GnuCashAccount> accounts = gnucashFile.getAccounts();
	for (GnuCashAccount account : accounts) {
	    System.out.println(account.getQualifiedName());
	}

	GnuCashWritableTransaction writableTransaction = gnucashFile.createWritableTransaction();
	writableTransaction.setDescription("check");
	writableTransaction.setCmdtyCurrID(new GCshCurrID("EUR"));
	writableTransaction.setDateEntered(LocalDateTime.now());

	GnuCashAccount acct = null;
	try {
	    acct = gnucashFile.getAccountByNameUniq(accountName, true);
	} catch ( NoEntryFoundException exc ) {
	    System.err.println("Found no account with ");
	    System.exit(1);
	} catch ( TooManyEntriesFoundException exc ) {
	    System.err.println("Found several accounts with that name");
	    System.exit(1);
	}
	
	GnuCashWritableTransactionSplit writingSplit = writableTransaction.createWritableSplit(acct);
	writingSplit.setValue(new FixedPointNumber(100));
	writingSplit.setDescription("Generated by GenTrx2 " + LocalDateTime.now().toString());

	Collection<? extends GnuCashTransaction> transactions = gnucashFile.getTransactions();
	for (GnuCashTransaction transaction : transactions) {
	    System.out.println(transaction.getDatePosted());
	    List<GnuCashTransactionSplit> splits = transaction.getSplits();
	    for (GnuCashTransactionSplit split : splits) {
		System.out.println("\t" + split.getQuantity());
	    }
	}

	// Caution: output file will always be in uncompressed XML format,
	// regardless of whether the input file was compressed or not.
	gnucashFile.writeFile(new File(gcshOutFileName));
	System.out.println("OK");
    }
}