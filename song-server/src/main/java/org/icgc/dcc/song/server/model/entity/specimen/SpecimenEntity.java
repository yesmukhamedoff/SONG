package org.icgc.dcc.song.server.model.entity.specimen;

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
@ToString(callSuper = true)
@MappedSuperclass
public class SpecimenEntity extends Specimen {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String specimenId;

  @Column(name = TableAttributeNames.DONOR_ID, nullable = false)
  private String donorId;

  public void setWithSpecimenEntity(@NonNull SpecimenEntity specimenEntity){
    setSpecimenId(specimenEntity.getSpecimenId());
    setDonorId(specimenEntity.getDonorId());
    setWithSpecimen(specimenEntity);
  }

  public static SpecimenEntity createSpecimenEntity(String specimenId, String donorId,
      String specimenSubmitterId, String specimenClass, String specimenType ){
    val specimen = createSpecimen(specimenSubmitterId, specimenClass, specimenType);
    return createSpecimenEntity(specimenId, donorId, specimen);
  }

  public static SpecimenEntity createSpecimenEntity(String specimenId, String donorId, @NonNull Specimen specimen){
    val s = new SpecimenEntity();
    s.setSpecimenId(specimenId);
    s.setDonorId(donorId);
    s.setWithSpecimen(specimen);
    return s;
  }

}
