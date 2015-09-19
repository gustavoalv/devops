package co.com.extensions.handlers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import co.com.extensions.util.ElementISOWrapper;
import co.com.extensions.util.ElementoISO8583;

import com.google.common.base.Charsets;
import com.itko.citi.Converter;

public class ISO8583Handler {
	
	
	private static Logger log = Logger.getLogger(ISO8583Handler.class);
	

	public static String getENCODING() {
		return ENCODING;
	}

	public void setENCODING(String eNCODING) {
		ENCODING = eNCODING;
	}

	public static String getConfigXml() {
		return CONFIG_XML;
	}

	public static String getCONFIG_XML() {
		return CONFIG_XML;
	}

	public void setCONFIG_XML(String cONFIG_XML) {
		CONFIG_XML = cONFIG_XML;
	}

	protected static String ENCODING = "";
	protected String EncodingParamDefault = Charsets.US_ASCII.name();
	private static String CONFIG_XML = "8583-config.xml";

	public static HashMap<String, String> readBitmap(String hexdata,boolean trace) {
		
		HashMap<String, String> hm = new HashMap<String, String>();
		String binaryresult = "";
		for (int i = 0; i < hexdata.length(); i++) {
			
			String hexa = hexdata.substring(i, i + 1).toUpperCase();
			binaryresult += CustomExtensionsHandler.convertHexToBinary(hexa.toUpperCase());

		}
		if (trace) {
//			System.out.println("binary result is: " + binaryresult);
		}
		
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < binaryresult.length(); j++) {
			int temp = Integer.parseInt(binaryresult.substring(j, j + 1));

			if (temp == 1) {
				hm.put(String.valueOf(j + 1), String.valueOf(j + 1));
				sb.append(j + 1).append(",");
			}
		}
		if (trace) {
			System.out.println("binary result is: " + sb.toString());
		}
		return hm;
	}

	public static Object ISO8583XmlToObject(String myfile, String direction, boolean isFile, boolean isBinary) throws Exception {

		log.info("Start processing: " + myfile);

		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

			dBuilder = dbFactory.newDocumentBuilder();
			if (isFile) {
				File f = new File(myfile);
				doc = dBuilder.parse(f);
			} else {
				StringReader sr = new StringReader(myfile);
				doc = dBuilder.parse(new InputSource(sr));
			}
		} catch (ParserConfigurationException e) {
			
			log.error("Error creando factory");
			throw new RuntimeException(e);
		}

		NodeList nodeList1 = doc.getElementsByTagName("*");
//		Element currentElement1;
//		String nodetype1;
//		String nodenum1;
		// String nodelength1;
//		String fieldlength1;
//		String emvtag = null;
		String emvpadding = "";
		// StringBuffer rqsb = new StringBuffer();
		/*HashMap<String, String> rqhm = new HashMap<String, String>();

		// System.out.println(nodeList.getLength());
		
		//Saca todo los campos que van en el XML y los pone en el HashMap rqhm
		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node = nodeList1.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				currentElement1 = (Element) node;
				nodetype1 = currentElement1.getAttribute("type");
				nodenum1 = currentElement1.getAttribute("num");
				fieldlength1 = currentElement1.getAttribute("length");
			
				if (nodetype1.contains("VAR") || nodetype1.contains("BITMAP")) {
					rqhm.put(nodenum1, fieldlength1);
					log.info("field: " + nodenum1 + " field length is: " + fieldlength1);
				}

			}
		}
		*/

		// process response;
		//
		Element currentElement;
		String msgtype = null;
		String myheader = "";
		String nodenum = null;
		String roottype;
//		String nodetype;
//		String nodelength = null;
		String fieldlength = null;
//		String encoding = null;

		StringBuffer sb = new StringBuffer();
