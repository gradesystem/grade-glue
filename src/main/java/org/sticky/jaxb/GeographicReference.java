package org.sticky.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple GeographicReference JAXB bean to use for producing comet code mappings dealing
 * with a GeographicReference and encapsulate a uri identifying this geographic reference
 * 
 * @author Emmanuel Blondel
 *
 */
@XmlRootElement(name = "GeographicReference")
@XmlAccessorType(XmlAccessType.FIELD)
public class GeographicReference implements Serializable {
	
	private static final long serialVersionUID = 556657976042229122L;
	
	@XmlElement(name = "Code")
	private Code _code;

	/**
	 * Class constructor
	 *
	 */
	public GeographicReference() {
	}

	/**
	 * Class constructor
	 *
	 * @param code
	 */
	public GeographicReference(Code code) {
		super();
		this._code = code;
	}

	final static public GeographicReference coding(Code code) {
		return new GeographicReference(code);
	}

	/**
	 * @return the 'code' value
	 */
	public Code getCode() {
		return this._code;
	}

	/**
	 * @param code
	 *            the 'code' value to set
	 */
	public void setCode(Code code) {
		this._code = code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this._code == null) ? 0 : this._code
						.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeographicReference other = (GeographicReference) obj;
		if (this._code == null) {
			if (other._code != null)
				return false;
		} else if (!this._code.equals(other._code))
			return false;
		return true;
	}
}