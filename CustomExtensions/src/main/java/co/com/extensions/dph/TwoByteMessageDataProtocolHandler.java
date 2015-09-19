package co.com.extensions.dph;

import org.apache.log4j.Logger;

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
public class TwoByteMessageDataProtocolHandler extends ParameterListDataProtocol {
	
	private static Logger logger = Logger.getLogger(TwoByteMessageDataProtocolHandler.class);
	
	protected static String BASE_24_MESSAGE_NAME = "Treat as Base24";
	protected static String BASE_24_MESSAGE_KEY = "Base24";
	protected static String BASE_24 = "false";
	
	protected static String INCLUDE_TWOBYTE_NAME = "Include Two Byte Mesage";
	protected static String INCLUDE_TWOBYTE_KEY = "TwoByte";
	protected static String INCLUDE_TWOBYTE = "false";
	
	
	@Override
	protected ParameterList createDefaultParameters() {
		ParameterList params = new ParameterList();

		params.addParameter(new Parameter(INCLUDE_TWOBYTE_NAME, INCLUDE_TWOBYTE_KEY, INCLUDE_TWOBYTE, Boolean.class));
		params.addParameter(new Parameter(BASE_24_MESSAGE_NAME, BASE_24_MESSAGE_KEY, BASE_24, Boolean.class));
		
		return params;
	}
	
	@Override
	public void updateRequest(TestExec testExec, Request request) {
		updateRequest(request);
	}
	
	@Override
	public void updateRequest(Request request) {
		
		logger.info("public void updateRequest(Request request)");
		boolean isBinary = request.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = request.getBodyAsByteArray();
			} else {
				payloadBody = request.getBodyAsString().getBytes();
			}
		
		byte [] messageByte = new byte[payloadBody.length-2];
		
		for (int i = 0; i < messageByte.length; i++) {
			messageByte[i] = payloadBody[i+2];
		}
		
		logger.info("Request messageByte.length " + messageByte.length);
		logger.info("Request messageByte.hexa " + Converter.convertByteToHex(messageByte));
		logger.info("Request payloadBody.length " + payloadBody.length);
		logger.info("Request payloadBody.hexa " + Converter.convertByteToHex(payloadBody));
		
		request.setBody(messageByte);
		
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
		
		logger.info("Two Byte public void updateResponse(Response response)");
		
		boolean isBinary = response.isBinary();
		byte[] payloadBody;
		try {
			if (isBinary) {
				payloadBody = response.getBodyAsByteArray();
			} else {
				payloadBody = response.getBodyAsString().getBytes();
			}
			
			
			byte [] messageByte = new byte[payloadBody.length-2];
			
			for (int i = 0; i < messageByte.length; i++) {
				messageByte[i] = payloadBody[i+2];
			}
			
			logger.info("Two Byte Response messageByte.length " + messageByte.length);
			logger.info("Two Byte Response payloadBody.length " + payloadBody.length);
			
			response.setBinary(true);
			response.setBody(messageByte);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * This method is called when is palyback
	 */
	@Override
	public void updateResponse(TestExec testExec, TransientResponse response) {
		try {
			
			logger.info("Two Byte PlayBack public void updateResponse(TestExec testExec, TransientResponse response)");
			
			ParameterList parameterList = this.getParameterList();
			
//			String valueBase24 = parameterList.get(BASE_24_MESSAGE_KEY);
			String includeTwoByte = parameterList.get(INCLUDE_TWOBYTE_KEY);
			
			logger.info("Hexa Message Received: " + Converter.convertByteToHex(response.getBodyAsByteArray()));
			
			int length =  response.getBodyAsByteArray().length;
			
			logger.info("length no evaluated yet if include two buye: " + length);
			if( Boolean.valueOf(includeTwoByte) ){
				length = length + 2;
			}
			
			String msgLength = Converter.convertIntToHex(length);
			
			while(msgLength.length()%2 != 0){
			    msgLength = "0" + msgLength;
			}
			
			if(msgLength.length() == 2){
				msgLength = "00" + msgLength; 
			}
			
			logger.info("msgLength: " + msgLength);
			logger.info("length: " + length);
			
			byte[] msgLengthByte = Converter.convertHexToByte(msgLength);
			
			byte [] responseByte = new byte [msgLengthByte.length + length];
			
			logger.info("responseByte.length: " + responseByte.length);
			
			for (int i = 0; i < msgLengthByte.length; i++) {
				responseByte[i] = msgLengthByte[i];
			}
			
			for (int i = 0; i < response.getBodyAsByteArray().length; i++) {
				responseByte[msgLengthByte.length+i] = response.getBodyAsByteArray()[i];
			}
					
			logger.info("Se proceso el mensaje");
			logger.info("Mensaje: " + new String(responseByte));
			logger.info("Mensaje: " + new String(responseByte, "cp500"));
			logger.info("Mensaje HEXA CP500: " + Converter.convertByteToHex(responseByte));
			
			response.setBody(responseByte);
			

		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

}
