/**
 * 
 */
package co.com.extensions.dph.paxus;

import org.apache.log4j.Logger;

import com.itko.citi.Converter;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Request;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.DataProtocol;

/**
 * @author alvgu02
 *
 */
public class PaxusDataProtocol extends DataProtocol {
	
	private static Logger logger = Logger.getLogger(PaxusDataProtocol.class);

	@Override
	public void updateRequest(TestExec testExec, Request request) {
		
		boolean isBinary = request.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = request.getBodyAsByteArray();
			} else {
				payloadBody = request.getBodyAsString().getBytes();
			}
			
			String xmlMessage = PaxusHandler.parseMessage(payloadBody, false);
			String hexadata = Converter.convertByteToHex(payloadBody);
			logger.info("Mensaje ASCII: " +  Converter.convertHexToString(hexadata));
			logger.info("Mensaje HEXA: " + hexadata);
			
			request.setBinary(false);
			request.setBody(xmlMessage);
			
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
				payloadBody = response.getBodyAsString().getBytes();
			}
			String xmlMessage = PaxusHandler.parseMessage(payloadBody, true);
			
			response.setBinary(false);
			response.setBody(xmlMessage);
			
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
			String xmlMessage = new String (response.getBodyAsByteArray());
			xmlMessage = testExec.parseInState(xmlMessage);
			
			logger.info("Message: " + xmlMessage);
			
			Object o = PaxusHandler.xmlToObjectMessage(xmlMessage);
			
			String result = (String) o;
			
			int largo = result.length() / 2;
			
			String longitud = Converter.convertIntToHex(largo);

			if(longitud.length()%2 != 0){
				longitud = "0" + longitud;
			}
			
			if(longitud.length() == 2){
				longitud = "00" + longitud;
			}
			
			String respuesta = longitud + result;
			
			logger.info("Mensaje ASCII: " + Converter.convertHexToString(respuesta));
			logger.info("Mensaje HEXA: " + respuesta);
				
			response.setBinary(true);
			response.setBody(Converter.convertHexToByte(respuesta));

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}

//	@Override
//	protected ParameterList createDefaultParameters() {
//		ParameterList p = new ParameterList();
////		   p.addParameter(new Parameter("Delimeters separated by | (for example: first|second ) ","delimiterString", StringUtils.EMPTY,String.class));
//		   p.addParameter(new Parameter("Delimitador","delimiterString", StringUtils.EMPTY,String.class));
//
//		return p;
//	}
	
}
