package co.com.extensions.dph;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import co.com.extensions.handlers.BMSHandler;
import co.com.extensions.handlers.CustomExtensionsHandler;
import co.com.extensions.handlers.Playback;
import co.com.extensions.handlers.Recording;
import co.com.extensions.util.ElementPS9;

import com.itko.citi.Converter;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.copybook.CopybookDataProtocol;
import com.itko.util.Parameter;
import com.itko.util.ParameterList;
import com.itko.util.XMLUtils;

/**
 * 
 * @author wtruong
 * 
 * Copybook Data Protocol Handler for response SOAP/XML with embedded copybook payload
 */
public class PS7DataProtocol extends CopybookDataProtocol {
	
	private static final String TAG_KEY = "TAG";
	
	private static final String RESPONSE_KEY = "Response";
	private static final String REQUEST_KEY = "Request";
	
	private static final String ER_TAG = "ER";
	private static final String AV_TAG = "AV";
	private static final String DC_TAG = "DC";
	private static final String JO_TAG = "JO";
	private static final String SG_TAG = "SG";
	private static final String CO_TAG = "CO";
	
	private static final String COPY_NAME_TAG = "COPY-NAME";
	
	private static final String ERROR_CODE = "ERROR-CODE";
	
	private static final String RESULT_TAG = "result";
	private static final String SEQUENCE_TAG = "sequence";
	private static final String AV_CODE = "CODE";
	private static final String WARN_DESC_ELEMENT = "WARN-DESC";
	private static final String ROOT_ELEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	
	private static final String COPY_VALUE = "C";
	private static final String BMS_VALUE = "B";
	
    protected static Logger log   = Logger.getLogger(PS7DataProtocol.class.getName());
    
	private Document finaldoc;
	
