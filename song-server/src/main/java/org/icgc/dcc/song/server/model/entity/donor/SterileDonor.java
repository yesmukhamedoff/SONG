package org.icgc.dcc.song.server.model.entity.donor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.DONOR)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileDonor extends AbstractDonorEntity {

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

}
