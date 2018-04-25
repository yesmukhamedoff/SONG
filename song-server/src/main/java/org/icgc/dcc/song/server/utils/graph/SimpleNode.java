package org.icgc.dcc.song.server.utils.graph;

import lombok.NonNull;
import lombok.Value;

@Value
public class SimpleNode<T> {
  @NonNull private final int id;
  @NonNull private final T value;

  public static <T> SimpleNode<T> node(int id, T value) {
    return new SimpleNode<>(id, value);
  }
}
