package Servidor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.*;

public class Hilo implements Runnable {

    private SSLSocket sCliente;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private PublicKey clavePublica;
    private PrivateKey clavePrivada;

    public Hilo(SSLSocket sCliente, KeyPair claves) throws IOException {
        this.sCliente = sCliente;
        this.ois = new ObjectInputStream(sCliente.getInputStream());
        this.oos = new ObjectOutputStream(sCliente.getOutputStream());
        this.clavePublica = claves.getPublic();
        this.clavePrivada = claves.getPrivate();
    }

    @Override
    public void run() {
        try {
            PublicKey claveCliente = (PublicKey) ois.readObject();
            oos.writeObject(clavePublica);

            byte[] mensaje = (byte[]) ois.readObject();
            String op = descifrar(mensaje, clavePrivada);

            switch (op) {
                case "registrarme":

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String descifrar(byte[] msg, PrivateKey clave) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clave);
            return new String(cipher.doFinal(msg));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] cifrar(String msg, PublicKey clave) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        return cipher.doFinal(msg.getBytes());
    }
}
