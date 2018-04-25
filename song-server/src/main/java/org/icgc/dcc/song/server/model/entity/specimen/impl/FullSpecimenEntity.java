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

package org.icgc.dcc.song.server.model.entity.specimen.impl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.entity.sample.impl.FullSampleEntity;
import org.icgc.dcc.song.server.model.entity.specimen.AbstractSpecimenEntity;
import org.icgc.dcc.song.server.model.enums.JsonAttributeNames;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Data
@Entity
@Table(name = TableNames.SPECIMEN)
@EqualsAndHashCode(callSuper = true, exclude = {
    LombokAttributeNames.donor,
    LombokAttributeNames.samples
})
@ToString(callSuper = true, exclude = {
    LombokAttributeNames.donor,
    LombokAttributeNames.samples
})
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class FullSpecimenEntity extends AbstractSpecimenEntity {

  @JsonIgnore
  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = TableAttributeNames.DONOR_ID, nullable = false)
  private FullDonorEntity donor;

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.SPECIMEN)
  private Set<FullSampleEntity> samples = newHashSet();

  @Override
  @JsonGetter(value = JsonAttributeNames.DONOR_ID)
  public String getDonorId(){
    return this.getDonor().getDonorId();
  }

  public void setParent(@NonNull FullDonorEntity donor){
    this.donor = donor;
    if (!donor.getSpecimens().contains(this)){
      donor.addSpecimen(this);
    }
  }

  public FullSpecimenEntity addSample(@NonNull FullSampleEntity sample){
    this.samples.add(sample);
    if (sample.getSpecimen() != this){
      sample.setParent(this);
    }
    return this;
  }

}
