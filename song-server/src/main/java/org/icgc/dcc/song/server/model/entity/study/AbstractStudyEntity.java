package org.icgc.dcc.song.server.model.entity.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterJoinTable;
import org.hibernate.annotations.ParamDef;
import org.icgc.dcc.song.server.model.entity.info.Info;
import org.icgc.dcc.song.server.model.entity.study.impl.StudyData;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@FilterDef(name="infoIdType", parameters={
    @ParamDef( name="idType", type="string" )
})
public abstract class AbstractStudyEntity extends StudyData implements StudyEntity {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String studyId;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.ID)
  @FilterJoinTable(name="infoIdType", condition = ":idType = Study")
  private Info info;


}
