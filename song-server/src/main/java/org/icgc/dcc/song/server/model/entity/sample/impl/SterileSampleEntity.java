package org.icgc.dcc.song.server.model.entity.sample.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.entity.sample.AbstractSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.SAMPLE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class SterileSampleEntity extends AbstractSampleEntity {

  @Column(name = TableAttributeNames.SPECIMEN_ID, nullable = false)
  private String specimenId;

  public static SterileSampleEntity createSterileSample(String id, String specimenId, Sample sample){
    val s = new SterileSampleEntity();
    s.setWithSample(sample);
    s.setSpecimenId(specimenId);
    s.setSampleId(id);
    return s;
  }

  public static SterileSampleEntity createSterileSample(String id, String specimenId,
      String sampleSubmitterId, String sampleType){
    val s = new SterileSampleEntity();
    s.setWith(sampleSubmitterId, sampleType);
    s.setSpecimenId(specimenId);
    s.setSampleId(id);
    return s;
  }


}
