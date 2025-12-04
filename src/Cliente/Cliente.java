package Cliente;

import Modelos.Usuario;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.*;
import java.sql.SQLOutput;

public class Cliente {
    public static void main(String[] args) {
        boolean logeado = false;

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

            //1. Se envía la clave publica del cliente al servidor
            oos.writeObject(publica);

            //2. Se recibe la clave pública del servidor
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

                //3. Se envía la opción elegida por el usuario
                oos.writeObject(op);

                switch (op) {
                    case "registrarme":
                        String nombre, apellido, edad, email, usr, pw;
                        byte[] nomCif = null, apeCif = null, edadCif = null, emailCif = null, usrCif = null, pwCif = null;

                        // Se piden los datos al usuario hasta que introduce unos válidos
                        do {
                            System.out.println("Introduce tu nombre: ");
                            nombre = br.readLine();
                            if (!validarNombre(nombre)) {
                                System.out.println("El nombre no puede contener caracteres que no sean letras.");
                                nombre = "";
                            } else {
                                nomCif = cifrar(nombre, claveServer);
                            }
                        } while (nombre.isEmpty());

                        do {
                            System.out.println("Introduce tu apellido: ");
                            apellido = br.readLine();
                            if (!validarApellido(apellido)) {
                                System.out.println("El apellido no puede contener caracteres que no sean letras.");
                                apellido = "";
                            } else {
                                apeCif = cifrar(nombre, claveServer);
                            }
                        } while (apellido.isEmpty());

                        do {
                            System.out.println("Introduce tu edad: ");
                            edad = br.readLine();
                            if (!validarEdad(edad)) {
                                System.out.println("La edad debe ser un numero entero");
                                edad = "";
                            } else {
                                edadCif = cifrar(edad, claveServer);
                            }
                        } while (edad.isEmpty());

                        do {
                            System.out.println("Introduce tu email: ");
                            email = br.readLine();
                            if (!validarEmail(email)) {
                                System.out.println("El email debe ser valido.");
                                email = "";
                            } else {
                                emailCif = cifrar(email, claveServer);
                            }
                        } while (email.isEmpty());

                        do {
                            System.out.println("Introduce un nombre de usuario");
                            usr = br.readLine();
                            if (!validarUsuario(usr)) {
                                System.out.println("El usuario debe tener entre 8 y 16 caracteres.");
                                usr = "";
                            } else {
                                usrCif = cifrar(usr, claveServer);
                            }
                        } while (usr.isEmpty());

                        do {
                            System.out.println("Introduce tu contraseña: ");
                            pw = br.readLine();
                            if (!validarContraseña(pw)) {
                                System.out.println("La contraseña debe tener entre 8 y 16 caracteres.");
                                pw = "";
                            } else {
                                pwCif = cifrar(pw, claveServer);
                            }
                        } while (pw.isEmpty());

                        //4.1 Se envían los datos del usuario
                        oos.writeObject(nomCif);
                        oos.writeObject(apeCif);
                        oos.writeObject(edadCif);
                        oos.writeObject(emailCif);
                        oos.writeObject(usrCif);
                        oos.writeObject(pwCif);

                        //4.2 Se recibe mensaje de confirmación
                        byte[] respuesta = (byte[]) ois.readObject();
                        String res = descifrar(respuesta, privada);
                        System.out.println(res);
                        break;

                    case "login":
                        System.out.println("Nombre de usuario: ");
                        String usuario = br.readLine();
                        System.out.println("Contraseña: ");
                        String pswd = br.readLine();

                        byte[] usCif = cifrar(usuario, claveServer);
                        byte[] pwdCif = cifrar(pswd, claveServer);

                        //5.1 Se envían las credenciales al servidor
                        oos.writeObject(usCif);
                        oos.writeObject(pwdCif);

                        //5.2 Se recibe la respuesta del login
                        boolean resLogin = ois.readBoolean();
                        if (resLogin) {
                            System.out.println("Te has logeado con exito!");
                            logeado = true;
                        } else {
                            System.out.println("Usuario o contraseña incorrectos.");
                        }
                        break;
                    case "comprar billetes":
                        //6.1 Se recibe el listado de billetes del cliente
                        byte[] listaCif = (byte[]) ois.readObject();
                        String lista = descifrar(listaCif, privada);
                        System.out.println("Elige un billete" + lista);

                        String bill = br.readLine().toUpperCase();
                        byte[] billCif = cifrar(bill, claveServer);
                        //Firma digital
                        Signature dsa = Signature.getInstance("SHA256withDSA");


                        //6.2 Se envía la opción elegida al servidor
                        oos.writeObject(billCif);
                        //6.3 Se recibe la respuesta del servidor
                        byte[] respCompra = (byte[]) ois.readObject();
                        String resp = descifrar(respCompra, privada);
                        System.out.println(resp);
                    case "salir":
                        System.out.println("Cerrando aplicacion...");
                        break;
                    default:
                        System.out.println("No has introducido una opción válida.");
                }
            } while (!op.equals("salir"));
            ois.close();
            oos.close();
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

    private static boolean validarNombre(String nombre) {
        return nombre.matches("^[A-Za-z]+$");
    }

    private static boolean validarApellido(String apellido) {
        return apellido.matches("^[A-Za-z]+$");
    }

    private static boolean validarEdad(String edad) {
        return edad.matches("^[0-9]{1,3}$");
    }

    private static boolean validarEmail(String email) {
        return email.matches("^.+@.+[.].+$");
    }

    private static boolean validarUsuario(String usuario) {
        return usuario.matches("^[.]{8,17}$");
    }

    private static boolean validarContraseña(String contraseña) {
        return contraseña.matches("^[.]{8,17}$");
    }
}
