package org.icgc.dcc.song.server.utils;

import org.icgc.dcc.song.server.model.legacy.Legacy;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EntityMapper {

  EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

  LegacyDto convertToLegacyDto(Legacy legacy);
  LegacyEntity convertToLegacyEntity(Legacy legacy);

}
