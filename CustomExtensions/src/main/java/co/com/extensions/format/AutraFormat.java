/**
 * 
 */
package co.com.extensions.format;

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
 
			String hexData = "01A4E2D9D3D5114040F8F5F2F0C2E2F0F6F5F0F0F0F5F0F9F0F0F0F0F1F3F2F1F2F9F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F8F0F0F1F4F2F3F8F3F7F0F0F0F0F0F0F0F9F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F6F0F5F6F0F1F0F0F0F0F0F0F0F0F0F0F3F6F1F8F9F9F0F0F0F0F0F0F0F1F2F0F6F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F2F0F1F4F1F0F0F6F1F3F2F1F2F9C2C2D7E2F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F0F040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040F0F0";

			byte[] dataByteAscii = Converter.convertHexToByte(hexData);
			
					
			String dataAscii = new String(dataByteAscii, "cp037");
			
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
