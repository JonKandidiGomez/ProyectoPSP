package Servidor;

import Controlador.Controlador;
import Modelos.Usuario;

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
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final PublicKey clavePublica;
    private final PrivateKey clavePrivada;
    private final Controlador controlador;

    public Hilo(SSLSocket sCliente, KeyPair claves) throws IOException {
        this.sCliente = sCliente;
        this.ois = new ObjectInputStream(sCliente.getInputStream());
        this.oos = new ObjectOutputStream(sCliente.getOutputStream());
        this.clavePublica = claves.getPublic();
        this.clavePrivada = claves.getPrivate();
        this.controlador = new Controlador(clavePrivada);
    }

    @Override
    public void run() {
        try {
            //1. Se recibe la clave publica del cliente
            PublicKey claveCliente = (PublicKey) ois.readObject();
            //2. Se envía la clave publica del servidor al cliente
            oos.writeObject(clavePublica);
            //3. Se recibe la opcion elegida por el usuario
            String op = (String) ois.readObject();

            switch (op) {
                case "registrarme":
                    //4.1 Se reciben los datos del usuario
                    String nombre = descifrar((byte[]) ois.readObject(), clavePrivada);
                    String apell = descifrar((byte[]) ois.readObject(), clavePrivada);
                    String edad = descifrar((byte[]) ois.readObject(), clavePrivada);
                    String email = descifrar((byte[]) ois.readObject(), clavePrivada);
                    String usr = descifrar((byte[]) ois.readObject(), clavePrivada);
                    byte[] pw = (byte[]) ois.readObject();

                    Usuario u = new Usuario(nombre, apell, edad, email, usr, pw);
                    String mensaje = "";
                    if (controlador.guardarUsuario(u)) {
                        mensaje = "Usuario registrado";
                    } else {
                        mensaje = "Ya existe un usuario con ese nombre";
                    }

                    byte[] mnsCif = cifrar(mensaje, claveCliente);

                     //4.2 Se envia mensaje de confirmación
                    oos.writeObject(mnsCif);
                    break;

                case "login":
                    //5.1
                    byte[] usrLogin = (byte[]) ois.readObject();
                    byte[] pwLogin = (byte[]) ois.readObject();


            }


        } catch (IOException e) {
            System.out.println("Error de E/S");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static String descifrar(byte[] msg, PrivateKey clave) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clave);
            return new String(cipher.doFinal(msg));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error al descifrar datos: " + e);
        }
        return null;
    }

    public static byte[] cifrar(String msg, PublicKey clave) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        return cipher.doFinal(msg.getBytes());
    }
}
