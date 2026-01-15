package ec.com.ecotrackapp.tda;

import java.io.Serializable;
import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.Comparator;
import java.util.NoSuchElementException;

/**
 * Implementación propia de una cola de prioridad usando un heap binario
 */
public class ColaPrioridad<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<T> heap;
    private Comparator<T> comparador;

    public ColaPrioridad(Comparator<T> comparador) {
        this.heap = new ArrayList<>();
        this.comparador = comparador;
    }

    public ColaPrioridad() {
        this(null);
    }

    public void encolar(T elemento) {
        heap.add(elemento);
        subir(heap.size() - 1);
    }

    public T desencolar() {
        if (estaVacia()) {
            throw new NoSuchElementException("La cola está vacía");
        }

        T elemento = heap.get(0);
        T ultimo = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, ultimo);
            bajar(0);
        }

        return elemento;
    }

    public T verPrimero() {
        if (estaVacia()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return heap.get(0);
    }

    public boolean estaVacia() {
        return heap.isEmpty();
    }

    public int getTamanio() {
        return heap.size();
    }

    public void limpiar() {
        heap.clear();
    }

    private void subir(int indice) {
        T elemento = heap.get(indice);

        while (indice > 0) {
            int indicePadre = (indice - 1) / 2;
            T padre = heap.get(indicePadre);

            if (comparar(elemento, padre) >= 0) {
                break;
            }

            heap.set(indice, padre);
            indice = indicePadre;
        }

        heap.set(indice, elemento);
    }

    private void bajar(int indice) {
        T elemento = heap.get(indice);
        int mitad = heap.size() / 2;

        while (indice < mitad) {
            int indiceHijoIzq = 2 * indice + 1;
            int indiceHijoDer = indiceHijoIzq + 1;
            int indiceHijoMenor = indiceHijoIzq;

            if (indiceHijoDer < heap.size() &&
                comparar(heap.get(indiceHijoDer), heap.get(indiceHijoIzq)) < 0) {
                indiceHijoMenor = indiceHijoDer;
            }

            if (comparar(elemento, heap.get(indiceHijoMenor)) <= 0) {
                break;
            }

            heap.set(indice, heap.get(indiceHijoMenor));
            indice = indiceHijoMenor;
        }

        heap.set(indice, elemento);
    }

    @SuppressWarnings("unchecked")
    private int comparar(T e1, T e2) {
        if (comparador != null) {
            return comparador.compare(e1, e2);
        } else if (e1 instanceof Comparable) {
            return ((Comparable<T>) e1).compareTo(e2);
        } else {
            throw new ClassCastException("Los elementos no son comparables");
        }
    }

    public boolean contiene(T elemento) {
        return heap.contains(elemento);
    }

    @Override
    public String toString() {
        if (estaVacia()) {
            return "Cola vacía";
        }
        return "Cola de prioridad con " + getTamanio() + " elementos";
    }
}
