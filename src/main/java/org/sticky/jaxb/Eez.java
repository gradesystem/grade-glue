package org.sticky.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple Eez JAXB bean to use for producing comet code mappings dealing
 * with a Eez object and encapsulate a code describing this eez.
 * 
 * @author Emmanuel Blondel
 *
 */
@XmlRootElement(name = "Eez")
@XmlAccessorType(XmlAccessType.FIELD)
public class Eez implements Serializable {
	
	private static final long serialVersionUID = 4309404806843871599L;
	
	@XmlElement(name = "Code")
	private Code _code;

	/**
	 * Class constructor
	 *
	 */
	public Eez() {
	}

	/**
	 * Class constructor
	 *
	 * @param code
	 */
	public Eez(Code code) {
		super();
		this._code = code;
	}

	final static public Eez coding(Code code) {
		return new Eez(code);
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
		Eez other = (Eez) obj;
		if (this._code == null) {
			if (other._code != null)
				return false;
		} else if (!this._code.equals(other._code))
			return false;
		return true;
	}
}