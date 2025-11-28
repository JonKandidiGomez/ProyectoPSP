package Servidor;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Servidor {
    public static void main(String[] args) {
        int puerto = 6000;
        System.setProperty("javax.net.ssl.keyStore", "./AlmacénSSL/AlmacenSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");
        SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try (SSLServerSocket servidorSSL = (SSLServerSocket) sfact.createServerSocket(puerto)) {
            SSLSocket c;

            KeyPair claves = generarClaves();

            while (true) {
                c = (SSLSocket) servidorSSL.accept();
                assert claves != null;
                Thread h = new Thread(new Hilo(c, claves));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyPair generarClaves() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al generar el par de claves.");
        }
        return null;
    }
}
