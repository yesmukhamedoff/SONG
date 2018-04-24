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
import org.icgc.dcc.song.server.model.entity.donor.Donor;
import org.icgc.dcc.song.server.model.entity.study.AbstractStudyEntity;
import org.icgc.dcc.song.server.model.entity.study.Study;
import org.icgc.dcc.song.server.model.entity.study.StudyEntity;
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
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.icgc.dcc.song.server.model.entity.study.StudyEntityMaps.DONOR_WITH_SAMPLES_PATH;
import static org.icgc.dcc.song.server.model.entity.study.StudyEntityMaps.SPECIMEN_WITH_SAMPLES_PATH;
import static org.icgc.dcc.song.server.model.entity.study.StudyEntityMaps.STUDY_WITH_SAMPLES_PATH;

@Entity
@Table(name = TableNames.STUDY)
@Data
@EqualsAndHashCode(callSuper=true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true, exclude = {
    ModelAttributeNames.DONORS,
    ModelAttributeNames.ANALYSES,
    ModelAttributeNames.UPLOADS,
    ModelAttributeNames.FILES})
//@NamedEntityGraph(name = "studyWithSamples",
//    attributeNodes =
//        @NamedAttributeNode(value = ModelAttributeNames.DONORS, subgraph = "donorWithSamples"),
//    subgraphs = {
//        @NamedSubgraph(name = "donorWithSamples",
//            attributeNodes =
//              @NamedAttributeNode(value = ModelAttributeNames.SPECIMENS, subgraph = "specimenWithSamples")),
//        @NamedSubgraph(name = "specimenWithSamples",
//            attributeNodes =
//            @NamedAttributeNode(value = ModelAttributeNames.SAMPLES))
//    }
//)
//@StudyEntityMaps.LoadStudyWithSamples
@NamedEntityGraph(name = STUDY_WITH_SAMPLES_PATH,
    attributeNodes =
    @NamedAttributeNode(value = ModelAttributeNames.DONORS, subgraph = DONOR_WITH_SAMPLES_PATH),
    subgraphs = {
        @NamedSubgraph(name = DONOR_WITH_SAMPLES_PATH,
            attributeNodes =
            @NamedAttributeNode(value = ModelAttributeNames.SPECIMENS, subgraph = SPECIMEN_WITH_SAMPLES_PATH)),
        @NamedSubgraph(name = SPECIMEN_WITH_SAMPLES_PATH,
            attributeNodes =
            @NamedAttributeNode(value = ModelAttributeNames.SAMPLES))
    }
)
public class FullStudyEntity extends AbstractStudyEntity {

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<File> files = newArrayList();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<BaseAnalysis> analyses = newArrayList();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<Upload> uploads = newArrayList();

  @JsonIgnore
  @OneToMany(cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      mappedBy = ModelAttributeNames.STUDY)
  private List<Donor> donors = newArrayList();

  public FullStudyEntity addDonor(@NonNull Donor donor){
    donors.add(donor);
    if (donor.getStudy() != this){
      donor.setStudy(this);
    }
    return this;
  }

  public static FullStudyEntity createFullStudyEntity(@NonNull String studyId, @NonNull Study study) {
    val s = new FullStudyEntity();
    s.setWithStudy(study);
    s.setStudyId(studyId);
    return s;
  }

  public static FullStudyEntity createFullStudyEntity(@NonNull StudyEntity studyEntity) {
    val s = new FullStudyEntity();
    s.setWithStudyEntity(studyEntity);
    return s;
  }

}
