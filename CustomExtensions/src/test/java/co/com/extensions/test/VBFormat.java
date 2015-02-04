/**
 * 
 */
package co.com.extensions.test;

import co.com.extensions.handlers.CustomExtensionsHandler;
import co.com.extensions.handlers.VBHandler;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 *
 */
public class VBFormat {
	
	public static void main(String argv[]) throws Exception {
		
		co.com.extensions.md5.MD5.encriptarPorMD5("clave");
		String configfile = "C:\\Lisa7.5.2\\vb-config.xml";
		

		// Conversion HEX a ISO8583 - Request
		{
 
			String message = "BAN0591W0105912014111810532500101INT000000000000000000000003BAN0591W171278900000J293867940";
			
			String resultXML;
			if(message.length() > 80){
			
				String hexaMessage = Converter.convertStringToHex(message);
				
				byte[] dataByteAscii = Converter.convertHexToByte(hexaMessage);
	
				// VB
				resultXML = VBHandler.VBParser(configfile, dataByteAscii, 160, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_REQUEST);
				
				// CONCENTRADO
	//			String resultHexa = VBHandler.VBParser(configfile, dataByteAscii, 96, true, BancoBogotaHandler.CONTENT_TYPE_HEXA, BancoBogotaHandler.TYPE_MESSAGE_REQUEST);
				System.out.println(resultXML);
				// String
	//			String resultString = ISO8583Handler.ISO8583Parser(configfile, dataAscii, 14, false, ISO8583Handler.CONTENT_TYPE_ASCII );
	//			System.out.println(resultString);
			} else {
				
				StringBuffer buffer = new StringBuffer();
				buffer.append("<Message id=\"" + message.substring(0,4) + "\">");
				buffer.append("<data>");
				buffer.append(message);
				buffer.append("<\\data>");
				buffer.append("<\\Message>");
				
				resultXML = buffer.toString();
			}
			
			System.out.println(resultXML);
			
		}
		
		
		
		// Conversion HEX a ISO8583 - Request
			{
	// 
				String message = "0003BAN0591W171000100000&#65533;&#65533;0003BAN0591W171000100000&#65533;&#65533;";
				
				String hexaMessage = Converter.convertStringToHex(message);
				
				byte[] dataByteAscii = Converter.convertHexToByte(hexaMessage);

				// VB
				String resultHexa = VBHandler.VBParser(configfile, dataByteAscii, 48, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE);
				
				System.out.println(resultHexa);
				String resultObject = null;
				
				try {
				
				resultObject = (String)VBHandler.VBXmlToObject(resultHexa, false);
				
				} catch (Exception e){
					
				}
				
				String result = CustomExtensionsHandler.convertHexToString(resultObject);
					
				System.out.println(result);
				
			}
		
	}

}
