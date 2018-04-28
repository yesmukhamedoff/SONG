package org.icgc.dcc.song.server.model.entity.specimen.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.specimen.Specimen;
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
public class SpecimenImpl extends Metadata implements Specimen {

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

  public static SpecimenImpl createSpecimenImpl(String specimenSubmitterId,
      String specimenClass, String specimenType){
    val s = new SpecimenImpl();
    s.setSpecimenClass(specimenClass);
    s.setSpecimenSubmitterId(specimenSubmitterId);
    s.setSpecimenType(specimenType);
    return s;
  }

}
