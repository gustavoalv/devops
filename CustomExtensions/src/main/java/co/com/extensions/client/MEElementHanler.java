package co.com.extensions.client;

import com.itko.util.ParameterList;

public class MEElementHanler {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ParameterList arguments = new ParameterList();
		ParameterList atributes = new ParameterList();
		
		String meElement = arguments.get("ME");
		String meElementTest = "0032CC10680014084CPBAST42004156891566";
		String copyLength = meElementTest.substring(0, 4);
		String copyType = meElementTest.substring(4, 5);
		String message = meElementTest.substring(5);
		String parameters = "ME_CopyLength=" + copyLength + "&ME_CopyType=" + copyType + "&ME_Message=" + message;
		arguments.addParameters(parameters);
		
		//p.addParameters("key1=val1&key2=val2");
		String theBody = "<OH>26100000000000477</OH><AV>PEA00230NO EXISTEN MAS PERSONAS                                                </AV><DE>1P101      E</DE><OC>B1PEM0E5S                000156891566CESAR               NU#EZ               SANCHEZ             FP3 UE3 </OC><OC>B1PEM0E5S                ELVIRA VARG#SS                                    CULHUACAN CTM OBRERO          </OC><OC>B1PEM0E5S                I3423454545555NUSC60010132323   INT.01 CCOYOACAN                      PATRIM C  </OC>";
		String root= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		System.out.println(root.length());
		String request = root + "<RESPONSE>" + theBody + "</RESPONSE>";
		
		request = request.substring(38);
		request = request.substring(10);
		request = request.substring(0, request.length() - 11);
		
		
	}

}
