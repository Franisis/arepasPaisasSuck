package seguridad2022_servidor;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

/* Esta clase contiene toda la lógica y métodos
 * necesarios para generar e intercambiar llaves 
 * references :
 * https://yoandroide.xyz/intercambio-de-claves-con-diffie-hellman-en-java/*/
public class Persona extends Thread {

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

    }

}
