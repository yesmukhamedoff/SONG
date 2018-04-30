package org.icgc.dcc.song.server.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.donor.DonorEntity;
import org.icgc.dcc.song.server.model.entity.sample.SampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.SpecimenEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CompositeEntity extends SampleEntity {

  private SpecimenEntity specimen;
  private DonorEntity donor;

}
