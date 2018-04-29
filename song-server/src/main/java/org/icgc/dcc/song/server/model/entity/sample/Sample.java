package org.icgc.dcc.song.server.model.entity.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public class Sample extends Metadata  {

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String sampleSubmitterId;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  public void setSampleType(String type) {
    Constants.validate(Constants.SAMPLE_TYPE, type);
    sampleType = type;
  }

  public void setWithSample(@NonNull Sample sample){
    setSampleSubmitterId(sample.getSampleSubmitterId());
    setSampleType(sample.getSampleType());
    setInfo(sample.getInfo());
  }

  public static Sample createSample(String sampleSubmitterId, String sampleType){
    val s = new Sample();
    s.setSampleSubmitterId(sampleSubmitterId);
    s.setSampleType(sampleType);
    return s;
  }

}
