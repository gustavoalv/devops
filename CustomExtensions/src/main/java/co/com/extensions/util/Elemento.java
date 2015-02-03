/**
 * 
 */
package co.com.extensions.util;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * @author alvgu02
 *
 */
public class Elemento {

	
	private static Logger log = Logger.getLogger(Elemento.class);
	
	/**
	 * 
	 */
	private String num;
	
	/**
	 * 
	 */
	private String type;
	
	/**
	 * 
	 */
	private String length;
	
	/**
	 * 
	 */
	private String encoding;
	
	/**
	 * 
	 */
	private String value;

	/**
	 * @return the num
	 */
	public String getNum() {
		return num;
	}

	/**
	 * @param num the num to set
	 */
	public void setNum(String num) {
		this.num = num;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the length
	 */
	public String getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(String length) {
		this.length = length;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param currentElement
	 */
	public static Elemento getElemento(Element currentElement) {
		Elemento elemento = new Elemento();
		elemento.setEncoding(currentElement.getAttribute("encoding"));
		elemento.setLength(currentElement.getAttribute("length"));
		elemento.setNum(currentElement.getAttribute("num"));
		elemento.setType(currentElement.getAttribute("mytype"));
		elemento.setValue(currentElement.getTextContent() );
		
		log.info("nodetype: " + elemento.getType() + " , " + "nodenum: " + elemento.getNum() + " , " + "nodelength: " + elemento.getLength() );
		
		return elemento;
	}
}
