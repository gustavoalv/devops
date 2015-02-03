package co.com.extensions.handlers;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;

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

import co.com.extensions.util.Elemento;

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
			System.out.println("binary result is: " + binaryresult);
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
			System.out.println(sb.toString());
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
		Element currentElement1;
		String nodetype1;
		String nodenum1;
		// String nodelength1;
		String fieldlength1;
		String emvtag = null;
		String emvpadding = "";
		// StringBuffer rqsb = new StringBuffer();
		HashMap<String, String> rqhm = new HashMap<String, String>();

		// System.out.println(nodeList.getLength());
		
		//Saca todo los campos que van en el XML y los pone en el HashMap rqhm
		//TODO: Validar si es requerido o si se puede mejorar
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

		// process response;
		//
		Element currentElement;
//		String nodevalue = "";
		String msgtype = null;
		String myheader = "";
		String nodenum = null;
		String roottype;
		String nodetype;
		String nodelength = null;
		String fieldlength = null;
		String codec = null;
		String encoding = null;

		StringBuffer sb = new StringBuffer();
//		StringBuffer sbemv = new StringBuffer();
		HashMap<String, String> temphm = new HashMap<String, String>();
		HashMap<String, String> emvhm = new HashMap<String, String>();
		// System.out.println(nodeList.getLength());
		// NodeList nodeList2 = doc.getElementsByTagName("*");
		Element e1, e2 = null;
		
		log.info("begin to process field 123...");
		
		// TODO: Chequear si esto aplica
		/*
		// Procesa el campo 123
		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node = nodeList1.item(i);
			nodevalue = "";
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				e1 = (Element) node;
				if (e1.getAttribute("num").equals("114")) {
					e2 = e1;
				} else if (e1.getAttribute("num").equals("123") && e2 != null
						&& e1 != null) {
					if (!e1.getTextContent().contains("{")
							&& e1.getTextContent().length() > 0) {
						if (BancoBogotaHandler.convertISOCountry(e1.getTextContent().substring(2, 4)) != null) {
							
							if (log.isDebugEnabled()){
								log.debug("Where are in 123 field");
								log.debug("reset country code: " + BancoBogotaHandler.convertISOCountry(e1.getTextContent().substring(2, 4)));
							}
							e2.setTextContent(BancoBogotaHandler.convertISOCountry(e1.getTextContent().substring(2, 4)));
							
						}
					}
				}

			}
		}
		*/
		
		//Construye el segundo Bitmap del XML
		// TODO: Validar si esto se puede mejorar
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
						
						Elemento elemento = Elemento.getElemento(currentElement);
						fieldlength = getLenthFromVariableAttribute( fieldlength, elemento);
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
					// TODO: Traer campos del XML
					currentElement = (Element) nodesChildBody;
					if (currentElement.hasAttributes()) {
						
						Elemento elemento = Elemento.getElemento(currentElement);
						if (Integer.parseInt(elemento.getNum()) <= 128) {
							
							fieldlength = getLenthFromVariableAttribute( fieldlength, elemento);
							
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
			
/*
		for (int i = 0; i < nodeList.getLength(); i++) {
			String nodevalue = "";
			Node node = nodeList.item(i);
			nodevalue = "";
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				currentElement = (Element) node;
				if (node.hasAttributes()) {
					
					// Obtengo los valores de cada elemento XML
					
					nodenum = currentElement.getAttribute("num");
					nodetype = currentElement.getAttribute("mytype");
					nodelength = currentElement.getAttribute("length");
					encoding = currentElement.getAttribute("encoding");

					log.info("nodetype: " + nodetype + " , " + "nodenum: " + nodenum + " , " + "nodelength: " + nodelength );

					// Se va a procesar los mensajes del primer bitmap
					if (Integer.parseInt(nodenum) <= 128) {

						nodevalue = currentElement.getTextContent();

						if (nodetype.contains("LLVAR") && !nodenum.equals("123")) {	
							nodelength = String.valueOf(nodevalue.length());
							if (nodetype.contains("LLLVAR")) {
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
						} else if (nodetype.contains("LLBIN")) {
							nodelength = String.valueOf(nodevalue.length() / 2);
							if (nodetype.contains("LLLBIN")) {
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
								
								String hexa = Converter.convertIntToHex(Integer.parseInt(nodelength));
								String pad = "";
								if (hexa.length() != 2) {
									for (int c = 0; c < 2 - t; c++) {
										pad += "0";
									}
								}
								fieldlength = pad + hexa;
							}
						} else {
							nodelength = currentElement.getAttribute("length");
						}
						System.out.println("before conversion: " + nodevalue);
						String hexvalue = "";
						if (!nodevalue.contains("{")) {
							int templength = nodevalue.length();
							System.out.println("value length: " + templength);
							String finalpadding = "";
							String padding;

							if (nodelength != null && nodelength.length() > 0 && !nodetype.contains("BIN")) {

								int mylength = Integer.parseInt(nodelength);
								if (fieldlength != null && fieldlength.length() > 0) {
									
									int tempfieldlength = Integer .parseInt(fieldlength);
									System.out.println("fieldlength: " + tempfieldlength + " ,mylength is:" + mylength + ", valuelength is: " + templength);
									
									if ((nodetype.contains("VAR") || nodetype.contains("BITMAP")) && tempfieldlength != mylength) {
										
										mylength = mylength / 2;
										System.out.println("VAR length: " + mylength);
									}
								}
								if (nodetype.contains("BINARY")) {
									mylength = mylength / 2;
								}

								if (templength < mylength) {
									System.out.println("in padding");

									if (nodetype.equals("NUMERIC")) {
										padding = "0";
									} else {
										padding = " ";
									}
									// padding = " ";
									for (int p = 0; p < mylength - templength; p++) {
										finalpadding += padding;
									}

								}
								System.out.println("fieldlength is: "
										+ fieldlength);
								if (direction.equals("right")) { 
									
									if (!nodetype.contains("VAR") && !nodetype.contains("BITMAP")) {
										
										nodevalue = nodevalue + finalpadding;
										
									} else {
										nodevalue = fieldlength + nodevalue + finalpadding;
									}
									System.out.println(nodevalue + ", valuelength is: " + nodevalue.length());
									
								} else {
									if (!nodetype.contains("VAR") && !nodetype.contains("BITMAP")) {

										System.out.println("encoding: " + encoding);
										if ((encoding != null) && (!encoding.equals(""))) {

											byte[] bites = nodevalue.getBytes(encoding);
											nodevalue = BancoBogotaHandler.convertByteToHex(bites);

										}
										nodevalue = finalpadding + nodevalue;
									} else {

										System.out.println("encoding else: " + encoding);

										if ((encoding != null) && (!encoding.equals(""))) {

											byte[] bites = nodevalue.getBytes(encoding);
											nodevalue = BancoBogotaHandler.convertByteToHex(bites);

										}

										if ((codec != null) && (!codec.equals(""))) {

											int largo = (Integer.parseInt(fieldlength)) / 2;
											fieldlength = Integer.toString(largo);

										}

										if ((fieldlength != null) && (!fieldlength.equals(""))) {
											String longitud = Converter.convertIntToHex(Integer.parseInt(fieldlength));

											if (longitud.length() % 2 != 0) {
												longitud = "0" + longitud;
											}

											fieldlength = longitud;
										}

										nodevalue = fieldlength + finalpadding + nodevalue;
									}
									System.out.println(nodevalue + ", valuelength is: " + nodevalue.length());
								}
								hexvalue = nodevalue;
							} else if (nodetype.contains("BIN")) {
								System.out.println("This is a Binary field, field value is:"+ nodevalue);
								
								if (nodetype.contains("LL")) {
									hexvalue = fieldlength + nodevalue;
								} else {
									hexvalue = nodevalue;
								}
							} else {
								System.out.println("empty value fieldlength is: " + fieldlength);
								nodevalue = fieldlength;
								hexvalue = nodevalue;
							}

							sb.append(hexvalue);
							System.out.println("after conversion: " + hexvalue);
							temphm.put(nodenum, nodenum);
						} else {
							System.out.println("line is skipped: " + nodenum);
						}
						if (Integer.parseInt(nodenum) == 65) {
							emvtag = nodevalue;
							 if (log.isDebugEnabled()){
								 log.debug("Variable emvtag on nodemun 65: " + emvtag);
							 }
						}
					} else if (Integer.parseInt(nodenum) > 128) {
						// emvpadding = emvtag + "000";
						nodevalue = currentElement.getTextContent().trim();
						System.out.println("field: " + nodenum + ", value is:"
								+ nodevalue);
						if (nodetype.contains("LL") && !nodenum.equals("123")) {
							nodelength = String.valueOf(nodevalue.length());
							if (nodetype.contains("LLL")) {
								int t = nodelength.length();
								String pad = "";
								if (t != 3) {
									for (int c = 0; c < 3 - t; c++) {
										pad += "0";
									}
								}
								fieldlength = pad + nodelength;
							} else if (nodetype.contains("LL")) {
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
						} else if (nodetype.contains("LL")) {
							nodelength = String.valueOf(nodevalue.length());
							if (nodetype.contains("LLL")) {
								int t = nodelength.length();
								String pad = "";
								if (t != 3) {
									for (int c = 0; c < 3 - t; c++) {
										pad += "0";
									}
								}
								fieldlength = pad + nodelength;
							} else if (nodetype.contains("LL")) {
								int t = nodelength.length();
								String pad = "";
								if (t != 2) {
									for (int c = 0; c < 2 - t; c++) {
										pad += "0";
									}
								}
								fieldlength = pad + nodelength;
							}
						} else {
							nodelength = currentElement.getAttribute("length");
							fieldlength = currentElement
									.getAttribute("fieldlength");
						}
						System.out.println("before conversion: " + nodevalue);
						String hexvalue = "";
						if (!nodevalue.contains("{")) {
							int templength = nodevalue.length();
							System.out.println("value length: " + templength);
							String finalpadding = "";
							String padding;

							if (nodelength != null && nodelength.length() > 0
									&& !nodetype.equals("BINARY")) {

								int mylength = Integer.parseInt(nodelength);
								if (fieldlength != null
										&& fieldlength.length() > 0) {
									int tempfieldlength = Integer
											.parseInt(fieldlength);
									System.out
											.println("fieldlength: "
													+ tempfieldlength
													+ " ,mylength is:"
													+ mylength
													+ ", valuelength is: "
													+ templength);
									if ((nodetype.contains("VAR") || nodetype
											.contains("BITMAP"))
											&& tempfieldlength != mylength) {
										mylength = mylength / 2;
										System.out.println("VAR length: "
												+ mylength);
									}
								}
								if (nodetype.contains("BINARY")) {
									mylength = mylength / 2;
								}

								if (templength < mylength) {
									System.out.println("in padding");

									if (nodetype.equals("NUMERIC")) {
										padding = "0";
									} else {
										padding = " ";
									}
									// padding = " ";
									for (int p = 0; p < mylength - templength; p++) {
										finalpadding += padding;
									}

								}
								System.out.println("fieldlength is: "
										+ fieldlength);
								if (direction.equals("right")) {
									if (!nodetype.contains("VAR")
											&& !nodetype.contains("BITMAP")) {
										nodevalue = nodevalue + finalpadding;
									} else {
										nodevalue = fieldlength + nodevalue
												+ finalpadding;
									}
									System.out.println(nodevalue
											+ ", valuelength is: "
											+ nodevalue.length());
								} else {
									if (!nodetype.contains("VAR")
											&& !nodetype.contains("BITMAP")) {
										nodevalue = finalpadding + nodevalue;
									} else {
										nodevalue = fieldlength + finalpadding
												+ nodevalue;
									}
									System.out.println(nodevalue
											+ ", valuelength is: "
											+ nodevalue.length());
								}
								hexvalue = nodevalue;
							} else if (nodetype.equals("BINARY")) {
								System.out
										.println("This is a Binary field, field value is:"
												+ nodevalue);
								hexvalue = nodevalue;
							} else {
								System.out
										.println("empty value fieldlength is: "
												+ fieldlength);
								nodevalue = fieldlength;
								hexvalue = nodevalue;
							}

							sbemv.append(hexvalue);
							System.out.println("after conversion: " + hexvalue);
							emvhm.put(nodenum, nodenum);
						} else {
							System.out.println("line is skipped: " + nodenum);
						}
					}

				}
			}
		}
		*/
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
		

		// TODO: Este header cuando tiene de largo 4 en HEXA.
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
	private static void setValueFromElemento(String direction, String fieldlength, Elemento elemento) throws Exception {
		
		String nodetype = elemento.getType();
		String nodelength = elemento.getLength();
		String encoding = elemento.getEncoding();
		String nodenum = elemento.getNum();
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
			
			if (nodelength != null && nodelength.length() > 0 ) {
				
				int mylength = Integer.parseInt(nodelength);
				
				if(!nodetype.contains("BIN")){
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
				}
				
				if(nodetype.equals("BINARY")){
					
					valor = CustomExtensionsHandler.convertHexToString(nodevalue);
					
				} else if (nodetype.contains("BIN") || (nodetype.contains("VAR"))){
					
					if(nodetype.contains("BIN")) {
						
						nodevalue = CustomExtensionsHandler.convertHexToString(nodevalue);
						
					} else if ((encoding != null) && (!encoding.equals(""))) {

						byte[] bites = nodevalue.getBytes(encoding);
						nodevalue = new String(bites);
					} 
					
					valor = fieldlength + finalpadding + nodevalue;
					
					if (direction.equals("right")) {
						
						valor = fieldlength +  nodevalue + finalpadding;
						
					} 
					
				} else{
					
					if ((encoding != null) && (!encoding.equals(""))) {

						byte[] bites = nodevalue.getBytes(encoding);
						nodevalue = new String(bites);

					}
					valor = finalpadding +nodevalue;
				}

				elemento.setValue(valor);
			} else {
				System.out.println("line is skipped: " + nodenum);
			}
		}
	}
			
			
			/*
			if (nodelength != null && nodelength.length() > 0 && !nodetype.contains("BIN")) {

				int mylength = Integer.parseInt(nodelength);
				if (fieldlength != null && fieldlength.length() > 0) {
					
					int tempfieldlength = Integer .parseInt(fieldlength);
					System.out.println("fieldlength: " + tempfieldlength + " ,mylength is:" + mylength + ", valuelength is: " + templength);
					
					if ((nodetype.contains("VAR") || nodetype.contains("BITMAP")) && tempfieldlength != mylength) {
						
						mylength = mylength / 2;
						System.out.println("VAR length: " + mylength);
					}
				}
				if (nodetype.contains("BINARY")) {
					mylength = mylength / 2;
				}

				if (templength < mylength) {
					System.out.println("in padding");

					if (nodetype.equals("NUMERIC")) {
						padding = "0";
					} else {
						padding = " ";
					}
					// padding = " ";
					for (int p = 0; p < mylength - templength; p++) {
						finalpadding += padding;
					}

				}
				System.out.println("fieldlength is: " + fieldlength);
				if (direction.equals("right")) { 
					
					if (!nodetype.contains("VAR") && !nodetype.contains("BITMAP")) {
						
						nodevalue =nodevalue + finalpadding;
						
					} else {
						nodevalue = fieldlength +nodevalue + finalpadding;
					}
					System.out.println(nodevalue + ", valuelength is: " +nodevalue.length());
					
				} else {
					if (!nodetype.contains("VAR") && !nodetype.contains("BITMAP")) {

						System.out.println("encoding: " + encoding);
						if ((encoding != null) && (!encoding.equals(""))) {

							byte[] bites = nodevalue.getBytes(encoding);
							nodevalue = BancoBogotaHandler.convertByteToHex(bites);

						}
						nodevalue = finalpadding +nodevalue;
					} else {

						System.out.println("encoding else: " + encoding);

						if ((encoding != null) && (!encoding.equals(""))) {

							byte[] bites =nodevalue.getBytes(encoding);
							nodevalue = BancoBogotaHandler.convertByteToHex(bites);

						}

						if ((fieldlength != null) && (!fieldlength.equals(""))) {
							String longitud = Converter.convertIntToHex(Integer.parseInt(fieldlength));

							if (longitud.length() % 2 != 0) {
								longitud = "0" + longitud;
							}

							fieldlength = longitud;
						}

						nodevalue = fieldlength + finalpadding +nodevalue;
					}
					System.out.println(nodevalue + ", valuelength is: " +nodevalue.length());
				}
				hexvalue = nodevalue;
			} else if (nodetype.contains("BIN")) {
				System.out.println("This is a Binary field, field value is:"+nodevalue);
				
				if (nodetype.contains("LL")) {
					hexvalue = fieldlength +nodevalue;
				} else {
					hexvalue =nodevalue;
				}
			} else {
				System.out.println("empty value fieldlength is: " + fieldlength);
				nodevalue = fieldlength;
				hexvalue =nodevalue;
			}
			
	}
*/
	/**
	 * Se obtiene el length de LLVAR, LLLVAR y LLBIN, apartir de largo del valor y lo rellena con ceros
	 * @param nodevalue
	 * @param fieldlength
	 * @param elemento
	 * @return
	 */
	private static String getLenthFromVariableAttribute(String fieldlength, Elemento elemento) {
		String nodelength;
		String nodenum = elemento.getNum();
		String nodetype = elemento.getType();
		String nodevalue = elemento.getValue();
		
		if (nodetype.contains("LVAR") && !nodenum.equals("123")) {	
			nodelength = String.valueOf(nodevalue.length());
			if (nodetype.contains("LLLVAR")) {
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
		} else if (nodetype.contains("LLBIN")) {
			nodelength = String.valueOf(nodevalue.length() / 2);
			if (nodetype.contains("LLLBIN")) {
				int t = nodelength.length();
				String pad = "";
				if (t != 3) {
					for (int c = 0; c < 3 - t; c++) {
						pad += "0";
					}
				}
				fieldlength = nodelength;
			} else if (nodetype.contains("LLBIN")) {
				int t = nodelength.length();
				
				String hexa = Converter.convertIntToHex(Integer.parseInt(nodelength));
				String pad = "";
				if (hexa.length() != 2) {
					for (int c = 0; c < 2 - t; c++) {
						pad += "0";
					}
				}
				fieldlength = hexa;
			}
		} else {
			fieldlength = elemento.getLength();
		}
		return fieldlength;
	}

/*
	public static Object ISO8583XmlToObject(String myfile, String direction,
			boolean isFile, boolean isBinary, String contenttype)
			throws Exception {
		// StringReader sr = new StringReader(xmldata);
		System.out.println("start processing: " + myfile);
		// String direction = "left";
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
			throw new RuntimeException(e);
		}

		NodeList nodeList1 = doc.getElementsByTagName("*");
		Element currentElement1;
		String nodetype1;
		String nodenum1;
		String fieldlength1;
		String emvtag = null;
		String emvpadding = "";
		HashMap<String, String> rqhm = new HashMap<String, String>();

		// System.out.println(nodeList.getLength());

		for (int i = 0; i < nodeList1.getLength(); i++) {
			Node node = nodeList1.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				currentElement1 = (Element) node;
				nodetype1 = currentElement1.getAttribute("mytype");
				nodenum1 = currentElement1.getAttribute("num");
				fieldlength1 = currentElement1.getAttribute("fieldlength");
				if (nodetype1.contains("VAR") || nodetype1.contains("BITMAP")) {
					rqhm.put(nodenum1, fieldlength1);
					System.out.println("field: " + nodenum1
							+ " field length is: " + fieldlength1);
				}

			}
		}

		// process response;
		//
		Element currentElement;
		String nodevalue = "";
		String msgtype = null;
		String myheader = "";
		String nodenum = null;
		String roottype;
		String nodetype;
		String nodelength = null;
		String fieldlength = null;
		StringBuffer sb = new StringBuffer();
		StringBuffer sbemv = new StringBuffer();
		HashMap<String, String> temphm = new HashMap<String, String>();
		HashMap<String, String> emvhm = new HashMap<String, String>();
		
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

				if (emvflag != null && emvflag.length() > 0
						&& Integer.parseInt(num) > 128) {
					// process bitmap3 if exists

					emvhm.put(num, num);

				}

			}
		}
		// set F65 value after bitmap calculation
		if (emvflag != null && emvflag.length() > 0) {
			System.out.println("process bitmap3");

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
				System.out.println("bitmap3: " + binarydata1);
				for (int e = 0; e < binarydata1.length(); e += 4) {
					// System.out.println("currentHex: "+binarydata.substring(e,e+4));
					bitdata1 += BancoBogotaHandler
							.convertBinaryToHex(binarydata1.substring(e, e + 4));
				}
				emvpadding = bitdata1;
				System.out.println(emvpadding);
				F65.setTextContent(emvpadding);
			}
		}

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			nodevalue = "";
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				// do something with the current element
				
				System.out.println("Iteracion: " + i);
				System.out.println(sb);
				
				currentElement = (Element) node;
				nodenum = currentElement.getAttribute("num");
				roottype = currentElement.getAttribute("type");
				nodetype = currentElement.getAttribute("mytype");
				nodelength = currentElement.getAttribute("length");
				fieldlength = currentElement.getAttribute("fieldlength");

				if (roottype != null && roottype.length() > 0) {
					if (currentElement.getAttribute("header") != null
							&& currentElement.getAttribute("header").length() > 0) {
						myheader = currentElement.getAttribute("header");
					}
					msgtype = BancoBogotaHandler.convertStringToHex(roottype);
				} else if (Integer.parseInt(nodenum) <= 128) {

					nodevalue = currentElement.getTextContent();

					if (nodetype.contains("LLVAR") && !nodenum.equals("123")) {
						nodelength = String.valueOf(nodevalue.length());
						if (nodetype.contains("LLLVAR")) {
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
							fieldlength = String.valueOf(nodevalue.length());
						}
					} else if (nodetype.contains("LLBIN")) {
						nodelength = String.valueOf(nodevalue.length() / 2);
						if (nodetype.contains("LLLBIN")) {
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
						}
					} else {
						nodelength = currentElement.getAttribute("length");
						fieldlength = currentElement
								.getAttribute("fieldlength");
					}
					System.out.println("before conversion: " + nodevalue);
					String hexvalue = "";
					if (!nodevalue.contains("{")) {
						int templength = nodevalue.length();
						System.out.println("value length: " + templength);
						String finalpadding = "";
						String padding;

						if (nodelength != null && nodelength.length() > 0
								&& !nodetype.contains("BIN")) {

							int mylength = Integer.parseInt(nodelength);
							if (fieldlength != null && fieldlength.length() > 0) {
								int tempfieldlength = Integer
										.parseInt(fieldlength);
								System.out.println("fieldlength: "
										+ tempfieldlength + " ,mylength is:"
										+ mylength + ", valuelength is: "
										+ templength);
								if ((nodetype.contains("VAR") || nodetype
										.contains("BITMAP"))
										&& tempfieldlength != mylength) {
									mylength = mylength / 2;
									System.out.println("VAR length: "
											+ mylength);
								}
							}
							if (nodetype.contains("BINARY")) {
								mylength = mylength / 2;
							}

							if (templength < mylength) {
								System.out.println("in padding");

								if (nodetype.equals("NUMERIC")) {
									padding = "0";
								} else {
									padding = " ";
								}
								// padding = " ";
								for (int p = 0; p < mylength - templength; p++) {
									finalpadding += padding;
								}

							}
							System.out
									.println("fieldlength is: " + fieldlength);
							if (direction.equals("right")) {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = nodevalue + finalpadding;
								} else {
									nodevalue = fieldlength + nodevalue
											+ finalpadding;
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							} else {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = finalpadding + nodevalue;
								} else {
									nodevalue = fieldlength + finalpadding
											+ nodevalue;
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							}
							hexvalue = BancoBogotaHandler
									.convertStringToHex(nodevalue);
						} else if (nodetype.contains("BIN")) {
							System.out
									.println("This is a Binary field, field value is:"
											+ nodevalue);
							if (nodetype.contains("LL")) {
								hexvalue = Converter
										.convertStringToHex(fieldlength)
										+ nodevalue;
							} else {
								hexvalue = nodevalue;
							}
						} else {
							System.out.println("empty value fieldlength is: "
									+ fieldlength);
							nodevalue = fieldlength;
							hexvalue = BancoBogotaHandler
									.convertStringToHex(nodevalue);
						}

						sb.append(hexvalue);
						System.out.println("after conversion: " + hexvalue);
						temphm.put(nodenum, nodenum);
					} else {
						System.out.println("line is skipped: " + nodenum);
					}
					if (Integer.parseInt(nodenum) == 65) {
						if (log.isDebugEnabled()){
							 log.debug("Variable emvtag on nodemun 65: " + emvtag);
						 }
					}
				} else if (Integer.parseInt(nodenum) > 128) {
					// emvpadding = emvtag + "000";
					nodevalue = currentElement.getTextContent().trim();
					System.out.println("field" + nodenum + ", value is:"
							+ nodevalue);
					if (nodetype.contains("LL") && !nodenum.equals("123")) {
						nodelength = String.valueOf(nodevalue.length());
						if (nodetype.contains("LLL")) {
							int t = nodelength.length();
							String pad = "";
							if (t != 3) {
								for (int c = 0; c < 3 - t; c++) {
									pad += "0";
								}
							}
							fieldlength = pad + nodelength;
						} else if (nodetype.contains("LL")) {
							int t = nodelength.length();
							String pad = "";
							if (t != 2) {
								for (int c = 0; c < 2 - t; c++) {
									pad += "0";
								}
							}
							fieldlength = pad + nodelength;
						} else {
							fieldlength = String.valueOf(nodevalue.length());
						}
					} else if (nodetype.contains("LL")) {
						nodelength = String.valueOf(nodevalue.length());
						if (nodetype.contains("LLL")) {
							int t = nodelength.length();
							String pad = "";
							if (t != 3) {
								for (int c = 0; c < 3 - t; c++) {
									pad += "0";
								}
							}
							fieldlength = pad + nodelength;
						} else if (nodetype.contains("LL")) {
							int t = nodelength.length();
							String pad = "";
							if (t != 2) {
								for (int c = 0; c < 2 - t; c++) {
									pad += "0";
								}
							}
							fieldlength = pad + nodelength;
						}
					} else {
						nodelength = currentElement.getAttribute("length");
						fieldlength = currentElement
								.getAttribute("fieldlength");
					}
					System.out.println("before conversion: " + nodevalue);
					String hexvalue = "";
					if (!nodevalue.contains("{")) {
						int templength = nodevalue.length();
						System.out.println("value length: " + templength);
						String finalpadding = "";
						String padding;

						if (nodelength != null && nodelength.length() > 0
								&& !nodetype.equals("BINARY")) {

							int mylength = Integer.parseInt(nodelength);
							if (fieldlength != null && fieldlength.length() > 0) {
								int tempfieldlength = Integer
										.parseInt(fieldlength);
								System.out.println("fieldlength: "
										+ tempfieldlength + " ,mylength is:"
										+ mylength + ", valuelength is: "
										+ templength);
								if ((nodetype.contains("VAR") || nodetype
										.contains("BITMAP"))
										&& tempfieldlength != mylength) {
									mylength = mylength / 2;
									System.out.println("VAR length: "
											+ mylength);
								}
							}
							if (nodetype.contains("BINARY")) {
								mylength = mylength / 2;
							}

							if (templength < mylength) {
								System.out.println("in padding");

								if (nodetype.equals("NUMERIC")) {
									padding = "0";
								} else {
									padding = " ";
								}
								// padding = " ";
								for (int p = 0; p < mylength - templength; p++) {
									finalpadding += padding;
								}

							}
							System.out
									.println("fieldlength is: " + fieldlength);
							if (direction.equals("right")) {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = nodevalue
											+ BancoBogotaHandler
													.convertStringToHex(finalpadding);
								} else {
									nodevalue = BancoBogotaHandler
											.convertStringToHex(fieldlength)
											+ nodevalue
											+ BancoBogotaHandler
													.convertStringToHex(finalpadding);
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							} else {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = BancoBogotaHandler
											.convertStringToHex(finalpadding)
											+ nodevalue;
								} else {
									nodevalue = BancoBogotaHandler
											.convertStringToHex(fieldlength
													+ finalpadding)
											+ nodevalue;
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							}
							hexvalue = nodevalue;
						} else if (nodetype.equals("BINARY")) {
							System.out
									.println("This is a Binary field, field value is:"
											+ nodevalue);
							hexvalue = nodevalue;
						} else {
							System.out.println("empty value fieldlength is: "
									+ fieldlength);
							nodevalue = fieldlength;
							hexvalue = nodevalue;
						}

						sbemv.append(hexvalue);
						System.out.println("after conversion: " + hexvalue);
						emvhm.put(nodenum, nodenum);
					} else {
						System.out.println("line is skipped: " + nodenum);
					}
				}

			}
		}
		// process bitmap1 and bitmap2
		System.out.println("process bitmap1 and bitmap2");
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
		System.out.println("bitmap1 and 2: " + binarydata);
		for (int e = 0; e < binarydata.length(); e += 4) {
			// System.out.println("currentHex: "+binarydata.substring(e,e+4));
			bitdata += BancoBogotaHandler.convertBinaryToHex(binarydata
					.substring(e, e + 4));
		}
		// bitdata = convertStringToHex(bitdata);
		System.out.println("bitmap1 and 2 Hex: " + bitdata);

		System.out.println(sb.toString() + sbemv.toString());
		String datahex = sb.toString() + sbemv.toString();
		String finaldata = (msgtype + bitdata + datahex).toUpperCase();
		int datalength = finaldata.length() / 2;
		String finalheader = Integer.toHexString(datalength);
		if (finalheader.length() < 4) {
			String padding = "";
			for (int n = 0; n < 4 - finalheader.length(); n++) {
				padding += "0";
			}
			finalheader = padding + finalheader;
		}

		if (contenttype.toUpperCase().equals("STRING")) {
			if (myheader.length() > 0 && myheader != null) {
				System.out.println("convert to string with header");
				msgtype = Converter.convertHexToString(msgtype);
				System.out.println("message type string: " + msgtype);
				System.out.println("bitmap data Hex: " + bitdata);
				datahex = Converter.convertHexToString(sb.toString()
						+ sbemv.toString());
				System.out.println("data field string: " + datahex);
				finaldata = finalheader + msgtype + bitdata + datahex;
			} else {
				System.out.println("convert to string wo header");
				msgtype = Converter.convertHexToString(msgtype);
				System.out.println("message type string: " + msgtype);
				System.out.println("bitmap data Hex: " + bitdata);
				datahex = Converter.convertHexToString(sb.toString()
						+ sbemv.toString());
				System.out.println("data field string: " + datahex);
				finaldata = msgtype + bitdata + datahex;
			}

		} else {
			if (myheader.length() > 0 && myheader != null) {
				System.out.println("convert to hex wo header");
				finaldata = (finalheader + msgtype + bitdata + datahex)
						.toUpperCase();
				System.out.println("message type Hex: " + msgtype);
				System.out.println("bitmap data Hex: " + bitdata);
				System.out.println("data field Hex: " + datahex);
				System.out.println("final data: " + finaldata);
			} else {
				System.out.println("convert to hex with header");
				finaldata = (msgtype + bitdata + datahex).toUpperCase();
				System.out.println("message type Hex: " + msgtype);
				System.out.println("bitmap data Hex: " + bitdata);
				System.out.println("data field Hex: " + datahex);
				System.out.println("final data: " + finaldata);
			}
			finaldata.toUpperCase();

		}

		// finaldata = (finalheader + msgtype + bitdata +
		// datahex).toUpperCase();

		if (isBinary) {
			return BancoBogotaHandler.convertHexToByte(finaldata);
		} else {
			return finaldata;
		}
	}

	
*/	
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
		int factor = 0;
		bitmapLength = 32;
		msgtypeLength = 8;
		factor = 2;
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
		System.out.println("i am using the parser as: "
				+ tempe.getAttribute("mti"));
		System.out.println("c my name is: " + tempe.getNodeName());
		System.out.println("c how many childs I have: "
				+ tempe.getChildNodes().getLength());
		// System.out.println(nodeToString(tempe));
		Element childtemp = null;
		
		if (tempe!= null)
		{
			int fieldstart = 0;
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
					
					String tagvalue = null;
					String LLlength = null;
					System.out.println("mytagname is: " + tagname + ", mytagnum is: " + tagnum + ", mytype is:" + tagtype + ", mylength is: " + taglength + ", start point is: " + fieldstart + ", got match lines? " + hm.containsKey(tagnum));

					String encoding = Charset.defaultCharset().displayName();
					
					if (hm.containsKey(tagnum)) {
						
						if ((tagEncoding != null) && (!tagEncoding.equals(""))) {
							
							encoding = tagEncoding;
						}
						
						if (taglength != null && taglength.length() > 0 && !tagtype.equals("BINARY")) {
							
							tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + Integer.parseInt(taglength) * factor),encoding);
							fieldstart += Integer.parseInt(taglength) * factor;

						} else {

							if (tagtype.equals("LLBIN")) {

								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 2));
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 2))) * factor;
								taglength = String .valueOf(fieldlength / 2);
								System.out.println("LLBIN length is: " + fieldlength);
								tagvalue = hexbody.substring(fieldstart + 2, fieldstart + 2 + fieldlength);
								fieldstart += 2 + fieldlength;

							} else if (tagtype.equals("LLBIN")) {

								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 4));
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 4))) * factor;
								taglength = String .valueOf(fieldlength / 2);
								System.out.println("LLBIN length is: " + fieldlength);
								tagvalue = hexbody.substring(fieldstart + 4, fieldstart + 4 + fieldlength);
								fieldstart += 4 + fieldlength;

							} else if (tagtype.equals("LLLBIN")) {

								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 6));
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 6))) * factor;
								taglength = String .valueOf(fieldlength / 2);
								System.out.println("LLLBIN length is: " + fieldlength);
								tagvalue = hexbody.substring(fieldstart + 6, fieldstart + 6 + fieldlength);
								fieldstart += 6 + fieldlength;

							}else if (tagtype.equals("LVAR")) {
								
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 2));
								
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 2)))* factor;
								taglength = String.valueOf(fieldlength / factor);
								System.out.println("LVAR length is: "+ fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 2, fieldstart + 2 + fieldlength), encoding);
								fieldstart += 2 + fieldlength;
								
							} else if (tagtype.equals("LLVAR")) {
								
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 4));
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 4)))* factor;
								taglength = String.valueOf(fieldlength / factor);
								System.out.println("LLVAR length is: "+ fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 4, fieldstart + 4 + fieldlength), encoding);
								fieldstart += 4 + fieldlength;
								
							} else if (tagtype.equals("LLLVAR")) {
								
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart, fieldstart + 6));
								int fieldlength = Integer.parseInt(CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart,fieldstart + 6))) * factor;
								taglength = String .valueOf(fieldlength / factor);
								System.out.println("LLLVAR length is: " + fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 6, fieldstart + 6 + fieldlength), encoding);
								fieldstart += 6 + fieldlength;
								
							} else if (tagtype.equals("BITMAP")) {

								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring( fieldstart, fieldstart + 32));
								int fieldlength = tagvalue.length() * factor;
								taglength = String.valueOf(fieldlength);
								System.out.println("BITMAP length is: " + fieldlength / factor);
								fieldstart += fieldlength;

							} else if (tagtype.equals("BINARY")) {
								
								int fieldlength = Integer.parseInt(taglength) * 2;
								System.out.println(hexbody.substring( fieldstart, fieldstart + fieldlength));
								tagvalue = hexbody.substring(fieldstart, fieldstart + fieldlength);
								// tagvalue =
								// convertHexToString(hexbody.substring(fieldstart,fieldstart+fieldlength));
								fieldstart += fieldlength;
							}
						}
						System.out.println("tagvalue " + i + ": " + tagvalue);
						Element childnode = finaldoc.createElement(tagname);
						childnode.setAttribute("num", tagnum);
						childnode.setAttribute("length", taglength);
