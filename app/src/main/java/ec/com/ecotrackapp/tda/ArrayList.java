package ec.com.ecotrackapp.tda;

import java.util.NoSuchElementException;

public class ArrayList<E> implements List<E> {
    private E[] elems;
    private int capacity = 100;
    private int effectiveSize;

    @SuppressWarnings("unchecked")
    public ArrayList() {
        elems = (E[]) new Object[capacity];
        effectiveSize = 0;
    }

    // ------------------------
    // Helpers privados
    // ------------------------
    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= capacity) return;

        int newCapacity = capacity;
        while (newCapacity < minCapacity) {
            newCapacity = newCapacity * 2; // crecimiento por duplicación
        }

        @SuppressWarnings("unchecked")
        E[] newArr = (E[]) new Object[newCapacity];

        for (int i = 0; i < effectiveSize; i++) {
            newArr[i] = elems[i];
        }

        elems = newArr;
        capacity = newCapacity;
    }

    private void checkIndexForGetSetRemove(int index) {
        if (index < 0 || index >= effectiveSize) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + effectiveSize
            );
        }
    }

    private void checkIndexForAdd(int index) {
        // en add(index, e) se permite index == size (insertar al final)
        if (index < 0 || index > effectiveSize) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + effectiveSize
            );
        }
    }

    // ------------------------
    // Métodos de la interfaz
    // ------------------------
    @Override
    public boolean addFirst(E e) {
        add(0, e);
        return true;
    }

    public boolean addLast(E e) {
        return add(e);
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        return remove(0);
    }

    @Override
    public E removeLast() {
        if (isEmpty()) throw new NoSuchElementException("List is empty");
        return remove(effectiveSize - 1);
    }

    @Override
    public boolean isEmpty() {
        return effectiveSize == 0;
    }

    @Override
    public int size() {
        return effectiveSize;
    }

    @Override
    public void add(int index, E element) {
        checkIndexForAdd(index);
        ensureCapacity(effectiveSize + 1);

        // desplazar a la derecha desde el final hasta index
        for (int i = effectiveSize; i > index; i--) {
            elems[i] = elems[i - 1];
        }

        elems[index] = element;
        effectiveSize++;
    }

    @Override
    public E get(int index) {
        checkIndexForGetSetRemove(index);
        return elems[index];
    }

    @Override
    public boolean add(E e) {
        ensureCapacity(effectiveSize + 1);
        elems[effectiveSize] = e;
        effectiveSize++;
        return true;
    }

    @Override
    public E remove(int index) {
        checkIndexForGetSetRemove(index);

        E removed = elems[index];

        // desplazar a la izquierda para “tapar” el hueco
        for (int i = index; i < effectiveSize - 1; i++) {
            elems[i] = elems[i + 1];
        }

        // liberar última referencia para ayudar al GC
        elems[effectiveSize - 1] = null;
        effectiveSize--;

        return removed;
    }

    @Override
    public E set(int index, E element) {
        checkIndexForGetSetRemove(index);

        E old = elems[index];
        elems[index] = element;
        return old;
    }

    @Override
    public void clear() {
        for (int i = 0; i < effectiveSize; i++) {
            elems[i] = null;
        }
        effectiveSize = 0;
    }
}
