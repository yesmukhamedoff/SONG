package org.icgc.dcc.song.server.model.entity.sample;

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
public class SampleEntity extends Sample {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String sampleId;

  @Column(name = TableAttributeNames.SPECIMEN_ID,
      updatable = false, unique = true, nullable = false)
  private String specimenId;

  public void setWithSampleEntity(@NonNull SampleEntity sampleEntity){
    setSampleId(sampleEntity.getSampleId());
    setSpecimenId(sampleEntity.getSpecimenId());
    setWithSample(sampleEntity);
  }

  public static SampleEntity createSampleEntity(String sampleId, String specimenId,
      String sampleSubmitterId, String sampleType){
    val sample = createSample(sampleSubmitterId, sampleType);
    return createSampleEntity(sampleId, specimenId, sample);
  }

  public static SampleEntity createSampleEntity(String sampleId, String specimenId, @NonNull Sample sample){
    val s = new SampleEntity();
    s.setSampleId(sampleId);
    s.setSpecimenId(specimenId);
    s.setWithSample(sample);
    return s;
  }

}
