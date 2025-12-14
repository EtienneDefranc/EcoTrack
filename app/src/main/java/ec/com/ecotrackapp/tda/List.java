package ec.com.ecotrackapp.tda;

public interface List<E> {
    public boolean addFirst(E e);
    public E removeFirst();
    public E removeLast();
    int size();
    boolean isEmpty();

    boolean add(E e);                 // agrega al final
    void add(int index, E element); // inserta

    E get(int index);
    E set(int index, E element);

    E remove(int index);

    void clear();
}
