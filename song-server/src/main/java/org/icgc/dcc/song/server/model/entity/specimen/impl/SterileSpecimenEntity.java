package org.icgc.dcc.song.server.model.entity.specimen.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.specimen.AbstractSpecimenEntity;
import org.icgc.dcc.song.server.model.entity.specimen.Specimen;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.SPECIMEN)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileSpecimenEntity extends AbstractSpecimenEntity {

  @Column(name = TableAttributeNames.DONOR_ID, nullable = false)
  private String donorId;

  public static SterileSpecimenEntity createSterileSpecimenEntity(String id,
      String donorId, @NonNull Specimen specimen){
    val s = new SterileSpecimenEntity();
    s.setWithSpecimen(specimen);
    s.setDonorId(donorId);
    s.setSpecimenId(id);
    return s;
  }
  public static SterileSpecimenEntity createSterileSpecimenEntity(String id,
      String donorId, String specimenSubmitterId, String specimenClass, String specimenType){
    val specimenData = createSpecimenImpl(specimenSubmitterId, specimenClass, specimenType);
    return createSterileSpecimenEntity(id, donorId, specimenData);
  }

}
