/**
 * 
 */
package co.com.extensions.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import co.com.extensions.util.ElementPS9;

/**
 * @author alvgu02
 *
 */
public class PS7Format {
	
	protected static Logger log   = Logger.getLogger(PS7Format.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String message1 = "@18F6KX@AVJLA0002 MANUTENCAO OKÓ@DCJLM0O3  PALTERAGRP 0000010002JLA0001MANUTENCAO OK                                                           Ó";

		List<String> elements = new ArrayList<String>();
		
		PS7Format format = new PS7Format();
		Map<String, List<ElementPS9>> data = format.getListElementsXMLString(message1);

		Iterator<Entry<String, List<ElementPS9>>> it = data.entrySet().iterator();
		while (it.hasNext()){
			Entry<String, List<ElementPS9>> pair = it.next();
			String elemento = pair.getKey();
			elements.add(elemento);
			log.info("Se agrego el elemento: " + elemento);
		}
		System.out.println("Listo!!");
	}
	
	/**
     * 
     * @param bodyAsString
     * @return
     */
    public Map<String, List<ElementPS9>> getListElementsXMLString(String bodyAsString) {
    	Map<String, List<ElementPS9>> data = new LinkedHashMap<String, List<ElementPS9>>();
    	
    	 log.info("RAW Message   : " + bodyAsString);
    	
    	if(bodyAsString.length()!=0){
//	    	int lenght = bodyAsString.length();
	        String messages [] = bodyAsString.split("@");
			
	        boolean primeraVez = true;
			for (int i = 0; i < messages.length; i++) {
				String item = messages[i];
				
				log.info("Item   :-" + item + "-");
				
				if((item != null) && (!item.equalsIgnoreCase(""))){
					
					if(primeraVez){
						
						ElementPS9 elementPS9Result =  new ElementPS9();
						String resultado = "resultado";
						elementPS9Result.setTagname(resultado);
						elementPS9Result.setContent(item.substring(0, 1));
						
						List<ElementPS9> listResultado =  new ArrayList<ElementPS9>();
						listResultado.add(elementPS9Result);
			        	
						data.put(resultado, listResultado);
						
						ElementPS9 elementPS9Secuencia =  new ElementPS9();
						String secuencia = "secuencia";
						elementPS9Secuencia.setTagname(secuencia);
						elementPS9Secuencia.setContent(item.substring(1, 6));
						
						List<ElementPS9> listSecuencia =  new ArrayList<ElementPS9>();
						listSecuencia.add(elementPS9Secuencia);
			        	
						data.put(secuencia, listSecuencia);
						
						primeraVez = false;
						
					} else {
					
						List<ElementPS9> list = null;
			        	
			        	ElementPS9 elementPS9 =  new ElementPS9();
			        	
			        	String tagname = item.substring(0, 2);
				        elementPS9.setTagname(tagname);
				        log.info("Tag Name   : " + tagname);
				        
				        String content = item.substring(2, item.length()-1);// contentssss
				        elementPS9.setContent(content);
				        log.info("Content    : " + content);
				        
				        if(data.containsKey(tagname)){
				        	list = data.get(tagname);
				        	list.add(elementPS9);
				        	
				        } else {
				        	list =  new ArrayList<ElementPS9>();
				        	list.add(elementPS9);
				        }
				        
				        data.put(tagname, list);
					}
					
				}
			}
		   
    	}
    	
		return data;
	}

}
