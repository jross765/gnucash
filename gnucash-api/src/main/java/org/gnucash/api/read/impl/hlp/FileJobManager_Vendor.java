package org.gnucash.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.List;

import org.gnucash.api.read.GnuCashGenerJob;
import org.gnucash.api.read.GnuCashVendor;
import org.gnucash.api.read.impl.spec.GnuCashVendorJobImpl;
import org.gnucash.api.read.spec.GnuCashVendorJob;
import org.gnucash.api.read.spec.WrongJobTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileJobManager_Vendor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FileJobManager_Vendor.class);
    
	// ---------------------------------------------------------------

	public static List<GnuCashVendorJob> getJobsByVendor(final FileJobManager jobMgr, final GnuCashVendor vend) {
		List<GnuCashVendorJob> retval = new ArrayList<GnuCashVendorJob>();

		for ( GnuCashGenerJob job : jobMgr.getGenerJobs() ) {
			if ( job.getOwnerID().equals(vend.getID()) ) {
				try {
					retval.add(new GnuCashVendorJobImpl(job));
				} catch (WrongJobTypeException e) {
					LOGGER.error("getJobsByVendor: Cannot instantiate GnuCashVendorJobImpl");
				}
			}
		}

		return retval;
	}

}
