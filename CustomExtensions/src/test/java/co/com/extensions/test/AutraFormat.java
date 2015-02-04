/**
 * 
 */
package co.com.extensions.test;

import co.com.extensions.handlers.AutraHandler;
import co.com.extensions.handlers.CustomExtensionsHandler;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 *
 */
public class AutraFormat {
	
	public static void main(String argv[]) throws Exception {
		
		String configfile = "D:\\BancoBogota\\PoC\\Proyectos\\LISA_Project_00\\Data\\CU3_ISC_TCP_AUTRA\\isc-config.xml";

		// Conversion HEX a ISO8583 - Request
		{
 
			String hexData = "003442524E2D3031342D57532D32312020201C803933303030301C815353445341444D1C8230303134303030351C9131353032313703";

			byte[] dataByteAscii = Converter.convertHexToByte(hexData);
			
					
//			String dataAscii = new String(dataByteAscii, "cp037");
			
			String dataAscii = new String(dataByteAscii);
			
			System.out.println(dataAscii);
			
			// AUTRA
			String resultHexa = AutraHandler.AutraParser(configfile, dataByteAscii, 80, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_REQUEST);
			
			// CONCENTRADO
//			String resultHexa = AutraHandler.AutraParser(configfile, dataByteAscii, 96, true, BancoBogotaHandler.CONTENT_TYPE_HEXA, BancoBogotaHandler.TYPE_MESSAGE_REQUEST);
			System.out.println(resultHexa);
			// String
//			String resultString = ISO8583Handler.ISO8583Parser(configfile, dataAscii, 14, false, ISO8583Handler.CONTENT_TYPE_ASCII );
//			System.out.println(resultString);
			
		}
		
		// Conversion HEX a ISO8583 - Request
			{
	// 
				String hexData = "00DB1140401D60E2D9D3D5F020C2E2F0F640404040F5F0F9F0F0F0F0F0F0F0F0F0F0F0F0F04E4040F5F2F5F0F9F0404011C2601D60F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F6F0F5F6F0";

				byte[] dataByteAscii = Converter.convertHexToByte(hexData);
									
				String dataAscii = new String(dataByteAscii, "cp037");
				
					
				System.out.println(dataAscii);
				
				// HEX
				String resultHexa = AutraHandler.AutraParser(configfile, dataByteAscii, 96, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE);
				System.out.println(resultHexa);
				
				
				
				String resultObject = (String)AutraHandler.AutraXmlToObject(resultHexa, false);
					
				System.out.println(resultObject);
				System.out.println(hexData);
				

			}
		
	}

}
