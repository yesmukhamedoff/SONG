package org.icgc.dcc.song.server.service;

import lombok.val;
import org.icgc.dcc.song.server.model.legacy.LegacyEntity;
import org.icgc.dcc.song.server.utils.EntityMapper;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityMapperTest {

  private EntityMapper entityMapper = Mappers.getMapper(EntityMapper.class);

  @Test
  public void testLegacyEntityConversion(){
    val legacyEntity = LegacyEntity.builder()
        .access("controlled")
        .fileName("fn1")
        .gnosId("an1")
        .id("fi1")
        .projectCode("ABC123")
        .build();

    val legacyDto = entityMapper.convertToLegacyDto(legacyEntity);
    assertThat(legacyDto).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyDto, legacyEntity)).isFalse();

    val legacyEntityCopy = entityMapper.convertToLegacyEntity(legacyEntity);
    assertThat(legacyEntityCopy).isEqualToComparingFieldByField(legacyEntity);
    assertThat(isObjectsEqual(legacyEntityCopy, legacyEntity)).isFalse();

    val legacyEntityCopy2 = entityMapper.convertToLegacyEntity(legacyDto);
    assertThat(legacyEntityCopy2).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyEntityCopy2, legacyDto)).isFalse();

    val legacyDtoCopy = entityMapper.convertToLegacyDto(legacyDto);
    assertThat(legacyDtoCopy).isEqualToComparingFieldByField(legacyDto);
    assertThat(isObjectsEqual(legacyDtoCopy, legacyDto)).isFalse();
  }

  private boolean isObjectsEqual(Object o1, Object o2){
    return o1 == o2;
  }



}
