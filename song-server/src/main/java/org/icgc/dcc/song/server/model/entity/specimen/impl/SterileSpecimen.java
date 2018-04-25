package org.icgc.dcc.song.server.model.entity.specimen.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.specimen.AbstractSpecimenEntity;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = TableNames.SPECIMEN)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SterileSpecimen extends AbstractSpecimenEntity {

  @Column(name = TableAttributeNames.DONOR_ID, nullable = false)
  private String donorId;

}
