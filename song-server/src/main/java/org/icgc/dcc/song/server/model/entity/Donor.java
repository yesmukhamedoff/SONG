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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.server.model.enums.Constants.DONOR_GENDER;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Entity(name = TableNames.DONOR)
@Table(name = TableNames.DONOR)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {ModelAttributeNames.STUDY})
@JsonPropertyOrder({
    ModelAttributeNames.DONOR_ID,
    ModelAttributeNames.DONOR_SUBMITTER_ID,
    ModelAttributeNames.STUDY_ID,
    ModelAttributeNames.DONOR_GENDER,
    ModelAttributeNames.SPECIMENS,
    ModelAttributeNames.INFO })
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Donor extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID, updatable = false, unique = true, nullable = false)
  private String donorId;

  @ManyToOne(cascade = CascadeType.ALL,
      fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.STUDY_ID)
  private Study study;

  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.DONOR )
  private List<Specimen> specimens = newArrayList();

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String donorSubmitterId;

  @Column(name = TableAttributeNames.GENDER, nullable = false)
  private String donorGender;

  public void setStudy(@NonNull Study study){
    this.study = study;
    if (!study.getDonors().contains(this)){
      study.withDonor(this);
    }
  }

  public void addSpecimen(@NonNull Specimen specimen){
    this.specimens.add(specimen);
    if (specimen.getDonor() != this){
      specimen.setDonor(this);
    }
  }

  public static Donor createDonor(String id, String submitterId, String gender){
    val d = new Donor();
    d.setDonorId(id);
    d.setDonorSubmitterId(submitterId);
    d.setDonorGender(gender);
    return d;
  }

  //RTISMA_HACK: this needs to be fixed
//  public static Donor create(String id, String submitterId, String studyId, String gender) {
//    val d = new Donor();
//    d.setDonorId(id);
//    d.setStudy(Study.create(studyId,"","","")); //RTISMA_HACK
//    d.setDonorSubmitterId(submitterId);
//    d.setDonorGender(gender);
//    return d;
//  }

  //RTISMA_TODO: remove this, should have its own validation. Need gender to be null so can create hibernate examples for
  // finding entities. This would servce as a data and request entity
  public void setDonorGender(String gender) {
    validate(DONOR_GENDER, gender);
    this.donorGender = gender;
  }

}
