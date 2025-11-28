package Cliente;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.*;

public class Cliente {
    public static void main(String[] args) {
        //Lector de datos desde consola
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        //conexión segura con el servidor
        String host = "localhost";
        int puerto = 6000;
        System.setProperty("javax.net.ssl.trustStore", "./AlmacénSSL/UsuarioAlmacenSSL");
        System.setProperty("javax.net.ssl.trustStorePassword", "Abcde12345");
        SSLSocketFactory sfact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket cliente;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        try {
            cliente = (SSLSocket) sfact.createSocket(host, puerto);
            oos = new ObjectOutputStream(cliente.getOutputStream());
            ois = new ObjectInputStream(cliente.getInputStream());

            //Generación de claves para intercambio de mensajes con el servidor (cifrado asimétrico)
            KeyPair claves = generarClaves();
            assert claves != null;
            PublicKey publica = claves.getPublic();
            PrivateKey privada = claves.getPrivate();

            //Se envía la clave publica del cliente al servidor
            oos.writeObject(publica);

            //Se recibe la clave pública del servidor
            PublicKey claveServer = (PublicKey) ois.readObject();

            String op;
            do {
                System.out.println("""
                        Elige una opción:
                        - Registrarme
                        - Login
                        - Comprar billetes
                        - Salir""");
                op = br.readLine().toLowerCase();

                byte[] mensaje = cifrar(op, claveServer);

                switch (op) {
                    case "registrarme":
                        oos.writeObject(mensaje);
                        String nombre;
                        String apellido;
                        int edad;
                        String email;
                        String usr;
                        String pw;

                        do {
                            System.out.println("Introduce tu nombre: ");
                            nombre = br.readLine();
                        } while (nombre.isEmpty());

                        do {
                            System.out.println("Introduce tu apellido: ");
                            apellido = br.readLine();
                        } while (apellido.isEmpty());

                        do {
                            System.out.println("Introduce tu edad (un numero entero): ");
                            try {
                                edad = Integer.parseInt(br.readLine());
                            } catch (NumberFormatException | IOException e) {
                                edad = -1;
                            }
                        } while (edad == -1);

                        do {
                            System.out.println("Introduce tu email: ");
                            email = br.readLine();
                        } while (email.isEmpty());

                        do {
                            System.out.println("Introduce un nombre de usuario");
                            usr = br.readLine();
                        } while (usr.isEmpty());

                }


            } while (!op.equals("salir"));


            cliente.close();
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e);
        } catch (ClassNotFoundException e) {
            System.out.println("Error, clase no encontrada: " + e);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    private static KeyPair generarClaves() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al generar el par de claves.");
            return null;
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

    private static byte[] cifrar(String msg, PublicKey clave) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, clave);
        return cipher.doFinal(msg.getBytes());
    }

    private String validarNombre(String nombre) {

    }
}
