package Controlador;

import Modelos.Billete;
import Modelos.Usuario;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class Controlador {

    private final List<Billete> billetes = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final PrivateKey claveServer;

    public Controlador(PrivateKey clave) {
        this.claveServer = clave;
        generarBilletes();
    }

    private void generarBilletes() {
        for (int i = 1; i < 50; i++) {
            billetes.add(new Billete("P" + i));
        }
    }

    public synchronized String mostrarBilletes() {
        String listado = "";
        for (Billete b : billetes) {
            if (b.isDisponible()) {
                listado = listado.concat(b.getCodigo() + "\n");
            }
        }
        return listado;
    }

    public synchronized boolean comprarBillete(String codigo) {
        for (Billete b : billetes) {
            if (b.getCodigo().equals(codigo)) {
                if (b.isDisponible()) {
                    b.setDisponible(false);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public synchronized boolean guardarUsuario(Usuario usuario) {
        if (usuarioExiste(usuario.getUsuario())) {
            return false;
        } else {
            usuarios.add(usuario);
            return true;
        }
    }

    private boolean usuarioExiste(String usuario) {
        boolean res = false;
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuario)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public synchronized boolean logearUsuario(byte[] usuario, byte[] contraseña) {
        boolean res = false;
        String usr = descifrar(usuario, claveServer);
        String pwDec = descifrar(contraseña, claveServer);
        byte[] pwHash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pwDec.getBytes());
            pwHash = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (usuarioExiste(usr)) {
            for (Usuario u : usuarios) {
                if (u.getUsuario().equals(usr)) {
                    System.out.println("Usuario encontrado");
                    return toHexadecimal(pwHash).equals(toHexadecimal(u.getContraseña()));
                }
            }
        }
        return res;
    }

    private String descifrar(byte[] msg, PrivateKey clave) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, clave);
            return new String(cipher.doFinal(msg));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException |
                 BadPaddingException e) {
            System.out.println("Error al descifrar datos: " + e);
        }
        return null;
    }

    private static String toHexadecimal(byte[] hash) {
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(b & 0xFF);
            if (h.length() == 1) hex.append("0");
            hex.append(h);
        }
        return hex.toString().toUpperCase();
    }
}
