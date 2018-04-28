package org.icgc.dcc.song.server.model.entity.sample.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public class SampleImpl extends Metadata implements Sample {

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String sampleSubmitterId;

  public void setSampleType(String type) {
    Constants.validate(Constants.SAMPLE_TYPE, type);
    sampleType = type;
  }

  public static SampleImpl createSampleImpl(String sampleSubmitterId, String sampleType){
    val s = new SampleImpl();
    s.setSampleSubmitterId(sampleSubmitterId);
    s.setSampleType(sampleType);
    return s;
  }

}
