package co.com.extensions.delimiter;

import java.io.DataInputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.itko.citi.Converter;
import com.itko.lisa.vse.stateful.protocol.tcp.delimiters.TCPDelimiter;

/**
 *
 */
public class TwoByteISO8583TCPDelimiter implements TCPDelimiter{
    
    private static Logger log = Logger.getLogger(TwoByteISO8583TCPDelimiter.class);

    private int startOfNextRequest = 0;
    private int endOfRequest = -1;
    private String message = null;

    private final static int HEADER_LEN = 2;


    /**
     * Works for both request and response
     *
     * @param bytes
     * @return
     */
    public boolean locateRequest(List<Byte> bytes) {
        if (!bytes.isEmpty()) {
            if (!log.isDebugEnabled()) {
                log.info("locateRequest(List<Byte> bytes)");
                log.info("num bytes: " + bytes.size());
            }
            // ignore blank requests
            if (bytes.size() < 2) {
                message = "first 2 bytes not yet received - unable to calculate length.";
                log.warn(message);
                return false;
            }
            byte[] sizeBytes = new byte[2];
            sizeBytes[0] = bytes.get(0);
            sizeBytes[1] = bytes.get(1);
            int messageLength = -1;

            //messageLength = calculateLength(bytes.get(0), bytes.get(1));
            messageLength = parseByteArrayToShort(sizeBytes,0);
            messageLength += HEADER_LEN;
            if (!log.isDebugEnabled()) {
                log.info("calculated message length: " + messageLength);
            }

            // if the message is larger than what we can read at once over the
            // socket return false; and then read the entire message the next
            // time
            
            byte[] bites = listToByteArray(bytes);
            String message = new String(bites);
            log.info("Message: " + message);
            
            log.info("Hexa Message: " + Converter.convertStringToHex(message));
            

            if (bytes.size() < messageLength) {
                endOfRequest = -1;
                startOfNextRequest = -1;
                return false;
            } else {
                endOfRequest = messageLength;
                startOfNextRequest = 0;
                return true;
            }
        } else if (log.isDebugEnabled()) {
                log.debug("Bytes is zero)");
		}
			
        return false;
    }

    /**
     * @return
     */
    public int getEndOfRequest() {
        if (log.isDebugEnabled()) {
            log.debug("getEndOfRequest()");
        }
        return endOfRequest;
    }

    /**
     * @return
     */
    public int getStartOfNextRequest() {
        if (log.isDebugEnabled()) {
            log.debug("getStartOfNextRequest()");
        }
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
     *  Calculates the length of a payload based on the first two bytes of the message.
     *
     * @param a
     * @param b
     * @return
     * @throws java.io.IOException
     */
    public int calculateLength(byte a, byte b) {


        return  (b + (a << 8)) + HEADER_LEN;
    }

    /**
     * Obtain the length in the first 2 bytes (Big Endian) and discard
     * 2 more.
     *
     * @param is a <code>java.io.InputStream</code> value
     * @return an <code>int</code> value
     */
    public int read(java.io.InputStream is)
            throws Exception
    {
        DataInputStream dis = new DataInputStream(is);

        // skip the length bytes
        byte [] b2  = new byte[2];

        dis.readFully(b2);

        return  ((b2[1] & 0xFF) + (b2[0] >> 8));
    }
    
    /**
     * 
     * @param bytes
     * @return
     */
    private byte[] listToByteArray(List<Byte> bytes){
    	byte[] ba = new byte[bytes.size()];
    	for (int i = 0; i < bytes.size(); i++) {
    		ba[i] = bytes.get(i);
		}
    	printBytes(ba);
    	return ba;
    }

	/**
	 * @param ba
	 */
	private void printBytes(byte[] ba) {
		String request = new String(ba);
    	
    	log.info("String message: " + request );
	}
}
