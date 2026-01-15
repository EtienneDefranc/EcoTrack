package ec.com.ecotrackapp.tda;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Implementación propia de una lista circular doblemente enlazada
 */
public class ListaCircularDoble<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private class Nodo implements Serializable {
        private static final long serialVersionUID = 1L;
        T dato;
        Nodo siguiente;
        Nodo anterior;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = this;
            this.anterior = this;
        }
    }

    private Nodo cabeza;
    private Nodo cursor; // Cursor para navegación
    private int tamanio;
    private int posicionCursor; // Posición actual del cursor (1-based para mostrar al usuario)

    public ListaCircularDoble() {
        this.cabeza = null;
        this.cursor = null;
        this.tamanio = 0;
        this.posicionCursor = 0;
    }

    public void agregar(T dato) {
        Nodo nuevoNodo = new Nodo(dato);

        if (cabeza == null) {
            cabeza = nuevoNodo;
            cursor = nuevoNodo;
            posicionCursor = 1;
        } else {
            Nodo ultimo = cabeza.anterior;
            ultimo.siguiente = nuevoNodo;
            nuevoNodo.anterior = ultimo;
            nuevoNodo.siguiente = cabeza;
            cabeza.anterior = nuevoNodo;
        }
        tamanio++;
    }

    public void agregarAlInicio(T dato) {
        agregar(dato);
        if (cabeza != null) {
            cabeza = cabeza.anterior;
            cursor = cabeza;
            posicionCursor = 1;
        }
    }

    public boolean eliminar(T dato) {
        if (cabeza == null) return false;

        Nodo actual = cabeza;
        do {
            if (actual.dato.equals(dato)) {
                if (tamanio == 1) {
                    cabeza = null;
                    cursor = null;
                    posicionCursor = 0;
                } else {
                    actual.anterior.siguiente = actual.siguiente;
                    actual.siguiente.anterior = actual.anterior;
                    if (actual == cabeza) {
                        cabeza = actual.siguiente;
                    }
                    if (actual == cursor) {
                        cursor = actual.siguiente;
                    }
                }
                tamanio--;
                if (posicionCursor > tamanio) {
                    posicionCursor = tamanio;
                }
                return true;
            }
            actual = actual.siguiente;
        } while (actual != cabeza);

        return false;
    }

    public T buscar(T dato) {
        if (cabeza == null) return null;

        Nodo actual = cabeza;
        do {
            if (actual.dato.equals(dato)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        } while (actual != cabeza);

        return null;
    }

    public T obtener(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }

        Nodo actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /**
     * Obtiene el elemento actual donde está el cursor
     */
    public T obtenerActual() {
        if (cursor == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return cursor.dato;
    }

    /**
     * Mueve el cursor al siguiente elemento y lo retorna
     */
    public T siguiente() {
        if (cursor == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        cursor = cursor.siguiente;
        posicionCursor++;
        if (posicionCursor > tamanio) {
            posicionCursor = 1; // Circular: vuelve al inicio
        }
        return cursor.dato;
    }

    /**
     * Mueve el cursor al elemento anterior y lo retorna
     */
    public T anterior() {
        if (cursor == null) {
            throw new NoSuchElementException("La lista está vacía");
        }
        cursor = cursor.anterior;
        posicionCursor--;
        if (posicionCursor < 1) {
            posicionCursor = tamanio; // Circular: va al final
        }
        return cursor.dato;
    }

    /**
     * Reinicia el cursor al primer elemento (cabeza)
     */
    public void reiniciarCursor() {
        cursor = cabeza;
        posicionCursor = (cabeza != null) ? 1 : 0;
    }

    /**
     * Obtiene la posición actual del cursor (1-based)
     */
    public int getPosicionCursor() {
        return posicionCursor;
    }

    public int getTamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new java.util.Iterator<T>() {
            private Nodo actual = cabeza;
            private int visitados = 0;

            @Override
            public boolean hasNext() {
                return actual != null && visitados < tamanio;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                visitados++;
                return dato;
            }
        };
    }

    public java.util.Iterator<T> iteradorReversa() {
        return new java.util.Iterator<T>() {
            private Nodo actual = (cabeza != null) ? cabeza.anterior : null;
            private int visitados = 0;

            @Override
            public boolean hasNext() {
                return actual != null && visitados < tamanio;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T dato = actual.dato;
                actual = actual.anterior;
                visitados++;
                return dato;
            }
        };
    }

    public void limpiar() {
        cabeza = null;
        cursor = null;
        tamanio = 0;
        posicionCursor = 0;
    }
}
