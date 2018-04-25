package org.icgc.dcc.song.server.model.entity.sample;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class AbstractSampleEntity extends Metadata implements SampleEntity {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String sampleId;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String sampleSubmitterId;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  @Override
  public void setSampleType(String type) {
    Constants.validate(Constants.SAMPLE_TYPE, type);
    sampleType = type;
  }

}
