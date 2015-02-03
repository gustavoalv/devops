package co.com.extensions.delimiter;

import java.util.List;

import org.apache.log4j.Logger;

import com.itko.citi.Converter;
import com.itko.lisa.vse.stateful.protocol.tcp.delimiters.TCPDelimiter;

public class HexaCharacterFromFileDelimiter implements TCPDelimiter {

	private static final Logger log = Logger.getLogger(HexaCharacterFromFileDelimiter.class);
	
	private int startOfNextRequest = 0;
	private int endOfRequest = -1;
	
		
	public HexaCharacterFromFileDelimiter() {
		
	}

	public void configure(String arg0) {
	}

	public int getEndOfRequest() {
		return endOfRequest;
	}

	public int getStartOfNextRequest() {
		return startOfNextRequest;
	}

	public boolean locateRequest(List<Byte> bytes) {
		
		log.info("HexaCharacterFromFileDelimiter.locateRequest(List<Byte> bytes)");
		
		if (!bytes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("locateRequest(List<Byte> bytes)");
                log.debug("num bytes: " + bytes.size());
            }
            byte[] bytesArray = listToByteArray(bytes);
            String hexaMessage = Converter.convertByteToHex(bytesArray);
            log.info("El mensaje en HEXA que me ha llegado es:\n" + hexaMessage);
            
            log.info("El mensaje en ISO-8859-1 que me ha llegado es:\n");
            printMessage(hexaMessage);
//            System.out.println("El mensaje en HEXA que me ha llegado es:\n" + hexaMessage);
            
            log.info("Voy a buscar si el mensaje tiene el delmitador en HEXA: FF");
            int delimiter = hexaMessage.indexOf("FF");
            
            if(delimiter != -1){
            	endOfRequest = delimiter;
				startOfNextRequest = 0;
				log.info("He encontrado el delimitador");
				
				log.info("endOfRequest: " + endOfRequest);
//				System.out.println("endOfRequest: " + endOfRequest);
				
				hexaMessage = hexaMessage.substring(0, delimiter);
				
				printMessage(hexaMessage);
				
				return true;
            } else {
            	log.info("NO encontre el delimitador");
            	printMessage(hexaMessage);
            }
		} 
		
		endOfRequest = -1;
		startOfNextRequest = -1;
		return false;
	}

	/**
	 * @param hexaMessage
	 */
	private void printMessage(String hexaMessage) {
		try {
			String message = Converter.convertHexToString(hexaMessage, "ISO-8859-1");
			log.info("El mensaje es: \n" + message );
//			System.out.println("El mensaje es: \n" + message );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("No pude convertir el mensaje de hexa a ISO-8859-1");
			e.printStackTrace();
		}
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
			HexaCharacterFromFileDelimiter delim = new HexaCharacterFromFileDelimiter();
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
			
			String hexaData = Converter.convertStringToHex(request);
			System.out.println(hexaData);
			
			byte[] requestBytes = Converter.convertStringToByte(request, "ISO-8859-1");
			for (int i = 0; i < requestBytes.length; i++) {
				bytes.add(new Byte(requestBytes[i]));
			}

			boolean resultado = delim.locateRequest(bytes);
			System.out.println("Resultado: " + resultado );
			
			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

	}

}
