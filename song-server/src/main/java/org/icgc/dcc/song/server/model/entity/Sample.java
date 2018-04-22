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
import org.icgc.dcc.song.server.model.JsonAttributeNames;
import org.icgc.dcc.song.server.model.Metadata;
import org.icgc.dcc.song.server.model.ModelAttributeNames;
import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.icgc.dcc.song.server.model.enums.Constants;
import org.icgc.dcc.song.server.model.enums.TableNames;
import org.icgc.dcc.song.server.repository.TableAttributeNames;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Entity
@Table(name = TableNames.SAMPLE)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { ModelAttributeNames.SPECIMEN })
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Sample extends Metadata {

  @Id
  @Column(name = TableAttributeNames.ID,
      updatable = false, unique = true, nullable = false)
  private String sampleId;

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.SPECIMEN_ID, nullable = false)
  private Specimen specimen;

  @JsonIgnore
  @ManyToMany(mappedBy = ModelAttributeNames.SAMPLES,
      cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<BaseAnalysis> analyses = newArrayList();

  @Column(name = TableAttributeNames.SUBMITTER_ID, nullable = false)
  private String sampleSubmitterId;

  @Column(name = TableAttributeNames.TYPE, nullable = false)
  private String sampleType;

  @JsonGetter(value = JsonAttributeNames.SPECIMEN_ID)
  public String getSpecimenId(){
    return getSpecimen().getSpecimenId();
  }

  public static Sample createSample(String id, String sampleSubmitterId, String type){
    val s = new Sample();
    s.setSampleId(id);
    s.setSampleSubmitterId(sampleSubmitterId);
    s.setSampleType(type);
    return s;
  }

  //RTISMA_HACK remove this create
//  public static Sample create(String id, @NonNull String submitter, String specimenId, String type) {
//    val sample = new Sample();
//    sample.setSampleId(id);
//
//    //RTISMA_HACK
//    sample.setSpecimen(Specimen.create(specimenId,"","",
//        SPECIMEN_CLASS.stream().findFirst().get(),
//        SPECIMEN_TYPE.stream().findFirst().get()));
//    sample.setSampleSubmitterId(submitter);
//
//    sample.setSampleType(type);
//
//    return sample;
//  }

  public void setSampleType(String type) {
    Constants.validate(Constants.SAMPLE_TYPE, type);
    sampleType = type;
  }


}
