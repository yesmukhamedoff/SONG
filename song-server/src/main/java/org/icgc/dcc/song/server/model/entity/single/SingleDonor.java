package org.icgc.dcc.song.server.model.entity.single;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.AbstractDonor;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.DONOR)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SingleDonor extends AbstractDonor{

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

}
