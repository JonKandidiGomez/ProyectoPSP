package Controlador;

import Modelos.Billete;
import Modelos.Usuario;

import java.util.ArrayList;
import java.util.List;

public class Controlador {

    private List<Billete> billetes = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();

    public Controlador() {
        generarBilletes();
    }

    private void generarBilletes() {
        for (int i = 1; i < 50; i++) {
            billetes.add(new Billete("P" + i));
        }
    }

    public void comprarBillete(String codigo) {

    }
}
