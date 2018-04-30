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
package org.icgc.dcc.song.server.model.analysis.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.icgc.dcc.song.server.model.analysis.AbstractAnalysisEntity;
import org.icgc.dcc.song.server.model.enums.TableAttributeNames;
import org.icgc.dcc.song.server.model.enums.TableNames;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static org.icgc.dcc.song.server.model.enums.Constants.VARIANT_CALL_TYPE;

@Entity
@Table(name = TableNames.VARIANTCALL)
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class VariantCallAnalysisEntity extends AbstractAnalysisEntity {

  @Column(name = TableAttributeNames.VARIANT_CALLING_TOOL)
  private String variantCallingTool;

  @Column(name = TableAttributeNames.MATCHED_NORMAL_SAMPLE_SUBMITTER_ID)
  private String matchedNormalSampleSubmitterId;

  @Override
  public String getAnalysisType() {
    return VARIANT_CALL_TYPE;
  }

  public static VariantCallAnalysisEntity createVariantCallAnalysis(String id, String tool, String submitterId) {
    val v = new VariantCallAnalysisEntity();
    v.setAnalysisId(id);
    v.setVariantCallingTool(tool);
    v.setMatchedNormalSampleSubmitterId(submitterId);
    return v;
  }

}
