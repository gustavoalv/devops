/**
 * 
 */
package co.com.extensions.handlers;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 * 
 */
public class BMSHandler {
	
	private static Logger logger = Logger.getLogger(BMSHandler.class);
	
	private static final String RESPONSE_ATTRIBUTE = "response";
	private static final String REQUEST_ATTRIBUTE = "request";
	
	private static final String BMS_CONFIG_ROOT_ELEMENT = "bms-config";
	private static final String BMS_CONFIG_ENCODING_ATTRIBUTE = "encoding";
	public static final String BMS_MESSAGE_ROOT_ELEMENT = "bms-message";
	public static final String BMS_UNKNOW_ROOT_ELEMENT = "unknow-bms";

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @param contentType
	 * @return
	 * @throws Exception
	 */
	public static String BMSParser(String configfile, Object o, String formato,boolean isBinary, String contentType, String typeMessage){
		
		if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_HEXA)) {

			logger.debug("BMS Message HEXA Content");
			return BMSHandler.BMSParserHex(configfile, o, formato,isBinary, typeMessage);

		} 
		
		logger.error("El Content-Type para analizar el mensage BMS no es válido");
		return null;

	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @return
	 */
	private static String BMSParserHex(String configfile, Object o, String formato, boolean isBinary, String typeMessage) {

		logger.debug("Begin to parse BMS message...");
		String hexbody = null;

		if (isBinary) {
			hexbody = CustomExtensionsHandler.convertByteToHex((byte[]) o);
		} else {
			hexbody = (String) o;
		}

		logger.debug("Raw Message: " + hexbody);
		logger.debug("Raw Length: " + hexbody.length());
		NodeList nl = null;

		String mytype = null;
		NodeList resultnl = null;

		try {

			String encoding = null;

			File file = new File(configfile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document docParse = dBuilder.parse(file);

			// Encoding del Documento
			NodeList iscList = docParse.getElementsByTagName(BMS_CONFIG_ROOT_ELEMENT);
			Element iscConfig = (Element) iscList.item(0);
			encoding = iscConfig.getAttribute(BMS_CONFIG_ENCODING_ATTRIBUTE);

			// Crear documento XML de BMS
			DocumentBuilderFactory finalFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
			Document finaldoc = finalBuilder.newDocument();
			Element rootElement = finaldoc.createElement(BMS_MESSAGE_ROOT_ELEMENT);


			// Procesar Body
			logger.debug("Begin to parse Body BMS message...");
			logger.debug("Body length: " + hexbody.length());

			Element requestElement = null;
			boolean processBody = true;
			if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
				// Se crea el elemento header-request
				requestElement = finaldoc.createElement(REQUEST_ATTRIBUTE);
			} else {
				requestElement = finaldoc.createElement(RESPONSE_ATTRIBUTE);
			}

			nl = docParse.getElementsByTagName("parse");
			Element parseConfig = null;
			// if (typeMessage.equals(BancoBogotaHandler.TYPE_MESSAGE_REQUEST))
			// {
			String atributo = null;
			atributo = RESPONSE_ATTRIBUTE;
			if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
				atributo = REQUEST_ATTRIBUTE;
				
			}
			
			for (int i = 0; i < nl.getLength(); i++) {
				parseConfig = (Element) nl.item(i);
				// String headertype = "header";
				mytype = parseConfig.getAttribute(atributo);
				if (mytype.equals(formato)) {
					logger.debug("got a match parsing from config file: "
							+ i);

					parseConfig = (Element) nl.item(i);
					break;
				} else {
					parseConfig = null;
				}
			}

			

			if (parseConfig == null) {
				String bodyMessage = CustomExtensionsHandler
						.convertHexToString(hexbody);
				requestElement.setTextContent(bodyMessage);
				processBody = false;
				logger.error("I didn't get a match parsing from config file");
				return null;
			}

			if (processBody) {

				Element requestConfig = null;
				if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
					// Se crea el elemento header-request
					requestConfig = (Element) parseConfig.getElementsByTagName(REQUEST_ATTRIBUTE).item(0);
				} else if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE)) {

					Element reponseNode = null;
					reponseNode = (Element) parseConfig.getElementsByTagName(RESPONSE_ATTRIBUTE).item(0);

					requestConfig = reponseNode;
				}
				
				NodeList fieldsConfig = requestConfig.getChildNodes();
				Element fieldConfig = null;
				int fieldstart = 0;

				try {
					for (int i = 0; i < fieldsConfig.getLength(); i++) {
						if (fieldsConfig.item(i).getNodeType() == Node.ELEMENT_NODE) {
							fieldConfig = (Element) fieldsConfig.item(i);

							String tagName = fieldConfig.getAttribute("name");
							String tagType = fieldConfig.getAttribute("type");
							String tagLength = fieldConfig
									.getAttribute("length");
							String tagvalue = null;

							logger.debug("mytagname is: " + tagName
									+ ", " + "mytype is:" + tagType
									+ ", mylength is: " + tagLength
									+ ", start point is: " + fieldstart);

							if (tagLength != null && tagLength.length() > 0
									&& !tagType.equals("HEXA")) {
								tagvalue = CustomExtensionsHandler
										.convertHexToString(
												hexbody.substring(
														fieldstart,
														fieldstart
																+ (Integer
																		.parseInt(tagLength) * 2)),
												encoding);
								fieldstart += (Integer.parseInt(tagLength) * 2);

							} else {
								if (tagType.equals("HEXA")) {
									tagvalue = hexbody
											.substring(
													fieldstart,
													fieldstart
															+ (Integer
																	.parseInt(tagLength) * 2));
									fieldstart += (Integer.parseInt(tagLength) * 2);
								}
							}

							logger.debug("tagvalue: " + tagvalue);
							Element childnode = finaldoc.createElement(tagName);
							childnode.setAttribute("length", tagLength);
							childnode.setAttribute("type", tagType);
							childnode.setTextContent(tagvalue);

							requestElement.appendChild(childnode);
						}
					}
				} catch (Exception e) {
					logger.error("EL Body esta mas grande que lo configurado");
				}

				int faltantes = hexbody.length() - fieldstart;
				if (faltantes > 0) {

					Element childnode = finaldoc.createElement("EXTRA_FILLER");
					childnode.setAttribute("length",
							Integer.toString(faltantes));
					childnode.setAttribute("type", "HEXA");

					String tagvalue = hexbody.substring(fieldstart,
							hexbody.length());

					childnode.setTextContent(tagvalue);

					requestElement.appendChild(childnode);
				}
			}

			rootElement.appendChild(requestElement);

			finaldoc.appendChild(rootElement);
			resultnl = finaldoc.getElementsByTagName(BMS_MESSAGE_ROOT_ELEMENT);

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}

		return CustomExtensionsHandler.nodeListToString(resultnl);
	}

	/**
	 * 
	 * @param myfile
	 * @param isFile
	 * @return
	 * @throws Exception
	 */
	public static Object BMSXmlToObject(String myfile, boolean isFile)
			throws Exception {

		logger.debug("start processing: " + myfile);

		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();

			dBuilder = dbFactory.newDocumentBuilder();
			if (isFile) {
				File f = new File(myfile);
				doc = dBuilder.parse(f);
			} else {
				StringReader sr = new StringReader(myfile);
				doc = dBuilder.parse(new InputSource(sr));
			}
		} catch (ParserConfigurationException e) {
			throw e;
		}

		NodeList nodeList1 = doc.getElementsByTagName("*");
		Element currentElement = doc.getDocumentElement();

		String nodetype;
		// String nodelength1;
		String value = "";
		String name;

		StringBuffer response = new StringBuffer();

		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node = nodeList1.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element

				currentElement = (Element) node;
				name = currentElement.getNodeName();
				nodetype = currentElement.getAttribute("type");
//				String length = currentElement.getAttribute("length");

				if (currentElement.getChildNodes().getLength() == 1) {
					value = currentElement.getTextContent();
					logger.debug("name: " + name + " value: " + value);
					if (!nodetype.equals("HEXA")) {
						if (value != null) {
							value = Converter
									.convertStringToHex(value);
							response.append(value);
						}
					} else {
						response.append(value);
					}
				}

				logger.debug(response.toString());

			}
		}
		return response.toString();
	}


}
