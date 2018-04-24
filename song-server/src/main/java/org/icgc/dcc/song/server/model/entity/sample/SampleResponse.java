package org.icgc.dcc.song.server.model.entity.sample;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.SAMPLE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class SampleResponse extends AbstractSampleEntity {

  @Column(name = TableAttributeNames.SPECIMEN_ID, nullable = false)
  private String specimenId;

  public static SampleResponse createSampleResponse(String id, String submitterId, String type, String specimenId){
    val s = new SampleResponse();
    s.setSpecimenId(specimenId);
    s.setSampleId(id);
    s.setSampleType(type);
    s.setSampleSubmitterId(submitterId);
    return s;
  }

}