	private static String BMS_CONFIG_FILE_NAME = "bms-config.xml";
	
	
    /* (non-Javadoc)
	 * @see com.itko.lisa.vse.stateful.protocol.copybook.CopybookDataProtocol#updateRequest(com.itko.lisa.test.TestExec, com.itko.lisa.vse.stateful.model.Request)
	 */
	/*
	@Override
	public void updateRequest(TestExec testExec, Request request) {
		
		try{
			
			this.createXMLDocument(REQUEST_KEY);
			Node rootElement = finaldoc.getFirstChild();
		
			String requestMessage = this.buildXMLRequest(request.getBodyAsString());
			
			List<String> elements = new ArrayList<String>();
			
			Map<String, List<ElementPS9>> data = getListElementsXMLString(request.getBodyAsString());
			
			Iterator<Entry<String, List<ElementPS9>>> it = data.entrySet().iterator();
			while (it.hasNext()){
				Entry<String, List<ElementPS9>> pair = it.next();
				String elemento = pair.getKey();
				elements.add(elemento);
				log.debug("Se agrego el elemento: " + elemento);
			}
			
	    	// Asume que viene un XML bien formado
//	        Recording rec = new Recording(requestMessage);
//			List<String> elements = rec.getListElements();
	        
	        for (String element : elements) {
	        	
				log.debug("El elemento que vamos a trabajar es: " + element);
//				CopybookDataProtocol dph =  new CopybookDataProtocol(this.getConfig());
				
//				rec.setElementName(element);
//				String copybook = rec.extract();
				List<ElementPS9> elementPS9s = data.get(element);
				
				for (ElementPS9 elementPS9 : elementPS9s) {
				
					String copybook = elementPS9.getContent();
					
					ParameterList metaData = request.getMetaData();
					metaData.removeParameter(TAG_KEY);
					
					Parameter p = null;
					String xmlCopybook = null;
					
					if(element.equalsIgnoreCase(IH_TAG)){
						
						request.setBody(copybook);
						p = new Parameter(TAG_KEY,element);
						metaData.addParameter(p);
						
						log.debug("Debe buscar el según el tag: " + request.getMetaData().get(TAG_KEY));
						log.debug("Debe va a convertir el texto: ->" + request.getBodyAsString() + "<-");
						log.debug("Preparando a procesar CopyBook");
						
						super.updateRequest(testExec, request);
						xmlCopybook = request.getBodyAsString();
						
						for (int i = 0; i < request.getArguments().size(); i++) {
							Parameter arg = request.getArguments().get(i);
							log.debug("Name: " + arg.getName() + "Value: " + arg.getValue());
						}
						
						log.debug("CopyBook Procesado IH: ");
						log.debug(xmlCopybook);
						
						xmlCopybook = this.trimXMLString(xmlCopybook);
						
						Recording recCopy = new Recording(xmlCopybook);
						Document doc = recCopy.getDoc();
						Element rootCopy = doc.getDocumentElement();
						
						
						Element firstChild = (Element)rootCopy.getFirstChild();
						log.debug("firstChild.getNodeName() IH: " + firstChild.getNodeName());
						
						Node nodeImported = finaldoc.importNode(rootCopy.getFirstChild(), true);
						
						rootElement.appendChild(nodeImported);
						
					} else {
						
						String copyLength = copybook.substring(0, 4);
						String copyType = copybook.substring(4, 5);
						String message = copybook.substring(5);
						
						log.debug("copyType: " + copyType);
						
						StringBuffer buffer = new StringBuffer();
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + ME_TAG + ">");
						buffer.append("<" + ME_COPY_LENGTH_ELEMENT + ">" + copyLength + "</" + ME_COPY_LENGTH_ELEMENT + ">");
						buffer.append("<" + ME_COPY_TYPE_ELEMENT + ">" + copyType + "</" + ME_COPY_TYPE_ELEMENT + ">");
						buffer.append("</" + ME_TAG + ">");
						
						log.debug("XML ME INICIAL: " + buffer);
						
						Recording me = new Recording(buffer.toString());
						Document docMe = me.getDoc();
						Element rootME = docMe.getDocumentElement();
						
						copybook = message;
						
						String formato = request.getArguments().get(IH_HEADER_TX_CODE);
						
						
						Node nodeImported = null;
						
						if(copyType.equalsIgnoreCase(COPY_VALUE)){
							
							request.setBody(copybook);
							p = new Parameter(TAG_KEY,formato);
							metaData.addParameter(p);
							
							log.debug("Debe buscar el según el tag: " + request.getMetaData().get(TAG_KEY));
							log.debug("Debe va a convertir el texto: ->" + request.getBodyAsString() + "<-");
							log.debug("Preparando a procesar CopyBook");
							
							super.updateRequest(testExec, request);
							xmlCopybook = request.getBodyAsString();
							
							log.debug("CopyBook Procesado: ");
							log.debug(xmlCopybook);
							
							xmlCopybook = trimXMLString(xmlCopybook);
							
							Recording recCopy = new Recording(xmlCopybook);
							Document doc = recCopy.getDoc();
							Element rootCopy = doc.getDocumentElement();
							
							Element firstChild = (Element)rootCopy.getFirstChild();
							log.debug("firstChild.getNodeName() COPY: " + firstChild.getNodeName());
							
							nodeImported = docMe.importNode(rootCopy.getFirstChild(), true);
							
						} else if(copyType.equalsIgnoreCase(BMS_VALUE)){
							
							String pathFile = this.getConfig().getCopybookFileDefinitionFolderParsed(testExec);
							
							log.debug("getFileDefinitionMapPathParsed: ");
							log.debug(pathFile);
							
							String configfile = pathFile + "\\" + BMS_CONFIG_FILE_NAME;
							
							String hexaMessage = Converter.convertStringToHex(copybook);
							
							byte[] dataByteAscii;
							String resultXML = null;
							try {
								dataByteAscii = Converter.convertHexToByte(hexaMessage);
								resultXML = BMSHandler.BMSParser(configfile, dataByteAscii, formato, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_REQUEST);
							} catch (Exception e) {
								log.fatal("Error convirtiendo el mensaja de HEX to ASCII");
							}
							
							Recording recCopy = null;
							if(resultXML != null){
								
								recCopy = new Recording(resultXML);
								
							} else {
								
								StringBuffer unknowMessage =  new StringBuffer();
								unknowMessage.append(ROOT_ELEMENT);
								unknowMessage.append("<" + BMSHandler.BMS_UNKNOW_ROOT_ELEMENT+ ">");
								unknowMessage.append(hexaMessage);
								unknowMessage.append("</" + BMSHandler.BMS_UNKNOW_ROOT_ELEMENT+ ">");
								resultXML = unknowMessage.toString();
								recCopy = new Recording(unknowMessage.toString());
								
							}
							
							
							log.debug("BMS Procesado: ");
							log.debug(resultXML);
							
							Document doc = recCopy.getDoc();
							Element rootCopy = doc.getDocumentElement();
							
							nodeImported = docMe.importNode(rootCopy, true);
							
							
						}
						rootME.appendChild(nodeImported);
						Node rootMEImported = finaldoc.importNode(rootME, true);
						
						rootElement.appendChild(rootMEImported);
					}
					
		        }
			}
	        
	        String xmlDocument = this.getXMLDocument();
			log.debug("xmlDocument getXMLDocument= " + xmlDocument);
			request.getArguments().clear();
	        request.setBody(xmlDocument);
	        
		}catch (Exception e){
			log.fatal(e.getMessage(), e);
			log.fatal("El mensaje no va a ser procesado: ->" + request.getBodyAsString() + "<-");
		}
	}
	
	*/

