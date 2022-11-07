package seguridad2022_servidor;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

/* Esta clase contiene toda la lógica y métodos
 * necesarios para generar e intercambiar llaves 
 * references :
 * https://yoandroide.xyz/intercambio-de-claves-con-diffie-hellman-en-java/*/
public class Persona extends Thread {
	

	public static final int PUERTO = 3400;
	public static final String SERVIDOR = "localhost";
    private int idcliente;
    private PrivateKey prk;
    private PublicKey puk;
    private PublicKey receivedPublicKey;
    
    private String secretM;
    private  byte[] encriptMensaje;
    private SecurityFunctions sf;

    

    

    public Persona(int i) {
        this.idcliente = i;
    }

    public PublicKey getPublicKey()
    {
        return puk;
    }

    public void generateKeys()
    {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(1024);

            final KeyPair keyPair = keyPairGenerator.generateKeyPair();

            prk = keyPair.getPrivate();
            puk  = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

    
    public void run() 
    {
    	generateKeys();
    	Socket socket = null;
    	PrintWriter escritor = null;
    	BufferedReader lector = null;
    	
    	
    	try {
    		socket = new Socket(SERVIDOR,PUERTO);
    		escritor = new PrintWriter(socket.getOutputStream(),true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	//protocolo
    	String fromserver = "";
    	//paso 1
    	
    	escritor.println("SECURE INIT");
    	
    	
    	//paso 2
        try {
    	protocoloCliente(lector, escritor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //paso ????
    	SecretKey llavefirma  ;
        SecretKey llaveCifrado  ;
        
        //SecretKey sk_srv = f.csk1(str_llave);
		//SecretKey sk_mac = f.csk2(str_llave);
			
        byte[] iv2 = generateIvBytes();
        String str_iv2 = byte2str(iv2);

    	
    	//fin protocolo
        escritor.close();
    	try {
            lector.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    public void protocoloCliente(BufferedReader lector,PrintWriter escritor) throws Exception {
    	receivedPublicKey = sf.read_kplus("datos_asim_srv.pub","concurrent server " + idcliente + ": ");
    	escritor.println("SECURE INIT");
    	
        

    	//paso 2
        String fs1 = lector.readLine();
        int g = Integer.parseInt(fs1);
        String fs2 = lector.readLine();
        int p = Integer.parseInt(fs2);
        String fs3 = lector.readLine();
        int gx = Integer.parseInt(fs3);

        //Esto es la firma(?)
        String esto = lector.readLine();
        byte[] firma = str2byte(esto);
        if (sf.checkSignature(receivedPublicKey, firma, esto))
        {
            escritor.println("OK");

        }else{
            escritor.println("ERROR");
        }
        
        
        
       
    
    	
    	//entrada = BufferedReader.read();
        String biCalculado = G2X(BigInteger.valueOf(g),BigInteger.valueOf(idcliente),BigInteger.valueOf(p)).toString();
        escritor.println(biCalculado);

        //paso 3 disque llave maestra
        String llaveComun = G2X(BigInteger.valueOf(gx), BigInteger.valueOf(idcliente), BigInteger.valueOf(p)).toString();

        SecretKey sk_srv = sf.csk1(llaveComun);
        SecretKey sk_mac = sf.csk2(llaveComun);
        
        byte[] iv2 = generateIvBytes();
        String str_iv2 = byte2str(iv2);
        IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);

        //Esto es lo que tenemos que cirfrar
        byte[] num = Integer.toString(idcliente).getBytes();

        byte[] consulta = sf.senc(num, sk_mac, ivSpec2, Integer.toString(idcliente));

        //achemak
        byte[] mac = sf.hmac(num, sk_mac);

        String consult = byte2str(consulta);
        String hmac = byte2str(mac);

        escritor.println(consult);
        escritor.println(hmac);
        escritor.println(str_iv2);
        
        
        //paso 12  ??????? como decifro ayuda
        String verificacion =lector.readLine();
        String reConsulta = lector.readLine();
        String respAcheMak = lector.readLine();
        String ivRecibido = lector.readLine();
        
        byte[] byteRecibidoConsulta = str2byte(reConsulta);
        byte[] byteRecibidoAchemak = str2byte(respAcheMak);
        byte[] byteRecibidoivRecibido = str2byte(ivRecibido);
        IvParameterSpec ivSpec1 = new IvParameterSpec(byteRecibidoivRecibido);
        byte[] decifrado = sf.sdec(byteRecibidoConsulta, sk_mac, ivSpec1);
        boolean verificar = sf.checkInt( decifrado,sk_mac,byteRecibidoAchemak);
        if (verificar)
        {
            escritor.println("Hubo ok \n");
        } else 
        {
            escritor.println("Hubo ERROR");
        }


    }

    
    private BigInteger G2X(BigInteger base, BigInteger exponente, BigInteger modulo) {
        return base.modPow(exponente,modulo);
    }

    public byte[] str2byte( String ss)
	{	
		// Encapsulamiento con hexadecimales
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}

   

    public String byte2str( byte[] b )
	{	
		// Encapsulamiento con hexadecimales
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}
    private byte[] generateIvBytes() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return iv;
	}

}
