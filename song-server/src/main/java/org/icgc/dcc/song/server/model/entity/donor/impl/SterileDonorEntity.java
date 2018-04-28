package org.icgc.dcc.song.server.model.entity.donor.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.donor.AbstractDonorEntity;
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.DONOR)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileDonorEntity extends AbstractDonorEntity {

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  public static SterileDonorEntity createSterileDonorEntity(@NonNull String id,
      @NonNull String studyId, @NonNull Donor donor){
    val d = createSterileDonorEntity(studyId, donor);
    d.setDonorId(id);
    return d;
  }

  public static SterileDonorEntity createSterileDonorEntity( @NonNull String studyId, @NonNull Donor donor){
    val d = new SterileDonorEntity();
    d.setWithDonor(donor);
    d.setStudyId(studyId);
    return d;
  }
}
