package org.icgc.dcc.song.server.model.entity.study.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.study.AbstractStudyEntity;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {ModelAttributeNames.DONORS})
public class StudyWithDonors<D extends FullDonorEntity> extends AbstractStudyEntity {

  private List<D> donors;

}
