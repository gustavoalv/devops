package co.com.extensions;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import co.com.extensions.handlers.CustomExtensionsHandler;

import com.google.common.base.Charsets;
import com.itko.citi.Converter;

public class CopyOfISO8583HandlerBackup {

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

	public static HashMap<String, String> readBitmap(String hexdata,
			boolean trace) {
		HashMap<String, String> hm = new HashMap<String, String>();
		String binaryresult = "";
		for (int i = 0; i < hexdata.length(); i++) {
			String hexa = hexdata.substring(i, i + 1);
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

	public static Object ISO8583XmlToObject(String myfile, String direction,
			boolean isFile, boolean isBinary) throws Exception {
		// StringReader sr = new StringReader(xmldata);
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
		// System.out.println(nodeList.getLength());
		// NodeList nodeList2 = doc.getElementsByTagName("*");
		Element e1, e2 = null;
		System.out.println("begin to process field 123...");
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
						if (CustomExtensionsHandler.convertISOCountry(e1.getTextContent().substring(2,
								4)) != null) {
							System.out.println("in 123");
							e2.setTextContent(CustomExtensionsHandler.convertISOCountry(e1
									.getTextContent().substring(2, 4)));
							System.out.println("reset country code: "
									+ CustomExtensionsHandler.convertISOCountry(e1.getTextContent()
											.substring(2, 4)));
						}
					}
				}

			}
		}
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
					bitdata1 += CustomExtensionsHandler.convertBinaryToHex(binarydata1.substring(e,
							e + 4));
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
				currentElement = (Element) node;
				nodenum = currentElement.getAttribute("num");
				roottype = currentElement.getAttribute("type");
				nodetype = currentElement.getAttribute("mytype");
				nodelength = currentElement.getAttribute("length");
				fieldlength = currentElement.getAttribute("fieldlength");
				
				System.out.println("nodetype: " + nodetype + " , " + "nodenum: " + nodenum + " , " + "nodelength: " + nodelength + " , " + "fieldlength: " + fieldlength);

				if (roottype != null && roottype.length() > 0) {
					if (currentElement.getAttribute("header") != null
							&& currentElement.getAttribute("header").length() > 0) {
						myheader = currentElement.getAttribute("header");
					}
					msgtype = roottype;
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
							hexvalue = nodevalue;
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
											+ finalpadding;
								} else {
									nodevalue = fieldlength
											+ nodevalue
											+ finalpadding;
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							} else {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = finalpadding
											+ nodevalue;
								} else {
									nodevalue = fieldlength
											+ finalpadding
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
			bitdata += CustomExtensionsHandler.convertBinaryToHex(binarydata.substring(e, e + 4));
		}
		System.out.println("bitmap1 and 2 Hex: " + bitdata);
		
