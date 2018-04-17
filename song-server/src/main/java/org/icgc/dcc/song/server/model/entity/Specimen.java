/*
 * Copyright (c) 2018. Ontario Institute for Cancer Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.icgc.dcc.song.server.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@Entity
@Table(name = TableNames.SPECIMEN)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { ModelAttributeNames.DONOR })
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Specimen extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String specimenId;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.DONOR_ID)
  private Donor donor;

//@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//  private List<Sample> samples;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String specimenSubmitterId;

  @Column(name = TableAttributeNames.CLASS, nullable = false)
  private String specimenClass;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String specimenType;

  // RTISMA_HACK need to remove this, now that relationships are properly mapped with JPA
//  public static Specimen create(String id, @NonNull String submitterId, String donorId, String specimenClass,
//                                String type) {
//    val s = new Specimen();
//    s.setSpecimenId(id);
//    s.setDonor(Donor.create(donorId, "", "", DONOR_GENDER.stream().findFirst().get())); //RTISMA_HACK  s.setDonorId(donorId);
//    s.setSpecimenSubmitterId(submitterId);
//    s.setSpecimenClass(specimenClass);
//    s.setSpecimenType(type);
//
//    return s;
//  }

  public void setDonor(@NonNull Donor donor){
    this.donor = donor;
    if (!donor.getSpecimens().contains(this)){
      donor.addSpecimen(this);
    }
  }

  public void setSpecimenClass(String specimenClass) {
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

}
