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
package org.icgc.dcc.song.server.model.entity.study.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.Upload;
import org.icgc.dcc.song.server.model.analysis.BaseAnalysis;
import org.icgc.dcc.song.server.model.entity.File;
import org.icgc.dcc.song.server.model.entity.donor.impl.FullDonorEntity;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = TableNames.STUDY)
@Data
@EqualsAndHashCode(callSuper=true,
    exclude = {
        LombokAttributeNames.donors,
        LombokAttributeNames.analyses,
        LombokAttributeNames.uploads,
        LombokAttributeNames.files }
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true, exclude = {
    LombokAttributeNames.donors,
    LombokAttributeNames.analyses,
    LombokAttributeNames.uploads,
    LombokAttributeNames.files
})
@NamedEntityGraph(name = "studyWithSamples",
    attributeNodes =
        @NamedAttributeNode(value = ModelAttributeNames.DONORS, subgraph = "donorWithSamples"),
    subgraphs = {
        @NamedSubgraph(name = "donorWithSamples",
            attributeNodes =
              @NamedAttributeNode(value = ModelAttributeNames.SPECIMENS, subgraph = "specimenWithSamples")),
        @NamedSubgraph(name = "specimenWithSamples",
            attributeNodes =
            @NamedAttributeNode(value = ModelAttributeNames.SAMPLES))
    }
)
public class FullStudyEntity extends AbstractStudyEntity {

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private Set<File> files = newHashSet();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private Set<BaseAnalysis> analyses = newHashSet();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private Set<Upload> uploads = newHashSet();

  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private Set<FullDonorEntity> donors = newHashSet();

  public static FullStudyEntity buildStudyIdOnly(@NonNull FullStudyEntity studyEntity){
    return buildStudyIdOnly(studyEntity.getStudyId());
  }

  public static FullStudyEntity buildStudyIdOnly(String studyId){
    val s = new FullStudyEntity();
    s.setStudyId(studyId);
    return s;
  }

  public FullStudyEntity addDonor(@NonNull FullDonorEntity donor){
    donors.add(donor);
    if (donor.getStudy() != this){
      donor.setParent(this);
    }
    return this;
  }

}