//		StringBuffer sbemv = new StringBuffer();
		HashMap<String, String> temphm = new HashMap<String, String>();
		HashMap<String, String> emvhm = new HashMap<String, String>();
		// System.out.println(nodeList.getLength());
		// NodeList nodeList2 = doc.getElementsByTagName("*");
		Element e1, e2 = null;
		
		log.info("begin to process field 123...");
		
		
		// Procesa el campo 123
		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node = nodeList1.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				e1 = (Element) node;
				if (e1.getAttribute("num").equals("114")) {
					e2 = e1;
				} else if (e1.getAttribute("num").equals("123") && e2 != null
						&& e1 != null) {
					if (!e1.getTextContent().contains("{")
							&& e1.getTextContent().length() > 0) {
						if (CustomExtensionsHandler.convertISOCountry(e1.getTextContent().substring(2, 4)) != null) {
							
							if (log.isDebugEnabled()){
								log.debug("Where are in 123 field");
								log.debug("reset country code: " + CustomExtensionsHandler.convertISOCountry(e1.getTextContent().substring(2, 4)));
							}
							e2.setTextContent(CustomExtensionsHandler.convertISOCountry(e1.getTextContent().substring(2, 4)));
							
						}
					}
				}

			}
		}
		
		//Construye el segundo Bitmap del XML
		NodeList nodeList = doc.getElementsByTagName("*");
		// process for F65
		Node F65 = null;
		String emvflag = null;
		Element tcurrentElement;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				tcurrentElement = (Element) node;
				String num = tcurrentElement.getAttribute("num");
				if (num.equals("65")) {
					emvflag = tcurrentElement.getTextContent();
					if (emvflag != null && emvflag.length() > 0) {
						F65 = tcurrentElement;
					}
				}

				if (emvflag != null && emvflag.length() > 0 && Integer.parseInt(num) > 128) {
					// process bitmap3 if exists
					emvhm.put(num, num);
				}

			}
		}
		// set F65 value after bitmap calculation
		if (emvflag != null && emvflag.length() > 0) {
			
			if (log.isDebugEnabled()){
				log.debug("Begin to process bitmap3");
			}

			if (emvhm.size() > 0) {
				StringBuffer btsb1 = new StringBuffer();
				// btsb1.append("1");
				for (int r = 129; r < 193; r++) {
					if (emvhm.containsKey(String.valueOf(r))) {
						btsb1.append("1");
					} else {
						btsb1.append("0");
					}
				}

				String binarydata1 = btsb1.toString();
				String bitdata1 = "";
				
				if (log.isDebugEnabled()){
					log.debug("bitmap3: " + binarydata1);
				}
				
				for (int e = 0; e < binarydata1.length(); e += 4) {
					// System.out.println("currentHex: "+binarydata.substring(e,e+4));
					bitdata1 += CustomExtensionsHandler.convertBinaryToHex(binarydata1.substring(e, e + 4));
				}
				emvpadding = bitdata1;
				log.info("Processing Bitmap3 variable emvpadding: " + emvpadding);
				F65.setTextContent(emvpadding);
			}
		}
		
		NodeList nodeMessage = doc.getElementsByTagName("Message");
		if(nodeMessage!=null){
			Element messageElement = (Element)nodeMessage.item(0);
			roottype = messageElement.getAttribute("mti");
			if (roottype != null && roottype.length() > 0) {
				msgtype = roottype;
			}
		}
		
		NodeList nodeHeader = doc.getElementsByTagName("Header");
		if(nodeHeader!=null){
			Element headerElement = (Element)nodeHeader.item(0);
			NodeList nodesChildsHeader = headerElement.getChildNodes();
			for (int i = 0; i < nodesChildsHeader.getLength(); i++) {
				Node nodesChildHeader = nodesChildsHeader.item(i);
				if (nodesChildHeader.getNodeType() == Node.ELEMENT_NODE) {
					
					currentElement = (Element) nodesChildHeader;
					if (currentElement.hasAttributes()) {
						
						ElementoISO8583 elemento = ElementoISO8583.getElemento(currentElement);
						fieldlength = getLenthFromVariableAttribute( elemento);
						setValueFromElemento(direction, fieldlength,elemento);
						String hexvalue = elemento.getValue();
						
						sb.append(hexvalue);
						log.info("Header: after conversion: " + hexvalue);
					}
				}
			}
		}
		
		myheader = sb.toString();
		sb = new StringBuffer();
		
		NodeList nodeBody = doc.getElementsByTagName("Body");
		if(nodeBody != null){
			Element bodyElement = (Element)nodeBody.item(0);
			NodeList nodesChildsBody = bodyElement.getChildNodes();
			for (int i = 0; i < nodesChildsBody.getLength(); i++) {
				Node nodesChildBody = nodesChildsBody.item(i);
				if (nodesChildBody.getNodeType() == Node.ELEMENT_NODE) {
					currentElement = (Element) nodesChildBody;
					if (currentElement.hasAttributes()) {
						
						ElementoISO8583 elemento = ElementoISO8583.getElemento(currentElement);
						if (Integer.parseInt(elemento.getNum()) <= 128) {
							
							fieldlength = getLenthFromVariableAttribute( elemento);
							setValueFromElemento(direction, fieldlength,elemento);
							
							String valor = elemento.getValue();
							sb.append(valor);
							log.info("Body: after conversion: " + valor);
							temphm.put(elemento.getNum(), elemento.getNum());
							nodenum = elemento.getNum();
							
							log.info("Message: " + sb.toString());
						}
					}
				}
			}
		}
		
		String data = sb.toString();
			
		// process bitmap1 and bitmap2
		log.info("process bitmap1 and bitmap2");
		StringBuffer btsb = new StringBuffer();

		int bitmappoint = 0;
		if (Integer.parseInt(nodenum) > 64) {
			bitmappoint = 129;
			btsb.append("1");
		} else {
			bitmappoint = 65;
			btsb.append("0");
		}
		for (int r = 2; r < bitmappoint; r++) {
			if (temphm.containsKey(String.valueOf(r))) {
				btsb.append("1");
			} else {
				btsb.append("0");
			}
		}

		String binarydata = btsb.toString();
		String bitdata = "";
		log.info("bitmap1 and 2: " + binarydata);
		for (int e = 0; e < binarydata.length(); e += 4) {
			// System.out.println("currentHex: "+binarydata.substring(e,e+4));
			bitdata += CustomExtensionsHandler.convertBinaryToHex(binarydata
					.substring(e, e + 4));
		}
		log.info("bitmap1 and 2 Hex: " + bitdata);

		// bitdata = BancoBogotaHandler.convertStringToHex(bitdata);
		// Bitdata esta en HEX
		log.info("bitmap1 and 2 String: " + bitdata);

