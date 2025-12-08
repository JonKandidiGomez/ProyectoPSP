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

    private final SSLSocket sCliente;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final PublicKey clavePublica;
    private final PrivateKey clavePrivada;
    private final Controlador controlador;
    private String usuarioLogeado = null;

    public Hilo(SSLSocket sCliente, KeyPair claves, Controlador control) throws IOException {
        this.sCliente = sCliente;
        this.ois = new ObjectInputStream(sCliente.getInputStream());
        this.oos = new ObjectOutputStream(sCliente.getOutputStream());
        this.clavePublica = claves.getPublic();
        this.clavePrivada = claves.getPrivate();
        this.controlador = control;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " conectado.");
            //1. Se recibe la clave publica del cliente
            PublicKey claveCliente = (PublicKey) ois.readObject();
            //2. Se envía la clave publica del servidor al cliente
            oos.writeObject(clavePublica);
            String op;
            do {
                //3. Se recibe la opción elegida por el usuario
                op = (String) ois.readObject();
                System.out.println(Thread.currentThread().getName() + " intenta " + op);
                switch (op) {
                    case "registrarme":
                        //4.1 Se reciben los datos del usuario
                        System.out.println(Thread.currentThread().getName() + " intenta mandar sus datos.");
                        String nombre = descifrar((byte[]) ois.readObject(), clavePrivada);
                        String apell = descifrar((byte[]) ois.readObject(), clavePrivada);
                        String edad = descifrar((byte[]) ois.readObject(), clavePrivada);
                        String email = descifrar((byte[]) ois.readObject(), clavePrivada);
                        String usr = descifrar((byte[]) ois.readObject(), clavePrivada);
                        byte[] pw = (byte[]) ois.readObject();
                        String pwDec = descifrar(pw, clavePrivada);

                        //Hasheo la contraseña para guardarla
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(pwDec.getBytes());
                        byte[] pwHash = md.digest();

                        System.out.println(Thread.currentThread().getName() + " envió de datos completado.");

                        Usuario u = new Usuario(nombre, apell, edad, email, usr, pwHash);
                        String mensaje;
                        if (controlador.guardarUsuario(u)) {
                            mensaje = "Usuario registrado";
                            System.out.println("Usuario " + Thread.currentThread().getName() + " registrado.");
                        } else {
                            mensaje = "Ya existe un usuario con ese nombre";
                            System.out.println(Thread.currentThread().getName() + " ha intentado registrarse con un usuario que ya existe.");
                        }

                        byte[] mnsCif = cifrar(mensaje, claveCliente);

                        //4.2 Se envia mensaje de confirmación
                        oos.writeObject(mnsCif);
                        System.out.println("Confirmación de registro enviada a " + Thread.currentThread().getName());
                        break;

                    case "login":
                        System.out.println(Thread.currentThread().getName() + " intenta logearse.");
                        //5.1 Se reciben las credenciales del cliente
                        byte[] usrLogin = (byte[]) ois.readObject();
                        byte[] pwLogin = (byte[]) ois.readObject();

                        String usuarioDec = descifrar(usrLogin, clavePrivada);
                        boolean respuesta = controlador.logearUsuario(usrLogin, pwLogin);

                        if (respuesta) {
                            this.usuarioLogeado = usuarioDec;
                        }

                        //5.2 Se envía la respuesta del login
                        oos.writeObject(respuesta);
                        System.out.println("Confirmación de inicio de sesión enviada a " + Thread.currentThread().getName());
                        break;

                    case "comprar billetes":
                        byte[] listado = cifrar(controlador.mostrarBilletes(), claveCliente);
                        //6.1 Se envía listado de billetes al cliente
                        oos.writeObject(listado);
                        //6.2 Se recibe la opcion elegida del cliente
                        byte[] billCif = (byte[]) ois.readObject();
                        String bill = descifrar(billCif, clavePrivada);

                        Signature verificar = Signature.getInstance("SHA256withRSA");
                        verificar.initVerify(claveCliente);
                        if (bill != null) {
                            verificar.update(bill.getBytes());
                        }

                        //6.3 Se recibe y se comprueba la firma digital del cliente
                        byte[] firmaCompra = (byte[]) ois.readObject();
                        boolean check = verificar.verify(firmaCompra);

                        String mensajeRespuesta = "Ha ocurrido algún error durante el proceso de compra";
                        if (check) {
                            if (usuarioLogeado != null) {
                                boolean compraOk = controlador.comprarBillete(bill, usuarioLogeado);
                                if (compraOk) {
                                    mensajeRespuesta = "¡Compra realizada con éxito! Billete: " + bill;
                                } else {
                                    mensajeRespuesta = "Error: El billete ya no está disponible o no existe.";
                                }
                            } else {
                                mensajeRespuesta = "Error: Usuario no encontrado";
                            }
                        }
                        byte[] respCompra = cifrar(mensajeRespuesta, claveCliente);
                        //6.4 Se envia la respuesta al cliente
                        oos.writeObject(respCompra);
                        break;

                    case "salir":
                        System.out.println("Usuario " + Thread.currentThread().getName() + " desconectado.");
                        break;
                }
            } while (!op.equals("salir"));
            ois.close();
            oos.close();
            sCliente.close();
        } catch (IOException e) {
            System.out.println("Error de E/S: " + e);
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

    public static byte[] cifrar(String msg, PublicKey clave) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, clave);
            return cipher.doFinal(msg.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error al cifrar datos: " + e);
        }
        return null;
    }
}
