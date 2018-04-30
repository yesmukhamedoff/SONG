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

package org.icgc.dcc.song.server.model.entity.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysisEntity;
import org.icgc.dcc.song.server.model.enums.LombokAttributeNames;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Entity
@Table(name = TableNames.SAMPLE)
@EqualsAndHashCode(callSuper = true
    , exclude = { LombokAttributeNames.analyses }
    )
@ToString(callSuper = true
    , exclude = { LombokAttributeNames.analyses }
)
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CompositeSampleEntity extends SampleEntity {

  //TODO: rtisma not ready for this yet....
  @JsonIgnore
  @ManyToMany(mappedBy = ModelAttributeNames.SAMPLES,
      cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<AbstractAnalysisEntity> analyses = newHashSet();

  public CompositeSampleEntity addAnalysis(@NonNull AbstractAnalysisEntity abstractAnalysis){
    this.analyses.add(abstractAnalysis);
    return this;
  }

  public static CompositeSampleEntity buildSampleCreateRequest(@NonNull String specimenId, @NonNull Sample sample){
    val s = new CompositeSampleEntity();
    s.setSpecimenId(specimenId);
    s.setWithSample(sample);
    return s;
  }

}
