package org.icgc.dcc.song.server.model.entity.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.icgc.dcc.song.server.model.entity.study.impl.StudyData;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AbstractStudyEntity extends StudyData implements StudyEntity {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String studyId;

  public void setWithStudyEntity(@NonNull StudyEntity studyEntity){
    setWithStudy(studyEntity);
    setStudyId(studyEntity.getStudyId());
  }
}
