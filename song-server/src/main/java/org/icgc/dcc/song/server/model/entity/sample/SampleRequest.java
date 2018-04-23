package org.icgc.dcc.song.server.model.entity.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SampleRequest extends AbstractSampleData {

  private String specimenId;

}
