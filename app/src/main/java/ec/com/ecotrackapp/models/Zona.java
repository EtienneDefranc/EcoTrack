package ec.com.ecotrackapp.models;

import java.io.Serializable;

/**
 * Clase que representa una zona urbana con su utilidad ambiental
 */
public class Zona implements Serializable, Comparable<Zona> {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private double pesoRecolectado;
    private double pesoPendiente;
    private int cantidadResiduos;
    private int umbralResiduos = 5; // a partir de 5 residuos acumulados se envia una notificacion
    public Zona(String nombre) {
        this.nombre = nombre;
        this.pesoRecolectado = 0;
        this.pesoPendiente = 0;
        this.cantidadResiduos = 0;
    }

    public Zona(String nombre, int umbralResiduos) {
        this.nombre = nombre;
        this.umbralResiduos = umbralResiduos;
        this.pesoRecolectado = 0;
        this.pesoPendiente = 0;
        this.cantidadResiduos = 0;
    }
    public double calcularUtilidad() {
        return pesoRecolectado - pesoPendiente;
    }

    public void agregarResiduoPendiente(double peso) {
        this.pesoPendiente += peso;
        this.cantidadResiduos++;
    }

    public void procesarResiduo(double peso) {
        if (pesoPendiente >= peso) {
            this.pesoPendiente -= peso;
            this.pesoRecolectado += peso;
        }
    }

    public boolean esCritica() {
        return calcularUtilidad() < 0;
    }

    public boolean superaUmbral() {
        return cantidadResiduos >= umbralResiduos;
    }

    public int getNivelPrioridad() {
        double utilidad = calcularUtilidad();
        if (utilidad < -50) return 10;
        if (utilidad < -20) return 8;
        if (utilidad < -10) return 6;
        if (utilidad < 0) return 4;
        if (utilidad < 10) return 2;
        return 1;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPesoRecolectado() {
        return pesoRecolectado;
    }

    public void setPesoRecolectado(double pesoRecolectado) {
        this.pesoRecolectado = pesoRecolectado;
    }

    public double getPesoPendiente() {
        return pesoPendiente;
    }

    public void setPesoPendiente(double pesoPendiente) {
        this.pesoPendiente = pesoPendiente;
    }

    public int getCantidadResiduos() {
        return cantidadResiduos;
    }

    public void setCantidadResiduos(int cantidadResiduos) {
        this.cantidadResiduos = cantidadResiduos;
    }

    public int getUmbralResiduos() { return umbralResiduos; }
    public void setUmbralResiduos(int umbralResiduos) { this.umbralResiduos = umbralResiduos; }


    @Override
    public int compareTo(Zona otra) {
        return Double.compare(this.calcularUtilidad(), otra.calcularUtilidad());
    }

    @Override
    public String toString() {
        return String.format("Zona: %s | Utilidad: %.2f | Recolectado: %.2f kg | Pendiente: %.2f kg | Residuos: %d",
                nombre, calcularUtilidad(), pesoRecolectado, pesoPendiente, cantidadResiduos);
    }
}

