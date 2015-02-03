package co.com.extensions.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.itko.citi.Converter;


public class ClienteISC {

        OutputStream out;
        InputStream in;
        DataOutputStream dos;
        DataInputStream dis;
        BufferedInputStream bis;
        BufferedOutputStream bos;
        Socket s;
    
   // public String operacion = "pago";
    
   // public String strHeader = "";
    
    public String operacion = "consulta";
    public String strHeader = "";
    
    
    public static void main(String[] args) {
    	System.out.print("INICIA CLIENTEISC \n");
        ClienteISC ClienteISC = new ClienteISC();
        ClienteISC.conectar();
        ClienteISC.enviar();
        ClienteISC.recibir();
        //ClienteISC.desconectar();
    }

    public void conectar() {

        //String IPDestino= "10.85.140.7";
        //int PuertoDestino = 40666;

//        String IPDestino = "10.85.140.5";
//    	String IPDestino = "10.86.83.139"; //10.85.140.5
//        int PuertoDestino = 40340; //
//        int PuertoDestino = 41999;
//    	int PuertoDestino = 8003; //42004
//    	int PuertoDestino = 42004;

        String IPDestino = "localhost";
//        
        int PuertoDestino = 6789;
    	
        int LongTramaRespuesta;
        byte tramaRespuestaBytes[];

        try {

            s = new Socket(IPDestino, PuertoDestino);
            dis = 
new DataInputStream(new BufferedInputStream(s.getInputStream()));
            dos = 
new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            
            short readShort = dis.readShort();
            int fff = (0x0000FFFF & readShort);
            int value = fff -2 ;
            LongTramaRespuesta = value;
                        
            tramaRespuestaBytes = new byte[LongTramaRespuesta];
            dis.readFully(tramaRespuestaBytes);
            ebcdic2Ascii(tramaRespuestaBytes);
            String respuesta = new String(tramaRespuestaBytes);
            
            String response = Converter.convertStringToHex(respuesta);
            System.out.println("RespuestaConexión:" + "*" + response + "*");


        } catch (Exception e) {
            System.out.println("Error= " + e);
        }


    }

    public static final void ebcdic2Ascii(byte[] vecbParam) {
        for (int i = 0; i < vecbParam.length; i++) {
            vecbParam[i] = (byte)vecbEBCDIC2ASCII[0x00FF & vecbParam[i]];
        }
    }