//						childnode.setAttribute("fieldlength", LLlength);
						childnode.setAttribute("mytype", tagtype);
						childnode.setTextContent(tagvalue);
						bodyElement.appendChild(childnode);
					}
				}
			}
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
						
						int fieldlength = Integer.parseInt(taglength) * 2;
						System.out.println(myheader.substring( fieldstart, fieldstart + fieldlength));
						tagvalue = myheader.substring(fieldstart, fieldstart + fieldlength);
						
						fieldstart += fieldlength;
					} else {
						
						tagvalue = CustomExtensionsHandler.convertHexToString(myheader.substring(fieldstart,fieldstart + Integer.parseInt(taglength) * factor),encoding);
						fieldstart += Integer.parseInt(taglength) * factor;
					}
					
					Element childnode = finaldoc.createElement(tagname);
					childnode.setAttribute("num", tagnum);
					childnode.setAttribute("length", taglength);
					childnode.setAttribute("mytype", tagtype);
					childnode.setTextContent(tagvalue);
					
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
								System.out.println("LLVAR length is: "+ fieldlength);

								tagvalue = hexbody.substring(fieldstart + 2, fieldstart + 2+ fieldlength);
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
			return ISO8583Handler.ISO8583ParserHex(configfile, o, header,
					isBinary);

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

}
