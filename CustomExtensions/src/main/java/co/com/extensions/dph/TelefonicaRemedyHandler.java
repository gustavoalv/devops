/**
 * 
 */
package co.com.extensions.dph;

import java.util.ArrayList;
import java.util.List;

import com.itko.util.Parameter;
import com.itko.util.ParameterList;

/**
 * @author alvgu02
 * 
 */
public class TelefonicaRemedyHandler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hola Mundo");
		
		// String value = "'No. Cuenta' = "70020424"";
		
		String request = "('Telefono' = \"4241074723\") AND (('Fecha Hora Creación' > \"04/11/2012\" AND 'Fecha Hora Creación' < \"23/04/2015\") OR ('Estado' = \"EN PROCESO\"))";
		//
//		String request = "'Telefono' = \"4241476377\" AND ('Fecha Hora Creación' > \"01/03/2015\")";
		
//		String request = "'No. Cuenta'  = \"70020424\"";
		
		
		
		ParameterList parametersList = getListParameters(request);
	}

	/**
	 * @param request
	 */
	public static ParameterList getListParameters(String request) {
		
		ParameterList parameterList = new ParameterList();


		char[] requestChar = request.toCharArray();
		
		List<String> listaArgumentos = new ArrayList<String>();
		
		{
			StringBuffer argumentoBuffer = new StringBuffer();
			
			StringBuffer operatorBuffer = new StringBuffer();
			
			int contadorRequest = 0;
			int contadorVariable = 0;
			int contadorValor = 0;
			int contadorOperador = 0;
			int contadorOperadores = 0;
			boolean operator = false;
			boolean expresion = true;
			
			while(contadorRequest < requestChar.length){
				char character = requestChar[contadorRequest];
				
				
				if(operator){
					if(character == ' '){
						contadorOperador++;
					} else if (contadorOperador != 2){
						operatorBuffer.append(character);
						contadorRequest++;
						continue;
					}
					
					if(contadorOperador == 2){
						
						contadorOperadores++;
						
						argumentoBuffer.append("'Operador");
						argumentoBuffer.append(contadorOperadores);
						argumentoBuffer.append("' = \"");
						argumentoBuffer.append(operatorBuffer);
						argumentoBuffer.append("\"");
						
						listaArgumentos.add(argumentoBuffer.toString());
						argumentoBuffer = new StringBuffer();
						
						operator = false;
						contadorOperador = 0;
						contadorRequest++;
						continue;
					}
					
				} else {
					
					argumentoBuffer.append(character);
					
					if(character == '\''){
						contadorVariable++;
						contadorRequest++;
						continue;
					}
					
					if(character == '('){
						expresion = false;
						contadorRequest++;
						continue;
					}
					
					if(character == '"'){
						contadorValor++;
					}
					
					if(character == ')'){
						expresion = true;
					}
					
					if(contadorVariable == 2 && contadorValor == 2 && expresion){
						
						//System.out.println("Se va a agregar: -" + argumentoBuffer.toString() + "-");
						listaArgumentos.add(argumentoBuffer.toString());
						argumentoBuffer = new StringBuffer();
						operator = true;
						contadorVariable = 0;
						contadorValor = 0;
					}
				}
				
				contadorRequest++;
			}
		}

		for (String argumento : listaArgumentos) {
			System.out.println("Argumento: -" + argumento + "-");
		}

		
		int contadorArgumentos = 0;
		
		for (String argumento : listaArgumentos) {
			
			contadorArgumentos++;
			
			StringBuffer nombreVariable = new StringBuffer();
			StringBuffer operador = new StringBuffer();
			StringBuffer value = new StringBuffer();
			
			char[] argumentoChar = argumento.toCharArray();
			int contador = 0;
			int contadorVariable = 0;
			int contadorValor = 0;
			
			while(contador < argumentoChar.length){
				char character = argumentoChar[contador];
				
				if(character == '\''){
					contadorVariable++;
					contador++;
					continue;
				}
				
				if(contadorVariable == 1){
					nombreVariable.append(character);
				}
				
				if(character == '"'){
					contadorValor++;
					contador++;
					continue;
				}
				
				if(contadorValor == 1){
					value.append(character);
				}
				
				if(contadorVariable == 2 && character != ' ' && contadorValor == 0){
					operador.append(character);
				}
				
				contador++;
			}
			
			Parameter parameter = new Parameter("Variable_" + contadorArgumentos, nombreVariable.toString());
			parameterList.addParameter(parameter);
			Parameter parameterOperador = new Parameter("Operador" + contadorArgumentos, operador.toString());
			parameterList.addParameter(parameterOperador);
			Parameter parameterValor = new Parameter("Valor" + contadorArgumentos, value.toString());
			parameterList.addParameter(parameterValor);
			
			System.out.println("nombreVariable: -" + nombreVariable + "-");
			System.out.println("operador: -" + operador + "-");
			System.out.println("value: -" + value + "-");
			
		}
		
		return parameterList;
	}
}

