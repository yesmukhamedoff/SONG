package org.icgc.dcc.song.server.converter;

import java.util.List;
import java.util.stream.Stream;

import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableList;
import static org.icgc.dcc.common.core.util.stream.Streams.stream;

public interface Converter<T, R> {

  <S extends T> R convert(S input);

  default <S extends T> List<R> convertToList(Stream<S> inputStream){
    return inputStream
        .map(this::convert)
        .collect(toImmutableList());
  }

  default <S extends T> List<R> convertToList(List<S> inputList){
    return convertToList(inputList.stream());
  }

  default <S extends T> List<R> convertToList(Iterable<S> inputIterable){
    return convertToList(stream(inputIterable));
  }

}
