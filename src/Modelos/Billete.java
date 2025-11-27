package Modelos;

public class Billete {

    private String codigo;
    private boolean disponible;

    public Billete(String codigo) {
        this.codigo = codigo;
        this.disponible = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        if (disponible) {
            return this.codigo + ": Disponible";
        } else {
            return this.codigo + ": Vendido";
        }
    }
}
