package org.icgc.dcc.song.server.model.entity.donor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@ToString(callSuper = true)
public class DonorEntity extends Donor {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String donorId;

  @Column(name = TableAttributeNames.STUDY_ID, nullable = false)
  private String studyId;

  public void setWithDonorEntity(@NonNull DonorEntity donorEntity){
    setDonorId(donorEntity.getDonorId());
    setStudyId(donorEntity.getStudyId());
    setWithDonor(donorEntity);
  }

  public static DonorEntity createDonorEntity(String donorId, String studyId,
      String donorSubmitterId, String donorGender){
    val donor = createDonor(donorSubmitterId, donorGender);
    return createDonorEntity(donorId, studyId, donor);
  }

  public static DonorEntity createDonorEntity(String donorId, String studyId, @NonNull Donor donor){
    val d = new DonorEntity();
    d.setDonorId(donorId);
    d.setStudyId(studyId);
    d.setWithDonor(donor);
    return d;
  }

}
