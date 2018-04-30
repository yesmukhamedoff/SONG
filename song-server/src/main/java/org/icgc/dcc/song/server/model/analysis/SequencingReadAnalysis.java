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

package org.icgc.dcc.song.server.model.analysis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.enums.ModelAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.Constants.LIBRARY_STRATEGY;
import static org.icgc.dcc.song.server.model.enums.Constants.SEQUENCING_READ_TYPE;
import static org.icgc.dcc.song.server.model.enums.Constants.validate;

@Entity
@Table(name = TableNames.SEQUENCINGREAD)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({
    ModelAttributeNames.ANALYSIS_ID,
    ModelAttributeNames.ALIGNED,
    ModelAttributeNames.ALIGNMENT_TOOL,
    ModelAttributeNames.LIBRARY_STRATEGY,
    ModelAttributeNames.PAIRED_END,
    ModelAttributeNames.REFERENCE_GENOME,
    ModelAttributeNames.INFO})
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
public class SequencingReadAnalysis extends AbstractAnalysis {

  @Column(name = TableAttributeNames.ALIGNED, nullable = true)
  private Boolean aligned;

  @Column(name = TableAttributeNames.ALIGNMENT_TOOL, nullable = true)
  private String alignmentTool;

  @Column(name = TableAttributeNames.INSERT_SIZE, nullable = true)
  private Long insertSize;

  @Column(name = TableAttributeNames.LIBRARY_STRATEGY, nullable = false)
  private String libraryStrategy;

  @Column(name = TableAttributeNames.PAIRED_END, nullable = true)
  private Boolean pairedEnd;

  @Column(name = TableAttributeNames.REFERENCE_GENOME, nullable = true)
  private String referenceGenome;

  @Override
  public String getAnalysisType() {
    return SEQUENCING_READ_TYPE;
  }

  public void setLibraryStrategy(String strategy) {
    validate(LIBRARY_STRATEGY, strategy);
    libraryStrategy = strategy;
  }

  public static SequencingReadAnalysis createSequencingReadAnalysis(Boolean aligned, String tool, Long size, String strategy,
      Boolean isPaired, String genome) {
    val s = new SequencingReadAnalysis();
    s.setAligned(aligned);
    s.setAlignmentTool(tool);
    s.setInsertSize(size);
    s.setLibraryStrategy(strategy);
    s.setPairedEnd(isPaired);
    s.setReferenceGenome(genome);
    return s;
  }

}
