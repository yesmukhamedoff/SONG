package org.icgc.dcc.song.server.converters;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.model.entity.study.impl.FullStudyEntity;

public class StudyConverters {

  @RequiredArgsConstructor
  public static class StudyWithDonorsConverter<R extends Study> implements Converter<FullStudyEntity, R>{

    @NonNull private final Class<R> rClass;

    @Override
    public R convert(FullStudyEntity fullStudyEntity) {
      return null;
    }

  }

}