	private String trimXMLString(String xmlCopybook) {
		String [] elements = xmlCopybook.split("\n");
		
		log.debug("trimXMLString xmlCopybook= " + xmlCopybook);
		
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < elements.length; i++) {
			buffer.append(elements[i].trim());
		}
		
		
		log.debug("trimXMLString xmlCopybook TRIMED " + buffer.toString());
		
		return buffer.toString();
	}

	/**
     * Extract copybook payload during recording
     * 
     * @param response
     *            the response to update.
     */
    @Override
    public void updateResponse(TestExec testExec, Response response) {
    	
    	boolean isBinary = response.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = response.getBodyAsByteArray();
			} else {
				payloadBody = response.getBodyAsString().getBytes();
			}
    		
    		log.info("PS7 public void updateResponse(TestExec testExec, Response response) ");
    		
    		String responsetMessage = new String(payloadBody, "cp500");
    		
    		log.info("PS7 response.getBodyAsString(): " + responsetMessage);
    		
    		log.info("PS7 Response responsetMessage.hexa " + Converter.convertStringToHex(responsetMessage));
    		
			log.info(new String());
    				
			this.createXMLDocument(RESPONSE_KEY);
			
			Node rootElement = finaldoc.getFirstChild();
			
//			String responsetMessage = response.getBodyAsString();
			
			List<String> elements = new ArrayList<String>();
			
			Map<String, List<ElementPS9>> data = getListElementsXMLString(responsetMessage);
			
			Iterator<Entry<String, List<ElementPS9>>> it = data.entrySet().iterator();
			while (it.hasNext()){
				Entry<String, List<ElementPS9>> pair = it.next();
				String elemento = pair.getKey();
				elements.add(elemento);
				log.info("Se agrego el elemento: " + elemento);
			}
			
			

	        
			for (String element : elements) {
				
				List<ElementPS9> mensajes = new ArrayList<ElementPS9>();
				mensajes = data.get(element);
	
				log.debug(element + " es el elemento que se va trabajar");
				
				String copybook = null;

				
				for (ElementPS9 string : mensajes) {
					
					copybook = string.getContent();
					
					ParameterList metaData = response.getMetaData();
					metaData.removeParameter(TAG_KEY);
					Parameter p = null;
					
					String copyNameTag = null;
					
					Node nodeImported = null;
					if(element.equalsIgnoreCase(RESULT_TAG)){
						
						StringBuffer buffer = new StringBuffer();
						
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + RESULT_TAG + ">");
						buffer.append(copybook);
						buffer.append("</" + RESULT_TAG + ">");
						
						Recording avDoc = new Recording(buffer.toString());
						Document docAV = avDoc.getDoc();
						Element rootAV = docAV.getDocumentElement();
						
						nodeImported = finaldoc.importNode(rootAV, true);
						
						rootElement.appendChild(nodeImported);
						
					} else if(element.equalsIgnoreCase(SEQUENCE_TAG)){
						
						StringBuffer buffer = new StringBuffer();
						
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + SEQUENCE_TAG + ">");
						buffer.append(copybook);
						buffer.append("</" + SEQUENCE_TAG + ">");
						
						Recording avDoc = new Recording(buffer.toString());
						Document docAV = avDoc.getDoc();
						Element rootAV = docAV.getDocumentElement();
						
						nodeImported = finaldoc.importNode(rootAV, true);
						
						rootElement.appendChild(nodeImported);
						
					} else if(element.equalsIgnoreCase(DC_TAG)){
						
						String copyNameEspace = copybook.substring(0, 8);
						String copyName = copyNameEspace.trim();
						String copybookMessage = copybook.substring(8);	
						
						copyNameTag = "<" + COPY_NAME_TAG + ">" + copyNameEspace + "</" + COPY_NAME_TAG + ">";
						
						StringBuffer buffer = new StringBuffer();
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + DC_TAG + ">");
						buffer.append(copyNameTag);
						buffer.append("</" + DC_TAG + ">");
						
						log.debug("DC: " + buffer.toString());
	
						Recording ocRec = new Recording(buffer.toString());
						Document docOC = ocRec.getDoc();
						Element rootOC = docOC.getDocumentElement();
						String xmlCopybook = null;
						
						log.info("CopyBook : ");
						log.info(copybook);
						
						log.info("CopyBook copybookMessage: ");
						log.info(copybookMessage);
						
						p = new Parameter(TAG_KEY,copyName);
						
						// TODO Se dejo así por ahora el formato del mensaje esta por un Copybook 
						String copyType = COPY_VALUE;
						
						if(copyType.equalsIgnoreCase(COPY_VALUE)){
							
							response.setBody(copybookMessage);
							p = new Parameter(TAG_KEY,copyName);
							metaData.addParameter(p);
							
							log.info("Debe buscar el según el tag: " + response.getMetaData().get(TAG_KEY));
							log.info("Debe va a convertir el texto: ->" + response.getBodyAsString() + "<-");
							log.info("Data Message: " + copybook);
							log.info("Preparando a procesar CopyBook");
							
							super.updateResponse(testExec, response);
							xmlCopybook = response.getBodyAsString();
							response.getMetaData().removeParameter("RESPONSE_PAYLOAD_DEF_NAME_KEY");
							
							log.info("CopyBook Procesado: ");
							log.info(xmlCopybook);
							
							xmlCopybook = this.trimXMLString(xmlCopybook);
							
							Recording recCopy = new Recording(xmlCopybook);
							Document docCopy = recCopy.getDoc();
							Element rootCopy = docCopy.getDocumentElement();
							
							Element firstChild = (Element)rootCopy.getFirstChild();
							log.info("firstChild.getNodeName() COPY: " + firstChild.getNodeName());
							
							nodeImported = docOC.importNode(rootCopy.getFirstChild(), true);
							
						} else if(copyType.equalsIgnoreCase(BMS_VALUE)){
							
							
							log.debug("Preparando a procesar BMS");
							
							String hexaMessage = Converter.convertStringToHex(copybook);
							log.debug(hexaMessage);
							log.debug(hexaMessage.length());
							
							byte[] dataByteAscii = Converter.convertHexToByte(hexaMessage);
							
							String pathFile = this.getConfig().getCopybookFileDefinitionFolderParsed(testExec);
							
							log.debug("getFileDefinitionMapPathParsed: ");
							log.debug(pathFile);
							
							String configfile = pathFile + "\\" + BMS_CONFIG_FILE_NAME;
							
							String bmsXML = BMSHandler.BMSParser(configfile, dataByteAscii, copyName, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE);
							
							Recording recBMS = null;
							if(bmsXML != null){
								
								recBMS = new Recording(bmsXML);
								
							} else {
								
								StringBuffer unknowMessage =  new StringBuffer();
								unknowMessage.append(ROOT_ELEMENT);
								unknowMessage.append("<" + BMSHandler.BMS_UNKNOW_ROOT_ELEMENT+ ">");
								unknowMessage.append(hexaMessage);
								unknowMessage.append("</" + BMSHandler.BMS_UNKNOW_ROOT_ELEMENT+ ">");
								bmsXML = unknowMessage.toString();
								recBMS = new Recording(unknowMessage.toString());
								
							}
							
							Document doc = recBMS.getDoc();
							Element rootBMS = doc.getDocumentElement();
							nodeImported = docOC.importNode(rootBMS, true);
							
							log.debug("BMS Procesado: ");
							log.debug(bmsXML);
							
						}
						
						rootOC.appendChild(nodeImported);
						Node rootOCImported = finaldoc.importNode(rootOC, true);
						rootElement.appendChild(rootOCImported);
						
						
					} else if(element.equalsIgnoreCase(AV_TAG)){
						
						String errorCode = "<" + AV_CODE + ">" + copybook.substring(0,7) + "</" + AV_CODE + ">";
						
						String copyMessage = copybook.substring(7);
						
						log.debug("Data Message: " + copyMessage);
						log.debug("Preparando a procesar AV");
						
						StringBuffer buffer = new StringBuffer();
						
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + AV_TAG + ">");
						buffer.append(errorCode);
						buffer.append("<" + WARN_DESC_ELEMENT + ">" + copyMessage + "</" + WARN_DESC_ELEMENT + ">");
						buffer.append("</" + AV_TAG + ">");
						
						Recording avDoc = new Recording(buffer.toString());
						Document docAV = avDoc.getDoc();
						Element rootAV = docAV.getDocumentElement();
						
						nodeImported = finaldoc.importNode(rootAV, true);
						
						rootElement.appendChild(nodeImported);
						
					} else {
						
						p = new Parameter(TAG_KEY,element);
							
						
						String xmlCopybook = null;
						
							
						response.setBody(copybook);
						metaData.addParameter(p);
						
						log.debug("Debe buscar el según el tag: " + response.getMetaData().get(TAG_KEY));
						log.debug("Debe va a convertir el texto: ->" + response.getBodyAsString() + "<-");
						log.debug("Preparando a procesar CopyBook");
						
						super.updateResponse(testExec, response);
						xmlCopybook = response.getBodyAsString();
						response.getMetaData().removeParameter("RESPONSE_PAYLOAD_DEF_NAME_KEY");
						
						log.debug("CopyBook Procesado: ");
						log.debug(xmlCopybook);
						
						xmlCopybook = this.trimXMLString(xmlCopybook);
						
						Recording recTag = new Recording(xmlCopybook);
						Document docTag = recTag.getDoc();
						Element rootTag = docTag.getDocumentElement();
						
						Element firstChild = (Element)rootTag.getFirstChild();
						log.debug("firstChild.getNodeName() COPY: " + firstChild.getNodeName());
						
						nodeImported = finaldoc.importNode(rootTag.getFirstChild(), true);
						
						rootElement.appendChild(nodeImported);
					}
					
					log.debug("Se agregó elemento= " + element);
				}
				
			}
			
			String xmlDocument = this.getXMLDocument();
			log.debug("xmlDocument getXMLDocument= " + xmlDocument);
	        response.setBody(xmlDocument);
	        
    	} catch (Exception e) {
			log.fatal(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
    /**
     * 
     * @param bodyAsString
     * @return
     */
    private Map<String, List<ElementPS9>> getListElementsXMLString(String bodyAsString) {
    	Map<String, List<ElementPS9>> data = new LinkedHashMap<String, List<ElementPS9>>();
    	
    	 log.info("RAW Message   : " + bodyAsString);
    	 // Remuevo el caracter HEXA FF
    	 bodyAsString = bodyAsString.substring(0, bodyAsString.length()-1);
    	 log.info("RAW Message   : " + bodyAsString);
    	
    	if(bodyAsString.length()!=0){
//	    	int lenght = bodyAsString.length();
	        String messages [] = bodyAsString.split("@");
			
	        boolean primeraVez = true;
			for (int i = 0; i < messages.length; i++) {
				String item = messages[i];
				
				log.info("Item   :-" + item + "-");
				
				if((item != null) && (!item.equalsIgnoreCase(""))){
					
					if(primeraVez){
						
						ElementPS9 elementPS9Result =  new ElementPS9();
						String resultado = RESULT_TAG;
						elementPS9Result.setTagname(resultado);
						elementPS9Result.setContent(item.substring(0, 1));
						
						List<ElementPS9> listResultado =  new ArrayList<ElementPS9>();
						listResultado.add(elementPS9Result);
			        	
						data.put(resultado, listResultado);
						
						ElementPS9 elementPS9Secuencia =  new ElementPS9();
						String secuencia = SEQUENCE_TAG;
						elementPS9Secuencia.setTagname(secuencia);
						elementPS9Secuencia.setContent(item.substring(1, 6));
						
						List<ElementPS9> listSecuencia =  new ArrayList<ElementPS9>();
						listSecuencia.add(elementPS9Secuencia);
			        	
						data.put(secuencia, listSecuencia);
						
						primeraVez = false;
						
					} else {
					
						List<ElementPS9> list = null;
			        	
			        	ElementPS9 elementPS9 =  new ElementPS9();
			        	
			        	String tagname = item.substring(0, 2);
				        elementPS9.setTagname(tagname);
				        log.info("Tag Name   : " + tagname);
				        
				        String content = item.substring(2, item.length()-1);// contentssss
				        elementPS9.setContent(content);
				        log.info("Content    : " + content);
				        
				        if(data.containsKey(tagname)){
				        	list = data.get(tagname);
				        	list.add(elementPS9);
				        	
				        } else {
				        	list =  new ArrayList<ElementPS9>();
				        	list.add(elementPS9);
				        }
				        
				        data.put(tagname, list);
					}
					
				}
			}
		   
    	}
    	
		return data;
	}

	/**
     * @throws Exception 
     * 
     */
    private void createXMLDocument (String element) {
    	
    	try {
	    	DocumentBuilderFactory finalFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder finalBuilder;
				finalBuilder = finalFactory.newDocumentBuilder();
			finaldoc = finalBuilder.newDocument();
			Element rootElement = finaldoc.createElement(element);
			finaldoc.appendChild(rootElement);
    	} catch (ParserConfigurationException e) {
    		// TODO Auto-generated catch block
    		log.error("No se pudo crear el documento inicial para el: " + element);
    	}
    }
    

    /**
     * 
     * @return
     */
	public String getXMLDocument() {
		try {
			return XMLUtils.toXML((Element)finaldoc.getFirstChild());
		} catch (Exception e) {
			e.printStackTrace();
			log.fatal(e.getMessage());
		}
		return "";
	}

	/**
     * Extract XML representation of PS9 during playback 
     * @param response
     *            the response to update.
     */
    @Override
    public void updateResponse(TestExec testExec, TransientResponse response) {
    	
    	try {
    	
    		log.debug("public void updateResponse(TestExec testExec, TransientResponse response)");
    		
	    	String xmlResponse = new String (response.getBodyAsByteArray());
	    	xmlResponse = testExec.parseInState(xmlResponse);
			
	        Playback play = new Playback(xmlResponse);
	        Document documento = play.getDocument();
	        
	        Element responseElement = documento.getDocumentElement();
	        NodeList listNodes = responseElement.getChildNodes();
	        
	        StringBuffer buffer = new StringBuffer();
	        
	        for (int i = 0; i < listNodes.getLength(); i++) {
				Node child = listNodes.item(i);
				if (child != null && child.getNodeType() == Node.ELEMENT_NODE) {
					
					String nodeName = child.getNodeName();
					log.debug("Nodo Name: = " + nodeName);
					String nodeValue = child.getNodeValue();
					log.debug("Nodo Value: = " + nodeValue);
					String nodeContent = child.getTextContent();
					log.debug("Nodo Content: = " + nodeContent);
					
					switch (nodeName) {
					
					case RESULT_TAG:
						
						buffer.append("@");
						buffer.append(nodeContent);
				        log.info("Mensaje Respuesta Result = " + nodeContent);
						break;
					
					case SEQUENCE_TAG:
												
						buffer.append(nodeContent);
				        log.info("Mensaje Respuesta Sqeuence = " + nodeContent);
						break;
					
					case DC_TAG:
						
						buffer.append("@");
						buffer.append(DC_TAG);
						
						StringBuffer bufferBMS = updateResponseDC(child, testExec, response);
						buffer.append(bufferBMS);
						
						buffer.append(Converter.convertHexToString("EE", "cp500"));
						
				        log.info("Mensaje Respuesta DC_TAG = " + bufferBMS.toString());
				        
				        log.info("Mensaje Respuesta DC_TAG HEXA = " + Converter.convertStringToHex(bufferBMS.toString()));
						break;
					
					case AV_TAG:
						
						buffer.append("@");
						buffer.append(AV_TAG);
						
						buffer.append(nodeContent);
						
						buffer.append(Converter.convertHexToString("EE", "cp500"));
						
						break;
	
					default:
						
						Parameter arg0 = new Parameter("RESPONSE_PAYLOAD_DEF_NAME_KEY", nodeName);
						
						buffer.append("@");
						buffer.append(nodeName);
						
						response.getMetaData().addParameter(arg0 );
						
						response.setBody(nodeContent);
						String result = updateResponseCopybook(testExec, response, child);
						buffer.append(result);
						
						response.getMetaData().removeParameter("RESPONSE_PAYLOAD_DEF_NAME_KEY");
						
						log.debug("Mensaje Respuesta TAG COPYBOOK= " + result);
						break;
					}
				}
	        }
	        buffer.append(Converter.convertHexToString("FF", "cp500"));
	        
	        String valor = Converter.convertStringToHex(buffer.toString(), "cp500");
	        
	        byte[] bytes = Converter.convertHexToByte(valor);
	        
	        log.info("Mensaje Respuesta = " + new String(bytes));
	        log.info("Mensaje Respuesta Hexa = " + Converter.convertByteToHex(bytes));
	        response.setBinary(true);
	        response.setBody(bytes);
    	}
    	catch (Exception e){
    		log.error(e.getMessage());
    		new RuntimeException(e);
    	}
    }

	/**
	 * @param child
	 * @param response 
	 * @param testExec 
	 * @return
	 * @throws Exception
	 */
	private StringBuffer updateResponseDC(Node child, TestExec testExec, TransientResponse response  ) throws Exception {
		StringBuffer bufferBMS = new StringBuffer();
		String copyName = null;
		String resultObject = null;
		
		StringBuffer bufferCopy = null;
		NodeList childNodesBMS = child.getChildNodes();
		
		// Convertimos el XML de BMS a BMS Format
		for (int j = 0; j < childNodesBMS.getLength(); j++) {
			Node childBMS = childNodesBMS.item(j);
			
			if (childBMS != null && childBMS.getNodeType() == Node.ELEMENT_NODE){
				
				Element element = (Element)childBMS;
				
				String xmlElement = null;
				
				String nodeNameBMS = element.getNodeName();
				log.debug("Nodo Name BMS: = " + nodeNameBMS);
				String nodeValue = element.getNodeValue();
				log.debug("Nodo Value BMS: = " + nodeValue);
				String nodeContent = element.getTextContent();
				log.debug("Nodo Content BMS: = " + nodeContent);
				
				switch (nodeNameBMS) {
				
					case BMSHandler.BMS_MESSAGE_ROOT_ELEMENT:
						
						bufferCopy = new StringBuffer();
						
						xmlElement = getStringXMLElement(element);
						resultObject = (String)BMSHandler.BMSXmlToObject(xmlElement, false);
						resultObject = CustomExtensionsHandler.convertHexToString(resultObject);
						
						bufferCopy.append(resultObject);
						
						log.debug("Mensaje BMS Procesado: = ->" + resultObject + "<-");
						log.debug("Mensaje BMS Longitud: = ->" + resultObject.length() + "<-");
						
						
						break;
						
					case BMSHandler.BMS_UNKNOW_ROOT_ELEMENT:
						
						bufferCopy = new StringBuffer();
						
//						xmlElement = getStringXMLElement(element);
//						resultObject = (String)BMSHandler.BMSXmlToObject(xmlElement, false);
						resultObject = CustomExtensionsHandler.convertHexToString(element.getTextContent());
						
						bufferCopy.append(resultObject);
						
						log.debug("Unknow BMS Mensaje ->" + resultObject + "<-");
						log.debug("Unknow BMS Mensaje Longitud: = ->" + resultObject.length() + "<-");
						
						
						break;
					
					case COPY_NAME_TAG:
						copyName = element.getTextContent();
						
						break;
					
					default:
						
						bufferCopy = new StringBuffer();
						
						xmlElement = getStringXMLElement(element);
						Parameter arg0 = new Parameter("RESPONSE_PAYLOAD_DEF_NAME_KEY", nodeNameBMS);
						
						response.getMetaData().addParameter(arg0 );
						
						response.setBody(nodeContent);
						String result = updateResponseCopybook(testExec, response, element);
						bufferCopy.append(result);
						
						response.getMetaData().removeParameter("RESPONSE_PAYLOAD_DEF_NAME_KEY");
						
						log.info("Mensaje Respuesta TAG COPYBOOK PARA DC= " + result);
						break;
						
				}
				
			}
				
		}
		
		bufferBMS.append(copyName);
		bufferBMS.append(bufferCopy);
		
		log.debug("Mensaje COPY o BMS Procesado" + bufferCopy);
		return bufferBMS;
	}
    

	private String getStringXMLElement(Element element) {
		// TODO Auto-generated method stub
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(element);
			transformer.transform(source, result);
			
			String xmlString = result.getWriter().toString();
			log.debug("Se convirtio en String: " + xmlString);
			return xmlString;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}catch (TransformerException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * 
	 * @param testExec
	 * @param response
	 * @return
	 */
	private String updateResponseCopybook(TestExec testExec,
			TransientResponse response, Node inputNode) {
		// TODO Auto-generated method stub
		
		String rootElement = "copybook-payload";
		String copybook = response.getBodyAsString();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(ROOT_ELEMENT);
		buffer.append("<");
		buffer.append(rootElement);
		buffer.append(">");
//		buffer.append(copybook);
		buffer.append("</");
		buffer.append(rootElement);
		buffer.append(">");
		
		Playback pb = new Playback(buffer.toString());
		Document document = pb.getDocument();
		
		
		Node paramNode = document.importNode(inputNode, true);
		document.getFirstChild().appendChild(paramNode);
		
		log.debug("Se dejo importar el nodo");
		
		String xmlCopybook = null;
		try {
			
			xmlCopybook = XMLUtils.toXML((Element)document.getFirstChild());
			log.debug("updateResponseCopybook XML Format");
			log.debug(xmlCopybook);
			
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
		
		response.setBody(xmlCopybook);
		super.updateResponse(testExec, response);
		return response.getBodyAsString();
	}
}
