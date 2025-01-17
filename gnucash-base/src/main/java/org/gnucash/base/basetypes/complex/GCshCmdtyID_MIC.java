package org.gnucash.base.basetypes.complex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fully-qualified (real) commodity ID (name space
 * {@link GCshCmdtyCurrNameSpace.MIC}).
 */
public class GCshCmdtyID_MIC extends GCshCmdtyID {

	private static final Logger LOGGER = LoggerFactory.getLogger(GCshCmdtyID_MIC.class);

	// ---------------------------------------------------------------

	private GCshCmdtyCurrNameSpace.MIC mic;

	// ---------------------------------------------------------------

	public GCshCmdtyID_MIC() {
		super();
		type = Type.SECURITY_MIC;
		mic = GCshCmdtyCurrNameSpace.MIC.UNSET;
	}

	public GCshCmdtyID_MIC(GCshCmdtyCurrNameSpace.MIC mic, String secCode) {

		super(mic.toString(), secCode);

		setType(Type.SECURITY_MIC);
		setMIC(mic);
	}

	public GCshCmdtyID_MIC(String nameSpace, String code) {

		super(nameSpace, code);

		setType(Type.SECURITY_MIC);
		setMIC(nameSpace);
	}

	// ---------------------------------------------------------------

	@Override
	public void setType(Type type) {
//        if ( type != Type.SECURITY_MIC )
//            throw new InvalidCmdtyCurrIDException();

		super.setType(type);
	}

	// ----------------------------

	public GCshCmdtyCurrNameSpace.MIC getMIC() {
		if ( type != Type.SECURITY_MIC )
			throw new InvalidCmdtyCurrTypeException();

		return mic;
	}

	public void setMIC(GCshCmdtyCurrNameSpace.MIC mic) {
		if ( type != Type.SECURITY_MIC )
			throw new InvalidCmdtyCurrTypeException();

		this.mic = mic;
	}

	public void setMIC(String micStr) {
		if ( micStr == null )
			throw new IllegalArgumentException("MIC string is null");

		if ( micStr.trim().equals("") )
			throw new IllegalArgumentException("MIC string is empty");

		setMIC(GCshCmdtyCurrNameSpace.MIC.valueOf(micStr.trim()));
	}

	// ---------------------------------------------------------------

	public static GCshCmdtyID_MIC parse(String str) {
		if ( str == null )
			throw new IllegalArgumentException("Argument string is null");

		if ( str.equals("") )
			throw new IllegalArgumentException("Argument string is empty");

		GCshCmdtyID_MIC result = new GCshCmdtyID_MIC();

		int posSep = str.indexOf(SEPARATOR);
		// Plausi ::MAGIC
		if ( posSep <= 3 || 
			 posSep >= str.length() - 2 )
			throw new InvalidCmdtyCurrIDException();

		String nameSpaceLoc = str.substring(0, posSep).trim();
		String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();

		if ( nameSpaceLoc.equals(GCshCmdtyCurrNameSpace.CURRENCY) ) {
			throw new InvalidCmdtyCurrTypeException();
		} else {
			result.setType(Type.SECURITY_MIC);
			result.setNameSpace(nameSpaceLoc);
			result.setMIC(nameSpaceLoc);
			result.setCode(currSecCodeLoc);
		}

		return result;
	}

	// ---------------------------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((mic == null) ? 0 : mic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		GCshCmdtyID_MIC other = (GCshCmdtyID_MIC) obj;
		if ( type != other.type )
			return false;
		if ( mic != other.mic )
			return false;
		if ( nameSpace == null ) {
			if ( other.nameSpace != null )
				return false;
		} else if ( !nameSpace.equals(other.nameSpace) )
			return false;
		if ( code == null ) {
			if ( other.code != null )
				return false;
		} else if ( !code.equals(other.code) )
			return false;
		return true;
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		return toStringShort();
	}

	@Override
	public String toStringShort() {
		if ( type != Type.SECURITY_MIC )
			return "ERROR";

		String result = mic.toString() + SEPARATOR + code;

		return result;
	}

	@Override
	public String toStringLong() {
		if ( type != Type.SECURITY_MIC )
			return "ERROR";

		String result = "CommodityID_MIC [";

		result += "namespace='" + getNameSpace() + "'";

		try {
			result += ", mic='" + getMIC() + "'";
		} catch (InvalidCmdtyCurrTypeException e) {
			result += ", mic=" + "ERROR";
		}

		result += ", secCode='" + getCode() + "'";

		result += "]";

		return result;
	}

}
