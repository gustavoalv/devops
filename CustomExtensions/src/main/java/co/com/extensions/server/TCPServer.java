/**
 * 
 */
package co.com.extensions.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 * 
 */
public class TCPServer {
	
	private ServerSocket welcomeSocket = null;
	private Socket connectionSocket = null;
	
	
	
	public TCPServer() throws IOException {
		super();
		welcomeSocket = new ServerSocket(8002);
	}

	public static void main(String argv[]) throws Exception {
		
		System.out.println("Se inicio el server");
		
		TCPServer server = new TCPServer();
		
		while (true) {
			
			if(server.getConnectionSocket() != null && server.getConnectionSocket().isConnected()){
				
				byte[] request = server.readBytes();
				String message = new String(request);
				
				System.out.println("Message Received Server: " + message);
				
				String sentence = "0210B23880012E92801800000000100000050130000000020000000728145358020246095351072807281110000000190375188410034062772=160720110000283000000190000190990363190001900001        2520000020000000003755235340120000000000001700120190CER1+0000130013TES1    P1110000000013798& 0000900798! P000036 000000000000000000000000000000000000! QT00032 0130000190000190990000007100000 ! B200158 7FF90000808000048000319D5C7EE14A9C790000020000000000000000003900014417017015072801757AFF1000180110A00003220000000000000000000000FF0000000000000000000000000000! B300080 CF00DBD00004604000200000000000001400024201000007A0000000041010000000000000000000! B400020 05151000000000    4 ! B500038 0010040AF623F057CE100012000000000000NN! B600260                              ! BJ00082                                                      5503715100000000";
				
//				String sentence = "42524e2d3031342d57532d32312020201c803933303030301c81205353445341444d1c8231343030313720201cae301cb0303403";
				
				
				sentence = Converter.convertStringToHex(sentence);
				
//				sentence = Converter.convertHexToString(sentence);
//				
//				String response = server.prepareMessage(sentence);
				
//				server.sendBytes(response.getBytes());
				server.sendBytes(Converter.convertHexToByte(sentence));
				
				
			} else {
				server.setConnectionSocket(server.getWelcomeSocket().accept());
			}
			
			
		}
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
		
		String messageRequest =Converter.convertHexToString(hexData);
		return messageRequest;
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
	    OutputStream out = connectionSocket.getOutputStream(); 
	    DataOutputStream dos = new DataOutputStream(out);

	    dos.writeShort(len);
	    if (len > 0) {
	        dos.write(myByteArray, start, len);
	    }
	}
	
	public byte[] readBytes() throws IOException {
	    // Again, probably better to store these objects references in the support class
	    InputStream in = connectionSocket.getInputStream();
	    DataInputStream dis = new DataInputStream(in);

	    int len = dis.readShort();
	    byte[] data = new byte[len];
	    if (len > 0) {
	        dis.readFully(data);
	    }
	    return data;
	}

	/**
	 * @return the welcomeSocket
	 */
	public ServerSocket getWelcomeSocket() {
		
		return welcomeSocket;
	}

	/**
	 * @param welcomeSocket the welcomeSocket to set
	 */
	public void setWelcomeSocket(ServerSocket welcomeSocket) {
		this.welcomeSocket = welcomeSocket;
	}

	/**
	 * @return the connectionSocket
	 */
	public Socket getConnectionSocket() {
		return connectionSocket;
	}

	/**
	 * @param connectionSocket the connectionSocket to set
	 */
	public void setConnectionSocket(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}
	
	
}
