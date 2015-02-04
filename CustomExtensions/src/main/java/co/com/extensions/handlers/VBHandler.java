/**
 * 
 */
package co.com.extensions.handlers;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
public class VBHandler {

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
	public static String VBParser(String configfile, Object o, int header,
			boolean isBinary, String contentType, String typeMessage)
			throws Exception {

		if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_HEXA)) {

			System.out.println("VB Message HEXA Content");
			return VBHandler.VBParserHex(configfile, o, header, isBinary,
					typeMessage);

		} else if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_ASCII)) {

			System.out.println("VB Message ASCII Content");
			return VBHandler.VBParserString(configfile, o, header, isBinary,
					typeMessage);

		} else {
			throw new Exception("Content Type no Valid");
		}

	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @return
	 */
	private static String VBParserHex(String configfile, Object o, int header,
			boolean isBinary, String typeMessage) {

		System.out.println("Begin to parse VB message...");
		String hexbody = null;

		if (isBinary) {
			hexbody = CustomExtensionsHandler.convertByteToHex((byte[]) o);
		} else {
			hexbody = (String) o;
		}

		System.out.println("Raw Message: " + hexbody);
		System.out.println("Raw Length: " + hexbody.length());
		NodeList nl = null;

		String msgtype = null;
		String mytype = null;
		HashMap<String, String> hm = null;
		NodeList resultnl = null;
		String myheader = "";

		try {

			String encoding = null;
			String parseMessage = null;
			if (header != 0) {
				myheader = hexbody.substring(0, header);
				hexbody = hexbody.substring(header, hexbody.length());
			}

			File file = new File(configfile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document docParse = dBuilder.parse(file);

			// Encoding del Documento
			NodeList iscList = docParse.getElementsByTagName("bms-config");
			Element iscConfig = (Element) iscList.item(0);
			encoding = iscConfig.getAttribute("encoding");

			// Crear documento XML de VB
			DocumentBuilderFactory finalFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
			Document finaldoc = finalBuilder.newDocument();
			Element rootElement = finaldoc.createElement("Message");

			Element headerElement = null;
			if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
				// Se crea el elemento header-request
				headerElement = finaldoc.createElement("header-request");
			} else {
				headerElement = finaldoc.createElement("header-response");
			}

			// Procesar header
			System.out.println("Begin to parse Header VB message...");
			System.out.println("Header length: " + myheader.length());

			System.out.println("Begin to parse Body VB message...");
			System.out.println("Body length: " + hexbody.length());

			if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
				// Se crea el elemento header-request
				nl = docParse.getElementsByTagName("header-request");
			} else {
				nl = docParse.getElementsByTagName("header-response");
			}

			Element headerConfig = (Element) nl.item(0);
			NodeList fieldsConfig = headerConfig.getChildNodes();
			Element fieldConfig = null;
			int fieldstart = 0;
			for (int i = 0; i < fieldsConfig.getLength(); i++) {
				if (fieldsConfig.item(i).getNodeType() == Node.ELEMENT_NODE) {
					fieldConfig = (Element) fieldsConfig.item(i);

					String tagName = fieldConfig.getAttribute("name");
					String tagType = fieldConfig.getAttribute("type");
					String tagLength = fieldConfig.getAttribute("length");
					String tagvalue = null;

					System.out.println("mytagname is: " + tagName + ", "
							+ "mytype is:" + tagType + ", mylength is: "
							+ tagLength + ", start point is: " + fieldstart);

					if (tagLength != null && tagLength.length() > 0
							&& !tagType.equals("HEXA")) {
						tagvalue = CustomExtensionsHandler.convertHexToString(
								myheader.substring(fieldstart, fieldstart
										+ (Integer.parseInt(tagLength) * 2)),
								encoding);
						fieldstart += (Integer.parseInt(tagLength) * 2);

					} else {
						if (tagType.equals("HEXA")) {
							tagvalue = myheader
									.substring(fieldstart, fieldstart
											+ (Integer.parseInt(tagLength) * 2));
							fieldstart += (Integer.parseInt(tagLength) * 2);
						}
					}

					if (tagName.equals("CODTRN")) {
						rootElement.setAttribute("id", tagvalue);
						parseMessage = tagvalue;

					}

					System.out.println("tagvalue: " + tagvalue);
					Element childnode = finaldoc.createElement(tagName);
					childnode.setAttribute("length", tagLength);
					childnode.setAttribute("type", tagType);
					childnode.setTextContent(tagvalue);

					headerElement.appendChild(childnode);

				}
			}

			int faltantesHeader = myheader.length() - fieldstart;
			if (faltantesHeader > 0) {

				Element childnode = finaldoc.createElement("EXTRA_FILLER");
				childnode.setAttribute("length",
						Integer.toString(faltantesHeader));
				childnode.setAttribute("type", "HEXA");

				String tagvalue = hexbody.substring(fieldstart,
						myheader.length());

				childnode.setTextContent(tagvalue);

				headerElement.appendChild(childnode);
			}
			rootElement.appendChild(headerElement);

			// Procesar Body
			System.out.println("Begin to parse Body VB message...");
			System.out.println("Body length: " + hexbody.length());
			// Se crea el elemento body

			Element requestElement = null;
			boolean processBody = true;
			if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
				// Se crea el elemento header-request
				requestElement = finaldoc.createElement("body-request");
			} else {
				requestElement = finaldoc.createElement("body-response");
			}

			nl = docParse.getElementsByTagName("parse");
			Element parseConfig = null;
			// if (typeMessage.equals(BancoBogotaHandler.TYPE_MESSAGE_REQUEST))
			// {

			for (int i = 0; i < nl.getLength(); i++) {
				parseConfig = (Element) nl.item(i);
				// String headertype = "header";
				mytype = parseConfig.getAttribute("type");
				if (mytype.equals(parseMessage)) {
					System.out.println("got a match parsing from config file: "
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
				System.out
						.println("I didn't get a match parsing from config file");
			}
			// } else if (typeMessage
			// .equals(BancoBogotaHandler.TYPE_MESSAGE_RESPONSE)) {
			//
			// boolean quiebre = false;
			// nl = docParse.getElementsByTagName("parse");
			// Element myElement = null;
			// for (int i = 0; i < nl.getLength(); i++) {
			// myElement = (Element) nl.item(i);
			// NodeList hijos = myElement.getChildNodes();
			// parseConfig = myElement;
			// for (int j = 0; j < hijos.getLength(); j++) {
			// Node hijo = hijos.item(j);
			// if (hijo.hasAttributes()) {
			//
			// Attr attr = (Attr) hijo.getAttributes()
			// .getNamedItem("length");
			// if (attr != null) {
			// String length = attr.getValue();
			// if ((length != null)
			// && (hexbody.length() == Integer
			// .parseInt(length))) {
			// parseConfig = myElement;
			// quiebre = true;
			// break;
			// }
			// }
			// }
			// }
			// if (quiebre) {
			// break;
			// }
			// }
			// }

			if (processBody) {

				Element requestConfig = null;
				if (typeMessage.equals(CustomExtensionsHandler.TYPE_MESSAGE_REQUEST)) {
					// Se crea el elemento header-request
					requestConfig = (Element) parseConfig.getElementsByTagName(
							"body-request").item(0);
				} else if (typeMessage
						.equals(CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE)) {

					Element reponseNode = null;
					reponseNode = (Element) parseConfig.getElementsByTagName(
							"body-response").item(0);

					requestConfig = reponseNode;
				}

				fieldsConfig = requestConfig.getChildNodes();
				fieldConfig = null;
				fieldstart = 0;
				try {
					for (int i = 0; i < fieldsConfig.getLength(); i++) {
						if (fieldsConfig.item(i).getNodeType() == Node.ELEMENT_NODE) {
							fieldConfig = (Element) fieldsConfig.item(i);

							String tagName = fieldConfig.getAttribute("name");
							String tagType = fieldConfig.getAttribute("type");
							String tagLength = fieldConfig
									.getAttribute("length");
							String tagvalue = null;

							System.out.println("mytagname is: " + tagName
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

							System.out.println("tagvalue: " + tagvalue);
							Element childnode = finaldoc.createElement(tagName);
							childnode.setAttribute("length", tagLength);
							childnode.setAttribute("type", tagType);
							childnode.setTextContent(tagvalue);

							requestElement.appendChild(childnode);
						}
					}
				} catch (Exception e) {
					System.out
							.println("EL Body esta mas grande que lo configurado");
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
			resultnl = finaldoc.getElementsByTagName("Message");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return CustomExtensionsHandler.nodeListToString(resultnl);
	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @return
	 */
	private static String VBParserString(String configfile, Object o,
			int header, boolean isBinary, String typeMessage) {
		return null;
	}

	public static Object VBXmlToObject(String myfile, boolean isFile)
			throws Exception {

		System.out.println("start processing: " + myfile);

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NodeList nodeList1 = doc.getElementsByTagName("*");
		Element currentElement = doc.getDocumentElement();

		String nodetype;
		String length;
		// String nodelength1;
		String fieldlength1;
		String emvtag = null;
		String emvpadding = "";
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
				length = currentElement.getAttribute("length");

				if (currentElement.getChildNodes().getLength() == 1) {
					value = currentElement.getTextContent();
					System.out.println("name: " + name + " value: " + value);
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

				System.out.println(response.toString());

			}
		}
		return response.toString();
	}

	// protected String getString (String tagName, Element element){
	// NodeList list = element.getElementsByTagName(tagName);
	// if(list != null && list.getLength() > 0){
	// NodeList subliList = list.item(0).getChildNodes();
	// }
	// }

}
