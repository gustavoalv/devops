/**
 * 
 */
package co.com.extensions.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 * 
 */
public class TCPClient {
	
	Socket clientSocket;
	
	
	public TCPClient() throws UnknownHostException, IOException {
		super();
		clientSocket = new Socket("localhost", 6002);
	}
	

	public static void main(String argv[]) throws Exception {

		String sentence = "ISO0250000550200F23CC581A8E3801A00000000140000BC194097446491751690   003000000009254000010721320515160416320501072001010700000120030811170000100191117001000101374097446491751690=2001                00000215160400002           0102       0000Seguros de Vida Colpat11001        11 CO0240000000000000000000000000270000000000002003021000000001700160090TES10000    019    TES1030        048& 0000200048! C000026                   20      11           284097446491751690            020                    0201000021723          009000000000012  B24 B24 1 038                                      ";
		
		TCPClient client = new TCPClient();
		
		String messageRequest = client.prepareMessage(sentence);
		client.sendBytes(messageRequest.getBytes());
		
		byte[] response = client.readBytes();
		String messageResponse = new String(response);
		
		System.out.println("Message Sent: " + messageRequest);
		System.out.println("Message Received: " + messageResponse);
		
		client.close();

	}

	/**
	 * @param sentence
	 * @return
	 * @throws Exception
	 */
	private String prepareMessage(String sentence) throws Exception {
		String hexData = Converter.convertStringToHex(sentence) + "03";
		
		int largo = hexData.length()/2;
		
		String longitud = Converter.convertIntToHex(largo);

		if(longitud.length()%2 != 0){
			longitud = "0" + longitud;
		}
		
		if(longitud.length() == 2){
			longitud = "00" + longitud; 
		}
		
		String resultHex = longitud + hexData;
		
		String messageRequest =Converter.convertHexToString(resultHex);
		return messageRequest;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void close () throws IOException {
//	    this.clientSocket.close();
	}
	
	public void sendBytes(byte[] myByteArray) throws IOException {
	    sendBytes(myByteArray, 0, myByteArray.length);
	}
	
	public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
	    if (len < 0)
	        throw new IllegalArgumentException("Negative length not allowed");
	    if (start < 0 || start >= myByteArray.length)
	        throw new IndexOutOfBoundsException("Out of bounds: " + start);
	    // Other checks if needed.

	    // May be better to save the streams in the support class;
	    // just like the socket variable.
	    OutputStream out = clientSocket.getOutputStream(); 
	    DataOutputStream dos = new DataOutputStream(out);

	    dos.writeShort(len);
	    if (len > 0) {
	        dos.write(myByteArray, start, len);
	    }
	}
	
	public byte[] readBytes() throws IOException {
	    // Again, probably better to store these objects references in the support class
	    InputStream in = clientSocket.getInputStream();
	    DataInputStream dis = new DataInputStream(in);
	    int len = dis.readShort();
//	    int len = 20;
	    byte[] data = new byte[len];
	    if (len > 0) {
	        dis.readFully(data);
	    }
	    return  data;
	}
}
