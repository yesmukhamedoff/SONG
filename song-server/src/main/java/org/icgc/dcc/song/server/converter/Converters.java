package org.icgc.dcc.song.server.converter;

import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.mapstruct.Mapper;

import java.util.function.Function;

public class Converters {

  @Mapper
  public interface LegacyConverter{

    LegacyDto convertToLegacyDto(LegacyEntity legacy);
    LegacyEntity convertToLegacyEntity(LegacyDto legacy);

  }

  public static <T,R> Converter<T, R> createConverter(Function<T,R> t2r){
    return new Converter<T, R>() {

      @Override public <S extends T> R convert(S input) {
        return t2r.apply(input);
      }
    };
  }


}
