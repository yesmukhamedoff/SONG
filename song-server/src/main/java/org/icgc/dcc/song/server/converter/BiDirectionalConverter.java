package org.icgc.dcc.song.server.converter;

public interface BiDirectionalConverter<T, R> {

  R convertForward (T t);
  T convertReverse (R r );
}
