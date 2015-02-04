package co.com.extensions.dph.paxus;

import java.io.StringReader;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import co.com.extensions.handlers.CustomExtensionsHandler;

import com.itko.citi.Converter;

/**
 * Clase para manipular los mensajes de Paxus.
 * Los mensajes Paxus se caracterizan por:
 * 1. Los dos primero bytes del mensajes determinan la longitud del mensajes de solicitud y respuesta. Este valor incluye los dos primeros bytes
 * 2. Existe un header en request y otro en response
 * 3. Cada campo esta definido por el valor Hexa 0x1C##. Donde ## es un valor que varía dependiendo del nombre del campo.
 * 4. Al final de cada mensaje se termina con el valor HEX 0x03
 * @author alvgu02
 *
 */
public class PaxusHandler {

	private static final String CODEBASE = "codebase";
	
	private static final String CODE = "code";
	
	private static final String LASTCHARACTER = "lastCharacter";
	
	private static Logger logger = Logger.getLogger(PaxusHandler.class);
	
	private static final String TXCODE = "1C80";
	
	private static ResourceBundle paxusDisplayNameBundle;
	
	private static ResourceBundle paxusConfigNameBundle;
	
	private static ResourceBundle paxusDisplayTxNameBundle; 
	
	

	/**
	 * 
	 * @param payloadBody
	 * @return
	 */
	public static String parseMessage (byte[] payloadBody, boolean isResponse){
		
		try {
			
			String codebase = getConfigDataProtocol(CODEBASE);
			
			// Crear documento XML de VB
			DocumentBuilderFactory finalFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
			Document finaldoc = finalBuilder.newDocument();
			String rootName = null;
			Element rootElement = null;
			if(isResponse){
				rootName = "Message-Response";
			} else {
				rootName = "Message-Request";
			}
			
			rootElement = finaldoc.createElement(rootName);
			
			String requestHexa = Converter.convertByteToHex(payloadBody);
			
		    int length = requestHexa.length();
		    
		    String requestsFilter = requestHexa.substring(0, length -2 );
		    
		    String messageLength = requestsFilter.substring(0, 4);
		    int lengthMessage = Converter.convertHexToInt(messageLength);
		    messageLength = Integer.toString(lengthMessage);
		    
		    Element childnode = finaldoc.createElement("messageLength");
		    childnode.setTextContent(messageLength);
		    rootElement.appendChild(childnode);
		    
		    //take the match string and split the request body according to it
	        Pattern pattern = Pattern.compile(codebase);
	        String[] data = pattern.split(requestsFilter.substring(4));
		        
		    //parse request array into arguments
			for (int x = 0; x < data.length; x++) {
				String field = data[x];
				
				if (x == 0) {
					String fieldUft = Converter.convertHexToString(field);
					childnode = finaldoc.createElement("header");
					childnode.setTextContent(fieldUft);
					rootElement.appendChild(childnode);
					
					logger.info("name: header value: " + fieldUft +  " hexValue: " + field );
					
				} else {
					
					String code = codebase + field.substring(0,2);
					String tagNamevalue = PaxusHandler.getAttributesDisplayName(code);
					
					if (!(tagNamevalue != null && !tagNamevalue.isEmpty())) {
						tagNamevalue = "Unknow" + codebase + field.substring(0,2);
					}
					
					
					
					String tagValue = field.substring(2);
					String tagValueUtf = Converter.convertHexToString(tagValue);
					
					logger.info("name: " + tagNamevalue + " value: " + tagValueUtf +  " hexValue: " + tagValue );
					childnode = finaldoc.createElement(tagNamevalue );
					
					if(isResponse){
						childnode.setAttribute(CODE, code);
					} else if(code.equals(TXCODE)){
						
						String tagValueDisplayUtf = PaxusHandler.getTxcodeDisplayName(tagValueUtf);
						
						if (tagValueDisplayUtf != null && !tagValueDisplayUtf.isEmpty()) {
							tagValueUtf = tagValueDisplayUtf;
						} 
					}
					
					childnode.setTextContent(tagValueUtf);
					rootElement.appendChild(childnode);
				}
			}
			
			finaldoc.appendChild(rootElement);
			NodeList resultnl = finaldoc.getElementsByTagName(rootName);
			return CustomExtensionsHandler.nodeListToString(resultnl);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	public static String xmlToObjectMessage (String message){
		
		String lastCharacter = getConfigDataProtocol(LASTCHARACTER);
		
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dbFactory.newDocumentBuilder();
			StringReader sr = new StringReader(message);
			doc = dBuilder.parse(new InputSource(sr));
			
			NodeList nodeList = doc.getElementsByTagName("*");
			Element currentElement = doc.getDocumentElement();
			
			StringBuffer response = new StringBuffer();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					// do something with the current element

					currentElement = (Element) node;
					String name = currentElement.getNodeName();

					if (currentElement.getChildNodes().getLength() == 1) {
						String value = currentElement.getTextContent();
//						System.out.println("name: " + name + " value: " + value);
						value = Converter.convertStringToHex(value);
						
						if (name.equals("header")) {
							
							response.append(value);
							
						} else if (!name.equals("messageLength")){
							
							String code = currentElement.getAttribute(CODE);
							if (value != null) {
								response.append(code);
								response.append(value);
							}
						}
					}
				}
			}
			
			String respuesta = response.toString() + lastCharacter;
			
			return respuesta;
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String getAttributesDisplayName(String key) {
		if (StringUtils.indexOf(key, "Unknown (") == 0) {
			return "Unknown";
		}

		String value = null;

		synchronized (PaxusHandler.class) {
			if (paxusDisplayNameBundle == null) {
				try {
					paxusDisplayNameBundle = ResourceBundle
							.getBundle("co.com.extensions.dph.paxus.paxus-attribtues-display");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		try {
			value = paxusDisplayNameBundle.getString(key);
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			value = "Unknown";
		}

		value = StringUtils.trim(value);
		return value;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String getTxcodeDisplayName(String key) {
		if (StringUtils.indexOf(key, "Unknown (") == 0) {
			return "Unknown";
		}

		String value = null;

		synchronized (PaxusHandler.class) {
			if (paxusDisplayTxNameBundle == null) {
				try {
					paxusDisplayTxNameBundle = ResourceBundle
							.getBundle("co.com.extensions.dph.paxus.paxus-tx-codes-display");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		try {
			value = paxusDisplayTxNameBundle.getString(key);
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			value = "Unknown";
		}

		value = StringUtils.trim(value);
		return value;
	}
	
	
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String getConfigDataProtocol(String key) {
		if (StringUtils.indexOf(key, "Unknown (") == 0) {
			return "Unknown";
		}

		String value = null;

		synchronized (PaxusHandler.class) {
			if (paxusConfigNameBundle == null) {
				try {
					paxusConfigNameBundle = ResourceBundle
							.getBundle("co.com.extensions.dph.paxus.paxus-config");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		try {
			value = paxusConfigNameBundle.getString(key);
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			value = "Unknown";
		}

		value = StringUtils.trim(value);
		return value;
	}
	
	public static void main(String[] args) {
		
		try{
			
		
			String mesage = "003442524e2d3031342d57532d32312020201c803933303030301c81205353445341444d1c8231343030313720201cae301cb0303403";
			

			byte[] payloadBody = Converter.convertHexToByte(mesage);
			
			String xmlMessage = PaxusHandler.parseMessage(payloadBody, false);
			System.out.println(xmlMessage);
			
			xmlMessage = PaxusHandler.xmlToObjectMessage(xmlMessage);
			System.out.println(xmlMessage);
			
			
			String xmlMessageRsp = PaxusHandler.parseMessage(payloadBody, true);
			System.out.println(xmlMessageRsp);
			
			xmlMessageRsp = PaxusHandler.xmlToObjectMessage(xmlMessageRsp);
			System.out.println(xmlMessageRsp);
			String mensaje = mesage.substring(4);
			System.out.println(mensaje);
			
			
			if(mensaje.equalsIgnoreCase(xmlMessageRsp)){
				System.out.println("Well Done!!!");
			}
			
			
			
		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}
	
}