    public static final byte vecbEBCDIC2ASCII[] = 
        new byte[] { (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, 
                     (byte)0x04, (byte)0x09, (byte)0x06, (byte)0x7f, 
                     (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, 
                     (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f, 
                     (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, 
                     (byte)0x14, (byte)0x0a, (byte)0x08, (byte)0x17, 
                     (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b, 
                     (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f, 
                     (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, 
                     (byte)0x24, (byte)0x25, (byte)0x17, (byte)0x1b, 
                     (byte)0x28, (byte)0x29, (byte)0x2a, (byte)0x2b, 
                     (byte)0x2c, (byte)0x05, (byte)0x06, (byte)0x07, 
                     (byte)0x30, (byte)0x31, (byte)0x16, (byte)0x33, 
                     (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x04, 
                     (byte)0x38, (byte)0x39, (byte)0x3a, (byte)0x3b, 
                     (byte)0x14, (byte)0x15, (byte)0x3e, (byte)0x1a, 
                     (byte)0x20, (byte)0x41, (byte)0x42, (byte)0x43, 
                     (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, 
                     (byte)0x48, (byte)0x49, (byte)0x4a, (byte)0x2e, 
                     (byte)0x3c, (byte)0x28, (byte)0x2b, (byte)0x7c, 
                     (byte)0x26, (byte)0x51, (byte)0x52, (byte)0x53, 
                     (byte)0x54, (byte)0x55, (byte)0x56, (byte)0x57, 
                     (byte)0x58, (byte)0x59, (byte)0x21, (byte)0x24, 
                     (byte)0x2a, (byte)0x29, (byte)0x3b, (byte)0x5e, 
                     (byte)0x2d, (byte)0x2f, (byte)0x62, (byte)0x63, 
                     (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, 
                     (byte)0x68, (byte)0x69, (byte)0x6a, (byte)0x2c, 
                     (byte)0x25, (byte)0x5f, (byte)0x3e, (byte)0x3f, 
                     (byte)0x70, (byte)0x71, (byte)0x72, (byte)0x73, 
                     (byte)0x74, (byte)0x75, (byte)0x76, (byte)0x77, 
                     (byte)0x78, (byte)0x60, (byte)0x3a, (byte)0x23, 
                     (byte)0x40, (byte)0x27, (byte)0x3d, (byte)0x22, 
                     (byte)0x80, (byte)0x61, (byte)0x62, (byte)0x63, 
                     (byte)0x64, (byte)0x65, (byte)0x66, (byte)0x67, 
                     (byte)0x68, (byte)0x69, (byte)0x8a, (byte)0x8b, 
                     (byte)0x8c, (byte)0x8d, (byte)0x8e, (byte)0x8f, 
                     (byte)0x90, (byte)0x6a, (byte)0x6b, (byte)0x6c, 
                     (byte)0x6d, (byte)0x6e, (byte)0x6f, (byte)0x70, 
                     (byte)0x71, (byte)0x72, (byte)0x9a, (byte)0x9b, 
                     (byte)0x9c, (byte)0x9d, (byte)0x9e, (byte)0x9f, 
                     (byte)0xa0, (byte)0x7e, (byte)0x73, (byte)0x74, 
                     (byte)0x75, (byte)0x76, (byte)0x77, (byte)0x78, 
                     (byte)0x79, (byte)0x7a, (byte)0xaa, (byte)0xab, 
                     (byte)0xac, (byte)0x5b, (byte)0xae, (byte)0xaf, 
                     (byte)0xb0, (byte)0xb1, (byte)0xb2, (byte)0xb3, 
                     (byte)0xb4, (byte)0xb5, (byte)0xb6, (byte)0xb7, 
                     (byte)0xb8, (byte)0xb9, (byte)0xba, (byte)0xbb, 
                     (byte)0xbc, (byte)0x5d, (byte)0xbe, (byte)0xbf, 
                     (byte)0x7b, (byte)0x41, (byte)0x42, (byte)0x43, 
                     (byte)0x44, (byte)0x45, (byte)0x46, (byte)0x47, 
                     (byte)0x48, (byte)0x49, (byte)0xca, (byte)0xcb, 
                     (byte)0xcc, (byte)0xcd, (byte)0xce, (byte)0xcf, 
                     (byte)0x7d, (byte)0x4a, (byte)0x4b, (byte)0x4c, 
                     (byte)0x4d, (byte)0x4e, (byte)0x4f, (byte)0x50, 
                     (byte)0x51, (byte)0x52, (byte)0xda, (byte)0xdb, 
                     (byte)0xdc, (byte)0xdd, (byte)0xde, (byte)0xdf, 
                     (byte)0x5c, (byte)0xe1, (byte)0x53, (byte)0x54, 
                     (byte)0x55, (byte)0x56, (byte)0x57, (byte)0x58, 
                     (byte)0x59, (byte)0x5a, (byte)0xea, (byte)0xeb, 
                     (byte)0xec, (byte)0xed, (byte)0xee, (byte)0xef, 
                     (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, 
                     (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, 
                     (byte)0x38, (byte)0x39, (byte)0xfa, (byte)0xfb, 
                     (byte)0xfc, (byte)0xfd, (byte)0xfe, (byte)0xff };

    public void enviar() {
    	/*PRUEBA PRODUCTIVIDAD DESDE CANALES HASTA OPEN*/  // CONSULTA PECO 31032014
    	/*byte bodyRequestBytes[] = hexa2Bytes("50523034 2d2d2d54 35313031 31313130"+
    	"3030302d 2d2d2d2d 2d313130 30303030"+
    	"302d3032 31323133 2d2d3931 312d2d2d"+
    	"34363436 32303031 31303030 34313232"+
    	"2d2d2d31 2d2d2d30 30353235 35373331"+
    	"372d2d30 30353235 35373331 37");*/
    	
    	/*byte bodyRequestBytes[] =				//Prueba WSRI puerto 40340                                
            hexa2Bytes(
            		"4c53 4e333234 434f5753 f0f0f0f0"+
            		"f0f05649 42411140 40f2f1f6 f4c2f8f4"+
            		"f4f8f2f3 f4f0f0f1 f8f0f0f0 f1f1f5f7"+
            		"f1f0e6f7 e2c9c3e2 f0f5f0f0 f0f0f0f0"+
            		"f0f01191 49d6c6f0 f111c46d f0f6f0f6"+
            		"f1f411c5 d3f211e4 f7f0f011 e4f8f0f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f011 92c7f0f1"+
            		"11c37bf0 f0f0f3f3 f4f0f5f1 f1f91191"+
            		"01f0f0f5 f711c6d4 f0f0f1f8 119501c2"+
            		"f8f4f4f8 f2f3f411 9502f111 9249f0f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
            		"f0f0f0f0 f0f0f0f0 f1f0f0f6 1192e2f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f01193b4"+
            		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
            		"f0f0f0f0 1193b5f0 f0f0f0f0 f0f0f0f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f011 93b6f0f0"+
            		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
            		"f0f0                               "
            		);
            		
            		*/
    	
        /*PECO DE PPE*/    	 	
        //byte bodyRequestBytes[] =                                 // CONSULTA PECO 31032014
      /*  hexa2Bytes(
        		"4c53"+
      		   "34333231 e5c9c2c1 114040d7"+
      		   "c5c3d6d7 d7c5f0f0 f0f3f1f0 f0f0f4f0"+
      		   "f0f0f1f4 f0f2f0f2 f0f0f0f0 f0f0f0f0"+
      		   "f0f0f0f0 f0f0f0f0 119149d7 c5d6f111"+
      		   "c46df0f5 f0f7f1f3 11914af1 f4f0f2f0"+
      		   "f21140c3 f111c37b f0f0f4f7 f3f9f3f0"+
      		   "f7f911b2 c3f2f0f1 f3f0f5f0 f7f3f0f2"+
      		   "c5c1f8f0 f0f0f4c3 d7404040 40404040"+
      		   "404040"); 
*/
    	
    	
    	//RECORDAR BORRAR LOS 2 primeros hexadecimales, #### EJEMPLO "0072"
    	//VALIDACION PIN AVPW
    	/*	"3030 30323937 e3e3c1e5 114040c1"+
		 "e5d7e6f0 f2f2f040 404040f1 f2f3f4f0"+
		 "f0f0f1f2 f3f0f0f3 40404040 40404040"+
		 "f0f0f0f0 f0f0f0f0 119149c9 d5f0f111"+
		 "c46df686 8582f2f5 11914af1 f2f3f0f0"+
		 "f31140c3 f0f0f0f0 f0f0f0f0 f1f0f2f0"+
		 "f3f0f4f2 119405f6 f7f7f240 40404040"+
		 "40404040 40404011 40c4f011 9406f0f0"+
		 "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f01194"+
		 "07f1c5f6 f7c2f4f2 c2c3f7c5 f3f7c2f1"+
		 "f311e4f2 c3404040 40404040 40404040"+
		 "40404040 40404040 40404040 40404040"
		 */
    	
    	
    	byte bodyRequestBytes[] =hexa2Bytes(				    	                             
    			"0b4c 534e3235 54544242 114040c2"+
    			"c2d7e2f0 f2f2f140 404040f1 f2f3f4f0"+
    			"f0f0f1f7 f1f8f5f0 40404040 40404040"+
    			"f0f0f0f0 f0f0f0f0 119149c2 c2f0f111"+
    			"c46df0f9 f1f8f1f4 11914af1 f7f1f8f5"+
    			"f01140c3 f111c37b f0f0f0f9 f3f7f2f8"+
    			"f7f111c3 f0f0f0f0 f0f0f0f0 f5f2f1f2"+
    			"f9f7f0f0 114040f0 f0f0f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0f0f0f0 f0f0f0f1 f0f1f6f0"+
    			"f8f1f8f4 f5119403 f0f0f1f4 11e4f3f0"+
    			"f8f0f0f2 f2f1f5f5 f3f111e4 f2d511e4"+
    			"f4f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f011"+
    			"e4f5f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
    			"11e4f6f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0f0f011 e4f7f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f011e4"+
    			"f8f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
    			"f0f0f0f0 f0404040 40404040 40404040"+
    			"40404040 40404040 40404040 40404040"
            		);
          	
    	
    	
    	/*
		//PRUEBA CONCENTRADOR AUTRA
		"0077e2d9 d3d51140 40f8f5f0 f0f0f2f3"+
		"f1f6f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
		"f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
		"f0f0f0f0 f0f0f0f0 f0f0f2f4 f3f1f6f9"+
		"f8f8c2f9 c3f3c3f4 c3f0f7f2 c1f5f8f7"+
		"f6c3f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f7"+
		"f0f6f1f2 f0f1f4f0 f9f2f2f1 f1f3f6f0"+
		"f6c1e5d7 e6f0f0"            */     
    	
    	
            		//Prueba Transferencia AVAL   
   /* 	"4c53 4e323938 e3e3c3c1 114040f0 f9f0f0f1 f9f3f3f2"+
        "f3f6f4f0 f0f0f6f0 f0f0f0f8 f3f9f5f1"+
        "e6f7e2c9 e2c3f0f5 f0f0f0f0 f0f0f0f0"+
        "11c37bf0 f1f9f1f5 f9f3f2f6 11c94df0"+
        "f711924a f0f211c1 c5f61191 01f0f0f2"+
        "f31191a1 f11193a1 f01111");*/
    	/*"4c53 003f4c53 4e303031 e3e3c3c1 114040f2"+
    	"f4f2f4f1 f0f0f0f0 f0f2f0f0 f0f1f8f0"+
    	"f0f0f1f4 f2f7f2f4 c2d6e7f9 f4f3f0f1"+
    	"f0f0f0f0 f0f0f0f0 119101f0 f0f0f0");  
*/
    	/*
    	byte bodyRequestBytes[] =	                                
            hexa2Bytes(
    	"4c53 4e303234 e3e3c3c1 114040d6"+
    	"c6c3c3f1 f0f0f0f0 f0f1f9f0 f0f0f8f0"+
    	"f0f0f1f4 f0f9f3f9 e6f7e2c9 d6d9f8f2"+
    	"f0f0f0f0 f0f0f0f0 119149d6 c6f0f111"+
    	"c46df0f6 f2f7f1f4 11914af1 f4f0f9f3"+
    	"f9119411 f111c37b f0f0f0f8 f2f5f2f9"+
    	"f9119110 f0f01191 03f0f011 9101f0f0"+
    	"f0f0");                               
*/
    	

        /* VALIDACION DE pin
        byte bodyRequestBytes[] = hexa2Bytes( "e3e3c2c2 114040c9" +
                "e5d7e6f0 f2f2f040 404040f1 f2f3f4f0" +
                "f0f0f1f9 f1f5f1f2 40404040 40404040" +
                "f0f0f0f0 f0f0f0f0 119149c9 d5f0f111" +
                "c46df0f7 f1f1f0f5 11914af1 f9f1f5f1" +
                "f21140c3 f0f0f0f7 f9f3f6f9 f1f7f311" +
                "9405f1f2 f3f4f5f6 f7f8f9f0 c1c2c3c4" +
                "c5c61140 c4f01194 06f0f0f0 f0f0f0f0" +
                "f0f0f0f0 f0f0f0f0 f0119407 f0f0f0f0" +
                "f0f0f0f0 f0f0f0f0 f1f2f3f7 11e4f2f1" +
                "40404040 40404040 40404040 40404040" +
                "40404040 40404040 40404040 40404040" +
                "40404040 40404040 40404040 40404040" +
                "40404040 40404040" );*/

        /*2164*/
        /*byte bodyRequestBytes[] =
         hexa2Bytes("c3d6e6e2 114040f2"+
                    "f1f6f4f5 f2f4f1f4 f2f8f5f0 f0f4f3f0"+
                    "f0f0f1f6 f2f1f0f7 e6f2e2c9 d6d9f9f1"+
                    "f0f0f0f0 f0f0f0f0 119149d6 c6f0f111"+
                    "c46df1f0 f0f9f0f8 11c5d3f2 11e4f7f0"+
                    "fC11e4f8 f0f8f9f0 f9f0f5f2 f1f1f111"+
                    "9249f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
                    "f0f0f0f1 f2f0f8f1 f6f5f3f1 f8f6f8f1"+
                    "1192c7f0 f211c37b f0f4f3f4 f6f2f9f2"+
                    "f3f41191 01f1f0f8 11c6d4f0 f0f4f311"+
                    "9501f5f2 f4f1f4f2 f8f51195 02f1");*/

        /*2164 * Consullta Valor Recaudo WS - Trama utilizando Concentrador*/
        /*byte bodyRequestBytes[] =
          hexa2Bytes("434f5753 56494241 114040f2"+
                     "f1f6f4f5 f2f4f1f4 f2f8f5f0 f0f4f3f0"+
                     "f0f0f1f6 f2f1f0f7 e6f2e2c9 d6d9f9f1"+
                     "f0f0f0f0 f0f0f0f0 119149d6 c6f0f111"+
                     "c46df1f0 f0f9f0f8 11c5d3f2 11e4f7f0"+
                     "f111e4f8 f0f0f0f8 f9f0f9f0 f5f2f1f1" +
                     "f1119249 f0f0f0f0 f0f0f0f0 f0f0f0f0"+
                     "f0f0f0f0 f0f1f2f0 f8f1f6f5 f3f1f8f6"+
                     "f8f11192 c7f0f211 c37bf0f4 f3f4f6f2"+
                     "f9f2f3f4 119101f0 f1f0f811 c6d4f0f0"+
                     "f4f31195 01f5f2f4 f1f4f2f8 f5119502"+
                     "f1");
          */           
         /*2161 * Pago Recaudos WS */
         /*byte bodyRequestBytes[] =
         hexa2Bytes("4c53 4e303036 e3e3c3c1 114040f2" +
         "f1f6f1f1 f9f2f4f3 f6f7f1f0 f0f8f0f0" +
         "f0f0f0f9 f5f0f1f9 c3c1d5c1 d3c5e2f1" +
         "f0f0f0f0 f01193c1 f011c15b f0f0f011" +
         "91d1f011 9192f0f0 f011c3f0 f1f0f0f0" +
         "f0f01192 c7f0f211 4040f4f3 f4f6f2f9" +
         "f2f3f411 c3f5f0f0 f0119661 f2f1f6f0" +
         "119249f1 f1f1f1f1 1192e2f2 f2f2f2f2" +
         "1193b411 93b51193 b61193b7 f1f8f411" +
         "93b8f011 93b9f011 93baf0f2 11c6d4f0" +
         "f0f0f8f0 119201f1 f8f411c3 f311c3f4" +
         "11910511 c37bf0f0 f0f0f0f0 f0f0f011" +
         "c3f2f0f0 f01193c4 f01140d5 d5119101" +
         "f0f0f011 91a1f111 a211b2c3 f2f0f0f8" +
         "f1f0f2f9 f1f7f3f1 f1f9f2f1 f6f1f0f0" +
         "11a2c9f1 11b2c3f2 f0f0f8f1 f0f3f1f1" +
         "f1f5f9f2 f8f2f1f6 f1f0f0");*/


         /*2121 * Pago Recaudos WS */
         /*byte bodyRequestBytes[] =
          hexa2Bytes("01604c53 4e303036 e3e3c3c1 114040f2" +
          "f1f2f1f1 f9f2f4f3 f6f7f1f0 f0f8f0f0" +
          "f0f0f0f9 f5f0f1f9 c3c1d5c1 d3c5e2f1" +
          "f0f0f0f0 f0f0f0f0 1193a1f0 1193c2f7" +
          "f7f7f0f9 f9f9f8f0 f0f1f7f0 f11193c5" +
          "f0f0f0f0 f0f0f0f0 f0f1f1f1 f8f8f0f6" +
          "f4f7f8f0 f2f9f3f8 f8119205 f0f0f0f0" +
          "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
          "f0f0f0f0 f01193c1 f0f0f011 c15bf0f0" +
          "f011c35a f8f0f6f2 f0f0f011 c3f0f0f0" +
          "f011c37b f0f0f0f0 f0f0f0f0 f0119105" +
          "f011c3f1 f8f0f6f2 f0f0f011 960911e4" +
          "f6f0f1f0 f1f1f9f0 f0119298 f8f0f6f2" +
          "f0f0f011 a2c3f011 93b01193 b2f0f0f0" +
          "f0f0f0f0 f0f0f0f0 f3f4f8f1 f5f11193" +
          "b3f011c3 f5f0f0f0 1193b7f0 f0f01193" +
          "b8f01193 b9f011c6 d4f0f0f5 f6119201" +
          "f0f0f011 9141f011 44204040 40404040" +
          "40404040 40e4f7f2 f0f0f8f1 f0f3f1c3" +
          "f311c3f4 11a2c9f1 40d5d511 9101f0f0" +
          "f01191a1 f111b2c3 f2f0f0f8 f1f0f3f1" +
          "f0f9f0f8 f1f0f2f1 f2f1f0f0 11114040");*/
          
          //String strHeader = "      "; Para 2164
          //String strHeader ;//Para 2121, 2161
          //strHeader = "";//Para 2121, 2161
          byte headerRequestBytes[] = strHeader.getBytes();
          
        /*2164 *0220006376843 Consullta Valor Recaudo WS - Trama sin utilizar Concentrador*/
        
        /* trama para consulta por factura
          "434f5753 f0f0f0f0 f0f05649 42411140"+
                   "40f2f1f6 f4f5f2f4 f1f4f2f8 f5f0f0f4"+
                   "f3f0f0f0 f1f6f2f1 f0f7e6f2 e2c9d6d9"+
                   "f9f1f0f0 f0f0f0f0 f0f01191 49d6c6f0"+
                   "f111c46d f1f0f0f9 f0f811c5 d3f211e4"+
                   "f7f0f111 e4f8f0f0 f0f8f9f0 f9f0f5f2"+
                   "f1f1f111 9249f0f0 f0f0f0f0 f0f0f0f0" +
                   "f0f0f0f0 f0f0f0f1 f1f0f9f1 f6f3f0f8"+
                   "f5f1f3f4 1192c7f0 f211c37b f0f4f3f4"+
                   "f6f2f9f2 f3f41191 01f0f1f0 f811c6d4"+
                   "f0f0f4f3 119501f5 f2f4f1f4 f2f8f511"+
                   "9502f1"
      */  
        
        /* trama para consulta por identificacion
         * "434f5753 f0f0f0f0 f0f05649 42411140"+
                   "40f2f1f6 f4f5f2f4 f1f4f2f8 f5f0f0f4"+
                   "f3f0f0f0 f1f6f2f1 f0f7e6f2 e2c9d6d9"+
                   "f9f1f0f0 f0f0f0f0 f0f01191 49d6c6f0"+
                   "f111c46d f1f0f0f9 f0f811c5 d3f111e4"+
                   "f7f0f111 e4f8f0f0 f0f0f0f0 f8f2f4f1"+
                   "f2f6f111 9249f0f0 f0f0f0f0 f0f0f0f0" +
                   "f0f0f0f0 f0f0f0f0 f2f2f0f0 f0f6f3f7"+
                   "f6f8f4f3 1192c7f0 f211c37b f0f4f3f4"+
                   "f6f2f9f2 f3f41191 01f0f1f0 f811c6d4"+
                   "f0f0f4f3 119501f5 f2f4f1f4 f2f8f511"+
                   "9502f1"
         */
         
         /* valor de prueba factura= 220006376843 10288912 1650 0038 1254
          * valor de prueba identificacion= 39384332       184 8810 01
          */
          /*CANALES CONSULTA F900    CLIENTE OK   064*/ 

    /*     byte bodyRequestBytes[] =
          hexa2Bytes("4c53 4e303136 434f5753 f0f0f0f0"+
           "f0f05649 42411140 40f2f0f0 f1c6f9f0"+
           "f0f7f9f9 f6f2f7f9 f7f0f0f0 f2f0f0f0"+
           "f0f9f2f5 f3f2e6f7 e2c9d6d9 f0f7f0f0"+
           "f0f0f0f0 f0f01140 c3f0f6f4 11950bf0"+
           "f0f1f0f0 f0f0f0f0 f1f8f911 b8b8f911"+
           "b8b9f9f9 f9f9f9f9 f9f9f911 9101f0f0"+
           "f0f01191 a1f11193 a1f01111")        ;  */
           
     /*CANALES CONSULTA F900    CLIENTE OK   019 M. ORDUZ*/ 

   /*       byte bodyRequestBytes[] =
     hexa2Bytes("4c53 4e303136 434f5753 f0f0f0f0"+
      "f0f05649 42411140 40f2f0f0 f1c6f9f0"+
      "f0f7f9f9 f6f2f7f9 f7f0f0f1 f3f0f0f0"+
      "f1f0f4f6 f0f6e6f7 e2c9c7c4 f5f4f0f0"+
      "f0f0f0f0 f0f01140 c3f0f1f9 11950bf0"+
      "f0f1f0f0 f0f0f1f2 f8f1f711 b8b8f911"+
      "b8b9f9f9 f9f9f9f9 f9f9f911 9101f0f0"+
      "f0f01191 a1f11193 a1f01111")         ;  */

        /*CANALES APERTURA OFNF    CLIENTE OK   064*/ 
        /*        byte bodyRequestBytes[] =
                 hexa2Bytes("4c53 4e303436 434f5753"+
                             "f0f0f0f0 f0f05649 42411140 40f2f0f2"+
                             "f1d6c6d5 c6f7f9f9 f6f2f7f9 f7f3f7f8"+  //Codigo de Secuencia f3f7f2f3 -- 3723
                             "f1f0f0f0 f1f3f5f1 f2f6f0c2 d6d7f0f0"+
                             "f3f1f0f0 f0f0f0f0 f0f01191 49d6c6f0"+
                             "f111c46d f0f4f2f3 f2f01191 4af1f3f5"+
                             "f1f2f611 40c3f0f6 f011950b f0f0f0f0"+
                             "f0f0f0f0 f0f0f0f0 11e4e3c1 95814040"+  //11e4e3 nombre
                             "d4c1d9c3 c5d3c140 c7d9c1d1 c1d3c5e2"+
                             "40c3c1d5 d699a485 828140a2 89868940"+ 
                             "f311e4f2 f011e4f3 f5f3f1f1"+  //Id del Cliente (8) f5f3f1f1f6f4f3f2 - 53116432
                             "f8f4f3f9 1195"+
                             "0fe21195 0cd51196 01d711e4 e511e6f5"+
                             "4011e5f1 f011950d 11951040 119511f0"+
                             "119101f0 f0f0f811 e4e6c5d4 d7d3c511"+
                             "e4f4f0f0 f0f0f0f0 f0f0f0f1 f0f0f0f0"+
                             "f0f0f0f0 11e4f5f0 f0f0f0f0 f0f0f0f0"+
                             "f0f2f0f0 f0f0f0f0 f011e4f6 f0f0f0f0"+
                             "f0f0f0f0 f0f0f1f5 f0f0f0f0 f0f011e4"+
                             "f7f0f0f0 f0f0f0f0 f0f1f0f0 f0f0f0f0"+
                             "f0f0f011 e4f8f0f1 f0f1f1f9 f0f011e5"+
                             "f8f0f0f0 f0f0f0f0 f0f1f111 e5f4f0f0"+
                             "f0f0f0f0 f0f0f0f1 11e5f2c3 d3d340f3"+
                             "f640f8f7 4060f4f7 5c5c5c5c 5c5c5c5c"+
                             "5c5c5c5c 5c5c5c5c 5c5c5c5c 5c5c5c5c"+
                             "5c5c5c5c 5c5c5c5c 5c5c5c5c 5c5c5c11"+
                             "e4f940f0 f0f1f111 e5f3c3d6 40404040"+
                             "40404040 11e5f5f3 f3f2f0f0 f3f24040"+
                             "40404040 40404040 40404011 e5f7f1f6"+
                             "f2f34040 40404040 40404040 4011e5f9"+
                             "40404040 404040f1 f1f111e6 f7c1c4c6"+
                             "c1e25c5c 5c5c5c5c 5c5c5c5c 5c5c5c5c"+
                             "5c5c5c5c 5c5c5c5c 5c5c5c5c 5c5c5c5c"+
                             "5c11e4f0 f0f0f0f0 f0f0f0f0 f0f0f011"+
                             "e6f84040 40404040 40404040 11e5f6f0"+
                             "f0f0f0f0 f0f0f0f0 f011e6f9 40404040"+
                             "40404040 404011e7 f1f0f0f6 f3f5f3f1"+
                             "f2f3f111 b8b8f911 b8b9f9f9 f9f9f9f9"+
                             "f9f9f911 11")       ; */
              
// ENTREGA TALONARIOS 2041
  /*      byte bodyRequestBytes[] =
        hexa2Bytes("4c53 4e303436" +
        "434F5753 F0F0F0F0 F0F05649 42411140" +
        "40F2F0F4 F1D6C6D5 C6C3F0F7 F1F5F4F7" +
        "F6F0F0F0 F2F0F0F0 F1F4F2F0 F3F3E6F7" +
        "E2C9D6D9 F0F2F0F0 F0F0F0F0 F0F01191" +
        "49D6C6F0 F111C46D F0F5F1F5 F2F01191" +
        "4AF1F4F2 F0F3F311 40C3F0F6 F01192C5" +
        "F1F2F3F4 F5F6F7F8 F9F0F1F2 11E4F2F0" +
        "11E4F3F1 F2F3F4F7 F8F9F6F5 1193A5F1" +
        "F2F3F4F5 F6F71193 A6F1F2F3 F4F5F6F7" +
        "1191A8D5 119101F0 F0F0F811 b8b8f911" +
        "b8b9f9f9 f9f9f9f9 f9f9f911 11")          ;  */             

     // ENTREGA TALONARIOS 2041 Syddar
        /*     byte bodyRequestBytes[] =
             hexa2Bytes("4c53 4e303436" +
     "434f5753 f0f0f0f0 f0f05649 42411140"+
     "40f2f0f4 f1d6c6c6 c1f7f9f9 f6f2f7f9"+
     "f7f0f0f2 f4f0f0f0 f1f5f1f9 f3f1f0c2"+
     "d6d7f1f0 f0f2f0f0 f0f0f0f0 f0f01191"+
     "49d6c6f0 f111c46d f2f0f1f3 f0f61191"+
     "4af1f5f1 f9f3f111 b8b8f911 b8b9f9f9"+
     "f9f9f9f9 f9f9f911 40c3f0f1 f91192c5"+
     "f0f0f1f0 f0f0f0f0 f0f2f5f0 11e4f2f1"+
     "11e4f3f8 f5f0f4f2 f0f1f5f6 f61193a5"+
     "f0f0f0f0 f0f0f111 93a6f0f0 f0f0f0f1"+
     "f01191a8 d5119101 f0f0f0f8 1111");*/


  /*CANALES DESINVERSION 2245    CLIENTE OK   019 M. ORDUZ*/ 

    /*   byte bodyRequestBytes[] =
  hexa2Bytes("4c534e303136"+
  "434f5753 f0f0f0f0 f0f05649 42411140"+
  "40f2f0f1 f2f2f2f4 f5c3f0f7 f1f5f4f7"+
  "f5f0f0f0 f6f0f0f0 f1f6f5f0 f4f2e6f7"+
  "e2c9d6d9 f0f2f0f0 f0f0f0f0 f0f011c3"+
  "f0f1f0f0 f0f0f0f0 f0f01140 c3f0f1f9"+
  "11e6f511 e5f1f011 c35af0f0 f0119515"+
  "f0f0f011 c37bf7f9 f6f0f5f3 f8f6f611"+
  "c3f1f1f0 f0f0f0f0 f0f0f011 e4f2f011"+
  "e4f3f7f8 f5f3f2f6 f2f41191 16f0f0f0"+
  "f3f5f5f3 f6f51191 2df11195 0bf0f0f1"+
  "f0f0f0f0 f1f2f8f1 f711c6d4 f0f0f0f6"+
  "1140c6f0 11b8b8f9 11b8b9f9 f9f9f9f9"+
  "f9f9f9f9 1191a1f1 1193a1f0 119101f0"+
  "f0f0f011 11")        ;*/
  
     /*CANALES DESINVERSION 2245, E6F5 con valor 0*/ 
  /*      byte bodyRequestBytes[] =
        hexa2Bytes("4c534e303136"+
        "434f5753 f0f0f0f0 f0f05649 42411140"+
        "40f2f0f1 f2f2f2f4 f5c3f0f7 f1f5f4f7"+
        "f5f0f0f0 f6f0f0f0 f1f6f5f0 f4f2e6f7"+
        "e2c9d6d9 f0f2f0f0 f0f0f0f0 f0f011c3"+
        "f0f1f0f0 f0f0f0f0 f0f01140 c3f0f1f9"+
        "11e6f5f0 11e5f1f0"+
        "11c35af0 f0f01195 15"+
        "f0f0f011 c37bf7f9 f6f0f5f3 f8f6f611"+
        "c3f1f1f0 f0f0f0f0 f0f0f011 e4f2f011"+
        "e4f3f7f8 f5f3f2f6 f2f41191 16f0f0f0"+
        "f3f5f5f3 f6f51191 2df11195 0bf0f0f1"+
        "f0f0f0f0 f1f2f8f1 f711c6d4 f0f0f0f6"+
        "1140c6f0 11b8b8f9 11b8b9f9 f9f9f9f9"+
        "f9f9f9f9 1191a1f1 1193a1f0 119101f0"+
        "f0f0f011 11")          ;*/
  
   /*CANALES DESINVERSION 2245, Syddar*/ 
   /*      byte bodyRequestBytes[] =
      hexa2Bytes("4c534e303136"+  
   "434f5753 f0f0f0f0 f0f05649 42411140"+
   "40f2f0f1 f2f2f2f4 f5f7f9f9 f6f2f7f9"+
   "f7f0f0f9 f5f0f0f0 f1f8f1f6 f2f2e6f7"+
   "e2c9c7c4 f5f4f0f0 f0f0f0f0 f0f011c3"+
   "f0f0f0f0 1140c3f0 f1f911e6 f511e5f1"+
   "f011c35a f5f0f0f0 f0f0f011 9515f0f0"+
   "f011b8b8 f911b8b9 f9f9f9f9 f9f9f9f9"+
   "f911c3f1 f5f0f0f0 f0f0f011 e4f2f111"+
   "e4f3f8f5 f0f4f2f0 f1f5f6f6 119116f0"+
   "f0f0f3f5 f5f3f6f5 11950bf0 f0f1f0f0"+
   "f0f0f0f0 f2f5f011 c6d4f0f0 f9f511c1"+
   "7af1f2f3 f4f5f6f7 1140c6f1 1191a1f1"+
   "1193a1f0 119101f0 f0f0f811 11");*/
  
  
        /*CANALES MODIFICACION RANGOS OFFB, Syddar*/ 
      /*        byte bodyRequestBytes[] =
           hexa2Bytes("4c534e303136"+  
        "434f5753 f0f0f0f0 f0f05649 42411140"+ 
        "40f2f0f4 f4d6c6c6 d4f7f9f9 f6f2f7f9"+ 
        "f7f0f0f4 f0f0f0f0 f1f0f3f1 f4f1e6f7"+
        "e2c9c7c4 f5f4f0f0 f0f0f0f0 f0f01191"+ 
        "49d6c6f0 f111c46d f2f0f1f3 f0f61191"+ 
        "4af1f0f3 f1f4f111 b8b8f911 b8b9f9f9"+ 
        "f9f9f9f9 f9f9f911 40c3f0f1 f91192c5"+ 
        "f0f0f1f0 f0f0f0f0 f5f9f7f8 1192e2f2"+ 
        "119101f0 f0f0f811 e4f2f211 e4f3f9f9"+ 
        "f0f5f0f6 f1f4f3f6 f81193a3 f1f0f0f0"+ 
        "f1f7f111 93a4f1f0 f0f0f1f8 f01193a5"+ 
        "f2f0f0f0 f1f8f111 93a6f2f0 f0f0f1f9"+ 
        "f0") ;*/
  
        // ANULACION TALONARIOS 2042 Produccion Talonario 2  
         /*        byte bodyRequestBytes[] =
                 hexa2Bytes("4c53 4e303436" +
         "434f5753 f0f0f0f0 f0f05649 42411140"+
         "40f2f0f4 f2d6c6c6 c1f7f9f9 f6f2f7f9"+
         "f7f0f0f2 f8f0f0f0 f1f1f2f5 f5f0e6f7"+
         "e2c9c7c4 f5f4f0f0 f0f0f0f0 f0f01191"+
         "49d6c6f0 f111c46d f2f0f1f3 f0f61191"+
         "4af1f1f2 f5f5f011 b8b8f911 b8b9f9f9"+
         "f9f9f9f9 f9f9f911 40c3f0f1 f91192c5"+
         "f0f0f1f0 f0f0f0f0 f0f2f5f0 11e4f2f1"+
         "11e4f3f0 f0f8f5f0 f4f2f0f1 f5f61193"+
         "a5f0f0f0 f0f0f0f0 1193a6f0 f0f0f0f0"+
         "f0f01191 a8e71191 01f0f0f0 f8") ;*/

        // ANULACION TALONARIOS 2042 Talonario 1
       /*         byte bodyRequestBytes[] =
        hexa2Bytes("4c53 4e303436" +
        "434F5753 F0F0F0F0 F0F05649 42411140"+
        "40F2F0F4 F2D6C6C6 C1F7F9F9 F6F2F7F9"+
        "F7F0F0F2 F5F0F0F0 F1F1F2F4 F5F1E6F7"+
        "E2C9C7C4 F5F4F0F0 F0F0F0F0 F0F01191"+
        "49D6C6F0 F111C46D F2F0F1F3 F0F61191"+
        "4AF1F1F2 F4F5F111 B8B8F911 B8B9F9F9"+
        "F9F9F9F9 F9F9F911 40C3F0F1 F91192C5"+
        "F0F0F1F0 F0F0F0F0 F0F2F5F0 11E4F2F1"+
        "11E4F3F0 F0F8F5F0 F4F2F0F1 F5F61193"+
        "A5F0F0F0 F0F0F0F0 1193A6F0 F0F0F0F0"+
        "F0F01191 A8E71191 01F0F0F0 F8");*/
        
        
        //COSNULTA TALONARIOS 2043 Syddar
    /*        byte bodyRequestBytes[] =
        hexa2Bytes("4c534e303436" +
        "434f5753 f0f0f0f0 f0f05649 42411140"+
        "40f2f0f4 f3d6c6c6 d4f7f9f9 f6f2f7f9"+
        "f7f0f0f0 f6f0f0f0 f1f3f3f8 f0f9f0c2"+
        "d6d7f1f0 f0f2f0f0 f0f0f0f0 f0f01191"+
        "49d6c6f0 f111c46d f2f0f1f3 f0f61191"+
        "4af1f3f3 f8f0f911 b8b8f911 b8b9f9f9"+
        "f9f9f9f9 f9f9f911 40c3f0f1 f91192c5"+
        "f0f0f1f0 f0f0f0f0 f5f9f7f8 1192e2f1"+
        "119101f0 f0f0f8");*/
        
     //COSNULTA TALONARIOS 2043 Fiduciaria
    /*         byte bodyRequestBytes[] =
     hexa2Bytes("4c534e303436" +   
     "434f5753 f0f0f0f0 f0f05649 42411140"+
     "40f2f0f4 f3d6c6c6 d4c3f0f7 f1f5f4f7"+ 
     "f6f0f0f0 f1f0f0f0 f1f1f3f5 f1f1e6f7"+
     "e2c9d6d9 f0f2f0f0 f0f0f0f0 f0f01191"+
     "49d6c6f0 f111c46d f2f0f1f3 f0f61191"+
     "4af1f1f3 f5f1f111 b8b8f911 b8b9f9f9"+
     "f9f9f9f9 f9f9f911 40c3f0f6 f41192c5"+
     "f0f0f1f0 f0f0f0f2 f3f7f2f5 1192e2f1"+
     "119101f0 f0f0f8");*/

     //DESINVERSION DESDE CANALES Fiduciaria SYDDAR
      /*  byte bodyRequestBytes[] =                

      hexa2Bytes("00f74c53 4e32 46 49 44 55 F0 F0 F0 F0 F0 F0 56 49 42 41 11 40"+ //   | ....000000..... 
                   "40 F2 F0 F1 F2 F2 F2 F4 F5 F7 F9 F9 F6 F2 F7 F9"+ //   |  201222457996279
                   "F7 F4 F0 F3 F1 F0 F0 F0 F1 F4 F1 F9 F2 F4 E6 F7"+ //   | 70020000141924W7
                   "E2 C9 C7 C4 F5 F4 F0 F0 F0 F0 F0 F0 F0 F0 11 C3"+ //   | SIGD5400000000.C
                   "F0 F0 F0 F0 11 40 C3 F0 F1 F9 11 E6 F5 11 E5 F1"+ //   | 0000. C060.W5.V1   !!***FONDO
                   "F0 11 C3 5A F0 F0 F0 F0 F0 F0 F0 F1 11 95 15 F0"+ //   | 0.C!00000001.n.0
                   "F0 F0 11 B8 B8 F9 11 B8 B9 F9 F9 F9 F9 F9 F9 F9"+ //   | 00...9..`9999999
                   "F9 F9 11 C3 F1 F0 F0 F0 F0 F0 F0 F0 F1 11 E4 F2"+ //   | 99.C100000001.U2
                   "F6 11 E4 F3 F1 F1 F1 F1 F5 F4 F2 F8 F4 F1 11"+ //   | 6.U399050614368.   01111542841
                   "91 16 F0 F0 F0 F3 F5 F5 F3 F6 F5 11 95 0B F0 F0"+ //   | j.000355365.n.00   001000026321 o 001000025830
                   "F1 F0 F0 F0 F0 F2 F5 F8 F3 F0 11 C6 D4 F2 F8 F1"+ //   | 1000005978.FM278   ****secuencia
                   "F0 11 C1 7A F4 F0 F0 F1 F4 F5 F5"+                //   | 84002354         *** VOLANTE *****
                   "11 40 C6 F0 11 91 A1 F1 11 93 A1 F0 11 91 01"+ //   | . F0.j~1.l~0.j.
                   "F0 F0 F0 F0 11 11");    //                               | 0000..
*/
       //DESINVERSION 2245 Fiduciaria SYDDAR, REVERSO HORARIO ADICIONAL
 /*      byte bodyRequestBytes[] =                
       hexa2Bytes("00f74c53 4e32"+ 
       "46 49 44 55 F0 F0 F0 F0 F0 F0 56 49 42 41 11 40"+ // | ....000000..... 
       "40 F2 F0 F1 F2 F2 F2 F4 F5 F7 F9 F9 F6 F2 F7 F9"+ // |  201222457996279
       "F7 F0 F0 F1 F3 F0 FC F0 F1 F8 F2 F9 F3 F2 F0 C2"+ // | 70013  0.0  1829320B     0.0   --> ESTADO
       "D6 D7 F0 F0 F3 F1 F0 F0 F0 F0 F0 F0 F0 F0 11 C1"+ // | OP003100000000.A
       "C5 F0 F0 F1 F0 11 C3 F0 F1 F7 F0 F0 F0 F0 F0 F0"+ // | E0010.C017000000
       "11 40 C3 F0 F1 F9 11 E6 F5 C5 11 E5 F1 F1 F8 F0"+ // | . C019.W5E.V1180
       "F1 F4 F2 11 C3 5A F0 F0 F0 11 95 15 F0 F0 F0 11"+ // | 142.C!000.n.000.
       "B8 B8 F9 11 B8 B9 F9 F9 F9 F9 F9 F9 F9 F9 F9 11"+ // | ..9..`999999999.
       "C3 7B F0 F0 F0 F9 F1 F9 F5 F3 F0 11 C3 F1 F1 F7"+ // | C#000919530.C117
       "F0 F0 F0 F0 F0 F0 11 E4 F2 F3 11 E4 F3 F1 F8 F0"+ // | 000000.U23.U3180
       "F1 F4 F2 11 91 16 F0 F0 F0 F3 F5 F5 F3 F6 F5 11"+ // | 142.j.000355365.
       "91 2D F2 11 95 0B F0 F0 F1 F0 F0 F0 F0 F2 F5 F7"+ // | j.2.n.0010000257
       "F8 F7 11 C6 D4 F0 F0 F1 F0 11 C1 7A F9 F9 F9 F8"+ // | 87.FM0010.A:9998
       "F8 F8 F4 11 40 C6 F1 11 91 A1 F1 11 93 A1 F0 11"+ // | 884. F1.j~1.l~0.
       "91 01 F0 F0 F0 F8 11 11"); //                         | j.0008..*/


/*
 * 
 /* INVERSION BBS                                             */ 

/*  byte bodyRequestBytes[] =

 hexa2Bytes( "0816264c 534e434f 5753 f0f0 f0f0f0f0 56494241 114040f2"+ //   ..<œ+ÛÛ
          "f0f1f4f0 f2f2f140 404040f1 f2f3f4f0"+ //   BFB0221    12340 
          "f0f0f0f8 f4f2f2f2 40404040 40404040"+ //   00084222         
          "f0f0f0f0 f0f0f0f0"    +  
         
           "11 b8b8f911 b8b9f9f9 f9f9f9f9 f9f9f9"+     // ..   
          "119149c2 c2f0f111"+ //   00000000.j‹BB01. 
          "c46df0f6 f1f2f1f3 1140c3f0 f6f21191"+ //   D_061213. C060.j 
          "2df011c3 f0f0f0f0 f0f0f0f0 f0f0f0f0"+ //   0.C00000000000
         "f0f1f0f0 11914af0 f8f4f2f2 f211c37b"+ //   0100.j›084222.C# 
          "f0f0f0f9 f1f9f3f6 f511e4 f3f1f1f0"+ //   0033961483.U3960 
          "f9f6f6f7 f4f5f811 e4f2d911 950bf0f0"+ //   5205202.U2R.n.01 
          "f1f0f0f0 f0f0f1f4 f4f61111 40404040"+ //   2563214521..     
           "40404040 40404040 40404040 40404040");                //  */


 

//COSNULTA PENALIZACION 2245 Fiduciaria SYDDAR
/*byte bodyRequestBytes[] =                

                hexa2Bytes("00f74c53 4e32"+ 
                "46 49 44 55 F0 F0 F0 F0 F0 F0 56 49 42 41 11 40"+ // | ....000000..... 
                "40 F2 F0 F1 F5 F2 F2 F4 F5 F7 F9 F9 F6 F2 F7 F9"+ // |  201522457996279
                "F7 F0 F0 F1 F5 F0 F0 F0 F1 F1 F5 F3 F4 F2 E6 F7"+ // | 70015000115342W7
                "E2 C9 C7 C4 F5 F4 F0 F0 F0 F0 F0 F0 F0 F0 11 C3"+ // | SIGD5400000000.C
                "F0 F0 F0 F0 11 40 C3 F0 F1 F9 11 E6 F5 11 E5 F1"+ // | 0000. C019.W5.V1
                "F0 11 C3 5A F5 F4 F0 F0 F0 F0 F0 11 95 15 F0 F0"+ // | 0.C!5400000.n.00
                "F0 11 B8 B8 F9 11 B8 B9 F9 F9 F9 F9 F9 F9 F9 F9"+ // | 0...9..`99999999
                "F9 11 C3 F1 F5 F4 F0 F0 F0 F0 F0 11 E4 F2 F3 11"+ // | 9.C15400000.U23.
                "E4 F3 F1 F8 F0 F1 F4 F2 11 91 16 F0 F0 F0 F3 F5"+ // | U3180142.j.00035
                "F5 F3 F6 F5 11 95 0B F0 F0 F1 F0 F0 F0 F0 F2 F5"+ // | 5365.n.001000025
                "F7 F8 F7 11 C6 D4 F6 F5 F3 F6 11 40 C6 F0 11 91"+ // | 787.FM0015. F0.j    ****secuencia  6536
                "A1 F1 11 93 A1 F0 11 91 01 F0 F0 F0 F8 11 11"); //    | ~1.l~0.j.0008..
*/

 //COSNULTA PENALIZACION 2245 Fiduciaria SYDDAR
/* byte bodyRequestBytes[] =                

                 hexa2Bytes(
 "4c53 4e323330 434f5753 f0f0f0f0"+ // |  .0<œ+€|ãœ0000
 "f0f05649 42411140 40f2f1f7 f9c2f6f0"+ // |   00™‹á¦.  2179B60
 "f0f9f7f6 f8f0f0f5 f7f0f0f0 f1f1f5f9"+ // |   0976800570001159
 "f4f8e6f7 e2c9c7c4 f0f5f0f0 f0f0f0f0"+ // |   48W7SIGD05000000
 "f0f01191 49d6c6f0 f111c46d f0f9f0f2"+ // |   00.j‹OF01.D_0902
 "f1f311c5 d3f21192 49f0f0f0 f0f0f0f0"+ // |   13.EL2.k‹0000000
 "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+ // |   0000000000000000
 "f0f0f0f0 f2f5f911 93c2f7f7 f0f0f1f0"+ // |   0000259.lB770010
 "f6f5f8f3 f7f3f511 c3f0f0f0 f0f0f0f0"+ // |   6583735.C0000000
 "f1f0f0f0 f0f0f0f0 f0119504 f2f0f1f4"+ // |   100000000.n.2014
 "f1f2f3f1 1192c7f0 f111c37b f0f1f0f6"+ // |   1231.kG01.C#0106
 "f5f8f3f7 f3f51191 01f0f2f7 f511c6d4"+ // |   583735.j.0275.FM
 "f0f0f5f7 119501c2 f6f0f0f9 f7f6f811"+ // |   0057.n.B6009768.
 "93b7f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"+ // |   l»00000000000000
 "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0"); // |   0000000000000000
*/

 //COSNULTA 2013 IVR
/*  byte bodyRequestBytes[] =

                 hexa2Bytes(
 "f0f0 f1f0f0f3 46494455 f0f0f0f0"+ // .X001003Ÿ‹ë¥0000
 "f0f0f2f0 f1f3f9f9 f9f9f9f9 f9f9f9f9"+ // 0020139999999999
 "4c6fa794 9340a585 99a28996 957e7ff1"+ // <?xml version=.1
 "4bf07f40 85958396 84899587 7e7fe4e3"+ // .0. encoding=.UT
 "c660f87f 6f6e4ce3 d9c1d5e2 c1c3c3c9"+ // F-8.?><TRANSACCI
 "d6d56e4c c8e3e7c3 c96ee5c9 c2c14c61"+ // ON><HTXCI>VIBA</
 "c8e3e7c3 c96e4cc8 e3e7e3e2 6ec9e5e3"+ // HTXCI><HTXTS>IVT
 "c54c61c8 e3e7e3e2 6e4cc8c9 c4c3d16e"+ // E</HTXTS><HIDCJ>
 "c9e5f0f0 f1f0f0f3 4c61c8c9 c4c3d16e"+ // IV001003</HIDCJ>
 "4cc8e2c5 c3e46ef0 f1f3f64c 61c8e2c5"+ // <HSECU>0136</HSE
 "c3e46e4c c8c2e8e2 e36ef0f0 f04c61c8"+ // CU><HBYST>000</H
 "c2e8e2e3 6e4cc8c8 d6d9c16e f1f1f1f2"+ // BYST><HHORA>1112
 "f5f74c61 c8c8d6d9 c16e4cc9 e3c5d96e"+ // 57</HHORA><ITER>
 "c9e5f0f1 4c61c9e3 c5d96e4c e3e7c6c3"+ // IV01</ITER><TXFC
 "6ef0f9f1 f2f1f34c 61e3e7c6 c36e4cc8"+ // >091213</TXFC><H
 "c8d6d9c1 6ef1f1f1 f2f5f74c 61c8c8d6"+ // HORA>111257</HHO
 "d9c16e4c e3c5e3c4 6ec6c24c 61e3c5e3"+ // RA><TETD>FB</TET
 "c46e4ce3 c5e3c36e c6c24c61 e3c5e3c3"+ // D><TETC>FB</TETC
 "6e4ce5d3 e3e76ef0 f0f0f0f0 f0f0f2f5"+ // ><VLTX>000000025
 "f6f1f2f8 f0f04c61 e5d3e3e7 6e4ce3c5"+ // 612800</VLTX><TE
 "d5c36ef0 f0f1f9f0 f0f1f0f0 f0f0f2f4"+ // NC>0019001000024
 "f9f6f64c 61e3c5d5 c36e4ce3 c5d5c46e"+ // 966</TENC><TEND>
 "f0f1f0f9 f0f0f1f0 f0f0f0f2 f2f9f8f6"+ // 0109001000022986
 "4c61e3c5 d5c46e4c d5d6c9c4 6e404040"+ // </TEND><NOID>   
 "f7f9f6f3 f5f4f1f2 4c61d5d6 c9c46e4c"+ // 79635412</NOID><
 "e3d7c9c4 6ec34c61 e3d7c9c4 6e4ce3c5"+ // TPID>C</TPID><TE
 "e3c26ef1 f0f94c61 e3c5e3c2 6e4cd5c5"+ // TB>109</TETB><NE
 "c4d76ef0 4c61d5c5 c4d76e4c d5c5c3d7"+ // DP>0</NEDP><NECP
 "6ef04c61 d5c5c3d7 6e4ce3c5 c3d96ef0"+ // >0</NECP><TECR>0
 "f1f94c61 e3c5c3d9 6e4c61e3 d9c1d5e2"+ // 19</TECR></TRANS
 "c1c3c3c9 d6d56e");                  //   ACCION>         
*/


        /*2165 * Pago Recaudos WS 
         *  VALOR: c3f0
         *  RECIBO: 9249
         *  NUMERO CUENTA: 4040
         * 
         * */        
        byte bodyRequestBytesPago[] = 
            hexa2Bytes("4c53 4e303036 e3e3c3c1 114040f2" + 
        "f1f6f5f1 f9f2f4f3 f6f7f1f0 f0f8f0f0" + 
        "f0f0f0f9 f5f0f1f9 c3c1d5c1 d3c5e2f1" + 
        "f0f0f0f0 f01193c1 f011c15b f0f0f011" +
        "911df011 9192f0f0 f011c3f0 f0f0f0f0" +
        "f0f1f9f0" + 
        "f0f01192 c7f0f211 4040f4f3 f4f6f2f9" +
        "f2f3f411 c3f5f0f0 f0119616 f2f1f6f0" +
        "119249f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f0f2f4f0 f0f0f4f5 f8f1f7f2" +
        "f81192e2 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f211 93b4f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 1193b5f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f093b6f0 f0f0f0f0 f0f0f0" +
        "f0f0f0f0 f0f0f0f0 f0f0f0f0 1193b7f1" +
        "f8f41193 b8f0f0f0 f0f0f0f0 f0f0f0f0" +
        "f0f0f0f0 f0f0f0f0 1193b9f0 f0f0f0f0" +
        "f0f0f0f0 f0f0f0f0 f0f0f0f0 f0f0f0f0" +
        "1193b0f0 1193baf0 f211c6d4 f0f0f0f8" +
        "f0119201" +
        "f1f8f411 c3f3f0f0 f0f011c3 f4f0f0f0" +
        "f0119105" +
        "f0f0f0f0 11c37bf0" +
        "f0f0f0f0 f0f0f0f0 11c3f2f0 f0f01193" +
        "c4f01140 d5d51191 01f0f0f0 1193a1f1" +
        "11b2c3f2 f0f0f8f1 f0f2f9f1 f7f3f1f1" +
        "f1f1f9f2 f1f6f1f0 f011a2d1 f0f0f0f0" +
        "f0f0f0f0 f1f1f0f0 f2f0f511 a2d2f111" +
        "a2d4f0f0 f0f3f4f3 f4f3f411 b2c3f0f1" +
        "f6f1f0f0");


        /* 0 con encabezado (2164)*/ 
        // byte TotalRequestBytes[] = null;  2200 0636 4115  4877833 cuenta 434629234
        int TotalRequestBytesLenght = 0;
        if (operacion.equalsIgnoreCase("consulta")) {
            TotalRequestBytesLenght = 
                    headerRequestBytes.length + bodyRequestBytes.length;
        } else {
            TotalRequestBytesLenght = 
                    headerRequestBytes.length + bodyRequestBytesPago.length;
        }

        byte TotalRequestBytes[] = new byte[TotalRequestBytesLenght];


        if (operacion.equalsIgnoreCase("consulta")) {
            System.arraycopy(headerRequestBytes, 0, TotalRequestBytes, 0, 
                             headerRequestBytes.length);
            System.arraycopy(bodyRequestBytes, 0, TotalRequestBytes, 
                             headerRequestBytes.length, 
                             bodyRequestBytes.length);

        } else {
            System.arraycopy(headerRequestBytes, 0, TotalRequestBytes, 0, 
                             headerRequestBytes.length);
            System.arraycopy(bodyRequestBytesPago, 0, TotalRequestBytes, 
                             headerRequestBytes.length, 
                             bodyRequestBytesPago.length);

        }

        /* Transacciones sin encabezado 2121, 2161*/
        /*byte TotalRequestBytes[] = new byte[bodyRequestBytes.length ];
        System.arraycopy(bodyRequestBytes,0,TotalRequestBytes,0,bodyRequestBytes.length);*/

        try {
            dos.writeShort((short)TotalRequestBytes.length + 
                           2); //(Comentario para Monetarias 2161)
            dos.write(TotalRequestBytes);
            dos.flush();

        } catch (Exception e) {
            System.out.println("Error= " + e);
        }
    }

    public void recibir() {
        try {
            int longResponse = ((0x0000FFFF & dis.readShort()) - 2);
            byte responseBytes[] = new byte[longResponse];
            dis.readFully(responseBytes);
            System.out.println("Respuesta:" + 
                               new String(responseBytes, "cp037"));
        } catch (Exception e) {
            System.out.println("Error= " + e);
        }


    }


    public void desconectar() {
        try {
            dis.close();
            dos.close();
            s.close();
            System.out.println("Desconectado:");
        } catch (Exception e) {
            System.out.println("Error= " + e);
        }


    }

    public static final byte[] hexa2Bytes(String strHexa) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < strHexa.length(); i += 2) {
            if (strHexa.charAt(i) == ' ') {
                i--;
                continue;
            }
            baos.write((0x00ff & 
                        Integer.parseInt(strHexa.substring(i, i + 2), 16)));
        }
        return baos.toByteArray();
    }
}

