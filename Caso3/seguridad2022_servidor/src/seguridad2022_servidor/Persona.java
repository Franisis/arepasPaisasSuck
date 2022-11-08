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
import java.util.concurrent.CyclicBarrier;

/* Esta clase contiene toda la lógica y métodos
 * necesarios para generar e intercambiar llaves 
 * references :
 * https://yoandroide.xyz/intercambio-de-claves-con-diffie-hellman-en-java/*/
public class Persona extends Thread {
	

	public static final int PUERTO = 4300;
	public static final String SERVIDOR = "localhost";
    private int idcliente;
    
    private PublicKey puk;
    private PublicKey receivedPublicKey;
    
    private long IcifrarConsulta;
    private long FcifrarConsulta;
    private long TcifrarConsulta;
    private long IgenerarAut;
    private long FgenerarAut;
    private long TgenerarAut;
    private long IverificaFirma;
    private long FverificaFirma;
    private long TverificarFirma;
    private long IcalG;
    private long FcalG;
    private long TcalG;
    
    private SecurityFunctions sf;
    private CyclicBarrier cb;
    

    public Persona(int i,CyclicBarrier barrera) {
        this.idcliente = i;
        cb = barrera;
    }

    public PublicKey getPublicKey()
    {
        return puk;
    }

    

    
    public void run() 
    {
    	
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
    	
        try {
    	protocoloCliente(lector, escritor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    	
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
        imprimirTiempos();
    }
    
    
    public void protocoloCliente(BufferedReader lector,PrintWriter escritor) throws Exception {
    	receivedPublicKey = sf.read_kplus("datos_asim_srv.pub","concurrent server " + idcliente + ": ");
    	//Paso 1
        escritor.println("SECURE INIT");
    	
        

    	//Paso 4
        String fs1 = lector.readLine();
        int g = Integer.parseInt(fs1);
        String fs2 = lector.readLine();
        int p = Integer.parseInt(fs2);
        String fs3 = lector.readLine();
        int gx = Integer.parseInt(fs3);

        //Esto es la firma(?)
        String esto = lector.readLine();
        byte[] firma = str2byte(esto);
        //Paso 5
        //verificacion de la firma
        IverificaFirma = System.currentTimeMillis();
        boolean verificacion1 = sf.checkSignature(receivedPublicKey, firma, esto);
        FverificaFirma = System.currentTimeMillis();
        TverificarFirma = FverificaFirma-IverificaFirma;
        

        if (verificacion1)
        {
            escritor.println("OK");

        }else{
            escritor.println("ERROR");
        }
        
        
    
    	//Paso 6a
    	String biCalculado = G2X(BigInteger.valueOf(g),BigInteger.valueOf(idcliente),BigInteger.valueOf(p)).toString();
        escritor.println(biCalculado);

        //Paso 7a 
        //calcular G de y
        IcalG = System.currentTimeMillis();
        String llaveComun = G2X(BigInteger.valueOf(gx), BigInteger.valueOf(idcliente), BigInteger.valueOf(p)).toString();
        FcalG = System.currentTimeMillis();
        TcalG = FcalG - IcalG;

        SecretKey sk_srv = sf.csk1(llaveComun);
        SecretKey sk_mac = sf.csk2(llaveComun);
        
        byte[] iv2 = generateIvBytes();
        String str_iv2 = byte2str(iv2);
        IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);

        //Paso 8
        byte[] num = Integer.toString(idcliente).getBytes();

        //cifrar consulta
        IcifrarConsulta = System.currentTimeMillis();
        byte[] consulta = sf.senc(num, sk_mac, ivSpec2, Integer.toString(idcliente));
        FcifrarConsulta = System.currentTimeMillis();
        TcifrarConsulta = FcifrarConsulta - IcifrarConsulta;
        
        //codigo de autenticacion
        IgenerarAut = System.currentTimeMillis();
        byte[] mac = sf.hmac(num, sk_mac);
        FgenerarAut = System.currentTimeMillis();
        TgenerarAut = FgenerarAut - IgenerarAut;

        String consult = byte2str(consulta);
        String hmac = byte2str(mac);

        escritor.println(consult);
        escritor.println(hmac);
        escritor.println(str_iv2);
        
        
        //Paso 12  ??????? como decifro ayuda
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
        //Paso 13
        if (verificar)
        {
            escritor.println("OK");
        } else 
        {
            escritor.println("ERROR");
        }


        cb.await();
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

   public void imprimirTiempos(){
    int id = this.idcliente;
    System.out.println("El tiempo de cifrado de la consulta para el cliente de id: "+ id +"es: "+ TcifrarConsulta);
    System.out.println("El tiempo para generar el codigo de autenticacion para el cliente de id: "+ id +"es: "+ TgenerarAut);
    System.out.println("El tiempo para la verificacion de la firma para el cliente de id: "+ id +"es: "+ TverificarFirma);
    System.out.println("El tiempo para calcular G^y para el cliente de id: "+ id +"es: "+ TcalG);

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
