package org.icgc.dcc.song.server.model.entity.study.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.study.AbstractStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.STUDY)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileStudyEntity extends AbstractStudyEntity {

  public static SterileStudyEntity createSterileStudy(@NonNull String id, @NonNull Study study){
    val s = new SterileStudyEntity();
    s.setWithStudy(study);
    s.setStudyId(id);
    return s;
  }

  public static SterileStudyEntity createSterileStudy(String id, String name, String organization, String description){
    return createSterileStudy(id, createStudyData(name, organization, description));
  }

}
