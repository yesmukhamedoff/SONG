package org.icgc.dcc.song.server.converter;

import lombok.Value;

@Value
public class BiDirectionalConverterImpl<T, R> implements BiDirectionalConverter<T, R> {

  private final Converter<T,R> forwardConverter;
  private final Converter<R,T> reverseConverter;

  @Override public R convertForward(T t) {
    return forwardConverter.convert(t);
  }

  @Override public T convertReverse(R r) {
    return reverseConverter.convert(r);
  }

}
