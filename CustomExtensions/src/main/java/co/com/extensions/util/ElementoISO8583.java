/**
 * 
 */
package co.com.extensions.util;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlValue;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author alvgu02
 *
 */
public class ElementoISO8583 extends NodeISO8583{

	
	private static Logger log = Logger.getLogger(ElementoISO8583.class);
	
	
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
	private String name;
	
	/**
	 * 
	 */
	private String classe;
	

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hasChilds
	 */
	public boolean hasChilds() {
		
		if((this.getHijos() != null) && (this.getHijos().getFields() != null) && (!this.getHijos().getFields().isEmpty())){
			
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean addHChilds(ElementoISO8583 hijo) {
		if(this.hasChilds()){
			return this.getHijos().getFields().add(hijo);
		} else {
			this.hijos = new ElementISOWrapper();
			hijos.setFields(new ArrayList<ElementoISO8583>());
			return hijos.getFields().add(hijo);
		}
	}

	/**
	 * 
	 */
	private String value;
	
	
	/**
	 * @return the value
	 */
	@XmlValue
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
	 * @return the hijos
	 */
	public ElementISOWrapper getHijos() {
		return hijos;
	}

	/**
	 * @param hijos the hijos to set
	 */
	public void setHijos(ElementISOWrapper hijos) {
		this.hijos = hijos;
	}

	/**
	 * 
	 */
	private ElementISOWrapper hijos;

	
	/**
	 * @return the classe
	 */
	public String getClasse() {
		return classe;
	}

	/**
	 * @param classe the classe to set
	 */
	public void setClasse(String classe) {
		this.classe = classe;
	}

	/**
	 * @param currentElement
	 */
	public static ElementoISO8583 getElemento(Element currentElement) {
		
		ElementoISO8583 elemento = new ElementoISO8583();
		elemento.setLength(currentElement.getAttribute("length"));
		elemento.setNum(currentElement.getAttribute("num"));
		elemento.setType(currentElement.getAttribute("type"));
		elemento.setName(currentElement.getNodeName());
		
		String textContent = currentElement.getTextContent().replace("\n", "");
		
		if(textContent != null && !textContent.isEmpty()){
			elemento.setValue(textContent );
			
		} else {
			
			ElementISOWrapper hijosVar = new ElementISOWrapper();
			hijosVar.setFields(new ArrayList<ElementoISO8583>());
			elemento.setHijos(hijosVar);
			NodeList nodesChildsHeader = currentElement.getChildNodes();
			for (int i = 0; i < nodesChildsHeader.getLength(); i++) {
				Node nodesChildHeader = nodesChildsHeader.item(i);
				if (nodesChildHeader.getNodeType() == Node.ELEMENT_NODE) {
					Element currentElementChild = (Element) nodesChildHeader;
					ElementoISO8583 hijo = ElementoISO8583.getElemento(currentElementChild);
					elemento.addHChilds(hijo);
				}
			}
		}
		
		log.info("nodetype: " + elemento.getType() + " , " + "nodenum: " + elemento.getNum() + " , " + "nodelength: " + elemento.getLength() );
		
		return elemento;
	}
}
