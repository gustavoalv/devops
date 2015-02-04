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
				
				String sentence = "42524e2d3031342d57532d32312020201c803933303030301c81205353445341444d1c8231343030313720201cae301cb0303403";
				
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
