package org.icgc.dcc.song.server.config;

import org.icgc.dcc.song.server.converter.Converter;
import org.icgc.dcc.song.server.converter.Converters.LegacyConverter;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.icgc.dcc.song.server.utils.EntityMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.icgc.dcc.song.server.converter.Converters.createConverter;

@Configuration
public class MapperConfig {

  @Bean
  public EntityMapper entityMapper(){
    return Mappers.getMapper(EntityMapper.class);
  }

  @Bean
  public LegacyConverter legacyConverter(){
    return Mappers.getMapper(LegacyConverter.class);

  }

  @Bean
  public Converter<LegacyEntity, LegacyDto>  legacyForwardConverter(LegacyConverter legacyConverter){
    return createConverter(legacyConverter::convertToLegacyDto);
  }

  @Bean
  public Converter<LegacyDto, LegacyEntity>  legacyReverseConverter(LegacyConverter legacyConverter){
    return createConverter(legacyConverter::convertToLegacyEntity);
  }

}
