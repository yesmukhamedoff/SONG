package org.icgc.dcc.song.server.converter.impl;

import org.icgc.dcc.song.server.converter.Converter;
import org.icgc.dcc.song.server.model.legacy.LegacyDto;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;

public class LegacyConverter {

  public abstract class DtoConverter implements Converter<LegacyDto, LegacyEntity>{ }

  public abstract class EntityConverter implements Converter<LegacyEntity, LegacyDto>{ }

}
