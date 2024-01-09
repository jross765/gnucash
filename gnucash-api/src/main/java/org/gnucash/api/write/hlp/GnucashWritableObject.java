package org.gnucash.api.write.hlp;

import org.gnucash.api.read.hlp.GnucashObject;
import org.gnucash.api.write.GnucashWritableFile;

/**
 * Interface that all interfaces for writable gnucash-entities shall implement
 */
public interface GnucashWritableObject {

    /**
     * @return the File we belong to.
     */
    GnucashWritableFile getWritableGnucashFile();

    /**
     * @param name the name of the user-defined attribute
     * @param value the value or null if not set
     * @see {@link GnucashObject#getUserDefinedAttribute(String)}
     */
    void setUserDefinedAttribute(String name, String value);
}