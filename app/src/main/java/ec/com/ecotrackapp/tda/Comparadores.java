package ec.com.ecotrackapp.tda;

import ec.com.ecotrackapp.models.Residuo;
import ec.com.ecotrackapp.tda.Comparator;

/**
 * Clase que contiene los comparadores para ordenar residuos
 */
public class Comparadores {

    /**
     * Comparador por peso (ascendente)
     */
    public static class ComparadorPorPeso implements Comparator<Residuo> {
        @Override
        public int compare(Residuo r1, Residuo r2) {
            return Double.compare(r1.getPeso(), r2.getPeso());
        }
    }

    /**
     * Comparador por tipo (alfabético)
     */
    public static class ComparadorPorTipo implements Comparator<Residuo> {
        @Override
        public int compare(Residuo r1, Residuo r2) {
            return r1.getTipo().getNombre().compareTo(r2.getTipo().getNombre());
        }
    }

    /**
     * Comparador por prioridad ambiental (descendente - mayor prioridad primero)
     */
    public static class ComparadorPorPrioridad implements Comparator<Residuo> {
        @Override
        public int compare(Residuo r1, Residuo r2) {
            return Integer.compare(r2.getPrioridadAmbiental(), r1.getPrioridadAmbiental());
        }
    }

    /**
     * Comparador por zona (alfabético)
     */
    public static class ComparadorPorZona implements Comparator<Residuo> {
        @Override
        public int compare(Residuo r1, Residuo r2) {
            return r1.getZona().compareTo(r2.getZona());
        }
    }

    /**
     * Comparador por fecha (más reciente primero)
     */
    public static class ComparadorPorFecha implements Comparator<Residuo> {
        @Override
        public int compare(Residuo r1, Residuo r2) {
            return r2.getFechaRecoleccion().compareTo(r1.getFechaRecoleccion());
        }
    }
}
