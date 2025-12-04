package Controlador;

import Modelos.Billete;
import Modelos.Usuario;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class Controlador {

    private List<Billete> billetes = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private PrivateKey claveServer;

    public Controlador(PrivateKey clave) {
        this.claveServer = clave;
        generarBilletes();
    }

    private void generarBilletes() {
        for (int i = 1; i < 50; i++) {
            billetes.add(new Billete("P" + i));
        }
    }

    public void comprarBillete(String codigo) {

    }

    public boolean guardarUsuario(Usuario usuario) {
        if (usuarioExiste(usuario)) {
            return false;
        } else {
            usuarios.add(usuario);
            return true;
        }
    }

    private boolean usuarioExiste(Usuario usuario) {
        boolean res = false;
        for (Usuario u : usuarios) {
            if (u.getUsuario().equals(usuario.getUsuario())) {
                res = true;
                break;
            }
        }
        return res;
    }

    private String descifrar(byte[] msg, PrivateKey clave) {
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
}
