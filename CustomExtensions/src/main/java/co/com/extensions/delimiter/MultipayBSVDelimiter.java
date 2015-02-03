package co.com.extensions.delimiter;

import java.util.List;

import org.apache.log4j.Logger;

import com.itko.citi.Converter;
import com.itko.lisa.vse.stateful.protocol.tcp.delimiters.TCPDelimiter;

public class MultipayBSVDelimiter implements TCPDelimiter {

	private static final Logger log = Logger.getLogger(MultipayBSVDelimiter.class);
	
	private static final String REQUEST_OR_RESPONSE_START_MARK = "<?xml";
	private static final String MULTIPAY_BVS_REQ_ELEMENT_START_TAG = "<bsvreq>";
	private static final String MULTIPAY_BVS_REQ_ELEMENT_END_TAG = "</bsvreq>";
	private static final String MULTIPAY_BVS_RSP_ELEMENT_START_TAG = "<bsvrsp>";
	private static final String MULTIPAY_BVS_RSP_ELEMENT_END_TAG = "</bsvrsp>";
	
	private int startOfNextRequest = 0;
	private int endOfRequest = -1;
	
		
	public MultipayBSVDelimiter() {
		
	}

	public void configure(String arg0) {
	}

	public int getEndOfRequest() {
		return endOfRequest;
	}

	public int getStartOfNextRequest() {
		return startOfNextRequest;
	}

	public boolean locateRequest(List<Byte> arg0) {
		log.debug("Entering MultipayBSVDelimiter1.locateRequest");
		System.out.println("Entering MultipayBSVDelimiter1.locateRequest");
		
		endOfRequest = -1;
		
		if (arg0 == null || arg0.isEmpty()) {
			log.debug("MultipayBSVDelimiter1.locateRequest: request or response is not complete (1)= null");
			System.out.println("MultipayBSVDelimiter1.locateRequest: request or response is not complete (1)= null");
			
			return false;
		}
		
		byte[] ba = new byte[arg0.size()];
		for (int i = 0; i < arg0.size(); i++) {
			ba[i] = arg0.get(i);
		}
		
		String msg = new String(ba);
		
		if (arg0.size() <= 10) {
			log.debug("MultipayBSVDelimiter1.locateRequest: request or response is not complete (2)= " + msg);
			System.out.println("MultipayBSVDelimiter1.locateRequest: request or response is not complete (2)= " + msg);
			
			return false;
		}
		
		int startMarkIndex = msg.indexOf(REQUEST_OR_RESPONSE_START_MARK);
		int reqStartElementIndex = msg.indexOf(MULTIPAY_BVS_REQ_ELEMENT_START_TAG);
		int rspStartElementIndex = msg.indexOf(MULTIPAY_BVS_RSP_ELEMENT_START_TAG);
		
		// Analiza un request
		if (((startMarkIndex == -1) && (reqStartElementIndex != -1)) || ((startMarkIndex != -1) && (reqStartElementIndex != -1) && (startMarkIndex < reqStartElementIndex))) {
			
			String message  = null;
			
			// Valida si el final del request ha llegado.
			int reqEndElementIndex = msg.indexOf(MULTIPAY_BVS_REQ_ELEMENT_END_TAG); // Se en que posición esta el </bsvreq>
			
			int lengthRequest = reqEndElementIndex + MULTIPAY_BVS_REQ_ELEMENT_END_TAG.length();
			if(reqEndElementIndex != -1){
				
				endOfRequest = lengthRequest;
				startOfNextRequest = 0;
				
				byte[] messageByte = new byte [lengthRequest];
				
				byte[] bites = this.listToByteArray(arg0);
				for (int i = 0; i < bites.length; i++) {
					if(i < lengthRequest){
						messageByte[i] = bites[i];
					}
				}
				
				message = new String(messageByte);
				
				log.debug("MultipayBSVDelimiter1.locateRequest: request is complete! (4)= " + message);
				System.out.println("MultipayBSVDelimiter1.locateRequest: request is complete! (4)= " + message);
				
				return true;
			}
			else {
				log.debug("MultipayBSVDelimiter1.locateRequest: request is not complete! (5)= " + msg);
				System.out.println("MultipayBSVDelimiter1.locateRequest: request is not complete! (5)= " + msg);
				
				endOfRequest = -1;
				startOfNextRequest = -1;
				return false;
			}
		}
		// Analiza un response
		else if (((startMarkIndex == -1) && (rspStartElementIndex != -1)) || ((startMarkIndex != -1) && (rspStartElementIndex != -1) && (startMarkIndex < rspStartElementIndex))) {
			
			String message  = null;
			
			// Valida si el final del response ha llegado.
			int rspEndElementIndex = msg.indexOf(MULTIPAY_BVS_RSP_ELEMENT_END_TAG); // Se en que posición esta el </bsvreq>
			int lengthResponse = rspEndElementIndex + MULTIPAY_BVS_RSP_ELEMENT_END_TAG.length();
			if(rspEndElementIndex != -1){
				
				endOfRequest = lengthResponse;
				startOfNextRequest = 0;
				
				byte[] messageByte = new byte [lengthResponse];
				
				byte[] bites = this.listToByteArray(arg0);
				for (int i = 0; i < bites.length; i++) {
					if(i < lengthResponse){
						messageByte[i] = bites[i];
					}
				}
				
				message = new String(messageByte);
				
				log.debug("MultipayBSVDelimiter1.locateRequest: response is complete! (6)= " + message);
				System.out.println("MultipayBSVDelimiter1.locateRequest: response is complete! (6)= " + message);
				
				return true;
			}
			else {
				log.debug("MultipayBSVDelimiter1.locateRequest: response is not complete! (7)= " + msg);
				System.out.println("MultipayBSVDelimiter1.locateRequest: response is not complete! (7)= " + msg);
				
				endOfRequest = -1;
				startOfNextRequest = -1;
				return false;
			}
		}
		
		log.debug("MultipayBSVDelimiter1.locateRequest: request or response is not complete (3)= " + msg);
		System.out.println("MultipayBSVDelimiter1.locateRequest: request or response is not complete (3)= " + msg);
		
		endOfRequest = -1;
		startOfNextRequest = -1;
		return false;
	}

