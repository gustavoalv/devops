package co.com.extensions.dph;

import java.io.File;

import org.apache.log4j.Logger;

import co.com.extensions.handlers.CustomExtensionsHandler;
import co.com.extensions.handlers.ISO8583Handler;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.itko.citi.Converter;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Request;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.ParameterListDataProtocol;
import com.itko.util.Parameter;
import com.itko.util.ParameterList;

/**
 * Iso8583DataProtocolHandler - A DPH for ISO-8583 messages
 * 
 */
public class Iso8583DataProtocolHandler extends ParameterListDataProtocol {
	
	private static Logger logger = Logger.getLogger(Iso8583DataProtocolHandler.class);
	
	
	protected static String ENCODING_PARAM_NAME = "Encoding";
	protected static String ENCODING_PARAM_KEY = "encoding";
	protected static String CONFIG_PARAM_PATH = "Config Path";
	protected static String CONFIG_PARAM_KEY = "config";
	protected static String HEADER_PARAM_NAME = "Header";
	protected static String HEADER_PARAM_KEY = "header";

	protected static String HEXA_MESSAGE_NAME = "Treat as Hexa";
	protected static String HEXA_MESSAGE_KEY = "Hexa";
	
	protected static String EncodingParamDefault = Charsets.UTF_8.name();

	protected static String CONFIG_XML = "C:\\Lisa7.5.2\\8583-config.xml";
	
	protected static String HEADER = "0";
	
	protected static String HEXA_MESSAGE = "true";

	@Override
	protected ParameterList createDefaultParameters() {
		ParameterList params = new ParameterList();

		params.addParameter(new Parameter(ENCODING_PARAM_NAME, ENCODING_PARAM_KEY, EncodingParamDefault, String.class));
		params.addParameter(new Parameter(CONFIG_PARAM_PATH, CONFIG_PARAM_KEY, CONFIG_XML, File.class));
		params.addParameter(new Parameter(HEADER_PARAM_NAME, HEADER_PARAM_KEY, HEADER, Integer.class));
		
		return params;
	}

	@Override
	public void updateRequest(TestExec testExec, Request request) {
		updateRequest(request);
	}

	@Override
	public void updateRequest(Request request) {
		
		boolean isBinary = request.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = request.getBodyAsByteArray();
			} else {
				payloadBody = request.getBodyAsString().getBytes(getEncoding());
			}
			
			ParameterList parameterList = this.getParameterList();

			String requestPre = new String(payloadBody); 
					
			String valueConfig = parameterList.get(CONFIG_PARAM_KEY);
			String valueHeader = parameterList.get(HEADER_PARAM_KEY);
			
			requestPre = Converter.convertByteToHex(payloadBody);
			
			logger.info("Mensaje ISO8583: " + requestPre);
			
			String message = ISO8583Handler.ISO8583Parser(valueConfig, requestPre, Integer.parseInt(valueHeader) , false, CustomExtensionsHandler.CONTENT_TYPE_HEXA );
			
			logger.info("Mensaje ISO Parser: " + message);
			
			request.setBinary(false);
			request.setBody(message);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * This method is called when is recording
	 */
	@Override
	public void updateResponse(Response response) {
		boolean isBinary = response.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = response.getBodyAsByteArray();
			} else {
				payloadBody = response.getBodyAsString()
						.getBytes(getEncoding());
			}
			
			ParameterList parameterList = this.getParameterList();

			String responsePre = new String(payloadBody); 
					
			String valueConfig = parameterList.get(CONFIG_PARAM_KEY);
			String valueHeader = parameterList.get(HEADER_PARAM_KEY);
			
			responsePre = Converter.convertByteToHex(payloadBody);

			logger.info("Mensaje ISO8583 ASCII: " + payloadBody);
			logger.info("Mensaje ISO8583  HEXA: " + responsePre);
				
			String message = ISO8583Handler.ISO8583Parser(valueConfig, responsePre, Integer.parseInt(valueHeader) , false, CustomExtensionsHandler.CONTENT_TYPE_HEXA );
			
			logger.info("Mensaje ISO Parser: " + message);
			
			response.setBinary(false);
			response.setBody(message);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * This method is called when is playback
	 */
	@Override
	public void updateResponse(TestExec testExec, TransientResponse response) {
		try {
			String xmlISO8583 = new String (response.getBodyAsByteArray());
			xmlISO8583 = testExec.parseInState(xmlISO8583);
			
			logger.info("Message ISO Parser: " + xmlISO8583);
			
			
			Object o = ISO8583Handler.ISO8583XmlToObject(xmlISO8583, "left", false, false);
			
			String result = (String) o;
			byte[] respuesta = result.getBytes();
			
			logger.info("Mensaje ISO8583 ASCII: " + new String(result));
			logger.info("Mensaje ISO8583  HEXA: " + CustomExtensionsHandler.convertByteToHex(respuesta));
				
			response.setBinary(true);
			response.setBody(respuesta);

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * 
	 * @return
	 */
	private String getEncoding() {
		String charset = getParameterList().get(ENCODING_PARAM_KEY);
		if (Strings.isNullOrEmpty(charset)) {
			charset = Charsets.US_ASCII.name();
		}
		return charset;
	}
}
