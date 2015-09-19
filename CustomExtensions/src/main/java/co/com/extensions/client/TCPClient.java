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
		clientSocket = new Socket("localhost", 8003);
	}
	

	public static void main(String argv[]) throws Exception {

//		String sentence = "42524e2d3031342d57532d32312020201c803933303030301c815353445341444d1c8230303134303031371c9131353032313903";
		
		String sentence = "    NXCARDP JLO301311NUO  1O00N2ALTERAGRP 0000010002                                                                               ";
//		sentence = Converter.convertHexToString(sentence);
		
		sentence = Converter.convertStringToHex(sentence, "cp500");
		
		TCPClient client = new TCPClient();
		
//		String messageRequest = client.prepareMessage(sentence);
		client.sendBytes(Converter.convertHexToByte(sentence));
		
		byte[] response = client.readBytes();
		String messageResponse = new String(response);
		
		System.out.println("Message Received: " + messageResponse);
		System.out.println("Message Received HEXA BYTE: " + Converter.convertByteToHex(response));
		
		System.out.println("Message Received: " + messageResponse);
		System.out.println("Message Received HEXA: " + Converter.convertStringToHex(messageResponse));
		System.out.println("Message Received HEXA CP500: " + Converter.convertStringToHex(messageResponse,"cp500"));
		
		client.close();
		
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
	    //TODO OJO LEE LOS DOS PRIMEROS BYTES
	    int len = dis.readShort();
//	    int len = 20;
	    byte[] data = new byte[len];
	    if (len > 0) {
	        dis.readFully(data);
	    }
	    return  data;
	}
}
