package co.com.extensions.handlers;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itko.util.XMLUtils;

public class Recording {
	
	private static Logger logger = Logger.getLogger(Recording.class);
	
	private String elementName;
	
	private Element xmlPayloadNode;
	private Document doc;
	
	
	/**
	 * 
	 * @param xml
	 */
	public Recording(String xml) {
		
		try {
			DocumentBuilder dBuilder;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setIgnoringElementContentWhitespace(true);

				dBuilder = dbFactory.newDocumentBuilder();
				StringReader sr = new StringReader(xml);
				doc = dBuilder.parse(new InputSource(sr));
		} catch (ParserConfigurationException e) {
			logger.error("Error creando documento");
			throw new RuntimeException(e);
		} catch (SAXException e) {
			logger.error("Error parseando el documento: " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Error de I/O al procesar el string XML: " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @param xml
	 * @param elementName
	 */
	public Recording(String xml, String elementName) {
		try {
			doc = XMLUtils.newXMLDocument(new InputSource(new StringReader(xml)));
			this.elementName = elementName;
			
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
	}
	
	public String extract() {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile("//*[local-name() = \"" + this.getElementName() + "\"]");
			Object body = expr.evaluate(doc, XPathConstants.NODE);
			if (body instanceof Element) {
				xmlPayloadNode = (Element)getLeafNode((Element)body);
				if (xmlPayloadNode != null) {
					Node firstChild = xmlPayloadNode.getFirstChild();
					if (firstChild != null) {
						return firstChild.getNodeValue();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.fatal(e.getMessage());
		}
		return null;
	}
	
	public void inject(String xmlCopybook) {
		try {
			Document xmlCopybookDoc = XMLUtils.newXMLDocument(new InputSource(new StringReader(xmlCopybook)));
			Node xmlCopybookNode = xmlCopybookDoc.getDocumentElement();
			xmlCopybookNode = doc.importNode(xmlCopybookNode, true);
			Node textNode = xmlPayloadNode.getFirstChild();
			if (textNode != null && textNode.getNodeType() == Node.TEXT_NODE) {
				xmlPayloadNode.removeChild(textNode);
			}
			xmlPayloadNode.appendChild(xmlCopybookNode);
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
	}
	
	public static Node getLeafNode(Node n) throws Exception {
		if (n != null) {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);
				if (child != null && child.getNodeType() == Node.ELEMENT_NODE) {
					return getLeafNode(child);
				}
			}
		}
		return n;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Node getElementNodeByName (){
		
		NodeList nodeList = doc.getElementsByTagName(this.getElementName());
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equalsIgnoreCase(this.getElementName())){
				Element element = (Element)node;
				return element;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getListElements(){
		List<String> elements = new ArrayList<String>();
		
		Node firstNode = doc.getFirstChild();
		NodeList childNodes = firstNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			elements.add(child.getNodeName());
		}
		
		return elements;
	}
	
	public String toString() {
		try {
			return XMLUtils.toXML(doc.getDocumentElement());
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
		return "";
	}
	
	
	
	/**
	 * @return the elementName
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * @param elementName the elementName to set
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	
	/**
	 * @return the doc
	 */
	public Document getDoc() {
		return doc;
	}

	/**
	 * @param doc the doc to set
	 */
	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public static void main(String[] args) {
		
		String sgml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><RESPONSE><OH>26100000000000477</OH><AV>PEA00230NO EXISTEN MAS PERSONAS                                                </AV><DE>1P101      E</DE><OC>B1PEM0E5S                000156891566CESAR               NU#EZ               SANCHEZ             FP3 UE3 </OC><OC>B1PEM0E5S                ELVIRA VARG#SS                                    CULHUACAN CTM OBRERO          </OC><OC>B1PEM0E5S                I3423454545555NUSC60010132323   INT.01 CCOYOACAN                      PATRIM C  </OC></RESPONSE>";
		
//		String xml = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
//				"    <soapenv:Header>\n" + 
//				"        <ISMHdr xmlns=\"http://ism.hsbc.com/ISMHdr/V2.0\" ISMHdrVersNum=\"02.00\" RqstIndictr=\"N\" ConsumerId=\"HSBCWLTH.RMP_UAT_GENSRV\" MsgSvceVersNum=\"01.00\" MsgOperVersNum=\"01.00\" UserId=\"\" EmplyUserId=\"\" GloblLogId=\"node-id-not-cfg_depunt-not-cfg\" MsgInstcId=\"2\" UserDviceId=\"\" SessnId=\"\" MsgCreatTmspType=\"Z\" MsgCreatTmsp=\"2012-06-19T19:02:08.238000+08:00\">\n" + 
//				"            <EAIRespeCde>\n" + 
//				"                <RespeCde RtrnCde=\"0\"/>\n" + 
//				"            </EAIRespeCde>\n" + 
//				"        </ISMHdr>\n" + 
//				"        <OpHdr xmlns=\"http://ism.hsbc.com/OpHdr/V2.0\" OpDefCnt=\"1\" IgnrErrFlag=\"Y\">\n" + 
//				"            <OpDefHdr seq=\"1\" SvceId=\"OH_CUST_COMM\" OpId=\"SENDCOMNFF\" SvceVersNum=\"02.00\" OperVersNum=\"01.00\" OPUId=\"\" CountryCde=\"\" PayloadRespCodec=\"\" PayloadRespCCSID=\"\"/>\n" + 
//				"            <AppExtnsArea>\n" + 
//				"                <AppExtnGrp seq=\"1\">\n" + 
//				"                    <AppExtn item=\"1\" type=\"OH_SERVICE_HEADER\" UserData=\"0100000                                                                               \"/>\n" + 
//				"                    <AppExtn item=\"2\" type=\"OH_UTILITY_SERVICE_HEADER\" UserData=\"0100000                                                                                                                                                                                    \"/>\n" + 
//				"                </AppExtnGrp>\n" + 
//				"            </AppExtnsArea>\n" + 
//				"            <RespeCdeGrp>\n" + 
//				"                <RespeCde seq=\"1\" RtrnCde=\"8\" ReasCde=\"CHM09\" DiagText=\"Key not found in table (WMB_CONSUMER)\"/>\n" + 
//				"            </RespeCdeGrp>\n" + 
//				"        </OpHdr>\n" + 
//				"    </soapenv:Header>\n" + 
//				"    <soapenv:Body>\n" + 
//				"        <RespePayload>\n" + 
//				"            <foo>\n" + 
//				"        Test\n" + 
//				"            </foo>\n" + 
//				"        </RespePayload>\n" + 
//				"    </soapenv:Body>\n" + 
//				"</soapenv:Envelope>";
		
		String xmlCopybook = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><copybook-payload><Header><OUTPUT-HEADER>      <PROTOCOL left-pad-length=\"0\" origin-length=\"2\">26</PROTOCOL>      <SRVC-RSP left-pad-length=\"0\" origin-length=\"1\">1</SRVC-RSP>      <PRCS-CTL left-pad-length=\"0\" origin-length=\"1\">0</PRCS-CTL>      <SEQ-NUM left-pad-length=\"0\" origin-length=\"8\">00000000</SEQ-NUM>      <MES-LNGT left-pad-length=\"0\" origin-length=\"5\">00477</MES-LNGT>    </OUTPUT-HEADER>  </Header></copybook-payload>";
		Recording e = new Recording(sgml);
		List<String> elements = e.getListElements();
		for (String element : elements) {
			e.setElementName(element);
			String extract = e.extract();
			System.out.println(extract);
		}
		
		e = new Recording(xmlCopybook);
		e.setElementName("Header");
		Node node = e.getElementNodeByName();
		node = node.getFirstChild();
		String text = node.getTextContent();
		String name = node.getNodeName();
		String value = node.getNodeValue();
		System.out.println(text);
//		e.inject(xmlCopybook);
//		System.out.println(e.toString());
	}
}
