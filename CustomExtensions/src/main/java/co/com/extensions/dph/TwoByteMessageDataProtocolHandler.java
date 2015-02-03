package co.com.extensions.dph;

import org.apache.log4j.Logger;

import co.com.extensions.handlers.CustomExtensionsHandler;

import com.itko.citi.Converter;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Request;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.DataProtocol;

/**
 * Iso8583DataProtocolHandler - A DPH for ISO-8583 messages
 * 
 * @author <a href="mailto:daniel.bingham@itko.com">Daniel Bingham</a>
 */
public class TwoByteMessageDataProtocolHandler extends DataProtocol {
	
	private static Logger logger = Logger.getLogger(TwoByteMessageDataProtocolHandler.class);
	
	
	@Override
	public void updateRequest(TestExec testExec, Request request) {
		updateRequest(request);
	}

	@Override
	public void updateRequest(Request request) {
		
//		logger.info("request.getOperation()" + request.getOperation());
//		String operation = request.getOperation(); 
		
		boolean isBinary = request.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = request.getBodyAsByteArray();
			} else {
				payloadBody = request.getBodyAsString().getBytes();
			}
			
			
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void updateResponse(Response response) {
		boolean isBinary = response.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = response.getBodyAsByteArray();
			} else {
				payloadBody = response.getBodyAsString().getBytes();
			}
			

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
     * 
     */
	@Override
	public void updateResponse(TestExec testExec, TransientResponse response) {
		try {
			String xmlISO8583 = new String (response.getBodyAsByteArray());
			xmlISO8583 = testExec.parseInState(xmlISO8583);
			
			logger.info("Message ISO Parser: " + xmlISO8583);
			
			
			byte[] mensajeBytes = xmlISO8583.getBytes();
			
			int largo = mensajeBytes.length;
			
			String longitud = Converter.convertIntToHex(largo);

			if(longitud.length()%2 != 0){
				longitud = "0" + longitud;
			}
			
			if(longitud.length() == 2){
				longitud = "00" + longitud;
			}
			
			byte[] longitudBytes = Converter.convertHexToByte(longitud);
			byte[] respuesta = new byte[longitudBytes.length + mensajeBytes.length];
			
			for (int i = 0; i < longitudBytes.length; i++) {
				respuesta[i] = longitudBytes[i];
			}
			
			for (int i = 0; i < mensajeBytes.length ; i++) {
				respuesta[i+ longitudBytes.length] = mensajeBytes[i];
			}
			

			logger.info("Se proceso el mensaje");
			logger.info("Mensaje ISO8583: " + new String(respuesta));
			logger.info("Mensaje ISO8583 HEXA: " + CustomExtensionsHandler.convertByteToHex(respuesta));
				
			response.setBinary(true);
			response.setBody(respuesta);
			

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

}