//		bitdata = BancoBogotaHandler.convertStringToHex(bitdata);
		// Bitdata esta en HEX
		System.out.println("bitmap1 and 2 String: " + bitdata);

		System.out.println(sb.toString() + sbemv.toString());
		String datahex = sb.toString() + sbemv.toString();
		
		// TODO: Este header cuando tiene de largo 4 en HEXA. 
		String finaldata = (msgtype + bitdata + datahex).toUpperCase();
		/*
		int datalength = finaldata.length() / 2;
		String finalheader = Integer.toHexString(datalength);
		if (finalheader.length() < 4) {
			String padding = "";
			for (int n = 0; n < 4 - finalheader.length(); n++) {
				padding += "0";
			}
			finalheader = padding + finalheader;
		}
		*/
		finaldata = (myheader + msgtype + bitdata + datahex).toUpperCase();
		System.out.println("myheader: " + myheader);
		System.out.println("message type Hex: " + msgtype);
		System.out.println("bitmap data Hex: " + bitdata);
		System.out.println("data field Hex: " + datahex);
		System.out.println("final data: " + finaldata);
		
		if (isBinary) {
			return CustomExtensionsHandler.convertHexToByte(finaldata);
		} else {
			return finaldata;
		}
	}

	public static Object ISO8583XmlToObject(String myfile, String direction,
			boolean isFile, boolean isBinary, String contenttype)
			throws Exception {
		// StringReader sr = new StringReader(xmldata);
		System.out.println("start processing: " + myfile);
		// String direction = "left";
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
		// System.out.println(nodeList.getLength());
		// NodeList nodeList2 = doc.getElementsByTagName("*");
		Element e1, e2 = null;
		/*
		 * System.out.println("begin to process field 123..."); for (int i = 0;
		 * i < nodeList1.getLength(); i++) { Node node = nodeList1.item(i);
		 * nodevalue = ""; if (node.getNodeType() == Node.ELEMENT_NODE) { // do
		 * something with the current element e1 = (Element) node; if
		 * (e1.getAttribute("num").equals("114")) { e2 = e1; } else if
		 * (e1.getAttribute("num").equals("123") && e2 != null && e1 != null) {
		 * if (!e1.getTextContent().contains("{") &&
		 * e1.getTextContent().length() > 0) { if
		 * (convertISOCountry(e1.getTextContent().substring(2, 4)) != null) {
		 * System.out.println("in 123"); e2.setTextContent(convertISOCountry(e1
		 * .getTextContent().substring(2, 4)));
		 * System.out.println("reset country code: " +
		 * convertISOCountry(e1.getTextContent() .substring(2, 4))); } } }
		 * 
		 * } }
		 */
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
					bitdata1 += CustomExtensionsHandler.convertBinaryToHex(binarydata1.substring(e,
							e + 4));
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
					msgtype = CustomExtensionsHandler.convertStringToHex(roottype);
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
							hexvalue = CustomExtensionsHandler.convertStringToHex(nodevalue);
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
							hexvalue = CustomExtensionsHandler.convertStringToHex(nodevalue);
						}

						sb.append(hexvalue);
						System.out.println("after conversion: " + hexvalue);
						temphm.put(nodenum, nodenum);
					} else {
						System.out.println("line is skipped: " + nodenum);
					}
					if (Integer.parseInt(nodenum) == 65) {
						emvtag = nodevalue;
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
											+ CustomExtensionsHandler.convertStringToHex(finalpadding);
								} else {
									nodevalue = CustomExtensionsHandler.convertStringToHex(fieldlength)
											+ nodevalue
											+ CustomExtensionsHandler.convertStringToHex(finalpadding);
								}
								System.out.println(nodevalue
										+ ", valuelength is: "
										+ nodevalue.length());
							} else {
								if (!nodetype.contains("VAR")
										&& !nodetype.contains("BITMAP")) {
									nodevalue = CustomExtensionsHandler.convertStringToHex(finalpadding)
											+ nodevalue;
								} else {
									nodevalue = CustomExtensionsHandler.convertStringToHex(fieldlength
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
			bitdata += CustomExtensionsHandler.convertBinaryToHex(binarydata.substring(e, e + 4));
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
			return CustomExtensionsHandler.convertHexToByte(finaldata);
		} else {
			return finaldata;
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
	private static String ISO8583ParserHex(String configfile, Object o, int header, boolean isBinary) {
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
		try {

			if (header != 0) {
				myheader = hexbody.substring(0, header);
				hexbody = hexbody.substring(header, hexbody.length());
			}

			String bitmap = null;
			msgtype = CustomExtensionsHandler.convertHexToString(hexbody.substring(0, msgtypeLength));

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
				mytype = tempe.getAttribute("type");
				if (mytype.equals(msgtype)) {
					System.out.println("got a match parsing from config file: "
							+ i);
					break;
				} else {
					tempe = (Element) nl.item(0);
				}
			}

			// create new xml document
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DocumentBuilderFactory finalFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder finalBuilder = finalFactory.newDocumentBuilder();
			Document finaldoc = finalBuilder.newDocument();
			Element rootElement = finaldoc.createElement("Message");
			rootElement.setAttribute("type", msgtype);
			rootElement.setAttribute("header", myheader);
			finaldoc.appendChild(rootElement);
			int fieldstart = 0;
			// hexbody =
			// convertHexToString(hexbody.substring(40,hexbody.length()));
			hexbody = hexbody.substring(msgtypeLength + bitmapLength,
					hexbody.length());
			System.out.println("fieldstring is: " + hexbody);
			System.out.println("i am using the parser as: "
					+ tempe.getAttribute("type"));
			System.out.println("c my name is: " + tempe.getNodeName());
			System.out.println("c how many childs I have: "
					+ tempe.getChildNodes().getLength());
			// System.out.println(nodeToString(tempe));
			Element childtemp = null;
			NodeList nodes = tempe.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					childtemp = (Element) nodes.item(i);
					System.out.println(childtemp.getNodeName());
					String tagname = childtemp.getAttribute("name").replaceAll(
							" ", "_");
					String tagnum = childtemp.getAttribute("num");
					String tagtype = childtemp.getAttribute("type");
					String taglength = childtemp.getAttribute("length");
					String tagvalue = null;
					String LLlength = null;
					System.out.println("mytagname is: " + tagname
							+ ", mytagnum is: " + tagnum + ", mytype is:"
							+ tagtype + ", mylength is: " + taglength
							+ ", start point is: " + fieldstart
							+ ", got match lines? " + hm.containsKey(tagnum));

					if (hm.containsKey(tagnum)) {
						if (taglength != null && taglength.length() > 0
								&& !tagtype.equals("BINARY")) {
							tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(
									fieldstart,
									fieldstart + Integer.parseInt(taglength)
											* factor));
							fieldstart += Integer.parseInt(taglength) * factor;

						} else {

							if (tagtype.equals("LVAR")) {
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart, fieldstart + 2));
								int fieldlength = Integer
										.parseInt(CustomExtensionsHandler.convertHexToString(hexbody
												.substring(fieldstart,
														fieldstart + 2)))
										* factor;
								taglength = String
										.valueOf(fieldlength / factor);
								System.out.println("LVAR length is: "
										+ fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart + 2, fieldstart
												+ 2 + fieldlength));
								fieldstart += 2 + fieldlength;
							} else if (tagtype.equals("LLVAR")) {
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart, fieldstart + 4));
								int fieldlength = Integer
										.parseInt(CustomExtensionsHandler.convertHexToString(hexbody
												.substring(fieldstart,
														fieldstart + 4)))
										* factor;
								taglength = String
										.valueOf(fieldlength / factor);
								System.out.println("LLVAR length is: "
										+ fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart + 4, fieldstart
												+ 4 + fieldlength));
								fieldstart += 4 + fieldlength;
							} else if (tagtype.equals("LLLVAR")) {
								LLlength = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart, fieldstart + 6));
								int fieldlength = Integer
										.parseInt(CustomExtensionsHandler.convertHexToString(hexbody
												.substring(fieldstart,
														fieldstart + 6)))
										* factor;
								taglength = String
										.valueOf(fieldlength / factor);
								System.out.println("LLLVAR length is: "
										+ fieldlength);
								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart + 6, fieldstart
												+ 6 + fieldlength));
								fieldstart += 6 + fieldlength;
							} else if (tagtype.equals("BITMAP")) {

								tagvalue = CustomExtensionsHandler.convertHexToString(hexbody
										.substring(fieldstart, fieldstart + 32));
								int fieldlength = tagvalue.length() * factor;
								taglength = String.valueOf(fieldlength);
								System.out.println("BITMAP length is: "
										+ fieldlength / factor);
								fieldstart += fieldlength;

							} else if (tagtype.equals("BINARY")) {
								int fieldlength = Integer.parseInt(taglength);
								System.out.println(hexbody.substring(
										fieldstart, fieldstart + fieldlength));
								tagvalue = hexbody.substring(fieldstart,
										fieldstart + fieldlength);
								// tagvalue =
								// convertHexToString(hexbody.substring(fieldstart,fieldstart+fieldlength));
								fieldstart += fieldlength;
							}
						}
						System.out.println("tagvalue " + i + ": " + tagvalue);
						Element childnode = finaldoc.createElement(tagname);
						childnode.setAttribute("num", tagnum);
						childnode.setAttribute("length", taglength);
						childnode.setAttribute("fieldlength", LLlength);
						childnode.setAttribute("mytype", tagtype);
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
	 * @return
	 */
	private static String ISO8583ParserString(String configfile, Object o, int header, boolean isBinary){
		
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
				mytype = tempe.getAttribute("type");
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
			rootElement.setAttribute("type", msgtype);
			rootElement.setAttribute("header", myheader);
			
			finaldoc.appendChild(rootElement);
			int fieldstart = 0;
			// hexbody =
			// convertHexToString(hexbody.substring(40,hexbody.length()));
			hexbody = hexbody.substring(msgtypeLength + bitmapLength,
					hexbody.length());
			System.out.println("fieldstring is: " + hexbody);
			System.out.println("i am using the parser as: "
					+ tempe.getAttribute("type"));
			System.out.println("c my name is: " + tempe.getNodeName());
			System.out.println("c how many childs I have: "
					+ tempe.getChildNodes().getLength());
			// System.out.println(nodeToString(tempe));
			Element childtemp = null;
			NodeList nodes = tempe.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					childtemp = (Element) nodes.item(i);
					System.out.println(childtemp.getNodeName());
					String tagname = childtemp.getAttribute("name").replaceAll(
							" ", "_");
					String tagnum = childtemp.getAttribute("num");
					String tagtype = childtemp.getAttribute("type");
					String taglength = childtemp.getAttribute("length");
					String tagEncoding = childtemp.getAttribute("encoding");
					
					String tagvalue = null;
					String LLlength = null;
					System.out.println("mytagname is: " + tagname
							+ ", mytagnum is: " + tagnum + ", mytype is:"
							+ tagtype + ", mylength is: " + taglength
							+ ", start point is: " + fieldstart
							+ ", got match lines? " + hm.containsKey(tagnum));

					if (hm.containsKey(tagnum)) {
						if (taglength != null && taglength.length() > 0
								&& !tagtype.equals("BINARY")) {
							tagvalue = hexbody.substring( fieldstart,
									fieldstart + Integer.parseInt(taglength)
											* factor);
							fieldstart += Integer.parseInt(taglength) * factor;

						} else {

							if (tagtype.equals("LVAR")) {
								LLlength = hexbody
										.substring(fieldstart, fieldstart + 1);
								int fieldlength = Integer
										.parseInt(hexbody
												.substring(fieldstart,
														fieldstart + 1))
										* factor;
								taglength = String
										.valueOf(fieldlength / factor);
								System.out.println("LVAR length is: "
										+ fieldlength);
								tagvalue = hexbody
										.substring(fieldstart + 1, fieldstart
												+ 1 + fieldlength);
								fieldstart += 1 + fieldlength;
							} else if (tagtype.equals("LLVAR")) {
								
								
								
								if((tagEncoding != null) && (!tagEncoding.equals(""))){
									factor = 2;
									LLlength = CustomExtensionsHandler.convertHexToString(hexbody
											.substring(fieldstart, fieldstart + 6));
									int fieldlength = Integer
											.parseInt(CustomExtensionsHandler.convertHexToString(hexbody
													.substring(fieldstart,
															fieldstart + 6)))
											* factor;
									taglength = String
											.valueOf(fieldlength / factor);
									System.out.println("LLLVAR length is: "
											+ fieldlength);
									tagvalue = CustomExtensionsHandler.convertHexToString(hexbody.substring(fieldstart + 6, fieldstart+ 6 + fieldlength),tagEncoding);
									fieldstart += 6 + fieldlength;
									
								} else {
									
									LLlength = hexbody
											.substring(fieldstart, fieldstart + 2);
									
									int fieldlength = Integer
											.parseInt(hexbody
													.substring(fieldstart,
															fieldstart + 2))
											* factor;
									taglength = String
											.valueOf(fieldlength / factor);
									System.out.println("LLVAR length is: "
											+ fieldlength);
									
									tagvalue = hexbody.substring(fieldstart + 2, fieldstart + 2 + fieldlength);
									fieldstart += 2 + fieldlength;
									
								}
								
								
							} else if (tagtype.equals("LLLVAR")) {
								LLlength = hexbody
										.substring(fieldstart, fieldstart + 3);
								int fieldlength = Integer
										.parseInt(hexbody
												.substring(fieldstart,
														fieldstart + 3))
										* factor;
								taglength = String
										.valueOf(fieldlength / factor);
								System.out.println("LLLVAR length is: "
										+ fieldlength);
								tagvalue = hexbody
										.substring(fieldstart + 3, fieldstart
												+ 3 + fieldlength);
								fieldstart += 3 + fieldlength;
							} else if (tagtype.equals("BITMAP")) {

								tagvalue = hexbody
										.substring(fieldstart, fieldstart + 16);
								int fieldlength = tagvalue.length() * factor;
								taglength = String.valueOf(fieldlength);
								System.out.println("BITMAP length is: "
										+ fieldlength / factor);
								fieldstart += fieldlength;

							} else if (tagtype.equals("BINARY")) {
								int fieldlength = Integer.parseInt(taglength);
								System.out.println(hexbody.substring(
										fieldstart, fieldstart + fieldlength));
								tagvalue = hexbody.substring(fieldstart,
										fieldstart + fieldlength);
								// tagvalue =
								// convertHexToString(hexbody.substring(fieldstart,fieldstart+fieldlength));
								fieldstart += fieldlength;
							}
						}
						System.out.println("tagvalue " + i + ": " + tagvalue);
						Element childnode = finaldoc.createElement(tagname);
						childnode.setAttribute("num", tagnum);
						childnode.setAttribute("length", taglength);
						childnode.setAttribute("fieldlength", LLlength);
						childnode.setAttribute("mytype", tagtype);
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
	public static String ISO8583Parser(String configfile, Object o, int header, boolean isBinary, String contentType) throws Exception{

		if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_HEXA)) {

			System.out.println("ISO Message HEXA Content");
			return CopyOfISO8583HandlerBackup.ISO8583ParserHex(configfile, o, header, isBinary);
			
		} else if (contentType.equals(CustomExtensionsHandler.CONTENT_TYPE_ASCII)) {
			
			System.out.println("ISO Message ASCII Content");
			return CopyOfISO8583HandlerBackup.ISO8583ParserString(configfile, o, header, isBinary);
			
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
