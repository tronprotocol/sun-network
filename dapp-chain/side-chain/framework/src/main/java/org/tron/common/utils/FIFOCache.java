package org.tron.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class FIFOCache<K,V> extends LinkedHashMap<K, V> {
  private final int SIZE;
  public FIFOCache(int size) {
    super();
    SIZE = size;
  }

  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > SIZE;
  }

}