	public String validate() {
		return null;
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	private byte[] listToByteArray(List<Byte> bytes) {
		byte[] ba = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++) {
			ba[i] = bytes.get(i);
		}
		return ba;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MultipayBSVDelimiter delim = new MultipayBSVDelimiter();
			List<Byte> bytes = new java.util.ArrayList<Byte>();
			
			String hexa = "FF";
			byte[] delimiter = Converter.convertHexToByte(hexa);
			String partMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n			<bsvreq>\n				<header>\n					<usuario>mlsusr</usuario>\n					<password>sqkup484</password>\n					<canal>mlistas</canal>\n					<requerimiento>cons_listas_cab</requerimiento>\n				</header>\n				<data>\n					<numlista></numlista>\n					<fecpres>20141128</fecpres>\n					<origen>PS</origen>\n				</data>\n			</bsvreq>\n";
//			String partMessage = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n			<bsvreq>\n				<header>\n					<usuario>mlsusr</usuario>\n					<password>sqkup484</password>\n					<canal>mlistas</canal>\n					<requerimiento>cons_listas_cab</requerimiento>\n				</header>\n				<data>\n					<numlista></numlista>\n					<fecpres>20141128</fecpres>\n					<origen>PS</origen>\n				</data>\n			</bsvreq>\n";
			byte [] partMessageByte = Converter.convertStringToByte(partMessage, "ISO-8859-1");
			
			int length = partMessageByte.length + delimiter.length;
			
			byte [] requestByte = new byte[length];
			
			for (int i = 0; i < partMessageByte.length; i++) {
				
				requestByte[i] = partMessageByte[i];
			}
			
			for (int i = 0; i < delimiter.length; i++) {
				requestByte[partMessageByte.length + i] = delimiter[i];
			}
			
			String request = new String(requestByte);
			
			System.out.println("request");
			System.out.println(request);
			
			byte[] requestBytes = Converter.convertStringToByte(request, "ISO-8859-1");
			for (int i = 0; i < requestBytes.length; i++) {
				bytes.add(new Byte(requestBytes[i]));
			}

			boolean resultado = delim.locateRequest(bytes);
			System.out.println("Resultado: " + resultado );
			
			
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
