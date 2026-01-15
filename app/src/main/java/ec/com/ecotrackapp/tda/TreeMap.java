package ec.com.ecotrackapp.tda;

import java.io.Serializable;

public class TreeMap<K extends Comparable<K>, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 1L;

    private static class Node<K, V> implements Serializable {
        private static final long serialVersionUID = 1L;
        K key;
        V value;
        Node<K, V> left, right;
        int height;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.height = 1;
        }
    }

    private Node<K, V> root;
    private int size;

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private int height(Node<K, V> n) {
        return (n == null) ? 0 : n.height;
    }

    private int getBalance(Node<K, V> n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    @Override
    public V get(K key) {
        Node<K, V> node = getNode(root, key);
        return (node == null) ? null : node.value;
    }

    private Node<K, V> getNode(Node<K, V> node, K key) {
        if (node == null || key.equals(node.key)) {
            return node;
        }
        if (key.compareTo(node.key) < 0) {
            return getNode(node.left, key);
        } else {
            return getNode(node.right, key);
        }
    }

    @Override
    public V put(K key, V value) {
        V oldValue = get(key);
        root = putNode(root, key, value);
        return oldValue;
    }

    private Node<K, V> putNode(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }

        if (key.compareTo(node.key) < 0) {
            node.left = putNode(node.left, key, value);
        } else if (key.compareTo(node.key) > 0) {
            node.right = putNode(node.right, key, value);
        } else {
            node.value = value;
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
        return rebalance(node);
    }

    private Node<K, V> rebalance(Node<K, V> z) {
        int balance = getBalance(z);
        if (balance > 1) {
            if (getBalance(z.left) < 0) {
                z.left = rotateLeft(z.left);
            }
            return rotateRight(z);
        }
        if (balance < -1) {
            if (getBalance(z.right) > 0) {
                z.right = rotateRight(z.right);
            }
            return rotateLeft(z);
        }
        return z;
    }

    private Node<K, V> rotateRight(Node<K, V> y) {
        Node<K, V> x = y.left;
        Node<K, V> T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private Node<K, V> rotateLeft(Node<K, V> x) {
        Node<K, V> y = x.right;
        Node<K, V> T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    @Override
    public V remove(K key) {
        V oldValue = get(key);
        if (oldValue != null) {
            root = removeNode(root, key);
            size--;
        }
        return oldValue;
    }

    private Node<K, V> removeNode(Node<K, V> node, K key) {
        if (node == null) {
            return null;
        }

        if (key.compareTo(node.key) < 0) {
            node.left = removeNode(node.left, key);
        } else if (key.compareTo(node.key) > 0) {
            node.right = removeNode(node.right, key);
        } else {
            if (node.left == null || node.right == null) {
                node = (node.left != null) ? node.left : node.right;
            } else {
                Node<K, V> temp = minValueNode(node.right);
                node.key = temp.key;
                node.value = temp.value;
                node.right = removeNode(node.right, temp.key);
            }
        }

        if (node == null) {
            return null;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));
        return rebalance(node);
    }

    private Node<K, V> minValueNode(Node<K, V> node) {
        Node<K, V> current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public List<Entry<K, V>> entrySet() {
        List<Entry<K, V>> list = new ArrayList<>();
        inOrder(root, list);
        return list;
    }

    private void inOrder(Node<K, V> node, List<Entry<K, V>> list) {
        if (node != null) {
            inOrder(node.left, list);
            list.add(new MapEntry(node.key, node.value));
            inOrder(node.right, list);
        }
    }

    private class MapEntry implements Entry<K, V> {
        private K key;
        private V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
