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

package org.icgc.dcc.song.server.model.entity.sample.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.icgc.dcc.song.server.model.entity.sample.AbstractSampleEntity;
import org.icgc.dcc.song.server.model.entity.sample.Sample;
import org.icgc.dcc.song.server.model.entity.specimen.impl.FullSpecimenEntity;
import org.icgc.dcc.song.server.model.enums.JsonAttributeNames;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = TableNames.SAMPLE)
@EqualsAndHashCode(callSuper = true, exclude = {
    LombokAttributeNames.specimen,
    LombokAttributeNames.analyses
})
@ToString(callSuper = true, exclude = {
    LombokAttributeNames.specimen,
    LombokAttributeNames.analyses
})
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FullSampleEntity extends AbstractSampleEntity {

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = TableAttributeNames.SPECIMEN_ID, nullable = false)
  private FullSpecimenEntity specimen;

  @JsonIgnore
  @ManyToMany(mappedBy = ModelAttributeNames.SAMPLES,
      cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<BaseAnalysis> analyses = newHashSet();

  @JsonGetter(value = JsonAttributeNames.SPECIMEN_ID)
  public String getSpecimenId(){
    return getSpecimen().getSpecimenId();
  }

  public void setParent(@NonNull FullSpecimenEntity specimen){
    this.specimen = specimen;
    if(!specimen.getSamples().contains(this)){
      specimen.addSample(this);
    }
  }


  public static FullSampleEntity createFullSampleEntity(String id, @NonNull FullSpecimenEntity specimenEntity,
     @NonNull Sample sample){
    val s  = new FullSampleEntity();
    s.setWithSample(sample);
    s.setSpecimen(specimenEntity);
    s.setSampleId(id);
    return s;
  }

  public static FullSampleEntity createFullSampleEntity(String id, @NonNull FullSpecimenEntity specimenEntity,
      String sampleSubmitterId, String sampleType){
    val sampleData = createSampleImpl(sampleSubmitterId, sampleType);
    return createFullSampleEntity(id, specimenEntity, sampleData);
  }

}
