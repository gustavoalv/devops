package co.com.extensions.md5;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MD5 {

	public static String encriptarPorMD5(String clave) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hex(md.digest(clave.getBytes("CP1252")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String hex(byte[] array) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
					1, 3));
		}

		return sb.toString();
	}

	public static void main(String argv[]) throws Exception {
		
		Date dNow = new Date( );
	      SimpleDateFormat ft = new SimpleDateFormat ("dd/MM/yy");
	      System.out.println(ft.format(dNow));
	      
	      if(dNow instanceof Date){
	    	  
	      }
		
		String aux = MD5.encriptarPorMD5("TI|400006|4|0.95|01-09-2014|2");
		System.out.println(aux);
	}
}
