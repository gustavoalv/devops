package co.com.extensions.dph.pichincha;

import java.io.StringReader;

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

import com.itko.util.XMLUtils;

public class RecordingXMLEmbedded {
	
	private static Logger logger = Logger.getLogger(RecordingXMLEmbedded.class);
	
	private Element xmlPayloadNode;
	private Document doc;
	
	public RecordingXMLEmbedded(String xml) {
		try {
			doc = XMLUtils.newXMLDocument(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
	}
	
	
	public String extract() {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile("//*[local-name() = \"Body\"]");
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
	
	public String toString() {
		try {
			return XMLUtils.toXML(doc.getDocumentElement());
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
		return "";
	}
	
	public static void main(String[] args) {
		String xml = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
				"    <soapenv:Header>\n" + 
				"        <ISMHdr xmlns=\"http://ism.hsbc.com/ISMHdr/V2.0\" ISMHdrVersNum=\"02.00\" RqstIndictr=\"N\" ConsumerId=\"HSBCWLTH.RMP_UAT_GENSRV\" MsgSvceVersNum=\"01.00\" MsgOperVersNum=\"01.00\" UserId=\"\" EmplyUserId=\"\" GloblLogId=\"node-id-not-cfg_depunt-not-cfg\" MsgInstcId=\"2\" UserDviceId=\"\" SessnId=\"\" MsgCreatTmspType=\"Z\" MsgCreatTmsp=\"2012-06-19T19:02:08.238000+08:00\">\n" + 
				"            <EAIRespeCde>\n" + 
				"                <RespeCde RtrnCde=\"0\"/>\n" + 
				"            </EAIRespeCde>\n" + 
				"        </ISMHdr>\n" + 
				"        <OpHdr xmlns=\"http://ism.hsbc.com/OpHdr/V2.0\" OpDefCnt=\"1\" IgnrErrFlag=\"Y\">\n" + 
				"            <OpDefHdr seq=\"1\" SvceId=\"OH_CUST_COMM\" OpId=\"SENDCOMNFF\" SvceVersNum=\"02.00\" OperVersNum=\"01.00\" OPUId=\"\" CountryCde=\"\" PayloadRespCodec=\"\" PayloadRespCCSID=\"\"/>\n" + 
				"            <AppExtnsArea>\n" + 
				"                <AppExtnGrp seq=\"1\">\n" + 
				"                    <AppExtn item=\"1\" type=\"OH_SERVICE_HEADER\" UserData=\"0100000                                                                               \"/>\n" + 
				"                    <AppExtn item=\"2\" type=\"OH_UTILITY_SERVICE_HEADER\" UserData=\"0100000                                                                                                                                                                                    \"/>\n" + 
				"                </AppExtnGrp>\n" + 
				"            </AppExtnsArea>\n" + 
				"            <RespeCdeGrp>\n" + 
				"                <RespeCde seq=\"1\" RtrnCde=\"8\" ReasCde=\"CHM09\" DiagText=\"Key not found in table (WMB_CONSUMER)\"/>\n" + 
				"            </RespeCdeGrp>\n" + 
				"        </OpHdr>\n" + 
				"    </soapenv:Header>\n" + 
				"    <soapenv:Body>\n" + 
				"        <RespePayload>\n" + 
				"            <foo>\n" + 
				"        Test\n" + 
				"            </foo>\n" + 
				"        </RespePayload>\n" + 
				"    </soapenv:Body>\n" + 
				"</soapenv:Envelope>";
		
		String xmlCopybook = "                <copybook-payload>\n" + 
				"                    <Body>\n" + 
				"                        <LISA-CPY>\n" + 
				"                            <HSBC-HEADER>\n" + 
				"                                <DETAILS>\n" + 
				"                                    <NAME left-pad-length=\"0\" origin-length=\"4\">HSBC</NAME>\n" + 
				"                                    <STATUS left-pad-length=\"0\" origin-length=\"2\">29</STATUS>\n" + 
				"                                </DETAILS>\n" + 
				"                            </HSBC-HEADER>\n" + 
				"                        </LISA-CPY>\n" + 
				"                    </Body>\n" + 
				"                </copybook-payload>";
		RecordingXMLEmbedded e = new RecordingXMLEmbedded(xml);
		String extract = e.extract();
		e.inject(xmlCopybook);
		System.out.println(extract);
		System.out.println(e.toString());
	}
}
