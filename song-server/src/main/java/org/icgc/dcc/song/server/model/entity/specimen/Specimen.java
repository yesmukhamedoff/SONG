package org.icgc.dcc.song.server.model.entity.specimen;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Specimen extends Metadata {

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String specimenSubmitterId;

  @Column(name = TableAttributeNames.CLASS, nullable = false)
  private String specimenClass;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String specimenType;

  public void setSpecimenClass(String specimenClass) {
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

  public void setWithSpecimen(@NonNull Specimen specimen){
    setSpecimenSubmitterId(specimen.getSpecimenSubmitterId());
    setSpecimenClass(specimen.getSpecimenClass());
    setSpecimenType(specimen.getSpecimenType());
    setInfo(specimen.getInfo());
  }

  public static Specimen createSpecimen(String specimenSubmitterId, String specimenClass, String specimenType){
    val s = new Specimen();
    s.setSpecimenSubmitterId(specimenSubmitterId);
    s.setSpecimenClass(specimenClass);
    s.setSpecimenType(specimenType);
    return s;
  }

}
