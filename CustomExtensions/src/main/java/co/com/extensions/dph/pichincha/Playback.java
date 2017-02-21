package co.com.extensions.dph.pichincha;

import com.itko.util.XMLUtils;

import java.io.*;
import java.util.logging.*;

import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class Playback {
	private static final String COPYBOOK_PAYLOAD_XPATH = "//*[local-name() = 'copybook-payload']";
	protected static Logger log   = Logger.getLogger(Playback.class.getName());
	
	private Document doc;
	private Element copybookPayloadNode;
	
	public Playback(String xml) {
		try {
			doc = XMLUtils.newXMLDocument(new InputSource(new StringReader(xml)));
		} catch (IOException e) {
			log.severe(e.getMessage());
		} catch (SAXException e) {
			log.severe(e.getMessage());
		}
	}
	
	/**
	 * @return XML representation of copybook payload embedded in SOAP/XML
	 */
	public String extract() {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(COPYBOOK_PAYLOAD_XPATH);
			Object obj = expr.evaluate(doc, XPathConstants.NODE);
			if (obj instanceof Element) {
				copybookPayloadNode = (Element)obj;
				return XMLUtils.toXML((Element)copybookPayloadNode);
			} else {
				log.severe("Using xpath: //copybook-payload; the result object " +
						"is not of org.w3c.dom.Element but of type: " + 
						obj == null ? "null" : obj.getClass().getName());
			}
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Injects copybook payload into SOAP/XML
	 * 
	 * @param copybook
	 */
	public void inject(String copybook) {
		if (copybookPayloadNode != null) {
			Node parentNode = copybookPayloadNode.getParentNode();
			parentNode.removeChild(copybookPayloadNode);
			parentNode.appendChild(doc.createTextNode(copybook));
		}
	}
	
	public String toString() {
		try {
			return XMLUtils.toXML(doc.getDocumentElement());
		} catch (Exception e) {
			return "";
		}
	}
	
	public static void main(String[] args) throws Exception {
		String xml = "<soapenv:Envelope xmlns=\"http://will.power\" xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
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
				"            <foo><copybook-payload>\n" + 
				"  <Body>\n" + 
				"    <LISA-CPY>\n" + 
				"      <HSBC-HEADER>\n" + 
				"        <DETAILS>\n" + 
				"          <NAME left-pad-length=\"0\" origin-length=\"4\">HSBC</NAME>\n" + 
				"          <STATUS left-pad-length=\"0\" origin-length=\"2\">29</STATUS>\n" + 
				"        </DETAILS>\n" + 
				"      </HSBC-HEADER>\n" + 
				"    </LISA-CPY>\n" + 
				"  </Body>\n" + 
				"</copybook-payload></foo>\n" + 
				"        </RespePayload>\n" + 
				"    </soapenv:Body>\n" + 
				"</soapenv:Envelope>";
		Playback p = new Playback(xml);
		p.extract();
		p.inject("HSBC29");
		System.out.println(p.toString());
//		Playback p = new Playback(xml);
//		String s = p.extract();
//		System.out.println(s);
	}
}
