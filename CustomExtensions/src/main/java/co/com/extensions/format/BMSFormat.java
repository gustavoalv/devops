/**
 * 
 */
package co.com.extensions.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.com.extensions.handlers.BMSHandler;
import co.com.extensions.handlers.CustomExtensionsHandler;
import co.com.extensions.util.ElementPS9;

import com.itko.citi.Converter;

/**
 * @author alvgu02
 *
 */
public class BMSFormat {
	
	public static void main(String argv[]) throws Exception {
		
//		String configfile = "C:\\Lisa7.5.2\\vb-config.xml";
		
		String configfile = "E:\\ApplicationDelivery\\CA_Service_Virtualization\\Projects\\DevTest\\Projects\\Bancomer_Mexico_Project_00\\Data\\CICS\\copybook\\bms-config.xml";
		

		
		// Conversion HEX a ISO8583 - Request
		{
 
//			String message = "BAN0591W0105912014111810532500101INT000000000000000000000003BAN0591W171278900000J293867940";
			
			String message = "                   D0037413   I                                                                                                                                                                                                        0000    ";
			String formato = "PEE5";
			
			String resultXML;
			
			String hexaMessage = Converter.convertStringToHex(message);
			
			byte[] dataByteAscii = Converter.convertHexToByte(hexaMessage);

			// VB
			resultXML = BMSHandler.BMSParser(configfile, dataByteAscii, formato, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_REQUEST);
			
			// CONCENTRADO
//			String resultHexa = BMSHandler.BMSParser(configfile, dataByteAscii, 96, true, BancoBogotaHandler.CONTENT_TYPE_HEXA, BancoBogotaHandler.TYPE_MESSAGE_REQUEST);
			System.out.println(resultXML);
			// String
//			String resultString = ISO8583Handler.ISO8583Parser(configfile, dataAscii, 14, false, ISO8583Handler.CONTENT_TYPE_ASCII );
//			System.out.println(resultString);
			
			String bodyAsString = "<OH>26100000000000538</OH><AV>PEA00230NO EXISTEN MAS PERSONAS                                                </AV><DE>1P1 0      E</DE><OC>C1YKNCE1AS56891566CESAR NU#EZ SANCHEZ                                         NUSC600101UE3 F32F32P3PATRIMONIAL C                 21/04/2004 1001/01/1960 55THE AMERICAN SCDDOMICILIO      5556950914MULTIFAMILIA                                                     NNS04                                                                            PERSONAS FISICAS                                         SIN CLASIFICACION CALCULADA                                                       </OC>";
			
//			String[] valores = sgml.split("(<.[^(><.)]+>)");
			
	    	
//	    	for (int i = 0; i < valores.length; i++) {
//	    		System.out.println("Valor: " + valores[i]);
//				String elementName = valores[i].substring(0,2);
//				System.out.println("Element: " + elementName);
//			}
	    	
	    	System.out.println("Termine");
	    	
	    	String testHtml = "xx <tag a =\"b\" c=  \'d\' e=f> contentssss </tag> zz";
	    	testHtml = bodyAsString;
	    	Map<String, String> data = new HashMap<String, String>();
	    	
	    	while(bodyAsString.length()!=0){
		    	int lenght = bodyAsString.length();
		    	Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
		        Matcher m = tagPattern.matcher(bodyAsString);
		        boolean tagFound = m.find(); // true
		        String tagOnly = m.group(0);// <tag a ="b" c= 'd' e=f> contentssss </tag>
		        String tagname = m.group(1);// tag
		        String attributes = m.group(2);// a ="b" c= 'd' e=f
		        String content = m.group(3);// contentssss
		        System.out.println("Tag Only   : " + tagOnly);
		        System.out.println("Tag Name   : " + tagname);
		        System.out.println("Attributes : " + attributes);
		        System.out.println("Content    : " + content);
		        data.put(tagname, content);
		        bodyAsString = bodyAsString.substring(tagOnly.length());
	    	}
	        
	    	System.out.println("Termine");
			
		}
		
		
		
		// Conversion HEX a ISO8583 - Request
			{
	// 
				String message = "               I3423454545555NUSC60010132323   INT.01 CCOYOACAN                      PATRIM C  ";
				String formato = "PEM0E5";
				
				String hexaMessage = Converter.convertStringToHex(message);
				
				byte[] dataByteAscii = Converter.convertHexToByte(hexaMessage);

				// BMS
				String resultHexa = BMSHandler.BMSParser(configfile, dataByteAscii, formato, true, CustomExtensionsHandler.CONTENT_TYPE_HEXA, CustomExtensionsHandler.TYPE_MESSAGE_RESPONSE);
				
				System.out.println(resultHexa);
				
				resultHexa = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bms-message>"
				+ "<response>"
				+ "<Filler length=\"12\" type=\"HEXA\">202020202020202020202020</Filler>"
				+ "<Attr_Length_Contenido length=\"1\" type=\"HEXA\">20</Attr_Length_Contenido>"
				+ "<ID_Campo-Contenido length=\"2\" type=\"HEXA\">2020</ID_Campo-Contenido>"
				+ "<Contenido length=\"80\" type=\"ALPHA\">000156891566CESAR NU#EZ SANCHEZ FP3 UE3 </Contenido>"
				+ "</response>"
				+ "</bms-message>";
				
					// TODO Auto-generated method stub
			    	
				String resultObject = null;
				
				try {
				
				resultObject = (String)BMSHandler.BMSXmlToObject(resultHexa, false);
				
				} catch (Exception e){
					
				}
				
				String result = CustomExtensionsHandler.convertHexToString(resultObject);
				
				System.out.println(result);
					
				System.out.println(message.equalsIgnoreCase(result));
				
			}
		
	}

}
