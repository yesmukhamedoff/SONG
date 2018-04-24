package org.icgc.dcc.song.server.converters;

public interface Converter<T, R> {

  R convert(T t);

}
