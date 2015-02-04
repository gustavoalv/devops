package co.com.extensions.test;

import java.util.Iterator;

import com.itko.citi.Converter;
import com.itko.util.Parameter;
import com.itko.util.ParameterList;

public class EvertecSplitter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
		String hexData = "003442524E2D3031342D57532D32312020201C803933303030301C815353445341444D1C8230303134303030351C9131353032313703";
		
		hexData = hexData.substring(0, hexData.length()-2);
		
		byte[] dataByteAscii;
		
		dataByteAscii = Converter.convertHexToByte(hexData);
		
		String dataHex = Converter.convertByteToHex(dataByteAscii);
		
		String[] campos = dataHex.split("1C");
		
		ParameterList p = new ParameterList();
		Parameter parameter;
		
		for (int i = 0; i < campos.length; i++) {
			parameter = new Parameter("key" + i, campos[i]);
			p.addParameter(parameter);
		}
		
		
		String dataAscii = new String(dataByteAscii);
		
		System.out.println(dataAscii);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
