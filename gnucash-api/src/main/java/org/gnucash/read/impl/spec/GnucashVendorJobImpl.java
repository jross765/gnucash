package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashJobImpl;
import org.gnucash.read.spec.GnucashVendorJob;

public class GnucashVendorJobImpl extends GnucashJobImpl
                                  implements GnucashVendorJob
{
    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    public GnucashVendorJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super(peer, gncFile);
    }

    // ----------------------------
    
    /**
     * {@inheritDoc}
     */
    public String getVendorType() {
        return getOwnerType();
    }

    /**
     * {@inheritDoc}
     */
    public String getVendorId() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashVendor getVendor() {
        return file.getVendorByID(getVendorId());
    }
}