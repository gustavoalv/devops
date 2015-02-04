package co.com.extensions.dph;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.itko.lisa.vse.stateful.model.Request;
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
public class PS9DataProtocol extends CopybookDataProtocol {
	
	private static final String TAG_KEY = "TAG";
	
	private static final String RESPONSE_KEY = "Response";
	private static final String REQUEST_KEY = "Request";
	
	private static final String ME_TAG = "ME";
	private static final String IH_TAG = "IH";
	private static final String OC_TAG = "OC";
	private static final String AV_TAG = "AV";
	private static final String OH_TAG = "OH";
	private static final String DE_TAG = "DE";
	
	private static final String COPY_TYPE_TAG = "COPY-TYPE";
	private static final String ID_MAP_TAG = "ID-MAP";
	private static final String COPY_NAME_TAG = "COPY-NAME";
	
	private static final String VAR_ELEMENT = "VAR-";
	private static final String ERROR_CODE = "ERROR-CODE";
	private static final String NUM_VAR_ELEMENT = "NUM-VAR";
	private static final String WARN_DESC_ELEMENT = "WARN-DESC";
	private static final String ROOT_ELEMENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	private static final String ME_COPY_LENGTH_ELEMENT = "COPY-LENGTH";
	private static final String ME_COPY_TYPE_ELEMENT = "COPY-TYPE";
	
	private static final String COPY_VALUE = "C";
	private static final String BMS_VALUE = "B";
	
	private static final String IH_HEADER_TX_CODE = "IH_HEADER_TX-CODE";
	
    protected static Logger log   = Logger.getLogger(PS9DataProtocol.class.getName());
    
	private Document finaldoc;
	
	private static String BMS_CONFIG_FILE_NAME = "bms-config.xml";
	
	
    /* (non-Javadoc)
	 * @see com.itko.lisa.vse.stateful.protocol.copybook.CopybookDataProtocol#updateRequest(com.itko.lisa.test.TestExec, com.itko.lisa.vse.stateful.model.Request)
	 */
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
    	