//		String datahex = sb.toString() + sbemv.toString();
		

		String finaldata = (msgtype + bitdata + data).toUpperCase();
		/*
		 * int datalength = finaldata.length() / 2; String finalheader =
		 * Integer.toHexString(datalength); if (finalheader.length() < 4) {
		 * String padding = ""; for (int n = 0; n < 4 - finalheader.length();
		 * n++) { padding += "0"; } finalheader = padding + finalheader; }
		 */
		finaldata = (myheader + msgtype + bitdata + data).toUpperCase();
		
		log.info("myheader: " + myheader);
		log.info("message type Hex: " + msgtype);
		log.info("bitmap data Hex: " + bitdata);
		log.info("data field Hex: " + data);
		log.info("final data: " + finaldata);

		if (isBinary) {
			return CustomExtensionsHandler.convertHexToByte(finaldata);
		} else {
			return finaldata;
		}
	}

	/**
	 * @param direction
	 * @param nodenum
	 * @param nodelength
	 * @param fieldlength
	 * @param encoding
	 * @param sb
	 * @param temphm
	 * @param elemento
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static void setValueFromElemento(String direction, String fieldlength, ElementoISO8583 elemento) throws Exception {
		
		if(elemento.hasChilds()){
			List<ElementoISO8583> hijos = elemento.getHijos().getFields();
			for (Iterator<ElementoISO8583> iteratorHijos = hijos.iterator(); iteratorHijos.hasNext();) {
				ElementoISO8583 hijo = (ElementoISO8583) iteratorHijos.next();
				setValueFromElemento(direction, fieldlength , hijo);
			}
		} else {
		
			String nodetype = elemento.getType();
			String nodelength = elemento.getLength();
			String nodevalue = elemento.getValue();
			
			if(log.isDebugEnabled()){
				log.debug("before conversion: " + nodevalue);
			}
			String valor = "";
			if (!nodevalue.contains("{")) {
				int templength = nodevalue.length();
				System.out.println("value length: " + templength);
				String finalpadding = "";
				String padding;
				
				if (nodetype.contains("LVAR")){
					
					valor = fieldlength + nodevalue;
					
				} else if (nodetype.contains("LBIN")){
					
					valor = fieldlength + Converter.convertStringToHex(nodevalue);
					
				}else if (nodelength != null && nodelength.length() > 0 ) {
					
					// Es un valor de tipo ALPHA, DATE, NUMERIC o BINARY
					
					int mylength = Integer.parseInt(nodelength);
					
					if (templength < mylength) {
						if (nodetype.equals("NUMERIC")) {
							padding = "0";
						} else {
							padding = " ";
						}
						for (int p = 0; p < mylength - templength; p++) {
							finalpadding += padding;
						}
					}
					
					if(nodetype.contains("BINARY")) {
						nodevalue = CustomExtensionsHandler.convertStringToHex(nodevalue);
					}
					
					valor = finalpadding + nodevalue;
					
					if (direction.equals("right")) {
						valor = nodevalue + finalpadding;
					} 
					
				}
				
				elemento.setValue(valor);
			}
		}
	}
			
	/**
	 * Se obtiene el length de LLVAR, LLLVAR y LLBIN, apartir de largo del valor y lo rellena con ceros
	 * @param nodevalue
	 * @param fieldlength
	 * @param elemento
	 * @return
	 */
	private static String getLenthFromVariableAttribute( ElementoISO8583 elemento) {
		
		String fieldlength = null;
		
		if(elemento.hasChilds()){
			List<ElementoISO8583> hijos = elemento.getHijos().getFields();
			for (Iterator<ElementoISO8583> iteratorHijos = hijos.iterator(); iteratorHijos.hasNext();) {
				ElementoISO8583 hijo = (ElementoISO8583) iteratorHijos.next();
				return getLenthFromVariableAttribute(hijo);
			}
		} else {
			String nodelength;
			String nodenum = elemento.getNum();
			String nodetype = elemento.getType();
			String nodevalue = elemento.getValue();
			
			if (nodetype.contains("LVAR") && !nodenum.equals("123")) {
				nodelength = String.valueOf(nodevalue.length());
				if (nodetype.contains("LLLLVAR")) {
					int t = nodelength.length();
					String pad = "";
					if (t != 4) {
						for (int c = 0; c < 4 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
				} else if (nodetype.contains("LLLVAR")) {
					int t = nodelength.length();
					String pad = "";
					if (t != 3) {
						for (int c = 0; c < 3 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
				} else if (nodetype.contains("LLVAR")) {
					int t = nodelength.length();
					String pad = "";
					if (t != 2) {
						for (int c = 0; c < 2 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
				} else {
					fieldlength = String
							.valueOf(nodevalue.length());
				}
			} else if (nodetype.contains("LBIN")) {
				nodelength = String.valueOf(nodevalue.length());
				
				if (nodetype.contains("LLLLBIN")) {
					int t = nodelength.length();
					String pad = "";
					if (t != 4) {
						for (int c = 0; c < 4 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
				} else if (nodetype.contains("LLLBIN")) {
					int t = nodelength.length();
					String pad = "";
					if (t != 3) {
						for (int c = 0; c < 3 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
				} else if (nodetype.contains("LLBIN")) {
					
					int t = nodelength.length();
					String pad = "";
					if (t != 2) {
						for (int c = 0; c < 2 - t; c++) {
							pad += "0";
						}
					}
					fieldlength = pad + nodelength;
					
				} else {
					fieldlength = String
							.valueOf(nodevalue.length());
				}
			} else {
				fieldlength = elemento.getLength();
			}
		}
		return fieldlength;
	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @return
	 */
	private static String ISO8583ParserHex(String configfile, Object o, int header, boolean isBinary) throws Exception{
		
		System.out.println("Begin to parse ISO message...");
		String hexbody = null;
		int bitmapLength = 0;
		int msgtypeLength = 0;
		
		bitmapLength = 32;
		msgtypeLength = 8;
		if (isBinary) {
			hexbody = CustomExtensionsHandler.convertByteToHex((byte[]) o);
		} else {
			hexbody = (String) o;
		}

		System.out.println(hexbody);
		NodeList nl = null;
		Element tempe = null;
		String msgtype = null;
		String mytype = null;
		HashMap<String, String> hm = null;
		NodeList resultnl = null;
		String myheader = null;

		if (header != 0) {
			myheader = hexbody.substring(0, header);
			hexbody = hexbody.substring(header, hexbody.length());
			System.out.println("MyHeader: " + myheader);
			System.out.println("HexBody: " + hexbody);
		}

		String bitmap = null;
		msgtype = CustomExtensionsHandler.convertHexToString(hexbody.substring(
				0, msgtypeLength));

		bitmap = Converter.convertHexToString(hexbody.substring(
				msgtypeLength, msgtypeLength + bitmapLength));

		// Se pasa a valores HEX en ASCII
		hm = readBitmap(bitmap, false);

		// There is secondary bitmap?
		if (hm.containsKey("1")) {
			bitmapLength = bitmapLength * 2;
			bitmap = Converter.convertHexToString(hexbody.substring(
					msgtypeLength, msgtypeLength + bitmapLength));
		}

		hm = readBitmap(bitmap, true);

		// hm = readBitmap(hexbody.substring(8, 40));
		System.out.println("msg type is: " + msgtype);

		File file = new File(configfile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		nl = doc.getElementsByTagName("parse");

		System.out.println("how many parsing config do i have? "
				+ nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			tempe = (Element) nl.item(i);
			// String headertype = "header";
			mytype = tempe.getAttribute("mti");
			if (mytype.equals(msgtype)) {
				System.out.println("got a match parsing from config file: "
						+ i);
				break;
			} else {
				tempe = (Element) nl.item(0);
			}
		}

		// create new xml document
		DocumentBuilderFactory finalFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
		Document finaldoc = finalBuilder.newDocument();
		Element rootElement = finaldoc.createElement("Message");
		
		
		Element bodyElement = finaldoc.createElement("Body");
		Element headerElement = finaldoc.createElement("Header");
		rootElement.setAttribute("mti", msgtype);
//		rootElement.setAttribute("header", myheader);
		
		finaldoc.appendChild(rootElement);
		
		// hexbody =
		// convertHexToString(hexbody.substring(40,hexbody.length()));
		hexbody = hexbody.substring(msgtypeLength + bitmapLength,
				hexbody.length());
		System.out.println("fieldstring is: " + hexbody);
		System.out.println("i am using the parser as: " + tempe.getAttribute("mti"));
		System.out.println("c my name is: " + tempe.getNodeName());
		System.out.println("c how many childs I have: " + tempe.getChildNodes().getLength());
		// System.out.println(nodeToString(tempe));
		
		List<ElementoISO8583> elementos = new ArrayList<ElementoISO8583>();
		
		if (tempe!= null) {
			checkChildNodes(hexbody, tempe, hm, elementos);
		}
		
		for (Iterator<ElementoISO8583> iterator = elementos.iterator(); iterator.hasNext();) {
			ElementoISO8583 elementoISO8583 = (ElementoISO8583) iterator.next();
			
			
			Element childnode = finaldoc.createElement(elementoISO8583.getName());
			childnode.setAttribute("length", elementoISO8583.getLength());
			childnode.setAttribute("num", elementoISO8583.getNum());
			childnode.setAttribute("type", elementoISO8583.getType());
			
			if(elementoISO8583.hasChilds())
			{
			
				List<ElementoISO8583> hijos = elementoISO8583.getHijos().getFields();
				for (Iterator<ElementoISO8583> iteratorHijos = hijos.iterator(); iteratorHijos.hasNext();) {
					ElementoISO8583 hijo = (ElementoISO8583) iteratorHijos.next();
					
					Element childnodeHijo = finaldoc.createElement(hijo.getName());
					childnodeHijo.setAttribute("length", hijo.getLength());
					childnodeHijo.setAttribute("num", hijo.getNum());
					childnodeHijo.setAttribute("type", hijo.getType());
					childnodeHijo.setTextContent(hijo.getValue());
					childnode.appendChild(childnodeHijo);
					
				}
			} else {
				
				childnode.setTextContent(elementoISO8583.getValue());
			}
			
			bodyElement.appendChild(childnode);
			
		}
		
		
		if((myheader != null) && (!myheader.equals(""))){ 
		
			int fieldstart = 0;
			
			nl = doc.getElementsByTagName("parse");

			System.out.println("how many parsing config do i have? "
					+ nl.getLength());
			for (int i = 0; i < nl.getLength(); i++) {
				tempe = (Element) nl.item(i);
				// String headertype = "header";
				mytype = tempe.getAttribute("mti");
				if (mytype.equals("header")) {
					System.out.println("got a match parsing from config file: "
							+ i);
					break;
				} else {
					tempe = (Element) nl.item(0);
				}
			}
			
			Element childtemp = null;
			int factor = 2;
			NodeList nodes = tempe.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					childtemp = (Element) nodes.item(i);
					
					String tagname = childtemp.getAttribute("name").replaceAll(" ", "_");
					String tagnum = childtemp.getAttribute("num");
					String tagtype = childtemp.getAttribute("type");
					String taglength = childtemp.getAttribute("length");
					String tagEncoding = childtemp.getAttribute("encoding");
					String taginclude = childtemp.getAttribute("include");
					
					String tagvalue = null;
					String encoding = Charset.defaultCharset().displayName();
					
					if ((tagEncoding != null) && (!tagEncoding.equals(""))) {
						
						encoding = tagEncoding;
					}
					
					if ((taglength == null) || (taglength.length() <= 0)){
						throw new RuntimeException("El valor campo \"length\" del elemento " + tagname + " en el XML esta vacio");
					}
					
					if (tagtype.equals("BINARY")) {
						
						int fieldlength = Integer.parseInt(taglength) * 4;
//						System.out.println(myheader.substring( fieldstart, fieldstart + fieldlength));
						tagvalue = CustomExtensionsHandler.convertHexToString(myheader.substring(fieldstart, fieldstart + fieldlength));
						
						fieldstart += fieldlength;
					} else {
						
						tagvalue = CustomExtensionsHandler.convertHexToString(myheader.substring(fieldstart,fieldstart + Integer.parseInt(taglength) * factor),encoding);
						fieldstart += Integer.parseInt(taglength) * factor;
					}
					
					Element childnode = finaldoc.createElement(tagname);
					childnode.setAttribute("num", tagnum);
					childnode.setAttribute("length", taglength);
					childnode.setAttribute("type", tagtype);
					childnode.setTextContent(tagvalue);
					System.out.println("tagvalue" + tagvalue);
					
					if((taginclude != null) && (!taginclude.equals("")))
					{
						if(Boolean.parseBoolean(taginclude)){
							headerElement.appendChild(childnode);
						}
						
					} else {
						headerElement.appendChild(childnode);
					}
					
					
				}
			}
		}
		
		rootElement.appendChild(headerElement);
		rootElement.appendChild(bodyElement);
		
		resultnl = finaldoc.getElementsByTagName("Message");

		return CustomExtensionsHandler.nodeListToString(resultnl);
	}

	/**
	 * @param hexbody
	 * @param tempe
	 * @param hm
	 * @param elementos
	 * @throws Exception
	 */
	private static void checkChildNodes(String hexbody, Element tempe, HashMap<String, String> hm, List<ElementoISO8583> elementos) throws Exception {
		NodeList nodes = tempe.getChildNodes();
		Element childtemp = null;
		int factor = 2;
		int fieldstart = 0;
		
		for (int i = 0; i < nodes.getLength(); i++) {
			
			if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				childtemp = (Element) nodes.item(i);
				
				if ((hm == null) || (hm.containsKey(childtemp.getAttribute("num")))) {
					
//						System.out.println("Got match lines? " + hm.containsKey(tagnum));
					
					System.out.println(childtemp.getNodeName());
					String tagname = childtemp.getAttribute("name").replaceAll(" ", "_");
					String tagnum = childtemp.getAttribute("num");
					String tagtype = childtemp.getAttribute("type");
					String taglength = childtemp.getAttribute("length");
					
					System.out.println("***** FIELD " + tagnum + "*****");
					System.out.println("Tag Num is: " + tagnum);
					System.out.println("Tag Name is: " + tagname);
					System.out.println("Tag Type is:" + tagtype);
					System.out.println("Tag length is: " + taglength);
					System.out.println("Start Point is: " + fieldstart);
					
					ElementoISO8583 elemento = new ElementoISO8583();
					elemento.setLength(taglength);
					elemento.setName(tagname);
					elemento.setNum(tagnum);
					elemento.setType(tagtype);
					if(childtemp.hasChildNodes()){
						
						ElementISOWrapper elementISOWrapper = new ElementISOWrapper();
						elementISOWrapper.setFields(new ArrayList<ElementoISO8583>());
						
						elemento.setHijos(elementISOWrapper);
					}
					
					fieldstart = processMessage(hexbody, factor, fieldstart, elemento);
					
					elementos.add(elemento);
					
					boolean hasChildNodes = childtemp.hasChildNodes();
					if(hasChildNodes){
						List<ElementoISO8583> elementosHijos = new ArrayList<ElementoISO8583>();
						checkChildNodes(Converter.convertStringToHex(elemento.getValue()),childtemp, null, elementosHijos);
						ElementISOWrapper elementISOWrapper = new ElementISOWrapper();
						elementISOWrapper.setFields(elementosHijos);
						elemento.setHijos(elementISOWrapper);
					}
					
				}
				
				
			}
		}
	}

	/**
	 * @param hexbody
	 * @param factor
	 * @param fieldstart
	 * @param elemento
	 * @return
	 * @throws Exception
	 */
	private static int processMessage(String hexbody, int factor,
			int fieldstart, ElementoISO8583 elemento) throws Exception {
		String tagvalue = null;
		
		String encoding = Charset.defaultCharset().displayName();
		
		if (elemento.getLength() != null && elemento.getLength().length() > 0 && !elemento.getType().equals("BINARY")) {
			
			tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + Integer.parseInt(elemento.getLength()) * factor),encoding);
			fieldstart += Integer.parseInt(elemento.getLength()) * factor;

		} else {

			if (elemento.getType().equals("LBIN")) {

				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 2))) * factor * 2;
				elemento.setLength(String .valueOf((fieldlength / factor)/2));
				System.out.println("LLBIN length is: " + fieldlength);
				tagvalue = hexbody.substring(fieldstart + 2, fieldstart + 2 + fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				fieldstart += 2 + fieldlength;

			} else if (elemento.getType().equals("LLBIN")) {

				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 4))) * factor * 2;
				elemento.setLength(String .valueOf((fieldlength / factor)/2));
				System.out.println("LLBIN length is: " + fieldlength);
				tagvalue = hexbody.substring(fieldstart + 4, fieldstart + 4 + fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				fieldstart += 4 + fieldlength;

			} else if (elemento.getType().equals("LLLBIN")) {

				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 6))) * factor * 2;
				elemento.setLength(String .valueOf((fieldlength / factor)/2));
				System.out.println("LLLBIN length is: " + fieldlength);
				tagvalue = hexbody.substring(fieldstart + 6, fieldstart + 6 + fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				fieldstart += 6 + fieldlength;

			}else if (elemento.getType().equals("LLLLBIN")) {
				
				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 8))) * factor * 2;
				elemento.setLength(String .valueOf((fieldlength / factor)/2));
				System.out.println("LLLLBIN length is: " + elemento.getLength());
				tagvalue = hexbody.substring(fieldstart + 8, fieldstart + 8 + fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				fieldstart += 8 + fieldlength;
				
			} else if (elemento.getType().equals("LVAR")) {
				
				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 2)))* factor;
				elemento.setLength(String.valueOf(fieldlength / factor));
				System.out.println("LVAR length is: "+ fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 2, fieldstart + 2 + fieldlength), encoding);
				fieldstart += 2 + fieldlength;
				
			} else if (elemento.getType().equals("LLVAR")) {
				
//								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 4));
				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 4)))* factor;
				elemento.setLength(String.valueOf(fieldlength / factor));
				System.out.println("LLVAR length is: "+ elemento.getLength());
				tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 4, fieldstart + 4 + fieldlength), encoding);
				fieldstart += 4 + fieldlength;
				
			} else if (elemento.getType().equals("LLLVAR")) {
				
				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 6))) * factor;
				elemento.setLength(String .valueOf(fieldlength / factor));
				System.out.println("LLLVAR length is: " + elemento.getLength());
				tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 6, fieldstart + 6 + fieldlength), encoding);
				fieldstart += 6 + fieldlength;
				
			} else if (elemento.getType().equals("LLLLVAR")) {
				
				int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 8))) * factor;
				elemento.setLength(String .valueOf(fieldlength / factor));
				System.out.println("LLLVAR length is: " + elemento.getLength());
				tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 8, fieldstart + 8 + fieldlength), encoding);
				fieldstart += 8 + fieldlength;
				
			} else if (elemento.getType().equals("BITMAP")) {

				tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring( fieldstart, fieldstart + 32));
				int fieldlength = tagvalue.length() * factor;
				elemento.setLength(String.valueOf(fieldlength));
				System.out.println("BITMAP length is: " + fieldlength / factor);
				fieldstart += fieldlength;

			} else if (elemento.getType().equals("BINARY")) {
				
				int fieldlength = Integer.parseInt(elemento.getLength()) * factor * 2;
				System.out.println("BINARY length is: " + fieldlength / factor);
				tagvalue = hexbody.substring(fieldstart, fieldstart + fieldlength);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue);
				fieldstart += fieldlength;
			}
		}
		
		System.out.println("Tag Value " + elemento.getNum() + ": " + tagvalue);
		System.out.println("***** FIELD *****");
		
		elemento.setValue(tagvalue);
		return fieldstart;
	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @return
	 */
	private static String ISO8583ParserString(String configfile, Object o,
			int header, boolean isBinary) {

		System.out.println("Begin to parse ISO message...");
		String hexbody = null;
		int bitmapLength = 16;
		int msgtypeLength = 4;
		int factor = 1;

		if (isBinary) {
			hexbody = new String((byte[]) o);
		} else {
			hexbody = (String) o;
		}

		System.out.println(hexbody);
		NodeList nl = null;
		Element tempe = null;
		String msgtype = null;
		String mytype = null;
		HashMap<String, String> hm = null;
		NodeList resultnl = null;
		String myheader = null;
		try {

			if (header != 0) {
				myheader = hexbody.substring(0, header);
				hexbody = hexbody.substring(header, hexbody.length());
			}

			String bitmap = null;

			msgtype = hexbody.substring(0, msgtypeLength);

			bitmap = hexbody.substring(msgtypeLength, msgtypeLength
					+ bitmapLength);

			// Se pasa a valores HEX en ASCII
			hm = readBitmap(bitmap, false);

			// There is secondary bitmap?
			if (hm.containsKey("1")) {
				bitmapLength = bitmapLength * 2;
				bitmap = hexbody.substring(msgtypeLength, msgtypeLength
						+ bitmapLength);
			}

			hm = readBitmap(bitmap, true);

			// hm = readBitmap(hexbody.substring(8, 40));
			System.out.println("msg type is: " + msgtype);

			File file = new File(configfile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			nl = doc.getElementsByTagName("parse");

			System.out.println("how many parsing config do i have? "
					+ nl.getLength());
			for (int i = 0; i < nl.getLength(); i++) {
				tempe = (Element) nl.item(i);
				// String headertype = "header";
				mytype = tempe.getAttribute("mti");
				if (mytype.equals(msgtype)) {
					System.out.println("got a match parsing from config file: "
							+ i);
					break;
				} else {
					tempe = (Element) nl.item(0);
				}
			}

			// create new xml document
			DocumentBuilderFactory finalFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
			Document finaldoc = finalBuilder.newDocument();
			Element rootElement = finaldoc.createElement("Message");
			rootElement.setAttribute("mti", msgtype);
			rootElement.setAttribute("header", myheader);

			finaldoc.appendChild(rootElement);
			int fieldstart = 0;
			// hexbody =
			// convertHexToString(hexbody.substring(40,hexbody.length()));
			hexbody = hexbody.substring(msgtypeLength + bitmapLength,hexbody.length());
			System.out.println("fieldstring is: " + hexbody);
			System.out.println("i am using the parser as: " + tempe.getAttribute("type"));
			System.out.println("c my name is: " + tempe.getNodeName());
			System.out.println("c how many childs I have: " + tempe.getChildNodes().getLength());
			
			// System.out.println(nodeToString(tempe));
			Element childtemp = null;
			NodeList nodes = tempe.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					childtemp = (Element) nodes.item(i);
					System.out.println(childtemp.getNodeName());
					String tagname = childtemp.getAttribute("name").replaceAll(" ", "_");
					String tagnum = childtemp.getAttribute("num");
					String tagtype = childtemp.getAttribute("type");
					String taglength = childtemp.getAttribute("length");
					String tagEncoding = childtemp.getAttribute("encoding");
					String classe = childtemp.getAttribute("class");

					System.out.println("tagEncoding: " + tagEncoding);

					String tagvalue = null;
					String LLlength = null;
					System.out.println("mytagname is: " + tagname + ", mytagnum is: " + tagnum + ", mytype is:" + tagtype + ", mylength is: " + taglength + ", start point is: " + fieldstart + ", got match lines? " + hm.containsKey(tagnum));

					if (hm.containsKey(tagnum)) {
						if (taglength != null && taglength.length() > 0 && !tagtype.equals("BINARY")) {

							factor = 1;
							tagvalue = hexbody.substring(fieldstart, fieldstart + Integer.parseInt(taglength) * factor);
							if ((tagEncoding != null) && (!tagEncoding.equals(""))) {
								factor = 2;
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + Integer.parseInt(taglength) * factor),tagEncoding);
							}
							fieldstart += Integer.parseInt(taglength) * factor;

						} else {

							if (tagtype.equals("LVAR")) {
								
								LLlength = hexbody.substring(fieldstart,fieldstart + 1);
								int fieldlength = Integer.parseInt(LLlength)* factor;
								taglength = String.valueOf(fieldlength / factor);
								System.out.println("LVAR length is: "+ fieldlength);
								tagvalue = hexbody.substring(fieldstart + 1,fieldstart + 1 + fieldlength);
								fieldstart += 1 + fieldlength;
								
							} else if (tagtype.equals("LLVAR")) {

								
								factor = 1;
								String largo = hexbody.substring(fieldstart,fieldstart + 2);
								int longitudDec = Converter.convertHexToInt(largo);
								int fieldlength = 0;
								LLlength = Integer.toString(longitudDec);

								if (tagnum.equals("35")) {
									longitudDec = longitudDec + 1;
								}

								System.out.println("tagEncoding: " + tagEncoding);
								if ((tagEncoding != null) && (!tagEncoding.equals(""))) {
									factor = 2;
								} 
								
								fieldlength = longitudDec * factor;
								taglength = String.valueOf(fieldlength/ factor);
								System.out.println("LLVAR length is: " + fieldlength);
								
								tagvalue = hexbody.substring(fieldstart + 2,fieldstart + 2+ fieldlength);
								
								if ((tagEncoding != null) && (!tagEncoding.equals(""))) {

									tagvalue = CustomExtensionsHandler.convertHexToString(tagvalue,tagEncoding);

								}
								
								fieldstart += 2 + fieldlength;

							} else if (tagtype.equals("LLBIN")) {

								factor = 2;

								String largo = hexbody.substring(fieldstart,fieldstart + 2);
								int longitudDec = Converter.convertHexToInt(largo);
								LLlength = Integer.toString(longitudDec);
								int fieldlength = longitudDec * factor;

								taglength = String.valueOf(fieldlength/ factor);
								System.out.println("LLBIN length is: "+ fieldlength);

								tagvalue = hexbody.substring(fieldstart + 2, fieldstart + 2+ fieldlength);
								
								tagvalue = processBinaryField(classe, tagvalue);
								
								fieldstart += 2 + fieldlength;

							} else if (tagtype.equals("LLLVAR")) {
								
								LLlength = hexbody.substring(fieldstart, fieldstart + 3);
								int fieldlength = Integer.parseInt(LLlength)* factor;
								
								taglength = String.valueOf(fieldlength / factor);
								System.out.println("LLLVAR length is: "+ fieldlength);
								tagvalue = hexbody.substring(fieldstart + 3,fieldstart + 3 + fieldlength);
								fieldstart += 3 + fieldlength;
								
							} else if (tagtype.equals("BITMAP")) {

								tagvalue = hexbody.substring(fieldstart,fieldstart + 16);
								int fieldlength = tagvalue.length() * factor;
								taglength = String.valueOf(fieldlength);
								System.out.println("BITMAP length is: " + fieldlength / factor);
								fieldstart += fieldlength;

							} else if (tagtype.equals("BINARY")) {
								
								int fieldlength = Integer.parseInt(taglength);
								System.out.println(hexbody.substring(fieldstart, fieldstart + fieldlength));
								tagvalue = hexbody.substring(fieldstart,fieldstart + fieldlength);
								fieldstart += fieldlength;
							}
						}
						System.out.println("tagvalue " + i + ": " + tagvalue);
						Element childnode = finaldoc.createElement(tagname);
						childnode.setAttribute("num", tagnum);
						childnode.setAttribute("length", taglength);
						childnode.setAttribute("type", tagtype);
						// childnode.setAttribute("fieldlength", LLlength);
						if ((tagEncoding != null) && (!tagEncoding.equals(""))) {
							childnode.setAttribute("encoding", tagEncoding);
						}

						childnode.setTextContent(tagvalue);
						rootElement.appendChild(childnode);
					}
				}
			}

			resultnl = finaldoc.getElementsByTagName("Message");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return CustomExtensionsHandler.nodeListToString(resultnl);
	}

	/**
	 * @param classe
	 * @param tagvalue
	 * @return
	 * @throws Exception
	 */
	private static String processBinaryField(String classe, String tagvalue)
			throws Exception {
		if(classe != null && classe.equals("HEXA")){
			tagvalue = Converter.convertHexToString(tagvalue);
		}
		return tagvalue;
	}

	/**
	 * 
	 * @param configfile
	 * @param o
	 * @param header
	 * @param isBinary
	 * @param contentType
	 * @return
	 */
	public static String ISO8583Parser(String configfile, Object o, int header,
			boolean isBinary, String contentType) throws Exception {

		if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_HEXA)) {

			System.out.println("ISO Message HEXA Content");
			return ISO8583Handler.ISO8583ParserHex(configfile, o, header, isBinary);

		} else if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_ASCII)) {

			System.out.println("ISO Message ASCII Content");
			return ISO8583Handler.ISO8583ParserString(configfile, o, header,
					isBinary);

		} else {
			throw new Exception("Content Type no Valid");
		}

	}

	public static String convertXMLToTxt(String myfile, boolean isFile)
			throws SAXException, IOException {

		DocumentBuilder dBuilder;
		Document doc = null;
		String nodevalue = "";
		String nodenum = "";
		String result = "";
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

		NodeList nodeList = doc.getElementsByTagName("*");
		Element currentElement;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				currentElement = (Element) node;
				nodevalue = currentElement.getTextContent();
				nodenum = currentElement.getAttribute("num");
				if (nodenum != null && nodenum.length() > 0) {
					sb.append(nodenum + ":" + nodevalue + "\n");
				}
				if (nodenum.equals("39")) {
					result = "Response Code: " + nodevalue;
				}

			}
		}
		return sb.toString() + result;
	}

	
	private String stringHex(String trama) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < trama.length(); i += 2) {
            String str = trama.substring(i, i + 2);
            int val = Integer.parseInt(str, 16);

            stb.append((char)val);
        }
        return stb.toString();
    }
    
    
    public static void main(String args[]) throws Exception{
        ISO8583Handler x = new ISO8583Handler();
        String message = "0000160102020070380001008000801642228230081760170000500000000600003589331807410825020138373635343332310203493030333833353839333330303030303030303030303036303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303073726f6c6573652020203030303030303030303030303030303030303030303030303030303030303030303030303030435445435445535049303033383335383933333030303030303030303032303135303832352020";
        System.out.println("Result0:-" + message.length());
        String result = x.stringHex("02006024340100800200164027483000034040000055354656000000000000000002013837363534333231000153");
        result = x.stringHex(message);
        System.out.println("Result1:-" + result);
        
        result = "0000160102020070380001008000801642228230081760170000500000000600003589331807410825020138373635343332310203493030333833353839333330303030303030303030303036303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303030303073726f6c6573652020203030303030303030303030303030303030303030303030303030303030303030303030303030435445435445535049303033383335383933333030303030303030303032303135303832352020";
        System.out.println("Result:-" + result);
        
//        Byte b1 = new Byte(Converter.convertHexToInt(character));
        
        byte[] b4 = Converter.convertHexToByte(result);
        
        int j = 0;
        byte [] bytes = new byte [result.length()/2];
        for(int i = 0 ;  i < result.length(); i=i+2 ){
        	j=i+2;
        	String character = result.substring(i, j);
        	int value = Converter.convertHexToInt(character);
        	byte b2 = (byte)value;
        	
        	Byte b = new Byte(b2);
        	int k = i/2;
        	System.out.println(k);
        	bytes[k] = b.byteValue();
//        	System.out.println(character);
        }
        
        String value = new String (bytes);
        System.out.println(value);
        
        String value4 = new String (b4);
        System.out.println(value4);
        
        
        
        
        String result1 = Converter.convertHexToString(result);
        System.out.println("Result1:-" + result1);
        
        
        String result4 = x.stringHex(result);
        System.out.println("Result:-" + result4);
        
        
        String result2 = x.stringHex(result);
        System.out.println("Result:-" + result2);
        System.out.println(Converter.convertStringToHex(result));
        
//        String result1 = x.hexString(result.getBytes());
//        System.out.println("Result1:-" + result1);
//        String result2 = Converter.convertHexToString(result);
//        System.out.println("Result1:-" + result2);
        
        System.out.println("Result1:-" + message);
        
        String msgLength = Converter.convertIntToHex(result.length());
        
        while(msgLength.length()%4 != 0){
			msgLength = "0" + msgLength;
		}
        
        System.out.println("msgLength:-" + msgLength);
        
        String response = msgLength + message;
        response = Converter.convertHexToString(response);
        System.out.println("Response: -" + response);
        
        
        String result3 = x.hexString(result.getBytes());
        System.out.println("Result2:-" + result3);
        
        
        System.out.println("FIN");
    }

    public static String hexString(byte[] b) {
        String[] hexStrings = new String[256];
        for (int i = 0; i < 256; i++) {
            StringBuilder d = new StringBuilder(2);
            char ch = Character.forDigit(((byte)i >> 4) & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            ch = Character.forDigit((byte)i & 0x0F, 16);
            d.append(Character.toUpperCase(ch));
            hexStrings[i] = d.toString();
        }
        StringBuilder d = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            d.append(hexStrings[(int)aB & 0xFF]);
        }
        return d.toString();
    }

    
}
