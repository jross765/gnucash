package org.gnucash.api.read.impl.aux;

import java.io.IOException;

import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrIDException;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.read.impl.GnucashFileImpl;
import org.gnucash.api.read.impl.hlp.FileStats;
import org.gnucash.api.read.impl.hlp.FileStats_Cache;
import org.gnucash.api.read.impl.hlp.FileStats_Counters;
import org.gnucash.api.read.impl.hlp.FileStats_Raw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshFileStats {
    
    public enum Type {
	RAW,
	COUNTER,
	CACHE
    }
    
    // ---------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshFileStats.class);
    
    // ---------------------------------------------------------------

    private FileStats_Raw      raw; 
    private FileStats_Counters cnt; 
    private FileStats_Cache    che; 

    // ---------------------------------------------------------------
    
    public GCshFileStats(GnucashFileImpl gcshFile) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException, IOException {
	raw = new FileStats_Raw(gcshFile);
	cnt = new FileStats_Counters(gcshFile);
	che = new FileStats_Cache(gcshFile);
    }

    // ---------------------------------------------------------------

    public int getNofEntriesAccounts(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesAccounts();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesAccounts();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesAccounts();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesTransactions(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesTransactions();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesTransactions();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesTransactions();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesTransactionSplits(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesTransactionSplits();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesTransactionSplits();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesTransactionSplits();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesGenerInvoices(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesGenerInvoices();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesGenerInvoices();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesGenerInvoices();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesGenerInvoiceEntries(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesGenerInvoiceEntries();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesGenerInvoiceEntries();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesGenerInvoiceEntries();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesCustomers(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesCustomers();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesCustomers();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesCustomers();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesVendors(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesVendors();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesVendors();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesVendors();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesEmployees(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesEmployees();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesEmployees();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesEmployees();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesGenerJobs(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesGenerJobs();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesGenerJobs();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesGenerJobs();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesCommodities(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesCommodities();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesCommodities();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesCommodities();
	}
	
	return FileStats.ERROR; // Compiler happy
    }
    
    // ----------------------------
    
    public int getNofEntriesTaxTables(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesTaxTables();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesTaxTables();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesTaxTables();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    public int getNofEntriesBillTerms(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesBillTerms();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesBillTerms();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesBillTerms();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

    // ----------------------------
    
    public int getNofEntriesPrices(Type type) {
	if ( type == Type.RAW ) {
	    return raw.getNofEntriesPrices();
	} else if ( type == Type.COUNTER ) {
	    return cnt.getNofEntriesPrices();
	} else if ( type == Type.CACHE ) {
	    return che.getNofEntriesPrices();
	}
	
	return FileStats.ERROR; // Compiler happy
    }

}