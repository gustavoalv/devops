package co.com.extensions.test;

import com.itko.citi.Converter;

public class ConvertTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String trama = "";
			String ascii = Converter.convertHexToString(trama);
			
			String hexa = Converter.convertStringToHex(ascii);
			
			System.out.println(hexa);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
