package co.com.extensions.delimiter;

import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;

import com.itko.citi.filter.Converter;
import com.itko.lisa.vse.stateful.protocol.tcp.delimiters.TCPDelimiter;

/**
 *
 */
public class CoordinadorTCPDelimiter implements TCPDelimiter {
	private static final Logger log = Logger.getLogger(CoordinadorTCPDelimiter.class);

	private int startOfNextRequest = 0;
	private int endOfRequest = -1;
	private String message = null;

	private final static int HEADER_LEN = 7;

	/**
	 * Works for both request and response
	 * 
	 * @param bytes
	 * @return
	 */
	public boolean locateRequest(List<Byte> bytes) {
		log.info("locateRequest(List<Byte> bytes)");
		log.info("num bytes: " + bytes.size());
		if (bytes.size() > 2) {

			byte[] bytesArray = listToByteArray(bytes);
			log.info("Converter.convertByteToHex(bytesArray)");
			String hexDat = Converter.convertByteToHex(bytesArray);

			String content = null;
			try {

				content = new String(bytesArray, "cp037");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				log.info("Error: ", e);
			}
			log.info("hexDat");
			printBytes(hexDat.getBytes());
			log.info("content cp037: " + content);
			log.info("content length cp037: " + content.length());
			
			if(bytes.size() == 25){
				log.info("Return FALSE DEFECTO bytes.size()");
				return false;
        	}

			byte[] longitud = new byte[2];
			longitud[0] = bytesArray[0];
			longitud[1] = bytesArray[1];

			short messageLength = parseByteArrayToShort(longitud, 0);
			log.info("Message Length: " + messageLength);

			byte[] sizeBytes = new byte[3];
			sizeBytes[0] = bytes.get(6);
			sizeBytes[1] = bytes.get(7);
			sizeBytes[2] = bytes.get(8);

			endOfRequest = messageLength;
			startOfNextRequest = 0;
			log.info("Return TRUE");
			return true;
		} else {
			log.debug("Bytes is zero)");
		}
		log.info("Return FALSE DEFECTO");
		return false;
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
	 * @param ba
	 */
	private void printBytes(byte[] ba) {
		String request = new String(ba);

		log.info(request);
	}

	/**
	 * @return
	 */
	public int getEndOfRequest() {
		log.info("getEndOfRequest()");
		return endOfRequest;
	}

	/**
	 * @return
	 */
	public int getStartOfNextRequest() {
		log.info("getStartOfNextRequest()");
		return startOfNextRequest;
	}

	/**
	 * @return
	 */
	public String validate() {
		if (log.isDebugEnabled()) {
			log.debug("validate()");
		}
		return message;
	}

	/**
	 * @param config
	 */
	public void configure(String config) {

	}

	public static short parseByteArrayToShort(byte[] byteVal, int startOffset) {
		int result = 0;

		for (int i = startOffset; i < startOffset + 2; i++) {
			if (i < byteVal.length)
				result = (result << 8) + (byteVal[i] & 0xFF);
			else {
				return -1;
			}
		}
		return (short) result;
	}

	/**
	 * Calculates the length of a payload based on the first two bytes of the
	 * message.
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @throws java.io.IOException
	 */
	public int calculateLength(byte a, byte b) {

		return (b + (a << 8)) + HEADER_LEN;
	}

	/**
	 * Obtain the length in the first 2 bytes (Big Endian) and discard 2 more.
	 * 
	 * @param is
	 *            a <code>java.io.InputStream</code> value
	 * @return an <code>int</code> value
	 */
	public int read(java.io.InputStream is) throws Exception {
		DataInputStream dis = new DataInputStream(is);

		// skip the length bytes
		byte[] b2 = new byte[2];

		dis.readFully(b2);

		return ((b2[1] & 0xFF) + (b2[0] >> 8));
	}
}
