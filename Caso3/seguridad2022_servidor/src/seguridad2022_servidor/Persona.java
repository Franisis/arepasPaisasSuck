package seguridad2022_servidor;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

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
    private byte[] secretKey;
    private String secretM;
    private  byte[] encriptMensaje;

    

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

    public void encriptMensaje(final String message)
    {
        try {
            final SecretKeySpec keySpec = new SecretKeySpec(secretKey, "DES");
            final Cipher        cipher  = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            final byte[] encryptedMessage = cipher.doFinal(message.getBytes());
            
            encriptMensaje = encryptedMessage;
            //persona.receiveAndDecryptMessage(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveAndDecryptMessage(final byte[] encryptedMessage)
    {
        try {
            final SecretKeySpec keySpec = new SecretKeySpec(secretKey, "DES");
            final Cipher        cipher  = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            secretM = new String(cipher.doFinal(encryptedMessage));
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
            fromserver = lector.readLine();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            String fs1 = lector.readLine();
            int g = Integer.valueOf(fs1);
            String fs2 = lector.readLine();
            int p = Integer.valueOf(fs2);
            String fs3 = lector.readLine();
            int gx = Integer.valueOf(fs3);
    	
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    	
        

    	int p = Integer.valueOf(fromserver);

        int gx = Integer.valueOf(fromserver);
    	
    	
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
    
    
    public void protocoloCliente(BufferedReader oIn,PrintWriter pOut) {
    	int entrada = 0;
    	boolean ejecutando = true;
    	pOut.println("SECURE INIT");
    	
    	
    	//paso 2
    	
    	//entrada = BufferedReader.read();
    	int G = Integer.valueOf(entrada);
    	
    	
    	
    	
    	
    }

}
