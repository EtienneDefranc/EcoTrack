package ec.com.ecotrackapp.tda;

import ec.com.ecotrackapp.tda.ArrayList;
import ec.com.ecotrackapp.tda.List;

public class HashMap<K, V> implements Map<K, V> {

    private static class Node<K, V> implements Map.Entry<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        // Implementación de equals y hashCode para cumplir con el contrato de Map.Entry
        @Override
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (key.equals(e.getKey()) &&
                    value.equals(e.getValue()))
                    return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return java.util.Objects.hashCode(key) ^ java.util.Objects.hashCode(value);
        }
    }

    private Node<K, V>[] table;
    private int size;
    private final float loadFactor;

    public HashMap() {
        this(16, 0.75f);
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity, float loadFactor) {
        this.loadFactor = loadFactor;
        this.table = new Node[initialCapacity];
        this.size = 0;
    }

    private int hash(Object key) {
        return (key == null) ? 0 : Math.abs(key.hashCode() % table.length);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(K key) {
        int index = hash(key);
        for (Node<K, V> e = table[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        int index = hash(key);
        for (Node<K, V> e = table[index]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }

        Node<K, V> newNode = new Node<>(key, value, table[index]);
        table[index] = newNode;
        size++;

        if (size > table.length * loadFactor) {
            resize(2 * table.length);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Node<K, V>[] newTable = new Node[newCapacity];
        for (Node<K, V> oldNode : table) {
            while (oldNode != null) {
                Node<K, V> next = oldNode.next;
                int index = Math.abs(oldNode.key.hashCode() % newCapacity);
                oldNode.next = newTable[index];
                newTable[index] = oldNode;
                oldNode = next;
            }
        }
        table = newTable;
    }

    @Override
    public V remove(K key) {
        int index = hash(key);
        Node<K, V> prev = null;
        Node<K, V> e = table[index];

        while (e != null) {
            if (e.key.equals(key)) {
                if (prev == null) {
                    table[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                size--;
                return e.value;
            }
            prev = e;
            e = e.next;
        }

        return null;
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
    }

    @Override
    public List<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entries = new ec.com.ecotrackapp.tda.ArrayList<>();
        for (Node<K, V> node : table) {
            for (Node<K, V> e = node; e != null; e = e.next) {
                entries.add(e);
            }
        }
        return entries;
    }

    public List<V> values() {
        List<V> values = new ec.com.ecotrackapp.tda.ArrayList<>();
        for (Node<K, V> node : table) {
            for (Node<K, V> e = node; e != null; e = e.next) {
                values.add(e.getValue());
            }
        }
        return values;
    }
}
