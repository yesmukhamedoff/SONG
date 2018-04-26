package org.icgc.dcc.song.server.model.entity.study;

import lombok.NonNull;
import org.icgc.dcc.song.server.model.entity.info.Infoable;

public interface StudyEntity extends Study, Infoable {

  String getStudyId();

  void setStudyId(String studyId);

  default void setWith(@NonNull String id, @NonNull Study study){
    setWithStudy(study);
    setStudyId(id);
  }

  default void setWithStudyEntity(@NonNull StudyEntity studyEntity){
    setWith(studyEntity.getStudyId(), studyEntity);
    setInfo(studyEntity.getInfo());
  }

}
