package org.sticky.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple generic Code JAXB bean to use for producing comet code mappings in a
 * compact fashion. Each code element handles a code, a type (associated with
 * code) and a label (useful for human to check mapping consistency)
 * 
 * @author Emmanuel Blondel
 *
 */
@XmlRootElement(name = "Code")
@XmlAccessorType(XmlAccessType.FIELD)
public class Code implements Serializable {
	
	private static final long serialVersionUID = 9007706386958542860L;

	@XmlAttribute(name = "type")
	private String _type;
	
	@XmlAttribute(name = "value")
	private String _value;
	
	@XmlAttribute(name = "label")
	private String _label;

	/**
	 * Class constructor
	 *
	 */
	public Code() {
	}

	/**
	 * Class constructor
	 *
	 * @param code
	 */
	public Code(String value, String type, String label) {
		super();
		this._type = type;
		this._value = value;
		this._label = label;
	}

	final static public Code coding(String value, String type, String label) {
		return new Code(value,type,label);
	}

	/**
	 * @return the code value
	 */
	public String getValue() {
		return this._value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setCode(String value) {
		this._value = value;
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
				+ ((this._type == null) ? 0 : this._type
						.hashCode())
				+ ((this._value == null) ? 0 : this._value
						.hashCode())
				+ ((this._label == null) ? 0 : this._label
						.hashCode())
				;
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
		
		Code other = (Code) obj;
		if (this._type == null) {
			if (other._type != null)
				return false;
		} else if (!this._type.equals(other._type))
			return false;
		if (this._value == null) {
			if (other._value != null)
				return false;
		} else if (!this._value.equals(other._value))
			return false;
		if (this._label == null) {
			if (other._label != null)
				return false;
		} else if (!this._label.equals(other._label))
			return false;
		return true;
	}
}