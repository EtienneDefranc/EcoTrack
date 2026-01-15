package ec.com.ecotrackapp.tda;

import ec.com.ecotrackapp.tda.Comparator;

public interface List<E> extends Iterable<E> {
    boolean addFirst(E e);
    E removeFirst();
    E removeLast();
    int size();
    boolean isEmpty();

    boolean add(E e);                 // agrega al final
    void add(int index, E element); // inserta
    void clear();
    boolean contains(Object o);
    E get(int index);
    int indexOf(Object o);
    int lastIndexOf(Object o);
    E remove(int index);
    boolean remove(Object o);
    E set(int index, E element);
    void sort(Comparator<? super E> c);
}
