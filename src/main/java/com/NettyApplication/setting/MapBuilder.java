package com.NettyApplication.setting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapBuilder<K, V> {

    private final Map<K, V> map;

    private MapBuilder() {
        map = new HashMap<>();
    }

    public Map<K, V> build() {
        return map;
    }

    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<>();
    }

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder<K, V> putAll(Map<K, V> m) {
        map.putAll(m);
        return this;
    }

    public MapBuilder<K, V> putIf(boolean condition, K key, V value) {
        if (condition) {
            map.put(key, value);
        }
        return this;
    }

    public MapBuilder<K, V> putIf(boolean condition, K key, Supplier<V> supplier) {
        if (condition) {
            map.put(key, supplier.get());
        }
        return this;
    }

    public static <K, V> Map<K, V> of(K key, V value) {
        return MapBuilder.<K, V>builder()
                .put(key, value)
                .build();
    }
}
