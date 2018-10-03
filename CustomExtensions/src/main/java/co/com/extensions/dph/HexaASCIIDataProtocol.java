/**
 * 
 */
package co.com.extensions.dph;

import org.apache.log4j.Logger;

import com.itko.citi.Converter;
import com.itko.lisa.test.TestExec;
import com.itko.lisa.vse.stateful.model.Response;
import com.itko.lisa.vse.stateful.model.TransientResponse;
import com.itko.lisa.vse.stateful.protocol.DataProtocol;

/**
 * @author alvgu02
 *
 */
public class HexaASCIIDataProtocol extends DataProtocol {
	
	private static Logger logger = Logger.getLogger(HexaASCIIDataProtocol.class);

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
			
			String message = new String(payloadBody);
			logger.info("Mensaje HEXA: " +  message);
			String responseASCII = Converter.convertHexToString(message);
			logger.info("Mensaje ASCII: " +  responseASCII);
			
			response.setBinary(false);
			response.setBody(responseASCII);
			
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
			String result = Converter.convertStringToHex(xmlMessage);
						
			logger.info("Mensaje ASCII: " + xmlMessage);
			logger.info("Mensaje HEXA: " + result);
				
			response.setBinary(true);
			response.setBody(result);

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
