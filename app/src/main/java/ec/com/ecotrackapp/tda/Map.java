package ec.com.ecotrackapp.tda;

import ec.com.ecotrackapp.tda.List;

public interface Map<K, V> {
    int size();
    boolean isEmpty();
    V get(K key);
    V put(K key, V value);
    V remove(K key);
    void clear();
    List<Entry<K, V>> entrySet();

    interface Entry<K, V> {
        K getKey();
        V getValue();
        V setValue(V value);
    }
}
