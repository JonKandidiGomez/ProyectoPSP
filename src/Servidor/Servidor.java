package Servidor;

import Controlador.Controlador;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class Servidor {
    public static void main(String[] args) {
        int puerto = 6000;
        System.setProperty("javax.net.ssl.keyStore", "./AlmacénSSL/AlmacenSSL.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "12345Abcde");
        SSLServerSocketFactory sfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try (SSLServerSocket servidorSSL = (SSLServerSocket) sfact.createServerSocket(puerto)) {
            SSLSocket c;

            KeyPair claves = generarClaves();
            PrivateKey clPriv = claves.getPrivate();
            Controlador controlador = new Controlador(clPriv);

            while (true) {
                c = (SSLSocket) servidorSSL.accept();
                Thread h = new Thread(new Hilo(c, claves, controlador));
                h.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error al generar claves: " + e);
        }
    }

    private static KeyPair generarClaves() throws NoSuchAlgorithmException {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();

    }
}
