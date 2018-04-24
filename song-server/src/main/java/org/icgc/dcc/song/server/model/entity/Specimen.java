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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.JsonAttributeNames;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_CLASS;
import static org.icgc.dcc.song.server.model.enums.Constants.SPECIMEN_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Data
@Entity
@Table(name = TableNames.SPECIMEN)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { ModelAttributeNames.DONOR, ModelAttributeNames.SAMPLES })
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@NamedEntityGraph(name = "specimenWithSamples",
    attributeNodes = @NamedAttributeNode(value = ModelAttributeNames.SAMPLES) )
public class Specimen extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String specimenId;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.DONOR_ID, nullable = false)
  private Donor donor;

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String specimenSubmitterId;

  @Column(name = TableAttributeNames.CLASS, nullable = false)
  private String specimenClass;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String specimenType;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.SPECIMEN)
  private List<Sample> samples = newArrayList();

  @JsonGetter(value = JsonAttributeNames.DONOR_ID)
  public String getDonorId(){
    return this.getDonor().getDonorId();
  }

  public static Specimen createSpecimen(String id, String submitterId, String specimenClass, String type){
    val s = new Specimen();
    s.setSpecimenId(id);
    s.setSpecimenClass(specimenClass);
    s.setSpecimenSubmitterId(submitterId);
    s.setSpecimenType(type);
    return s;
  }

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

//  public void setDonor(@NonNull Donor compositeDonor){
//    this.donor = compositeDonor;
//    if (!compositeDonor.getSpecimens().contains(this)){
//      compositeDonor.addSpecimen(this);
//    }
//  }

  public void setSpecimenClass(String specimenClass) {
    validate(SPECIMEN_CLASS, specimenClass);
    this.specimenClass = specimenClass;
  }

  public void setSpecimenType(String type) {
    validate(SPECIMEN_TYPE, type);
    specimenType = type;
  }

}