    	try {
    		
    		log.debug("response.getBodyAsString(): ");
			log.debug(response.getBodyAsString());
    				
			this.createXMLDocument(RESPONSE_KEY);
			
			Node rootElement = finaldoc.getFirstChild();
			
			String responsetMessage = this.buildXMLRequest(response.getBodyAsString());
			
			List<String> elements = new ArrayList<String>();
			
			Map<String, List<ElementPS9>> data = getListElementsXMLString(response.getBodyAsString());
			
			Iterator<Entry<String, List<ElementPS9>>> it = data.entrySet().iterator();
			while (it.hasNext()){
				Entry<String, List<ElementPS9>> pair = it.next();
				String elemento = pair.getKey();
				elements.add(elemento);
				log.debug("Se agrego el elemento: " + elemento);
			}
			
			
//			String stringBMS = "<OC>B";
//			boolean isBMS = false; 
//			
//			
//			if(response.getBodyAsString().contains(stringBMS)){
//				isBMS = true;
////				String [] elementos = response.getBodyAsString().split("<OC>");
//			}
    	
//			Recording rec = null;
//			if(!isBMS){
//		    	String responseMessage = this.buildXMLResponse(response.getBodyAsString());
//		    	// Asume que viene un XML bien formado
//		        rec = new Recording(responseMessage);
//		        elements = rec.getListElements();
//		        
//			} else {
//				data = getListElementsXMLString(response.getBodyAsString());
//				
//				
//				Iterator<Entry<String, List<ElementPS9>>> it = data.entrySet().iterator();
//				while (it.hasNext()){
//					Entry<String, List<ElementPS9>> pair = it.next();
//					String elemento = pair.getKey();
//					elements.add(elemento);
//					log.debug("Se agrego el elemento: " + elemento);
//				}
//				
//			}
	        
			for (String element : elements) {
				
				List<ElementPS9> mensajes = new ArrayList<ElementPS9>();
				mensajes = data.get(element);
	
				log.debug(element + " es el elemento que se va trabajar");
				
				String copybook = null;
//				if(!isBMS) {
//					rec.setElementName(element);
//					ElementPS9 elementPS9 =  new ElementPS9();
//					elementPS9.setContent(rec.extract());
//					mensajes.add(elementPS9);
//				} else {
//				}
				
				for (ElementPS9 string : mensajes) {
					
					copybook = string.getContent();
					
					ParameterList metaData = response.getMetaData();
					metaData.removeParameter(TAG_KEY);
					Parameter p = null;
					
					String copyType = null;
					String idMap = null;
					String copyNameTag = null;
					
					Node nodeImported = null;
					
					if(element.equalsIgnoreCase(OC_TAG)){
						
						String copyName = copybook.substring(2, 10).trim();
						
						copyType = copybook.substring(0, 1);
						
						String copyTypeTag = "<" + COPY_TYPE_TAG + ">" + copybook.substring(0, 1) + "</" + COPY_TYPE_TAG + ">";
						idMap = "<" + ID_MAP_TAG + ">" + copybook.substring(1, 2) + "</" + ID_MAP_TAG + ">";
						copyNameTag = "<" + COPY_NAME_TAG + ">" + copybook.substring(2, 10) + "</" + COPY_NAME_TAG + ">";
						
						StringBuffer buffer = new StringBuffer();
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + OC_TAG + ">");
						buffer.append(copyTypeTag);
						buffer.append(idMap);
						buffer.append(copyNameTag);
						buffer.append("</" + OC_TAG + ">");
						
						log.debug("OC: " + buffer.toString());
	
						Recording ocRec = new Recording(buffer.toString());
						Document docOC = ocRec.getDoc();
						Element rootOC = docOC.getDocumentElement();
						String xmlCopybook = null;
						
						String copyMessage = copybook.substring(10);
						p = new Parameter(TAG_KEY,copyName);
						copybook = copyMessage;
						
						
						if(copyType.equalsIgnoreCase(COPY_VALUE)){
							
							response.setBody(copybook);
							p = new Parameter(TAG_KEY,copyName);
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
							
							Recording recCopy = new Recording(xmlCopybook);
							Document docCopy = recCopy.getDoc();
							Element rootCopy = docCopy.getDocumentElement();
							
							Element firstChild = (Element)rootCopy.getFirstChild();
							log.debug("firstChild.getNodeName() COPY: " + firstChild.getNodeName());
							
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
						
						String errorCode = "<" + ERROR_CODE + ">" + copybook.substring(0,7) + "</" + ERROR_CODE + ">";
						String numerVar = copybook.substring(7,8);
						int numero = Integer.parseInt(numerVar);
						numerVar = "<" + NUM_VAR_ELEMENT + ">" + numerVar + "</" + NUM_VAR_ELEMENT + ">";
						
						int i = 1;
						int index = 8;
						int next = 0;
						StringBuffer buffer = new StringBuffer();
						
						buffer.append(ROOT_ELEMENT);
						buffer.append("<" + AV_TAG + ">");
						buffer.append(errorCode);
						buffer.append(numerVar);
								
						while (i <= numero) {
							next = index + 20;
							buffer.append("<" + VAR_ELEMENT + i + ">" + copybook.substring(index + next) + "</" + VAR_ELEMENT + i + ">");
							index = index + 20;
							i++;
						}
						buffer.append("<" + WARN_DESC_ELEMENT + ">" + copybook.substring(next) + "</" + WARN_DESC_ELEMENT + ">");
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
    	
    	while(bodyAsString.length()!=0){
//	    	int lenght = bodyAsString.length();
	    	Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
	        Matcher m = tagPattern.matcher(bodyAsString);
	        boolean tagFound = m.find(); // true
	        
	        if(tagFound){
	        	
	        	List<ElementPS9> list = null;
	        	
	        	ElementPS9 elementPS9 =  new ElementPS9();
	        	
		        String tagOnly = m.group(0);// <tag a ="b" c= 'd' e=f> contentssss </tag>
		        elementPS9.setTagOnly(tagOnly);
		        log.debug("Tag Only   : " + tagOnly);
		        
		        String tagname = m.group(1);// tag
		        elementPS9.setTagname(tagname);
		        log.debug("Tag Name   : " + tagname);
		        
		        String attributes = m.group(2);// a ="b" c= 'd' e=f
		        elementPS9.setAttributes(attributes);
		        log.debug("Attributes : " + attributes);
		        
		        String content = m.group(3);// contentssss
		        elementPS9.setContent(content);
		        log.debug("Content    : " + content);
		        
		        if(data.containsKey(tagname)){
		        	list = data.get(tagname);
		        	list.add(elementPS9);
		        	
		        } else {
		        	list =  new ArrayList<ElementPS9>();
		        	list.add(elementPS9);
		        }
		        
		        data.put(tagname, list);
		        bodyAsString = bodyAsString.substring(tagOnly.length());
		        
	        } else{
	        	//log.error("Not found tag");
	        	break;
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
     * @param bodyAsString
     * @return
     */
    private String buildXMLResponse(String bodyAsString) {
    	String response = ROOT_ELEMENT + "<"+ RESPONSE_KEY + ">" + bodyAsString + "</" + RESPONSE_KEY + ">";
    	log.info("**********************************************************************");
    	log.info(response);
    	log.info("**********************************************************************");
    	
    	return response;
	}
    
    /**
     * 
     * @param bodyAsString
     * @return
     */
    private String buildXMLRequest(String bodyAsString) {
    	
    	int index = bodyAsString.indexOf("<");
    	
    	String response = ROOT_ELEMENT + "<"+ REQUEST_KEY + ">" + bodyAsString.substring(index) + "</" + REQUEST_KEY + ">";
    	log.info("**********************************************************************");
    	log.info(response);
    	log.info("**********************************************************************");
    	
    	return response;
	}
    
    
	

	/**
	 * 
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
					
					case OC_TAG:
						
						buffer.append("<");
						buffer.append(OC_TAG);
						buffer.append(">");
						
						StringBuffer bufferBMS = updateResponseOC(child, testExec, response);
						buffer.append(bufferBMS);
				        
				        buffer.append("</");
						buffer.append(OC_TAG);
						buffer.append(">");
						
				        log.debug("Mensaje Respuesta OC_TAG BMS = " + bufferBMS.toString());
						break;
					
					case AV_TAG:
						
						buffer.append("<");
						buffer.append(AV_TAG);
						buffer.append(">");
						
						buffer.append(nodeContent);
						
				        buffer.append("</");
						buffer.append(AV_TAG);
						buffer.append(">");
						break;
	
					default:
						
						Parameter arg0 = new Parameter("RESPONSE_PAYLOAD_DEF_NAME_KEY", nodeName);
						
						buffer.append("<");
						buffer.append(nodeName);
						buffer.append(">");
						
						response.getMetaData().addParameter(arg0 );
						
						response.setBody(nodeContent);
						String result = updateResponseCopybook(testExec, response, child);
						buffer.append(result);
						
						buffer.append("</");
						buffer.append(nodeName);
						buffer.append(">");
						
						response.getMetaData().removeParameter("RESPONSE_PAYLOAD_DEF_NAME_KEY");
						
						log.debug("Mensaje Respuesta TAG COPYBOOK= " + result);
						break;
					}
				}
	        }
	        
	        log.debug("Mensaje Respuesta = " + buffer.toString());
	        response.setBody(buffer.toString());
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
	private StringBuffer updateResponseOC(Node child, TestExec testExec, TransientResponse response  ) throws Exception {
		StringBuffer bufferBMS = new StringBuffer();
		String copyType = null;
		String idMap = null;
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
					
					case COPY_TYPE_TAG:
						
						copyType = element.getTextContent();
						break;
						
					case ID_MAP_TAG:
						
						idMap = element.getTextContent();
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
						
						log.debug("Mensaje Respuesta TAG COPYBOOK PARA OC= " + result);
						break;
						
				}
				
			}
				
		}
		
		bufferBMS.append(copyType);
		bufferBMS.append(idMap);
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
